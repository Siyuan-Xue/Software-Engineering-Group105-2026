# TA Recruitment System 数据层交付包

这组文档把 `BUPT TA Recruitment System` 从 Sprint 1 到 Sprint 3 的数据库设计，整理成了一套可直接交给后端同学使用的实现说明。

推荐阅读顺序：

1. [Sprint 1-3 数据库逐表分析](./sprint1-to-sprint3-database-analysis.md)
2. [Sprint 3 JSON 数据库架构说明](./sprint3-json-database-architecture.md)
3. [Java 数据层 UML 类图](./java-database-layer-class-diagram.md)
4. [Sprint 3 全量 ER 图](./sprint3-entity-relationship-overview.md)
5. [后端数据库使用指南](./backend-database-usage.md)
6. [新旧数据库升级影响说明](./database-upgrade-impact.md)

这份交付包遵循以下约束：

- 只覆盖 Sprint 3 终态的 11 张业务表。
- 不引入 JPA、Hibernate、MyBatis、Spring Data 等数据库框架。
- 默认使用 Jackson + JSON 文件持久化。
- 上层调用方只依赖 `TaDatabase` 和语义化 Repository。
- 只考虑单 JVM 进程独占 `data/` 目录的场景。
- 启动策略为“全新初始化”，不对旧版 4 表数据做隐式升级。

建议目录落地：

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

对外最简使用方式：

```java
JobQuery query = new JobQuery();
query.setNow(Instant.now());

TaDatabase db = DatabaseProvider.get(servletContext);

User user = db.users()
    .findByEmail("alice@example.com")
    .orElseThrow();

List<Job> jobs = db.jobs().listOpen(query);
List<Resume> resumes = db.resumes().listByUserId(user.getId());
```
