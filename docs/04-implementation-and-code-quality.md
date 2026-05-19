# 04 实现与代码质量

## 课程依据

Implementation slides 强调：实现不是机械地把设计翻译成代码，而是持续管理可读性、可维护性、集成、配置、异常、代码审查和版本控制。Secure Development、SOLID、Design Patterns slides 进一步要求把安全和质量内建到代码结构中，而不是在最后补丁式加入。

| Slide | 本文档产物 |
|---|---|
| EBU6304_07 Implementation | 编码规范、集成、配置、异常处理、版本控制 |
| EBU6304_13 Secure Software Development | 输入校验、认证授权、文件上传、CSRF、secret 管理 |
| EBU6304_14 Design Principles | SOLID、内聚、耦合、重复代码控制 |
| EBU6304_15 Design Patterns | Facade、Repository、Filter 等实现证据 |
| EBU6304_16 AI for Software Development | AI 功能作为辅助工具时的可靠性和审计 |

## Implementation Stack

| Area | Choice | Evidence |
|---|---|---|
| Language/runtime | Java 17 | `pom.xml`、README |
| Web framework | Jakarta Servlet 6 + JSP + JSTL | `src/main/java/com/bupt/ta/web`、`src/main/webapp` |
| Build/test | Maven + JUnit 5 | `pom.xml`、`src/test/java` |
| Persistence | Jackson JSON files | `db/store`、`db/repository`、`db/facade` |
| UI | JSP portal pages, shared components, Tailwind CDN | `src/main/webapp/portal`、`WEB-INF/jsp/components` |
| Optional AI | Qwen/DashScope | `QwenAiService` |

## Module Responsibilities

| Module | Responsibility | Should not do |
|---|---|---|
| `web/filter` | Cross-cutting request processing: encoding, authentication context, CSRF | Encode business state transitions |
| `web/servlet` | Parse requests, call services, choose redirect/forward | Directly manipulate JSON files or duplicate service rules |
| `service` | Business workflows and cross-entity coordination | Render JSP or depend on request attributes |
| `domain/entity` | Persistent domain state | Know about HTTP, sessions, JSP or filesystem layout |
| `domain/enums` | Stable business vocabulary and state values | Store display strings that belong to i18n/views |
| `db/facade` | Database assembly and typed repository access | Implement hiring decisions |
| `db/repository` | Table-specific persistence query and validation | Orchestrate multi-step workflows |
| `db/store` | Generic JSON table loading, copying, locking and writing | Know TA recruitment business meaning |
| `util` | Focused utility helpers for files, labels, password hashing | Become a miscellaneous business dumping ground |

## Coding Standards

| Rule | Required practice | Current evidence |
|---|---|---|
| Naming | Classes use Java conventions and business terms; routes use stable nouns | `ApplicationService`, `ResumeFileUpload`, `/admin/database` |
| Layering | Servlet -> Service -> `TaDatabase` -> Repository -> Store | Most workflows follow this path |
| Validation | Validate input at service/repository boundary and reject invalid state | `ConstraintViolationException`, service tests |
| Errors | User-correctable errors become flash/form messages; unexpected errors are logged | servlet try/catch patterns、`ServletContext.log` |
| Dates/money | Use Java time and `BigDecimal` where needed | domain entities and services |
| Passwords | Hash passwords, never store or log plain password | `PasswordUtil`, admin reset flow |
| Secrets | API keys read from env/system properties server-side only | `QwenAiService`, README |
| Data paths | Uploaded files and snapshots stay under configured data directory | `AppConfig`, `ResumeFilePaths`, `ApplicationSubmissionFiles` |
| Tests | New business rules require focused unit or integration tests | `src/test/java` |

## Configuration Management

| Configuration | Source | Default / behavior | Review note |
|---|---|---|---|
| Data directory | `TA105_DATA_DIR` env or system property | Falls back to local `data/` | Must not commit runtime data |
| Qwen API key | `QWEN_API_KEY` env/system property | AI unavailable if absent | Key must never appear in client HTML or Git |
| Qwen models | `QWEN_MODEL`, `QWEN_TEXT_MODEL` | Defaults in `QwenAiService` | Changes should be documented in README |
| Context path | Tomcat deployment | `/ta105` in README examples | Routes in docs omit or include context consistently |
| Demo seed data | Empty data directory | Seeder creates demo users and fixtures | Demo password is for coursework only |

## Security Coding Rules

| Rule | Implementation evidence | Future review question |
|---|---|---|
| Require authentication for protected routes | `AuthFilter` redirects unauthenticated users | Is the route intentionally public? |
| Enforce role and ownership in sensitive workflows | `DbDemoServlet.requireAdmin`, service owner checks | Could a user act on another user's resource? |
| Protect unsafe HTTP methods from CSRF | `CsrfFilter`, `CsrfTokens`, JSP token injection | Does every form/fetch include token? |
| Validate uploads beyond extension | `ResumeFileUpload` checks extension, MIME, signature and size | Can content spoofing bypass checks? |
| Keep uploaded/snapshot paths confined | `ResumeFilePaths`, `ApplicationSubmissionFiles` | Are path joins normalized and checked? |
| Avoid direct public password reset | Admin reset only | Is there any public route that changes credentials? |
| Audit high-impact actions | `AuditLog`, `AdminAuditServlet`, AI request audit | Are enough metadata captured without exposing private prompts? |
| Treat AI as advisory | AI servlets require consent and return suggestions | Could AI output be mistaken as final decision? |

## Persistence Quality

| Concern | Current implementation | Review expectation |
|---|---|---|
| Atomic writes | `AtomicJsonFileWriter` writes temp file then moves atomically where supported | Keep all JSON table writes through `JsonTableStore` |
| Concurrency | `JsonTableStore` uses `ReadWriteLock` per table | Adequate for single JVM coursework; document limitation for production |
| Defensive copies | Store returns copies using Jackson conversion | Prevent accidental mutation of in-memory snapshot |
| Table initialization | Missing JSON file is created with empty envelope | Empty demo data directory starts reliably |
| Corruption detection | Rows without IDs throw `DatabaseCorruptionException` | Do not silently ignore malformed data |
| Cross-table consistency | Services coordinate related writes | Add tests for any workflow touching multiple tables |

## Code Review Checklist

| Dimension | Review question | Pass criteria |
|---|---|---|
| Requirement fit | Does the change map to a stated requirement or user story? | Requirement ID or clear product rationale is referenced |
| Layering | Is business logic in Service rather than JSP/Servlet? | Servlet remains request orchestration only |
| Role and ownership | Are TA/MO/Admin permissions and ownership checked? | Unauthorized path returns redirect/forbidden/error |
| State transitions | Are application/job/workload statuses valid? | Illegal transitions are rejected and tested |
| Validation | Are null, blank, enum, UUID, date and numeric boundaries handled? | Invalid input cannot corrupt data |
| Security | Are CSRF, upload, path, password, secret and XSS risks considered? | Controls exist or risk is explicitly accepted |
| Persistence | Are JSON writes atomic and cross-table side effects coherent? | Repository/service tests cover success and failure cases |
| AI | Is consent required and is AI output advisory? | `AiRequestGuard` or equivalent guard is used |
| Exceptions | Are expected failures user-readable and unexpected failures logged? | No swallowed exception that hides data loss |
| Duplication | Is duplicated logic extracted only when it improves clarity? | Shared helpers exist for repeated file/security logic |
| Tests | Are meaningful tests added or updated? | Unit/integration/security regression tests cover the change |
| Documentation | Does user-facing or architectural behavior need docs? | README/docs updated for routes, config or workflow changes |

## Static Search Review Targets

These searches should be repeated before major submission or release.

```bash
rg -n "sendError|sendRedirect|currentUser|getRole|UserRole" src/main/java/com/bupt/ta/web src/main/java/com/bupt/ta/service
rg -n "Files\\.|Path|normalize|upload|download|submittedResume" src/main/java/com/bupt/ta
rg -n "QWEN_API_KEY|API_KEY|password|secret|token" src/main/java src/main/webapp README.md
rg -n "catch \\(.*\\) \\{\\s*\\}" src/main/java
rg -n "innerHTML|escapeXml|c:out|fn:escapeXml" src/main/webapp
```

## Implementation Risks And Conventions

| Risk | Convention |
|---|---|
| Large Servlet becomes hard to maintain | Move validation and state logic into Service; keep JSP model preparation focused |
| JSON store used like production database | Clearly document single-JVM assumption and avoid concurrent external writes |
| New route forgotten in navigation/security | Update route table, sidebar/header if appropriate, tests and docs |
| AI prompt grows without privacy review | Keep consent text, minimise data sent, avoid prompt/output audit storage |
| Test count grows but assertions are weak | Prefer behavior assertions over only "does not throw" tests |

## Definition Of Done For Code Changes

1. Requirement or defect is clear.
2. Implementation follows existing package and naming patterns.
3. Role/ownership/security implications are checked.
4. Tests cover main path and at least one meaningful edge or failure path.
5. `mvn test` passes.
6. Documentation is updated when routes, setup, behavior, risks or assumptions changed.
7. No runtime data, API keys, screenshots or process notes are staged.
