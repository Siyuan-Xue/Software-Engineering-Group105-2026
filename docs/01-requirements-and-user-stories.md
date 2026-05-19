# 01 需求与用户故事

## 课程依据

EBU6304 的需求与敏捷相关 slides 强调：软件工程不是只写代码，而是通过可追踪的需求、用户故事、验收标准和迭代反馈降低误解风险。对 QM HIRE 来说，需求文档需要回答四个问题：谁使用系统、他们要完成什么、什么条件下算完成、哪些非功能约束会影响设计。

| Slide | 本文档提炼 |
|---|---|
| EBU6304_01 Introduction to Software Engineering | 明确问题、stakeholders、质量属性和工程化交付 |
| EBU6304_02 Agile Software Development | 用 increment、backlog、sprint review 和反馈闭环组织工作 |
| EBU6304_03 Requirements | 区分功能需求、非功能需求、约束和验收条件 |
| EBU6304_04 User stories and prototyping | 用用户故事、persona、原型和验收标准表达需求 |
| EBU6304_17 Revision | 需求、设计、实现、测试之间需要可追踪 |

## Stakeholders

| Stakeholder | 目标 | 关注点 |
|---|---|---|
| TA applicant | 查找适合自己的 TA 岗位，维护简历，提交申请，跟踪 offer | 易用性、申请状态透明、隐私、简历上传安全 |
| Module Organiser (MO) | 发布岗位，筛选申请者，查看匹配分析，发送 offer 或拒绝 | 岗位生命周期、候选人排序、工作量约束、决策可解释 |
| Administrator | 管理用户、技能目录、审计日志和工作量 | 访问控制、可审计性、数据一致性、维护效率 |
| Course staff / assessor | 评估课程项目是否满足软件工程要求 | 需求追踪、设计质量、测试证据、伦理与安全 |
| Maintainer | 后续修复、扩展和部署系统 | 模块边界、配置管理、可测试性、运行说明 |

## Glossary

| Term | Definition |
|---|---|
| TA | Teaching Assistant，学生申请者角色 |
| MO | Module Organiser，发布和审核 TA 岗位的教师或课程负责人 |
| Vacancy / Job | 一个可申请的 TA 岗位，包含课程信息、时间、名额和技能要求 |
| Resume | TA 的简历版本，可包含上传文件和技能记录 |
| Application | TA 对某个岗位的一次申请，包含状态、cover letter 和简历快照 |
| Match score | 系统根据技能、工作量和可选 AI 结果生成的匹配分析 |
| Workload record | 已接受岗位产生的 TA 工作量记录 |
| Audit log | 对关键管理、状态变更和 AI 请求的审计记录 |
| AI consent | 用户在触发 AI 功能前对数据发送和辅助性质作出的显式同意 |

## Product Scope

### In Scope

- TA 自助注册、登录、个人设置、简历维护和岗位申请。
- MO 创建、编辑、发布、取消岗位，并审核申请。
- Admin 管理用户、技能、审计日志、数据库演示页和工作量监控。
- JSON 文件持久化，满足课程演示和无外部数据库部署约束。
- 可选 Qwen/DashScope AI 辅助，包括简历建议、岗位匹配、候选人排序和文案建议。
- 安全基础能力：RBAC、CSRF、防护性文件上传、会话加固、AI 同意与审计。

### Out of Scope

- 真实学校身份认证、邮件发送、支付、正式人事系统集成。
- 多节点高可用部署和分布式事务。
- 由 AI 自动作出录用决定。
- 面向公众的生产级合规归档系统。

## Functional Requirements

| ID | Requirement | Primary users | Current implementation evidence | Acceptance criteria |
|---|---|---|---|---|
| FR-01 | 用户可以注册、登录、退出并维护个人设置 | TA、MO、Admin | `RegisterServlet`、`LoginServlet`、`LogoutServlet`、`SettingsServlet`、`AuthService`、`TaAccountService` | TA 自助注册只能创建 TA；登录成功后 session 轮换；退出后受保护页面跳转登录 |
| FR-02 | TA 可以创建、上传、复制、编辑和删除简历 | TA | `ResumesServlet`、`ResumeService`、`ResumeFileUpload` | 合法文件被保存到数据目录；非法扩展、MIME、签名或超限文件被拒绝 |
| FR-03 | TA 可以浏览、筛选、收藏岗位并提交申请 | TA | `VacanciesServlet`、`VacancyDetailServlet`、`FavoritesServlet`、`ApplicationSubmitServlet` | 已发布且开放的岗位可申请；重复申请被阻止；申请保存简历快照 |
| FR-04 | TA 可以跟踪申请、撤回 pending 申请、接受或拒绝 offer | TA | `ApplicationsServlet`、`ApplicationDecisionServlet`、`ApplicationService` | 状态转换遵守业务规则；越权操作被拒绝 |
| FR-05 | MO 可以创建、编辑、发布、取消岗位和维护技能要求 | MO | `VacancyCreateServlet`、`VacancyEditServlet`、`JobService`、`JobRequirementRepository` | MO 只能管理自己的岗位；取消岗位会处理相关申请状态 |
| FR-06 | MO 可以审核申请、查看匹配分析、下载提交的简历快照并发送决策 | MO | `ApplicationDetailServlet`、`MatchAnalysisServlet`、`MatchingService`、`ApplicationSubmissionFiles` | MO 只能审核自己岗位的申请；匹配分析在审核流程中刷新；最终决策由人作出 |
| FR-07 | Admin 可以管理用户、技能、审计日志、数据库演示页和工作量 | Admin | `AdminUsersServlet`、`AdminSkillsServlet`、`AdminAuditServlet`、`DbDemoServlet`、`WorkloadsServlet`、`AdminService` | `/admin/*` 和 `/workloads` 受角色控制；数据库演示页入口为 `/admin/database` |
| FR-08 | 系统可以在配置 AI key 时提供可选 AI 辅助，未配置时使用规则兜底 | TA、MO | `QwenAiService`、`AiRequestGuard`、AI Servlet、`MatchingService` | 未同意 AI 时拒绝请求；AI 不可用时核心招聘流程仍可运行 |
| FR-09 | 系统记录关键状态变化和 AI 请求审计日志 | MO、Admin、Maintainer | `AuditLogRepository`、`AdminAuditServlet`、`AuditAction` | 创建用户、岗位、申请状态变化、AI 请求等可在审计页检索 |
| FR-10 | 系统用 JSON 文件持久化招聘数据并保证单 JVM 内一致读写 | All | `TaDatabase`、`FileTaDatabase`、`JsonTableStore`、`AtomicJsonFileWriter` | 空数据目录自动初始化；保存操作原子写入；测试可用临时数据目录隔离 |

## Non-Functional Requirements

| ID | Quality attribute | Requirement | Evidence | Priority |
|---|---|---|---|---|
| NFR-01 | Security | 受保护页面必须登录；敏感功能必须检查角色和所有权 | `AuthFilter`、各 Servlet 和 Service 的角色检查 | 高 |
| NFR-02 | Security | 所有 unsafe HTTP 请求必须通过 CSRF 校验 | `CsrfFilter`、`CsrfTokens`、JSP token 注入 | 高 |
| NFR-03 | Security | 文件上传必须限制大小、扩展名、MIME 和文件签名 | `ResumeFileUpload`、`ResumeFileUploadTest` | 高 |
| NFR-04 | Privacy | AI 功能必须显式同意，不记录完整 prompt 或模型输出 | `AiRequestGuard`、`AuditAction.AI_REQUEST` | 高 |
| NFR-05 | Maintainability | Web、Service、Persistence、Domain 分层清晰 | `web/servlet`、`service`、`db`、`domain` 包 | 高 |
| NFR-06 | Reliability | 数据写入应避免部分写入和损坏 | `AtomicJsonFileWriter`、`JsonTableStore` | 高 |
| NFR-07 | Testability | 核心业务、Repository、Filter、Servlet 和工具类应有自动化测试 | `src/test/java` | 高 |
| NFR-08 | Usability | 主要 TA、MO、Admin 工作流应能从登录后的侧栏或页面入口完成 | JSP portal pages、README route table | 中 |
| NFR-09 | Portability | 应可在 Java 17、Maven、Tomcat 11 上无外部数据库运行 | `pom.xml`、`README.md`、`AppConfig` | 中 |
| NFR-10 | Performance | 课程规模数据下页面与匹配分析应可交互响应 | JSON 数据量小、Repository 内存快照 | 中 |

## User Stories And Acceptance Criteria

### TA Applicant Stories

| ID | User story | Acceptance criteria | Trace |
|---|---|---|---|
| US-TA-01 | 作为 TA，我希望自助注册账号，以便开始申请岗位 | 只能创建 TA 角色；邮箱唯一；弱或不匹配密码被拒绝 | FR-01、`RegisterServletTest` |
| US-TA-02 | 作为 TA，我希望维护多个简历版本，以便针对不同岗位申请 | 可以创建、编辑、复制、删除未被锁定的简历；技能和可用时间可保存 | FR-02、`ResumeServiceTest` |
| US-TA-03 | 作为 TA，我希望上传 PDF/DOC/DOCX 简历，以便 MO 查看正式文件 | 只接受允许类型；保存路径在数据目录内；申请时生成快照 | FR-02、FR-03、`ResumeFileUploadTest` |
| US-TA-04 | 作为 TA，我希望浏览和收藏开放岗位，以便比较机会 | 可查看开放岗位、筛选岗位、收藏和取消收藏 | FR-03 |
| US-TA-05 | 作为 TA，我希望提交申请并跟踪状态，以便知道后续动作 | 申请状态显示 pending/reviewing/offered/accepted/rejected/withdrawn/declined；pending 可撤回 | FR-03、FR-04、`ApplicationServiceTest` |
| US-TA-06 | 作为 TA，我希望在同意后使用 AI 简历建议，以便改进材料 | 未勾选 AI 同意时请求失败；同意后只作为建议，不自动提交申请 | FR-08、`AiRequestGuardTest` |

### MO Stories

| ID | User story | Acceptance criteria | Trace |
|---|---|---|---|
| US-MO-01 | 作为 MO，我希望发布和维护岗位，以便招募合适 TA | 可创建 draft/open 岗位；只能编辑自己拥有的岗位；取消岗位后相关申请被处理 | FR-05、`JobServiceTest` |
| US-MO-02 | 作为 MO，我希望定义技能要求，以便候选人匹配有依据 | 可以为岗位添加 required/preferred 技能和熟练度要求 | FR-05、`JobRequirementRepository` |
| US-MO-03 | 作为 MO，我希望查看申请详情和提交时的简历快照，以便公平审核 | MO 只能看自己岗位申请；下载的是提交时快照，不受 TA 后续简历修改影响 | FR-06、`ApplicationSubmissionFilesTest` |
| US-MO-04 | 作为 MO，我希望刷新匹配分析并查看解释，以便辅助但不替代决策 | 进入 review 后可刷新；显示规则分、AI/兜底分、缺失技能、推荐说明；最终 offer/reject 由 MO 点击 | FR-06、FR-08、`MatchingServiceTest` |

### Admin Stories

| ID | User story | Acceptance criteria | Trace |
|---|---|---|---|
| US-AD-01 | 作为 Admin，我希望管理用户，以便创建 MO/Admin 或维护 TA 账号 | 可创建角色账号、编辑资料、重置密码、禁用非 Admin；操作记录审计 | FR-07、`AdminUsersServletTest` |
| US-AD-02 | 作为 Admin，我希望维护技能目录，以便岗位和简历使用一致术语 | 已被引用的技能受到保护；新增和更新可审计 | FR-07 |
| US-AD-03 | 作为 Admin，我希望查看工作量，以便识别过载 TA | 可查看 accepted 工作量、周小时、容量、估算收入和再平衡建议 | FR-07、`AdminServiceTest` |
| US-AD-04 | 作为 Admin，我希望访问数据库演示页，以便检查课程 JSON 数据层 | `/admin/database` 仅 Admin 可进入；非 Admin 返回 forbidden | FR-07、`DbDemoServlet` |

## Requirements Traceability Matrix

| Requirement | Domain / service | Web route or UI | Tests / QA evidence | Related docs |
|---|---|---|---|---|
| FR-01 | `AuthService`、`TaAccountService`、`User` | `/login`、`/register`、`/settings` | `AuthServiceTest`、`LoginServletTest`、`RegisterServletTest`、`SettingsServletTest` | 03、04、05 |
| FR-02 | `ResumeService`、`Resume`、`ResumeSkill` | `/resumes` | `ResumeServiceTest`、`ResumeFileUploadTest`、`ResumeFilePathsTest` | 02、04、06 |
| FR-03 | `JobService`、`ApplicationService`、`Application` | `/vacancies`、`/vacancy`、`/application/submit` | `ApplicationServiceTest`、`ApplicationSubmissionFilesTest` | 02、03、05 |
| FR-04 | `ApplicationService` | `/applications`、application decision POSTs | `ApplicationDecisionServletTest`、`ApplicationServiceTest` | 03、05 |
| FR-05 | `JobService`、`JobRequirementRepository` | `/vacancy/create`、`/vacancy/edit` | `JobServiceTest` | 02、03 |
| FR-06 | `MatchingService`、`ApplicationSubmissionFiles` | `/application/detail`、`/match-analysis` | `MatchingServiceTest`、`ApplicationSubmissionFilesTest` | 03、05、06 |
| FR-07 | `AdminService`、`DbDemoService` | `/admin/users`、`/admin/skills`、`/admin/audit`、`/admin/database`、`/workloads` | `AdminServiceTest`、`AdminUsersServletTest`、`DbDemoServiceTest` | 03、04、07 |
| FR-08 | `QwenAiService`、`AiRequestGuard` | AI servlet endpoints | `QwenAiServiceTest`、`AiRequestGuardTest`、AI servlet tests | 06 |
| FR-09 | `AuditLogRepository`、`AuditLog` | `/admin/audit` | repository/service tests | 06 |
| FR-10 | `TaDatabase`、`JsonTableStore`、`AtomicJsonFileWriter` | all persistence-backed flows | `TaDatabaseIntegrationTest`、`JsonTableStoreTest`、repository tests | 03、04、05 |

## Backlog Style Quality Notes

| Backlog item | Rationale | Priority |
|---|---|---|
| Add end-to-end browser tests for the highest-value TA/MO/Admin flows | Unit and integration coverage is strong, but full JSP navigation tests would catch wiring regressions | 中 |
| Add explicit performance threshold for match analysis under seeded demo data | Current design is adequate for coursework scale; threshold would make quality measurable | 低 |
| Add exported sample data dictionary for each JSON table | Would help maintainers reason about persistence without opening code | 低 |
