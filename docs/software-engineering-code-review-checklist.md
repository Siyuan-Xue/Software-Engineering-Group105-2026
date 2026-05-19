# 基于 EBU6304 软件工程课件的项目代码审查清单

审查对象：QM HIRE / TA105 Teaching Assistant recruitment system  
审查日期：2026-05-19  
审查范围：`src/main/java`、`src/main/webapp`、配置、测试、README、接口契约与课程项目文档。`target/`、运行时 `data/` 和构建产物不作为主体，只在讨论持久化、安全和质量风险时参考。

## 1. 课程内容提炼

| 课件 | 主要授课内容 | 对代码审查的含义 |
|---|---|---|
| `EBU6304_01_Introduction to Software Engineering.pdf` | 软件质量属性，用户视角的功能性、可用性、可靠性、性能、安全、兼容性，以及开发者视角的灵活性、互操作、合规、可扩展、可维护；软件工程强调有纪律、系统化、可度量的方法。参考 p23, p24, p27, p28, p29, p31。 | 审查不能只看“能跑”，还要看安全、可维护、扩展、文档、团队协作和质量保证。 |
| `EBU6304_02_Agile Software Development.pdf` | 软件过程包含需求、分析、设计、实现、验证、部署、维护；敏捷以迭代方式并行推进规格、设计、实现、测试，并持续让用户评估增量。参考 p2, p11, p12, p13, p25, p26, p27。 | 审查要看迭代证据、可变需求下的扩展性、持续测试、用户反馈与维护能力。 |
| `EBU6304_03_Requirements.pdf` | 需求来自利益相关者；功能需求说明系统做什么，非功能需求说明质量属性和约束；非功能需求应可验证、可度量。参考 p3, p4, p12, p13, p14, p15, p18, p19, p20, p33。 | 审查要追踪 TA、MO、Admin 的需求是否落到路由、服务、数据和测试，并检查性能、安全、可用性等是否有可验证指标。 |
| `EBU6304_04_User stories and prototyping.pdf` | 用户故事使用“作为某类用户，我想要某目标，以便获得某价值”；需维护项目词汇表、epic、验收标准、优先级、估算和原型。参考 p4, p11, p16, p22, p23, p29, p30, p33, p36, p40, p45。 | 审查要看角色故事、验收标准、术语一致性、原型/手册证据，以及故事是否可测试。 |
| `EBU6304_05_Analysis.pdf` | 分析阶段识别概念模型与类；Entity/Boundary/Control 用于区分持久化信息、交互边界和流程控制。参考 p7, p8, p10, p11, p13, p15。 | 审查要看实体、Servlet/JSP 边界、Service 控制逻辑是否职责清晰。 |
| `EBU6304_06_Design.pdf` | 好设计应满足需求、有架构、模块化、可维护、可追踪、文档化；核心概念包括分离关注点、封装、抽象、模块化、低耦合和高内聚。参考 p7, p8, p15, p18, p19, p21。 | 审查要看层次、包结构、耦合、内聚、重复逻辑和重构机会。 |
| `EBU6304_07_Implementation.pdf` | 代码质量不止正确，还要可读、可维护、一致、可测试；代码审查清单包括单测、命名、常量、未使用对象、方法职责、重复、错误处理、注释；构建与集成应自动化，文档应帮助维护。参考 p4, p6, p15, p17, p21, p23, p24, p26, p30。 | 审查要覆盖编码规范、异常处理、CI/构建、注释质量、README 和开发文档。 |
| `EBU6304_08_Testing.pdf` | 测试过程包含单元、系统、验收；测试模型包含用例、数据、结果和报告；策略应覆盖单元、集成、系统、验收、功能、性能、安全、可用性、回归；黑盒关注需求，白盒关注内部逻辑，使用等价类、边界值、覆盖率、路径测试等。参考 p7, p8, p10, p11, p12, p18, p20, p21, p23, p24, p27, p30, p31, p35, p53, p55。 | 审查要看测试层级、边界与异常测试、回归测试、安全测试、性能测试和测试报告。 |
| `EBU6304_09_Software Architecture-2.pdf` | 架构是主要设计决策；4+1 视图覆盖逻辑、开发、进程、物理和场景；分层架构分离职责并提升可维护和可测试，但也可能引入性能开销或过度工程；Web 架构通常是客户端服务器多层结构，REST 强调统一接口和无状态。参考 p2, p3, p11, p12, p13, p14, p20, p21, p22, p32, p33。 | 审查要看系统是否有清晰分层、视图/文档、部署假设、运行时并发和 Web 接口一致性。 |
| `EBU6304_10_Project Management.pdf` | 项目管理保障按时、预算内和有质量交付；活动包含计划、成本、时间、监控、评审、人员、报告、风险和质量管理；进度需要任务拆分、依赖、关键路径和报告。参考 p2, p8, p9, p10, p14, p15, p17, p20, p23, p36, p43, p47, p56。 | 审查要看项目文档、验收清单、交付计划、风险记录和持续监控。 |
| `EBU6304_11_Ethics and AI in Software Engineering.pdf` | AI 与软件会影响招聘、筛选等决策；数据和模型可能有选择、标注、模型、无意识偏见；公平概念之间有取舍；需要审计算法、公平性、透明度和职业伦理。参考 p3, p5, p7, p8, p13, p14, p18, p19, p24, p26, p27, p28。 | 审查 AI 排序、筛选建议和招聘工作流时，要看偏见提醒、人工决策边界、透明度和审计证据。 |
| `EBU6304_12_Risk and Quality Management.pdf` | 风险管理包括识别、分析、计划、监控；质量管理建立过程和标准；高质量软件应符合规格、可用、高效、易修改、结构好、可理解；还要权衡安全、可靠、性能、可维护等可见和不可见质量。参考 p3, p5, p7, p8, p12, p15, p16, p17, p18, p21, p22, p27, p28, p33, p34。 | 审查要明确风险优先级、质量标准、可接受风险和改进路线。 |
| `EBU6304_13_Secure Software Development.pdf` | 安全开发保护数据、资金、声誉和合规；功能正确不等于安全；AI 代码可能看似正确但不安全；安全需要结构化审查、自动分析、人工推理、输入验证、授权、密钥保护、安全加密、边界测试。参考 p2, p3, p4, p5, p6, p7, p10, p11, p13, p14, p15, p16, p18, p20。 | 审查要优先看认证授权、输入验证、上传、密钥、CSRF、XSS、AI 输出、静态分析和安全测试。 |
| `EBU6304_14_Design Principles-2.pdf` | SOLID 包含 SRP、OCP、LSP、ISP、DIP；SRP 强调一个变化原因，OCP 通过抽象支持扩展，LSP 要求子类型可替换，ISP 避免胖接口，DIP 要求高层和低层都依赖抽象。参考 p7, p9, p21, p26, p31, p35, p40, p41, p43, p45。 | 审查要看类职责、接口大小、继承/抽象正确性、依赖方向和可扩展点。 |
| `EBU6304_15_Design Patterns.pdf` | 设计模式是命名、可复用的问题解决方案，可提升高内聚、低耦合、可维护、复用和鲁棒性；课件覆盖 Facade、Observer、Proxy、Singleton、Strategy、Factory、Adapter。参考 p2, p3, p8, p15, p16, p17, p26, p27, p31, p32, p40, p43, p51, p55, p58。 | 审查模式使用是否解决真实问题，是否滥用，是否能降低耦合和变化成本。 |
| `EBU6304_16_AI for Software Development.pdf` | AI 是开发协作者，可支持需求澄清、设计决策、代码生成、重构、调试、测试、部署和维护；有效使用 AI 需要判断任务适配性、工具选择、安全合规和伦理考虑。参考 p2, p4, p5, p6, p7, p8, p9, p10。 | 审查 AI 辅助功能和 AI 辅助开发痕迹时，要看人类责任、验证、测试和合规边界。 |
| `EBU6304_17_Revision.pdf` | 复习课件汇总课程目标：评价质量、理解过程、需求、用户故事、原型、分析/设计/架构、实现/测试、项目/风险/质量、伦理/安全、SOLID 和设计模式。参考 p2, p3, p4, p5, p6, p7, p8, p9。 | 审查清单应覆盖全课程，不只覆盖安全或设计模式。 |

## 2. 项目审查基线

- 技术栈：Java 17、Jakarta Servlet 6、JSP、Jackson JSON 文件存储、JUnit 6、可选 Qwen/DashScope AI。证据见 `pom.xml` 和 `README.md:56-66`。
- 架构说明：README 描述单 WAR 部署、`AuthFilter -> Servlet -> Service -> TaDatabase -> JSON Table Store` 的路径，以及 AI 只在服务端使用 API key。证据见 `README.md:70-106`。
- 数据层：实际代码包含 `domain`、`db/core`、`db/store`、`db/repository`、`db/facade`、`service`、`web/servlet` 等包；`docs/ta-recruitment-system/sprint3-json-database-architecture.md` 对 11 张 JSON 表、Repository 和 Facade 有较完整解释。
- 测试基线：2026-05-19 执行 `mvn test`，结果为 51 个测试全部通过、0 失败、0 错误、0 跳过。
- 静态搜索基线：已检查角色授权、文件操作、密码/API key、异常吞掉、输出转义、上传入口、AI 调用入口和 CSRF 关键词。

## 3. 总体结论

本项目已经具备较完整的课程项目工程化形态：角色清楚，主流程完整，分层基本合理，JSON 数据访问层有 Repository/Facade 封装，AI 功能有 UI 免责声明和配置开关，测试覆盖了不少核心 Service 与数据层行为。

最初最需要优先处理的是安全和契约一致性：原 `/db-demo` 作为公开演示入口具备写库能力，`forgot-password` 不需要邮件/token/当前密码即可重置 TA 密码，项目整体没有 CSRF 防护，上传文件主要依赖扩展名而非内容校验，接口契约写“TA-only registration”但实现允许 MO 自助注册。这些问题在真实部署中风险较高；当前已按本清单完成高优先级修复，并将数据库演示入口规范到 `/admin/database`。

## 4. 逐项审查清单与项目评估

### C01. 软件质量属性是否覆盖完整

- 审查问题：系统是否同时考虑功能性、可用性、可靠性、性能、安全、兼容性、可维护性和可扩展性，而不是只实现页面流程。
- 课程依据：`EBU6304_01_Introduction to Software Engineering.pdf` p23, p24, p29, p31；`EBU6304_12_Risk and Quality Management.pdf` p17, p18, p21, p22。
- 项目证据：README 列出 TA/MO/Admin/Platform 功能，见 `README.md:24-52`；架构和设计选择见 `README.md:70-106`；最终验收清单覆盖 build、TA、MO、Admin、通知和持久化，见 `docs/final-acceptance-checklist.md`。
- 结论：部分通过。
- 建议优先级：中。
- 建议：补充可度量的非功能目标，例如登录响应时间、并发写入限制、上传大小与格式策略、安全测试通过标准、可用性验收标准。

### C02. 软件过程与敏捷迭代是否有证据

- 审查问题：需求、分析、设计、实现、测试、部署、维护是否形成可追踪工作流，是否有迭代产物。
- 课程依据：`EBU6304_02_Agile Software Development.pdf` p2, p11, p12, p27；`EBU6304_10_Project Management.pdf` p8, p9, p10。
- 项目证据：中期文档描述 3 个 Sprint，见 `docs/midterm-acceptance-report.md`；数据层设计文档说明 Sprint 3 JSON 数据库架构；集成清单记录当前状态和联调建议，见 `FRONTEND_BACKEND_INTEGRATION_CHECKLIST.md:13-29`。
- 结论：通过。
- 建议优先级：低。
- 建议：在 README 或 `docs/README.md` 中把 Sprint 文档、验收清单和本审查清单互相链接，形成更清楚的交付路径。

### C03. 需求是否可追溯到角色和功能

- 审查问题：TA、MO、Admin 三类用户故事是否能追踪到页面、Servlet、Service、数据表和测试。
- 课程依据：`EBU6304_03_Requirements.pdf` p3, p4, p12, p13, p14；`EBU6304_04_User stories and prototyping.pdf` p4, p16, p29, p36。
- 项目证据：README 功能按 TA/MO/Admin 划分，见 `README.md:26-52`；接口契约定义角色、路由和全局授权规则，见 `FRONTEND_BACKEND_INTERFACE_CONTRACT.md:36-42`；中期文档写出核心用户故事。
- 结论：部分通过。
- 建议优先级：中。
- 建议：为每个核心用户故事加上验收测试 ID 或文档链接，例如 “TA submit application -> `/application` -> `ApplicationService.submit` -> `ApplicationServiceTest`”。

### C04. 非功能需求是否可验证

- 审查问题：安全、性能、可用性、可靠性、兼容性等非功能需求是否有客观指标。
- 课程依据：`EBU6304_03_Requirements.pdf` p15, p18, p19, p20, p33。
- 项目证据：`README.md` 给出运行环境和技术栈；`docs/final-acceptance-checklist.md` 给出人工验收流程；`docs/non-functional-requirements.md` 新增安全、认证、授权、上传、AI 隐私、可用性、可靠性、数据一致性、兼容性和性能目标及验证方法。
- 结论：通过。
- 建议优先级：低。
- 建议：后续把性能目标自动化，例如 Playwright 冒烟测试或 JMeter 基准。

### C05. 用户故事是否具备验收标准

- 审查问题：每个故事是否有“完成后应为真”的验收标准。
- 课程依据：`EBU6304_04_User stories and prototyping.pdf` p16, p18, p22, p23, p29。
- 项目证据：`docs/final-acceptance-checklist.md` 已覆盖 TA、MO、Admin 和通知持久化的人工验收步骤；`src/test/java` 有 51 个自动测试。
- 结论：部分通过。
- 建议优先级：中。
- 建议：把 `final-acceptance-checklist.md` 的步骤映射到具体用户故事，并标注自动测试或手工测试状态。

### C06. 项目术语是否一致

- 审查问题：TA、MO、Admin、Vacancy、Application、Resume、Workload、Skill 等术语是否统一，是否避免同义词混用。
- 课程依据：`EBU6304_04_User stories and prototyping.pdf` p11。
- 项目证据：接口契约统一 `userRole`、`vacancyId`、`applicationId`、`resumeId`、`errorMessage`、`successMessage` 等字段，见 `FRONTEND_BACKEND_INTERFACE_CONTRACT.md:12-34`；README 和 JSP 基本使用相同业务词。
- 结论：通过。
- 建议优先级：低。
- 建议：把“Vacancy/Job”“MO/Module Organiser”等中英文同义词收进项目 glossary，减少文档之间的轻微漂移。

### C07. 分析模型是否体现 Entity/Boundary/Control

- 审查问题：持久化实体、用户/外部边界、控制流程是否有清晰分离。
- 课程依据：`EBU6304_05_Analysis.pdf` p7, p8, p10, p11, p13, p15。
- 项目证据：`domain/entity` 承载实体；JSP 和 Servlet 是边界；`service` 协调业务流程；`db/repository` 和 `db/facade` 负责数据访问；`ApplicationService` 明确协调申请生命周期，见 `src/main/java/com/bupt/ta/service/ApplicationService.java:35-40`。
- 结论：通过。
- 建议优先级：低。
- 建议：在架构文档中明确标注 EBC 对应关系，可作为课程展示亮点。

### C08. 分层架构是否清晰且依赖方向合理

- 审查问题：Web 层、Service 层、Repository 层、Store 层是否分层清楚，上层是否避免直接访问底层 JSON。
- 课程依据：`EBU6304_06_Design.pdf` p7, p8；`EBU6304_09_Software Architecture-2.pdf` p20, p21, p22。
- 项目证据：README 架构图描述 `Servlet -> Service -> DB -> Store`，见 `README.md:70-100`；`FileTaDatabase` 将 11 个 Repository 收口，见 `src/main/java/com/bupt/ta/db/facade/FileTaDatabase.java:70-114`。
- 结论：通过。
- 建议优先级：低。
- 建议：继续避免 JSP/Servlet 直接操作 JSON 文件，新增业务仍从 Service 和 `TaDatabase` 进入。

### C09. 架构视图和部署假设是否充分

- 审查问题：是否说明逻辑视图、开发视图、进程视图、物理/部署视图和关键场景。
- 课程依据：`EBU6304_09_Software Architecture-2.pdf` p11, p12, p13, p14。
- 项目证据：README 有单 WAR、Tomcat、JSON 文件、Qwen 外部服务的部署图，见 `README.md:70-100`；数据层文档有包结构和事务模型。
- 结论：部分通过。
- 建议优先级：中。
- 建议：补一张 4+1 视图摘要，特别是运行时并发、JSON 文件单 JVM 假设、AI 调用超时和数据目录部署位置。

### C10. Facade / Repository 模式是否用得恰当

- 审查问题：模式是否降低复杂度，而不是为了模式而模式。
- 课程依据：`EBU6304_15_Design Patterns.pdf` p2, p3, p15, p16, p17。
- 项目证据：`TaDatabase` 和 `FileTaDatabase` 作为数据库门面，统一暴露 `users()`、`resumes()`、`jobs()` 等入口，见 `src/main/java/com/bupt/ta/db/facade/FileTaDatabase.java:121-184`；`BaseJsonRepository` 复用 CRUD。
- 结论：通过。
- 建议优先级：低。
- 建议：保留当前 Facade，后续迁移 SQLite/MySQL 时只替换 Repository 实现。

### C11. 是否存在遗留或重复抽象

- 审查问题：是否有已废弃但仍留在源码中的入口，增加理解成本。
- 课程依据：`EBU6304_07_Implementation.pdf` p15；`EBU6304_12_Risk and Quality Management.pdf` p27, p28。
- 项目证据：`src/main/java/com/bupt/ta/persistence/TaDatabase.java` 和 `DatabaseProvider.java` 是 `@Deprecated` 包装器，且静态搜索未发现生产代码使用它们。
- 结论：部分通过。
- 建议优先级：低。
- 建议：如果不需要向后兼容，删除或在文档中说明其遗留用途，避免新同学误用 `com.bupt.ta.persistence`。

### C12. SOLID: SRP 是否基本满足

- 审查问题：类和方法是否有单一职责，避免一个类混合 UI、业务、持久化、文件和 AI。
- 课程依据：`EBU6304_14_Design Principles-2.pdf` p9, p18；`EBU6304_07_Implementation.pdf` p6, p15。
- 项目证据：数据读写拆到 `JsonTableStore`，原子写拆到 `AtomicJsonFileWriter`，业务流程拆到多个 Service；但 `ResumesServlet` 同时处理页面加载、上传、编辑、AI Review 和视图组装，文件较重。
- 结论：部分通过。
- 建议优先级：中。
- 建议：后续可把 `ResumesServlet` 的上传、AI、视图模型构建拆成 helper/service，降低单类变化原因。

### C13. SOLID: OCP 和 Strategy 是否支持变化

- 审查问题：新增评分、AI 模型、存储实现或文件类型时，是否尽量扩展而不是修改大量现有逻辑。
- 课程依据：`EBU6304_14_Design Principles-2.pdf` p21, p26, p27；`EBU6304_15_Design Patterns.pdf` p51。
- 项目证据：Repository 接口有利于替换存储；但 AI 模型选择、评分公式、文件类型白名单和内容类型判断主要由 `if/else` 或硬编码完成，见 `QwenAiService.java:39-45`、`ResumeFileUpload.java:15-25`、`ApplicationDetailServlet.java:191-200`。
- 结论：部分通过。
- 建议优先级：中。
- 建议：把评分策略、文件类型策略、AI 模型配置封装成可替换组件，降低新增类型和模型时的修改面。

### C14. SOLID: ISP 和接口大小是否合理

- 审查问题：接口是否过大，调用者是否被迫依赖无关方法。
- 课程依据：`EBU6304_14_Design Principles-2.pdf` p36, p40, p41。
- 项目证据：Repository 接口按实体拆分，`CrudRepository` 只含基础 CRUD；`TaDatabase` 是应用级聚合门面，方法多但语义清楚。
- 结论：通过。
- 建议优先级：低。
- 建议：如未来模块继续增大，可以按 bounded context 拆分 `TaDatabase` 的只读查询视图和写操作视图。

### C15. SOLID: DIP 是否落实到高层业务

- 审查问题：高层业务是否依赖抽象而不是具体文件存储实现。
- 课程依据：`EBU6304_14_Design Principles-2.pdf` p43, p44, p45。
- 项目证据：Service 层依赖 `TaDatabase` 接口，见 `ApplicationService.java:42-48`、`MatchingService.java:37-43`；Web 层通过 `DatabaseProvider.get()` 获取门面。
- 结论：通过。
- 建议优先级：低。
- 建议：保持 Service 构造函数注入接口，测试和替换存储会更容易。

### C16. 认证是否稳健

- 审查问题：登录是否验证激活状态、密码哈希是否安全、是否避免把密钥或明文密码写入客户端。
- 课程依据：`EBU6304_13_Secure Software Development.pdf` p2, p4, p18；`EBU6304_01_Introduction to Software Engineering.pdf` p23。
- 项目证据：`AuthService` 验证账号 active 并用 `PasswordUtil.checkPassword`，见 `src/main/java/com/bupt/ta/service/AuthService.java`；`PasswordUtil` 使用 BCrypt，见 `src/main/java/com/bupt/ta/util/PasswordUtil.java`；登录成功后 `LoginServlet` 调用 `req.changeSessionId()`；README 说明 API key 只在服务端配置。
- 结论：部分通过。
- 建议优先级：中。
- 建议：已修复 session fixation 风险；仍建议加入登录失败限速或锁定策略。

### C17. 自助密码重置是否安全

- 审查问题：忘记密码是否有邮件验证、一次性 token、当前密码或管理员审批。
- 课程依据：`EBU6304_13_Secure Software Development.pdf` p2, p4, p18；`EBU6304_12_Risk and Quality Management.pdf` p5, p8。
- 项目证据：`ForgotPasswordServlet` 现在只接收 email 并返回通用提示，不再修改密码；`TaAccountService.resetPasswordForTa` 已移除；管理员可在 `/admin/users` 中重置密码，且管理员初始/重置密码最少 8 位。
- 结论：通过。
- 建议优先级：低。
- 建议：若未来接入邮件基础设施，可升级为一次性 token 或验证码流程；上线前继续避免账号存在性枚举。

### C18. 注册角色契约是否一致

- 审查问题：接口契约、前端和后端实现是否对自助注册角色达成一致。
- 课程依据：`EBU6304_03_Requirements.pdf` p14；`EBU6304_04_User stories and prototyping.pdf` p11；`EBU6304_12_Risk and Quality Management.pdf` p17。
- 项目证据：接口契约保持“TA self-service registration (TA role only)”；`RegisterServlet.parseSelfRegistrationRole` 和 `TaAccountService.registerUser` 均强制自助注册为 `UserRole.TA`；`register.jsp` 已移除 MO 选项；测试覆盖 MO 参数被降级为 TA。
- 结论：通过。
- 建议优先级：低。
- 建议：后续若开放 MO 注册，应作为新的需求变更同步契约、审批流和权限测试。

### C19. 授权是否集中且一致

- 审查问题：敏感操作是否在服务端做角色和对象所有权校验，而不是只靠按钮显隐。
- 课程依据：`EBU6304_13_Secure Software Development.pdf` p18；`EBU6304_09_Software Architecture-2.pdf` p20。
- 项目证据：`ApplicationDecisionServlet` 按 action 要求 TA 或 MO，见 `src/main/java/com/bupt/ta/web/servlet/ApplicationDecisionServlet.java:63-96`；`ApplicationService.assertMoOwnsApplication` 校验 MO 是否拥有岗位，见 `ApplicationService.java:306-310`；`AiMoApplicantsRankServlet` 校验 MO 和岗位 owner，见 `src/main/java/com/bupt/ta/web/servlet/AiMoApplicantsRankServlet.java:55-104`。
- 结论：部分通过。
- 建议优先级：中。
- 建议：把角色和 ownership 校验抽成统一 helper 或 annotation-like pattern，降低 28 个 Servlet 中散落判断导致的遗漏风险。

### C20. `/admin/database` 是否应公开

- 审查问题：演示入口是否可被未登录用户访问，是否能读取或修改真实数据。
- 课程依据：`EBU6304_13_Secure Software Development.pdf` p2, p4, p18；`EBU6304_12_Risk and Quality Management.pdf` p5, p8。
- 项目证据：`AuthFilter` 不再公开放行 `/admin/database`；`DbDemoServlet.doGet/doPost` 均调用 `requireAdmin`，未登录会重定向登录，非 Admin 返回 403；POST 同时受全局 CSRF 防护。
- 结论：通过。
- 建议优先级：低。
- 建议：若未来保留演示入口，建议继续使用单独 demo 数据目录，避免误操作正式数据。

### C21. CSRF 防护是否存在

- 审查问题：所有会改变状态的 POST 表单是否有服务端校验 token。
- 课程依据：`EBU6304_13_Secure Software Development.pdf` p4, p13, p18。
- 项目证据：新增 `CsrfFilter` 与 `CsrfTokens`，所有 POST/PUT/PATCH/DELETE 必须携带 `_csrf` 参数或 `X-CSRF-Token`；共享 `portal_theme.jsp` 会自动为 POST 表单、动态表单、multipart 表单和同源 `fetch` 注入 token；新增 `CsrfTokensTest` 覆盖匹配逻辑。
- 结论：通过。
- 建议优先级：低。
- 建议：后续可补充 SameSite cookie、Origin/Referer 检查和端到端浏览器回归测试。

### C22. 文件上传是否安全

- 审查问题：上传是否限制大小、路径、扩展名、MIME/魔数、恶意内容和下载行为。
- 课程依据：`EBU6304_13_Secure Software Development.pdf` p4, p13, p18；`EBU6304_08_Testing.pdf` p23, p24。
- 项目证据：`@MultipartConfig` 限制 15MB 文件和 20MB 请求；上传文件名用 `Paths.get(...).getFileName()` 和 UUID 保存；`ResumeFileUpload` 现在校验扩展名、MIME 和 PDF/PNG/JPEG/DOC/DOCX/TXT 文件头；`ResumesServlet` 与 `ApplicationSubmitServlet` 复用该工具；下载改为 `Content-Disposition: attachment` 并设置 `X-Content-Type-Options: nosniff`；新增 `ResumeFileUploadTest`。
- 结论：部分通过。
- 建议优先级：中。
- 建议：课程项目已完成最小安全检查；生产环境仍建议接入恶意文件扫描、隔离存储和更完整的文档解析安全策略。

### C23. 输出编码和 XSS 防护是否到位

- 审查问题：用户输入、错误消息、AI 输出、查询参数是否经过 HTML 转义。
- 课程依据：`EBU6304_13_Secure Software Development.pdf` p18；`EBU6304_07_Implementation.pdf` p15。
- 项目证据：共享 flash 使用 `<c:out>` 输出消息，见 `src/main/webapp/WEB-INF/jsp/components/flash_messages.jsp:3-21`；大量 JSP 使用 `<c:out>` 或 `fn:escapeXml`，例如 `resumes.jsp:200-222`、`vacancies.jsp:179-217`；AI 结果由前端渲染时仍需确认是否按安全 Markdown/HTML 渲染。
- 结论：部分通过。
- 建议优先级：中。
- 建议：统一封装 AI/Markdown 渲染 sanitizer，禁止直接 `innerHTML` 插入未净化模型输出或用户输入。

### C24. 密钥和第三方 AI 配置是否安全

- 审查问题：API key 是否只在服务端读取，是否避免提交到仓库和暴露给浏览器。
- 课程依据：`EBU6304_13_Secure Software Development.pdf` p18；`EBU6304_16_AI for Software Development.pdf` p10。
- 项目证据：`QwenAiService.resolveApiKey` 只从环境变量读取，见 `src/main/java/com/bupt/ta/service/QwenAiService.java:76-79`；请求头在服务端设置 `Authorization`，见 `QwenAiService.java:558-564`；README 和 AI 配置文档说明 `QWEN_API_KEY` 和 consent/fallback，见 `docs/guides/ai-and-configuration.md:1-29`。
- 结论：通过。
- 建议优先级：低。
- 建议：增加 secret scanning 到提交前或 CI，避免未来误提交 key。

### C25. AI 同意、隐私和伦理边界是否清楚

- 审查问题：用户是否在发送个人信息到第三方模型前看到说明并明确同意，AI 结果是否被标注为非决定性。
- 课程依据：`EBU6304_11_Ethics and AI in Software Engineering.pdf` p3, p5, p7, p8, p14, p18, p24, p27；`EBU6304_16_AI for Software Development.pdf` p5, p6, p7, p8；`EBU6304_13_Secure Software Development.pdf` p10, p20。
- 项目证据：README 已更新为端到端 AI gating；AI 弹窗仍要求用户点击同意；前端 AI 请求携带 `aiConsent=true`；`AiRequestGuard` 在服务端强制校验同意并写入 `AI_REQUEST` 审计事件；新增 `AiRequestGuardTest`。
- 结论：通过。
- 建议优先级：低。
- 建议：未来可把同意文本版本号、模型版本和数据保留策略写入审计元数据。

### C26. AI 排序和招聘公平性是否有审计机制

- 审查问题：AI 用于申请者排序和建议时，是否能解释、审计、避免单独决定录用。
- 课程依据：`EBU6304_11_Ethics and AI in Software Engineering.pdf` p13, p14, p18, p19, p24, p28。
- 项目证据：Applications 页面文案提醒 AI 排序仅供参考；`AiMoApplicantsRankServlet` 要求 MO 只能排序自己岗位申请；所有 AI endpoint 现在通过 `AiRequestGuard.appendAudit` 记录操作者、目标、feature 与 consent，但不持久化完整 prompt/输出以降低隐私风险。
- 结论：部分通过。
- 建议优先级：中。
- 建议：继续明确不得自动录用或拒绝；如需要更强可追溯性，可在不泄露敏感简历全文的前提下记录模型版本、同意文本版本和最小化输入/输出摘要。

### C27. 持久化匹配分析是否准确表达 AI/fallback

- 审查问题：系统是否避免把规则分误称为 AI 分，是否说明 fallback。
- 课程依据：`EBU6304_16_AI for Software Development.pdf` p4, p8；`EBU6304_11_Ethics and AI in Software Engineering.pdf` p24。
- 项目证据：`MatchingService.runAnalysis` 始终先计算规则分，并把 `aiScore` 设为规则分；即使配置了 API key，也说明 persistent analysis 使用 rule-based fallback，见 `src/main/java/com/bupt/ta/service/MatchingService.java:62-80`。
- 结论：部分通过。
- 建议优先级：中。
- 建议：UI 字段从 “AI/Fallback 分” 改得更明确，例如 “规则兜底分”；若未来真的调用 AI，应把 AI 调用、失败和 fallback 状态拆成枚举字段。

### C28. JSON 数据存储是否保证基本一致性

- 审查问题：文件读写是否原子、是否有锁、是否处理损坏文件。
- 课程依据：`EBU6304_12_Risk and Quality Management.pdf` p5, p8, p17, p18；`EBU6304_09_Software Architecture-2.pdf` p13。
- 项目证据：`JsonTableStore` 使用 `ReadWriteLock`、内存快照、缺失文件初始化和损坏文件异常，见 `src/main/java/com/bupt/ta/db/store/JsonTableStore.java:22-33`、`157-188`；`AtomicJsonFileWriter` 先写 `.tmp` 再 atomic move，见 `src/main/java/com/bupt/ta/db/store/AtomicJsonFileWriter.java:13-31`。
- 结论：通过。
- 建议优先级：低。
- 建议：已在部署文档中明确单 JVM 独占 `data/` 目录；若部署形态变化，需迁移到事务型数据库。

### C29. 跨表“事务”是否真正可回滚

- 审查问题：跨表操作失败时，是否能回滚之前已经写入的 JSON 文件。
- 课程依据：`EBU6304_12_Risk and Quality Management.pdf` p5, p7, p8；`EBU6304_09_Software Architecture-2.pdf` p13。
- 项目证据：`FileTaDatabase.executeAtomically` 只提供一把 JVM 内锁，见 `src/main/java/com/bupt/ta/db/facade/FileTaDatabase.java:177-184`；`ApplicationService.acceptOffer` 在锁内连续保存申请、workload、通知、审计等，见 `ApplicationService.java:237-251`，但没有失败回滚机制。
- 结论：部分通过。
- 建议优先级：中。
- 建议：已在非功能需求和部署文档中明确这是“互斥区”而非完整事务；若要求强一致，增加预写日志、整体快照回滚或迁移到事务型数据库。

### C30. 业务状态流是否有约束

- 审查问题：申请、岗位、workload 状态变化是否防止非法跳转，并有通知/审计副作用。
- 课程依据：`EBU6304_05_Analysis.pdf` p15；`EBU6304_12_Risk and Quality Management.pdf` p17, p18。
- 项目证据：`ApplicationService` 对 submit、review、offer、accept、decline、withdraw 做状态检查，见 `ApplicationService.java:73-130`、`166-304`；`JobService.changeStatus` 取消岗位时联动申请和 workload，见 `src/main/java/com/bupt/ta/service/JobService.java:93-134`。
- 结论：通过。
- 建议优先级：低。
- 建议：增加状态机图和非法跳转测试，帮助维护者理解边界。

### C31. 审计日志是否覆盖关键操作

- 审查问题：登录、创建、更新、状态变化、AI 分析等是否留下审计证据。
- 课程依据：`EBU6304_12_Risk and Quality Management.pdf` p15, p18；`EBU6304_11_Ethics and AI in Software Engineering.pdf` p24。
- 项目证据：`LoginServlet.appendLoginAudit` 记录成功/失败登录，见 `src/main/java/com/bupt/ta/web/servlet/LoginServlet.java:99-116`；`ApplicationService.appendAudit` 记录申请变化，见 `ApplicationService.java:478-491`；`MatchingService.runAnalysis` 记录 match score 更新，见 `MatchingService.java:83-101`。
- 结论：部分通过。
- 建议优先级：中。
- 建议：补齐 `/admin/database`、AI 即时排序/建议、管理员用户密码重置、忘记密码重置等安全敏感动作的审计。

### C32. 输入校验是否系统化

- 审查问题：请求参数、UUID、枚举、数值、日期、业务外键是否被验证。
- 课程依据：`EBU6304_13_Secure Software Development.pdf` p16, p18；`EBU6304_08_Testing.pdf` p23, p24, p27。
- 项目证据：`ApplicationSubmitServlet` 校验登录、TA 角色、vacancyId、resumeId、文件扩展名，见 `ApplicationSubmitServlet.java:49-108`；Repository 层验证必填和唯一约束，见 `JsonUserRepository.java:61-78`、`JsonSkillRepository.java:35-44`。
- 结论：部分通过。
- 建议优先级：中。
- 建议：建立统一 request validator，避免各 Servlet 自己解析导致规则不一致；为日期、工时、金额、GPA、labels 增加边界测试。

### C33. 错误处理是否清楚且不过度泄露

- 审查问题：错误是否给用户明确反馈，同时避免泄露内部路径、堆栈和第三方错误细节。
- 课程依据：`EBU6304_07_Implementation.pdf` p15；`EBU6304_13_Secure Software Development.pdf` p18。
- 项目证据：`RedirectUrls.withQueryParam` 统一编码重定向消息，见 `src/main/java/com/bupt/ta/web/util/RedirectUrls.java:13-19`；共享 flash 会转义消息，见 `flash_messages.jsp:3-21`；但部分 catch 将 `ex.getMessage()` 直接回显给用户，例如 `ApplicationSubmitServlet.java:98-100`。
- 结论：部分通过。
- 建议优先级：中。
- 建议：用户侧使用通用安全消息，详细异常写 servlet log 或审计日志，特别是文件路径、AI API 错误和数据库损坏信息。

### C34. 代码质量和命名是否清晰

- 审查问题：命名、文件组织、注释、常量、方法长度和重复代码是否符合实现课件中的代码质量要求。
- 课程依据：`EBU6304_07_Implementation.pdf` p4, p15, p24, p26, p30。
- 项目证据：包结构较清楚，JavaDoc 对核心 Service 和数据层有说明；但静态搜索发现 `PasswordUtil.main` 会打印测试 hash，见 `src/main/java/com/bupt/ta/util/PasswordUtil.java:31-32`；部分 Servlet 捕获宽泛 `Exception`。
- 结论：部分通过。
- 建议优先级：低。
- 建议：删除生产工具类中的 `main`；逐步收窄宽泛 catch；为长 Servlet 建立视图模型和 helper。

### C35. 注释是否解释“为什么”而不是重复“做什么”

- 审查问题：注释是否对复杂逻辑有帮助，是否避免噪音。
- 课程依据：`EBU6304_07_Implementation.pdf` p24, p25, p26, p27。
- 项目证据：`ApplicationService` 对申请生命周期和 workload 创建时机有有用 JavaDoc，见 `ApplicationService.java:35-40`、`175-179`、`207-209`；`QwenAiService` 对不同 AI 功能有说明。
- 结论：通过。
- 建议优先级：低。
- 建议：继续保留业务规则型注释，减少显而易见的逐行注释。

### C36. 构建、集成和回归测试是否自动化

- 审查问题：是否能自动构建和测试，是否有回归测试基线。
- 课程依据：`EBU6304_07_Implementation.pdf` p17, p21, p23；`EBU6304_08_Testing.pdf` p21。
- 项目证据：Maven Surefire 配置在 `pom.xml`；`mvn test` 当前 58 tests 全部通过；README 要求 `mvn test` 和 `mvn clean package`，见 `README.md:118-120`。
- 结论：通过。
- 建议优先级：低。
- 建议：若使用 GitHub，配置 CI 在 push/PR 自动运行 `mvn test`，并保存测试报告。

### C37. 测试层级是否完整

- 审查问题：是否覆盖单元、集成、系统、验收、性能、安全、可用性和回归测试。
- 课程依据：`EBU6304_08_Testing.pdf` p7, p10, p11, p12, p18, p55。
- 项目证据：测试主要覆盖 Service、Repository、Store、配置、少量 Servlet 错误态；例如 `ApplicationServiceTest` 覆盖重复投递、offer/workload、上传快照、引用简历删除，见 `src/test/java/com/bupt/ta/service/ApplicationServiceTest.java:34-121`；`JsonTableStoreTest` 覆盖初始化、查询、损坏 JSON、失败写入，见 `src/test/java/com/bupt/ta/db/store/JsonTableStoreTest.java:26-86`。
- 结论：部分通过。
- 建议优先级：中。
- 建议：补充浏览器/E2E 测试、CSRF/授权负向测试、上传安全测试、AI endpoint 直连测试和性能 smoke test。

### C38. 测试用例是否使用边界值和等价类

- 审查问题：对密码长度、上传大小、日期范围、GPA、小时数、slots、枚举、UUID 是否覆盖边界和非法输入。
- 课程依据：`EBU6304_08_Testing.pdf` p23, p24, p25, p27, p31。
- 项目证据：`TaAccountServiceTest` 覆盖注册/重置若干分支；`ApplicationServiceTest` 覆盖业务路径；但未看到上传扩展名伪造、15MB 边界、过期岗位、CSRF、session fixation、非法角色自助注册等测试。
- 结论：部分通过。
- 建议优先级：中。
- 建议：为每个高风险输入建立等价类和边界表，并在 JUnit 中覆盖至少正常、低边界、高边界、非法格式、越权五类。

### C39. 性能和可扩展性是否经过考虑

- 审查问题：数据增长、并发请求、列表查询、通知数、文件大小和 AI 超时是否有性能策略。
- 课程依据：`EBU6304_01_Introduction to Software Engineering.pdf` p23, p24；`EBU6304_09_Software Architecture-2.pdf` p4, p20, p21, p29；`EBU6304_12_Risk and Quality Management.pdf` p23。
- 项目证据：JSON store 每次保存会整体重写表文件，见 `JsonTableStore.java:191-194`；README 明确适合 coursework 和 single-JVM demo，见 `README.md:102-106`；AI HTTP 超时为 90 秒，见 `QwenAiService.java:558-564`。
- 结论：部分通过。
- 建议优先级：中。
- 建议：记录“课程演示规模”假设；若用户或申请量上升，迁移数据库、分页查询、异步 AI、缓存通知计数。

### C40. 项目风险是否被识别和监控

- 审查问题：是否有项目、产品/技术、业务风险清单和应对方案。
- 课程依据：`EBU6304_10_Project Management.pdf` p8, p36, p47；`EBU6304_12_Risk and Quality Management.pdf` p3, p5, p7, p8, p34。
- 项目证据：已有最终验收清单和部署/AI 文档；`docs/risk-register.md` 新增 JSON 单 JVM、跨表事务、DB demo、CSRF、密码重置、AI 偏见、第三方 API、上传文件、静态分析和契约漂移等风险及缓解措施。
- 结论：通过。
- 建议优先级：低。
- 建议：每个 Sprint 结束前复查高影响风险，并在发布前确认风险状态仍准确。

### C41. 项目管理和交付文档是否足够

- 审查问题：新成员是否能根据文档完成构建、部署、验收和功能理解。
- 课程依据：`EBU6304_07_Implementation.pdf` p24, p27；`EBU6304_10_Project Management.pdf` p8, p9, p17, p56。
- 项目证据：README 有快速开始、部署、demo 账号、数据目录、AI 配置和文档索引；`docs/README.md` 索引用户手册、getting started、deployment、AI、final checklist，见 `docs/README.md:1-20`。
- 结论：通过。
- 建议优先级：低。
- 建议：把本审查清单加入 `docs/README.md`，并在最终提交前更新 README 的安全限制说明。

### C42. Web 接口是否符合契约

- 审查问题：route、method、参数、attribute、pageState 是否与契约一致。
- 课程依据：`EBU6304_03_Requirements.pdf` p14, p18；`EBU6304_07_Implementation.pdf` p24。
- 项目证据：接口契约有详尽 route 和字段；集成清单记录 `/admin/database` 仍存在且不应作为正式业务联调基线，见 `FRONTEND_BACKEND_INTEGRATION_CHECKLIST.md:21-29`；但注册角色契约和实现不一致，见 C18。
- 结论：部分通过。
- 建议优先级：中。
- 建议：在最终提交前做一次“契约对代码”审计，至少修正注册角色、`/admin/database` 暴露范围和 AI consent 的服务端约束。

### C43. 设计模式是否有助于降低耦合

- 审查问题：设计模式是否对应真实问题，并使代码更可维护、可测试、可复用。
- 课程依据：`EBU6304_15_Design Patterns.pdf` p3, p8, p16, p17, p31, p40, p51, p55, p58。
- 项目证据：Facade 和 Repository 使用合理；Singleton-like 的 `DatabaseProvider` 通过 ServletContext 持有单例，见 `DatabaseProvider.java:18-33`；目前没有明显滥用模式。
- 结论：通过。
- 建议优先级：低。
- 建议：不要为了覆盖课件而硬加 Observer/Strategy/Factory；只有在通知分发、评分策略、文件类型策略变复杂时再引入。

### C44. 安全测试和静态分析是否纳入流程

- 审查问题：是否运行自动化安全工具、依赖漏洞检查、静态分析，并人工复核 AI/安全敏感代码。
- 课程依据：`EBU6304_13_Secure Software Development.pdf` p7, p11, p13, p14, p18, p20；`EBU6304_08_Testing.pdf` p11, p53。
- 项目证据：已新增 `CsrfTokensTest`、`AiRequestGuardTest`、`ResumeFileUploadTest`，覆盖 CSRF token、AI 同意、上传伪装文件等安全回归点；仍未看到 SpotBugs、Checkstyle、OWASP Dependency-Check、Semgrep、ZAP/Burp。
- 结论：部分通过。
- 建议优先级：中。
- 建议：下一步至少增加依赖漏洞检查和静态分析；对 `/admin/database` 管理员授权、密码帮助流程、管理员操作补充 servlet/filter 级回归测试。

### C45. 可维护性和未来演进是否清楚

- 审查问题：未来从 JSON 迁移真实数据库、替换 AI 模型、扩展角色或增加 API 时是否有清楚路径。
- 课程依据：`EBU6304_01_Introduction to Software Engineering.pdf` p24；`EBU6304_06_Design.pdf` p7, p8；`EBU6304_14_Design Principles-2.pdf` p21, p43, p45。
- 项目证据：数据访问封装和 `TaDatabase` 门面为迁移留出空间；配置和 README 说明数据目录；AI 模型由环境变量/系统属性解析，见 `QwenAiService.java:81-96`。
- 结论：通过。
- 建议优先级：低。
- 建议：把“JSON -> SQLite/MySQL”的迁移前提和不兼容点写入设计文档，避免最终阶段临时改动。

## 5. 优先修复建议

| 优先级 | 建议 |
|---|---|
| 已修复 | `/admin/database` 已限制为 Admin，并受 CSRF 防护。 |
| 已修复 | `forgot-password` 不再公开修改密码，改为管理员重置路径。 |
| 已修复 | 已加入全局 CSRF 防护，覆盖 POST 表单、动态表单和同源 fetch。 |
| 已修复 | 自助注册已统一为 TA-only，契约、前端、服务和测试同步。 |
| 已修复 | 上传文件已增加 MIME/魔数校验，下载改为 attachment + nosniff。 |
| 中 | 继续引入静态分析和依赖漏洞检查工具，覆盖授权、AI endpoint、管理员操作。 |
| 已修复 | 已增加非功能需求和风险登记表，明确性能、并发、安全和 AI 伦理假设。 |
| 已修复 | AI endpoint 已要求服务端同意标记并写入 AI_REQUEST 审计。 |
| 中 | 明确 `executeAtomically` 是互斥区而非完整事务，或补充回滚能力。 |
| 低 | 清理废弃 persistence wrapper、`PasswordUtil.main`、长 Servlet 和宽泛异常捕获。 |

## 6. 验证记录

- PDF 解析：17 个课件均可解析，页数和文本页数如下：36、29、33、45、32、31、30、55、38、57、28、34、21、46、59、30、14；每份课件均在本清单中至少引用一次。
- 修复执行：2026-05-19 已按高优先级项落地安全修复，覆盖 `/admin/database`、`forgot-password`、TA-only 注册、CSRF、上传校验、AI 同意/审计和安全测试；并补充非功能需求、风险登记与 JSON 单 JVM/非事务部署说明。
- 测试执行：`mvn test` 于 2026-05-19 运行成功，58 tests, 0 failures, 0 errors, 0 skipped。
- 静态检查：通过 `rg` 检索了 `UserRole`、`currentUser`、`sendError`、`csrf`、`Part`、`Files.copy`、`QWEN_API_KEY`、`PasswordUtil`、`System.out`、`catch (Exception)`、`c:out`、`fn:escapeXml` 等风险点。
