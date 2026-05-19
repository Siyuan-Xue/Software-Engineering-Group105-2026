# QM HIRE 软件工程最终文档索引

本文档目录是 EBU6304 软件工程课程的最终交付型文档集合。它替代早期 sprint 记录、接口沟通稿、阶段性验收笔记等过程文档；过程材料已移动到本地忽略目录 `.local-docs-archive/2026-05-19-process-docs/`，不再作为远端交付物追踪。

## 文档范围

QM HIRE 是 Group 105 的 TA 招聘系统，覆盖 TA 注册、岗位浏览、简历管理、申请提交、MO 审核、offer 处理、工作量监控、管理员维护、审计日志和可选 AI 辅助。最终文档聚焦课程要求的软件工程证据：需求、分析、设计、架构、实现、测试、风险、质量、安全、伦理和部署维护。

## 文档清单

| 文件 | 主要内容 | 对应课程主题 | 评审用途 |
|---|---|---|---|
| [01-requirements-and-user-stories.md](01-requirements-and-user-stories.md) | stakeholders、glossary、功能需求、非功能需求、用户故事、验收标准、需求追踪矩阵 | Requirements、User stories and prototyping、Agile | 判断系统目标、范围、验收标准和实现证据是否一致 |
| [02-analysis-models.md](02-analysis-models.md) | EBC 分析、用例图、领域模型、ER/数据模型、核心 activity diagrams | Analysis、EBC、UML analysis models | 判断问题域建模是否覆盖 TA 招聘业务 |
| [03-architecture-and-design.md](03-architecture-and-design.md) | 4+1 view、组件/包图、部署图、sequence diagrams、state diagram、SOLID、设计模式 | Design、Software Architecture、Design Principles、Design Patterns | 判断设计是否可维护、可演化、职责清晰 |
| [04-implementation-and-code-quality.md](04-implementation-and-code-quality.md) | 实现规范、模块职责、配置、异常、安全编码、Repository/Facade、代码审查清单 | Implementation、Secure Development、SOLID | 指导代码审查和后续维护 |
| [05-testing-and-quality-assurance.md](05-testing-and-quality-assurance.md) | 测试策略、单元/集成/系统/验收、安全/性能/回归测试、报告模板 | Testing、Quality Management | 证明测试覆盖和质量门禁 |
| [06-risk-security-and-ai-ethics.md](06-risk-security-and-ai-ethics.md) | 风险登记表、威胁模型、安全控制、AI 同意/隐私/公平性/人工决策边界 | Risk and Quality Management、Secure Development、Ethics and AI | 证明风险、安全和 AI 伦理被显式管理 |
| [07-user-deployment-and-maintenance.md](07-user-deployment-and-maintenance.md) | 用户手册、管理员入口、运行部署、数据目录、演示账号、故障排查、维护说明 | Project Management、Implementation、Final delivery | 支持评审者运行、演示和维护系统 |
| [08-coursework-documentation-traceability.md](08-coursework-documentation-traceability.md) | 17 份 slides 到文档/diagram 的覆盖矩阵、diagram inventory、交付完整性检查 | Revision、全部课程主题 | 证明 slides 要求的文档与图已覆盖 |

## 图表格式

所有图表均使用 Mermaid 嵌入 Markdown，包括：

- 用例图
- EBC / 领域 class diagram
- ER diagram
- Activity diagrams
- Sequence diagrams
- State diagram
- Component / package diagram
- Deployment diagram
- 4+1 view summary

## 源码与文档边界

这些文档描述当前代码现状，不修改业务逻辑。当前实现的主要证据位于：

- `src/main/java/com/bupt/ta/web/servlet/`：Servlet 路由与控制器入口
- `src/main/java/com/bupt/ta/service/`：招聘、简历、认证、匹配、管理、AI 等业务服务
- `src/main/java/com/bupt/ta/db/`：JSON 数据库 Facade、Repository、Store
- `src/main/java/com/bupt/ta/domain/`：领域实体、枚举和值对象
- `src/main/webapp/`：JSP 页面、公共组件与静态资源
- `src/test/java/`：单元、集成、安全辅助和 Servlet 测试

## 原始课程资料

`docs/slides/` 中的 EBU6304 PDF 是本地课程资料来源。它们用于整理文档依据，但不作为 Git 远端交付资产追踪，以避免把大文件和课堂原始材料提交到仓库。
