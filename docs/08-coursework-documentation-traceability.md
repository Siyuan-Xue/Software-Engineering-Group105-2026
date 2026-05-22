# 08-coursework-documentation-traceability


## Slide-To-Deliverable Matrix

| Slide | Course topic | Expected software engineering evidence | Covered by |
|---|---|---|---|
| EBU6304_01 Introduction to Software Engineering | 软件工程目标、stakeholders、质量、交付物 | 项目范围、stakeholders、质量属性、最终文档索引 | `docs/README.md`、`01-requirements-and-user-stories.md` |
| EBU6304_02 Agile Software Development | 敏捷、迭代、backlog、increment、feedback | 用户故事、验收标准、backlog-style notes、Definition of Done | `01-requirements-and-user-stories.md`、`04-implementation-and-code-quality.md` |
| EBU6304_03 Requirements | 功能/非功能需求、约束、需求追踪 | FR/NFR、scope、glossary、requirements traceability matrix | `01-requirements-and-user-stories.md` |
| EBU6304_04 User stories and prototyping | 用户故事、persona、验收标准、原型反馈 | TA/MO/Admin stories、acceptance criteria、manual workflow expectations | `01-requirements-and-user-stories.md`、`07-user-deployment-and-maintenance.md` |
| EBU6304_05 Analysis | EBC、用例、领域模型、activity diagram | EBC 表和图、use case diagram、domain model、ER diagram、activity diagrams | `02-analysis-models.md` |
| EBU6304_06 Design | UML design、对象交互、状态、职责 | sequence diagrams、state diagram、package/component design、SOLID review | `03-architecture-and-design.md` |
| EBU6304_07 Implementation | 编码、配置、集成、异常、版本控制 | implementation stack、module responsibilities、coding standards、DoD | `04-implementation-and-code-quality.md`、`07-user-deployment-and-maintenance.md` |
| EBU6304_08 Testing | 测试层级、黑盒/白盒、边界值、回归 | test strategy、test inventory、black-box/white-box plan、security tests、report template | `05-testing-and-quality-assurance.md` |
| EBU6304_09 Software Architecture | 架构风格、视图、部署、质量属性 | 4+1 view、component diagram、package diagram、deployment diagram、architecture decisions | `03-architecture-and-design.md` |
| EBU6304_10 Project Management | 计划、风险、质量、交付、维护 | risk register、quality plan、deployment checklist、maintenance tasks | `06-risk-security-and-ai-ethics.md`、`07-user-deployment-and-maintenance.md` |
| EBU6304_11 Ethics and AI in Software Engineering | AI 伦理、隐私、公平、责任 | AI ethics policy、consent、human final decision、privacy controls | `06-risk-security-and-ai-ethics.md` |
| EBU6304_12 Risk and Quality Management | 风险概率/影响、质量门禁、缓解计划 | risk register、quality objectives、QA checklist、known limitations | `05-testing-and-quality-assurance.md`、`06-risk-security-and-ai-ethics.md` |
| EBU6304_13 Secure Software Development | 安全需求、威胁、输入校验、认证授权 | threat model、secure development checklist、CSRF/upload/RBAC/secret controls | `04-implementation-and-code-quality.md`、`06-risk-security-and-ai-ethics.md` |
| EBU6304_14 Design Principles | SOLID、内聚、耦合、可维护性 | SOLID review、layering rules、code review checklist | `03-architecture-and-design.md`、`04-implementation-and-code-quality.md` |
| EBU6304_15 Design Patterns | 常用模式、何时使用/避免过度设计 | Facade、Repository、Filter、DTO、fallback strategy notes | `03-architecture-and-design.md`、`04-implementation-and-code-quality.md` |
| EBU6304_16 AI for Software Development | AI 辅助开发/产品、可靠性、限制 | AI risk controls、consent/audit、fallback、manual accountability | `06-risk-security-and-ai-ethics.md` |
| EBU6304_17 Revision | 全课程复习、需求-设计-测试-质量闭环 | 本追踪矩阵、diagram inventory、final completeness checklist | `08-coursework-documentation-traceability.md` |

## Diagram Inventory

| Diagram | Mermaid type | Location | Course purpose |
|---|---|---|---|
| System context | `flowchart` | `02-analysis-models.md` | 系统边界和外部依赖 |
| Use case diagram | `flowchart` | `02-analysis-models.md` | 角色和用例覆盖 |
| EBC diagram | `classDiagram` | `02-analysis-models.md` | Boundary-Control-Entity 分析 |
| Domain model | `classDiagram` | `02-analysis-models.md` | 领域对象和关系 |
| ER / data model | `erDiagram` | `02-analysis-models.md` | JSON 表式数据关系 |
| TA application activity | `flowchart` | `02-analysis-models.md` | TA 申请流程 |
| MO review activity | `flowchart` | `02-analysis-models.md` | MO 审核流程 |
| Admin management activity | `flowchart` | `02-analysis-models.md` | Admin 用户管理流程 |
| 4+1 view summary | `flowchart` | `03-architecture-and-design.md` | 架构视图 |
| Component diagram | `flowchart` | `03-architecture-and-design.md` | 组件依赖 |
| Package/layer diagram | `flowchart` | `03-architecture-and-design.md` | 开发视图和分层 |
| Deployment diagram | `flowchart` | `03-architecture-and-design.md` | 物理部署 |
| Login sequence | `sequenceDiagram` | `03-architecture-and-design.md` | 登录和 session |
| TA application sequence | `sequenceDiagram` | `03-architecture-and-design.md` | 核心申请交互 |
| MO review sequence | `sequenceDiagram` | `03-architecture-and-design.md` | 审核和 offer |
| AI consent sequence | `sequenceDiagram` | `03-architecture-and-design.md` | AI 请求边界 |
| Application state diagram | `stateDiagram-v2` | `03-architecture-and-design.md` | 申请状态机 |
| Test strategy diagram | `flowchart` | `05-testing-and-quality-assurance.md` | 测试层次和回归 |
| Security threat model | `flowchart` | `06-risk-security-and-ai-ethics.md` | 威胁建模 |

## Final Documentation Completeness Checklist

| Required artifact | Status | Evidence |
|---|---|---|
| Final docs index | Covered | `docs/README.md` |
| Stakeholder and scope document | Covered | `01-requirements-and-user-stories.md` |
| Glossary | Covered | `01-requirements-and-user-stories.md` |
| Functional requirements | Covered | `01-requirements-and-user-stories.md` |
| Non-functional requirements | Covered | `01-requirements-and-user-stories.md` |
| User stories and acceptance criteria | Covered | `01-requirements-and-user-stories.md` |
| Requirements traceability matrix | Covered | `01-requirements-and-user-stories.md` |
| Use case diagram | Covered | `02-analysis-models.md` |
| EBC analysis | Covered | `02-analysis-models.md` |
| Domain class diagram | Covered | `02-analysis-models.md` |
| ER/data model | Covered | `02-analysis-models.md` |
| Activity diagrams | Covered | `02-analysis-models.md` |
| 4+1 architecture view | Covered | `03-architecture-and-design.md` |
| Component diagram | Covered | `03-architecture-and-design.md` |
| Package/layer diagram | Covered | `03-architecture-and-design.md` |
| Deployment diagram | Covered | `03-architecture-and-design.md` |
| Sequence diagrams | Covered | `03-architecture-and-design.md` |
| State diagram | Covered | `03-architecture-and-design.md` |
| SOLID review | Covered | `03-architecture-and-design.md` |
| Design pattern review | Covered | `03-architecture-and-design.md` |
| Implementation guide | Covered | `04-implementation-and-code-quality.md` |
| Code review checklist | Covered | `04-implementation-and-code-quality.md` |
| Testing strategy | Covered | `05-testing-and-quality-assurance.md` |
| Security test plan | Covered | `05-testing-and-quality-assurance.md` |
| Risk register | Covered | `06-risk-security-and-ai-ethics.md` |
| Threat model | Covered | `06-risk-security-and-ai-ethics.md` |
| AI ethics policy | Covered | `06-risk-security-and-ai-ethics.md` |
| User manual | Covered | `07-user-deployment-and-maintenance.md` |
| Deployment and maintenance guide | Covered | `07-user-deployment-and-maintenance.md` |
| Slide traceability | Covered | `08-coursework-documentation-traceability.md` |

## Git Tracking Policy

| Path | Tracking decision | Reason |
|---|---|---|
| `docs/*.md` | Track | Final coursework engineering evidence |
| `docs/slides/` | Ignore | Original course PDFs; large and not final authored deliverables |
| `docs/screenshots/` | Ignore | Generated visual assets can bloat Git history |
| `docs/**/*.pdf` | Ignore | Avoid committing large source/export files |
| `.local-docs-archive/` | Ignore | Local-only archive of process notes |
| Runtime `data/` | Ignore | Contains mutable local data and uploaded files |
| `target/` | Ignore | Build output |

## Route And Terminology Checks

| Item | Expected final term | Notes |
|---|---|---|
| Admin database route | `/admin/database` | Replaces the old public/demo-style route wording |
| Applicant role | TA applicant | Self-registration creates TA only |
| Recruiter role | MO | Module Organiser owns vacancies and reviews applications |
| AI decision boundary | AI assistance/advice | Final hiring decision remains human-controlled |
| Persistence | JSON file store | Not an external relational database |

## Final Review Checklist

1. `git ls-files docs` should show only final Markdown docs.
2. `git ls-files` should not include old frontend interface notes, old sprint/database process notes, or the earlier standalone code-review checklist.
3. `git check-ignore -v docs/slides/EBU6304_01_Introduction\ to\ Software\ Engineering.pdf` should confirm slides are ignored.
4. Search for the old database-demo route string in `docs README.md`; it should return no active final documentation reference.
5. `mvn test` and `git diff --check` should pass before commit and push.
