# TA Recruitment 本地数据库使用指南

## 1. 这套数据库是什么

这不是“让业务代码自己去读写 JSON 文件”的方案，而是一套对后端暴露数据库语义的本地数据层。

后端同学只需要关心：

- 调哪个 Repository
- 用什么查询对象
- 哪些跨表写必须放进原子区

不需要关心：

- JSON 文件路径
- 表文件初始化
- 原子写入
- Jackson 配置
- 多表落盘顺序

当前数据库覆盖 Sprint 3 终态 11 张表：

- `users`
- `resumes`
- `jobs`
- `applications`
- `skills`
- `resume_skills`
- `job_requirements`
- `workload_records`
- `match_scores`
- `notifications`
- `audit_logs`

## 2. 初始化与获取方式

Web 应用启动时，`AppContextListener` 会自动初始化数据库并挂到 `ServletContext`。

业务代码统一这样获取：

```java
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;

TaDatabase db = DatabaseProvider.get(servletContext);
```

不要自己 `new JsonTableStore`、`new Json*Repository`，也不要直接访问 `data/*.json`。

## 3. 目录与数据文件

代码结构：

```text
src/main/java/com/bupt/ta/
  domain/
    entity/
    enums/
    value/
  db/
    core/
    store/
    repository/
    facade/
  service/
```

运行期数据目录默认是：

```text
data/
  users.json
  resumes.json
  jobs.json
  applications.json
  skills.json
  resume_skills.json
  job_requirements.json
  workload_records.json
  match_scores.json
  notifications.json
  audit_logs.json
```

每个表文件统一格式：

```json
{
  "version": 1,
  "rows": []
}
```

注意：

- 新系统要求使用全新 `data/` 目录
- 如果目录里已有旧版 4 表遗留 JSON，启动会失败并提示先备份/清空目录

## 4. Repository 入口清单

`TaDatabase` 暴露的数据库入口：

```java
db.users();
db.resumes();
db.jobs();
db.applications();
db.skills();
db.resumeSkills();
db.jobRequirements();
db.workloadRecords();
db.matchScores();
db.notifications();
db.auditLogs();
db.executeAtomically(() -> { ... });
```

常用语义化方法：

- `db.users().findByEmail(email)`
- `db.users().listByRole(UserRole.TA)`
- `db.resumes().listByUserId(userId)`
- `db.resumes().duplicate(resumeId)`
- `db.jobs().listOpen(jobQuery)`
- `db.jobs().listByPoster(posterId)`
- `db.jobs().countAccepted(jobId)`
- `db.applications().listByJobId(jobId)`
- `db.applications().listByResumeId(resumeId)`
- `db.applications().listByStatuses(jobId, statuses)`
- `db.applications().existsByTaAndJob(taUserId, jobId)`
- `db.skills().findByNameIgnoreCase(name)`
- `db.resumeSkills().listByResumeId(resumeId)`
- `db.jobRequirements().listByJobId(jobId)`
- `db.workloadRecords().findByApplicationId(applicationId)`
- `db.workloadRecords().aggregateBySemester(semester)`
- `db.matchScores().findByApplicationId(applicationId)`
- `db.notifications().listByUser(notificationQuery)`
- `db.notifications().markAllRead(userId)`
- `db.auditLogs().search(auditLogQuery)`

## 5. 原子区使用规则

单表写入可以直接保存：

```java
db.users().save(user);
db.resumes().save(resume);
```

跨表写入必须统一进入原子区：

```java
db.executeAtomically(() -> {
    db.applications().save(application);
    db.workloadRecords().save(record);
    db.notifications().save(notification);
    db.auditLogs().append(log);
});
```

必须进原子区的典型场景：

- 接受 offer
- 取消岗位并联动申请
- 落盘 `match_scores + notifications + audit_logs`
- 批量替换某份简历的技能

原子区内禁止做长耗时调用，比如：

- AI 网络请求
- 外部文件解析
- 长时间阻塞等待

正确模式是先算好结果，再进入原子区落盘。

## 6. 常见调用示例

### 6.1 登录

```java
User user = db.users()
    .findByEmail(email)
    .orElseThrow();
```

### 6.2 查当前开放岗位

```java
JobQuery query = new JobQuery();
query.setNow(Instant.now());

List<Job> jobs = db.jobs().listOpen(query);
```

### 6.3 查某个 TA 的简历

```java
List<Resume> resumes = db.resumes().listByUserId(userId);
```

### 6.4 提交申请

推荐走 `ApplicationService.submit(...)`，不要自己直接写 `applications`。

```java
Application application = applicationService.submit(
    taUserId,
    resumeId,
    jobId,
    coverLetter
);
```

### 6.5 接受 offer

推荐走 `ApplicationService.acceptOffer(...)`，它会一起处理：

- 更新 `applications`
- 创建/更新 `workload_records`
- 写 `notifications`
- 写 `audit_logs`

### 6.6 查通知

```java
NotificationQuery query = new NotificationQuery();
query.setUserId(userId);
query.setUnreadOnly(true);
query.setLimit(20);

List<Notification> notifications = db.notifications().listByUser(query);
```

### 6.7 查审计日志

```java
AuditLogQuery query = new AuditLogQuery();
query.setEntityType(EntityType.APPLICATION);
query.setSize(50);

List<AuditLog> logs = db.auditLogs().search(query);
```

## 7. 推荐调用层次

建议分层：

- Servlet: 组装参数、做权限检查、调用 Service
- Service: 跨表规则、状态机、通知、审计、原子区
- Repository: 语义化查询与表级约束
- Store: JSON 文件安全持久化

不要让 Servlet 直接做复杂跨表联动。

## 8. 禁止事项

- 不要直接读写 `data/*.json`
- 不要在 Servlet 里 `new Json*Repository`
- 不要在跨表写场景里只改一张表
- 不要把 AI 调用放进 `executeAtomically(...)`
- 不要把 `/admin/database` 当作正式业务联调基线；当前仓库虽仍保留该 demo 路由，但它只是数据库能力演示入口

## 9. 当前兼容保留项

为了不打断现有页面，以下字段仍被数据库正式保留：

- `users.department`
- `users.studentId`
- `users.bio`
- `users.notificationsEnabled`
- `users.preferredLanguage`
- `users.preferredAppearance`
- `users.savedJobIds`
- `resumes.uploadedFilePath`
- `resumes.originalFileName`
- `jobs.hourlyRate`

这几项是兼容扩展，不改变 11 表核心结构。
