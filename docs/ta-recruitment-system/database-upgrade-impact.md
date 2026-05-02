# 新数据库版本升级影响说明

## 1. 本次升级做了什么

数据库系统从旧版 Sprint 1 风格的 4 表样例实现，升级为 Sprint 3 终态的 11 表本地 JSON 数据库。

旧版核心特征：

- 只有 `users / resumes / jobs / applications`
- Service 基本是单表透传
- 若干 Servlet 直接 `new Json*Repository`
- `/db-demo` 暴露旧样例操作入口

新版核心特征：

- 完整 11 表
- `TaDatabase` 统一门面
- Repository 语义化查询
- Service 负责跨表联动、审计、通知、工作量闭环
- 启动时严格校验数据目录

## 2. 对后端同学的必要修改

### 2.1 获取数据库的方式变了

旧写法：

```java
new JsonUserRepository(...)
new JsonJobRepository(...)
```

新写法：

```java
TaDatabase db = DatabaseProvider.get(servletContext);
```

之后统一从 `db.users() / db.jobs() / db.applications()` 进入。

### 2.2 不再允许 Servlet 直接 new repository

必须改成：

- Servlet 拿 `TaDatabase`
- 复杂业务走 Service
- Repository 只通过 `TaDatabase` 暴露

### 2.3 跨表业务不要自己拼

以下场景必须走 Service，而不是自己分散写表：

- 提交申请
- 接受/拒绝/撤回 offer
- 取消岗位
- 刷新匹配分析
- 替换简历技能

原因是这些动作会联动：

- `applications`
- `workload_records`
- `notifications`
- `audit_logs`
- `match_scores`

### 2.4 `jobs().listOpen(...)` 签名变了

旧版常见写法：

```java
db.jobs().listOpen(Instant.now())
```

新版正式写法：

```java
JobQuery query = new JobQuery();
query.setNow(Instant.now());
db.jobs().listOpen(query);
```

当前项目中的兼容 Service 已经帮现有页面兜住，但新代码请直接用 `JobQuery`。

### 2.5 `/db-demo` 不再作为正式业务联调基线

当前代码现实：

- 仓库里仍然保留了 `/db-demo`
- `DbDemoService` 也仍然存在
- 该页面现在更像数据库能力演示和调试入口，而不是正式业务流程的一部分

联调建议：

- 不要把 `/db-demo` 当作业务页面实现标准
- 不要把 `DbDemoService` 当作 portal 业务 servlet 的目标结构
- 正式联调仍应以 `/dashboard`、`/vacancies`、`/applications`、`/resumes`、`/settings`、`/workloads` 等业务路由为准

## 3. 对前端同学的必要修改

### 3.1 路由不变，但数据来源变了

当前主要业务路由保持不变：

- `/login`
- `/vacancies`
- `/vacancy`
- `/application`
- `/applications`
- `/resumes`
- `/settings`
- `/workloads`

前端不需要因为数据库升级而整体改路由。

### 3.2 岗位要求现在优先来自结构化表

`vacancy_detail.jsp` 中的 `vacancy.requirements` 不再长期依赖硬编码文案，优先来自：

- `job_requirements`
- `skills`

如果后端没有给岗位配置结构化要求，才会回退到通用文案。

### 3.3 工作量页现在来自真实 `workload_records`

`/workloads` 不再显示 mock 数据，而是显示数据库真实聚合结果。

前端需要注意：

- 空数据状态会更常见
- 某些开发环境在没有录用记录时页面会为空表

### 3.4 申请列表中的状态来源更真实

`/applications` 现在直接来自 `applications.status`，并做展示态映射。

前端如果以后要新增状态样式，需要同步考虑这些值：

- `Submitted`
- `Under Review`
- `Offer Pending`
- `Accepted`
- `Rejected`
- `Declined`
- `Withdrawn`

## 4. 兼容保留项

为了减少现有页面改动，本次保留了以下兼容字段：

### 4.1 `users`

- `department`
- `studentId`
- `bio`
- `notificationsEnabled`
- `preferredLanguage`
- `preferredAppearance`
- `savedJobIds`

### 4.2 `resumes`

- `uploadedFilePath`
- `originalFileName`

### 4.3 `jobs`

- `hourlyRate`

这些字段仍是正式持久化字段，不需要前端立刻改掉。

## 5. 明确移除或不再推荐的内容

### 5.1 不再作为正式 schema 的字段

- `Resume.availabilityJson`

正式模型已经变为 `List<AvailabilitySlot>`。当前如果某处还需要字符串形式，只能在 Web 层做适配，不能再把它当数据库正式字段继续扩散。

### 5.2 不再保留的旧数据库调用方式

- 直接读写 JSON 文件
- 直接操作 `JsonTableStore`
- 直接 `new Json*Repository`
- 依赖旧 4 表样例实现的测试或 demo 代码

## 6. 数据目录变化

本次升级采用“全新初始化”策略。

这意味着：

- 新系统要求使用新的空 `data/` 目录
- 不做旧版 4 表自动迁移
- 如果目录里存在旧版遗留 JSON，启动会失败

团队切换时建议步骤：

1. 备份旧 `data/` 目录
2. 清空或更换为新的 `data/` 目录
3. 启动应用，让系统自动创建 11 个空表文件
4. 重新导入测试数据或使用默认测试账号联调

## 7. 推荐迁移顺序

后端建议按这个顺序改：

1. 所有 Servlet 改成从 `DatabaseProvider.get(...)` 拿 `TaDatabase`
2. 把直接 new repository 的写法全部删掉
3. 把跨表动作迁移到 Service
4. 接口联调通过后，再补结构化技能与匹配能力

前端建议按这个顺序配合：

1. 先按现有路由继续联调
2. 确认列表/详情页能吃到真实 11 表数据
3. 再逐步增强状态显示、工作量展示、匹配解释展示
