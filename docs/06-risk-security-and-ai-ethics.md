# 06 Risk, Security, and AI Ethics

## Course Basis

EBU6304 emphasises that software quality does not only come from completed functionality, but also from risk identification, risk control, quality management, secure development, and ethical judgement. For a TA recruitment system, security and ethics are especially important because the system handles student resumes, application status, workload, and AI-assisted evaluation.

| Slide | Artifact in this document |
|---|---|
| EBU6304_10 Project Management | Risk register, priority, ownership, and tracking |
| EBU6304_11 Ethics and AI in Software Engineering | AI assistance, privacy, fairness, transparency, and human accountability |
| EBU6304_12 Risk and Quality Management | Risk probability/impact, quality control, and mitigation plan |
| EBU6304_13 Secure Software Development | Threat modelling, authentication and authorisation, input validation, and secret management |
| EBU6304_16 AI for Software Development | Reliability boundaries when AI is used in development and product capabilities |

## Risk Register

| ID | Risk | Probability | Impact | Current mitigation | Owner | Priority |
|---|---|---|---|---|---|---|
| R-01 | Unauthorised users access TA, MO, or Admin data | Medium | High | `AuthFilter`, role and ownership checks in Servlet/Service, tests | Backend | High |
| R-02 | CSRF causes users to submit state changes without their knowledge | Medium | High | `CsrfFilter`, JSP token injection, CSRF tests | Backend | High |
| R-03 | Malicious file upload or path traversal | Medium | High | Extension, MIME, signature, and size validation, path restriction | Backend | High |
| R-04 | AI requests leak privacy or are misunderstood as automatic hiring decisions | Medium | High | Explicit consent, audit, minimal logging, human final decision | Product/Backend | High |
| R-05 | JSON files become corrupted or partially written | Low | High | Atomic writes, read/write locks, corruption exception, tests | Backend | High |
| R-06 | Multi-user concurrency or multiple Tomcat instances cause file data races | Medium | Medium | Documentation clearly states the single-JVM coursework deployment assumption | Maintainer | Medium |
| R-07 | Inconsistency between requirements and implementation makes review untraceable | Medium | Medium | Requirements matrix, document index, traceability document | Team | Medium |
| R-08 | Test coverage is biased toward happy paths | Medium | Medium | Boundary/negative/security test checklist and regression gate | QA | Medium |
| R-09 | API keys or runtime data are accidentally committed | Medium | High | `.gitignore` ignores large files, data, archives, slides, plus secret search | Maintainer | High |
| R-10 | Unescaped UI output causes XSS | Low | High | JSP/JSTL output escaping convention, static search, and manual review | Frontend/Backend | Medium |
| R-11 | Inaccurate workload calculation affects fair allocation | Medium | Medium | `AdminService` aggregation tests, manual review suggestions | Backend | Medium |
| R-12 | Remote AI unavailability affects the core workflow | Medium | Medium | Rule-based matching fallback; AI remains optional | Backend | Medium |

## Security Threat Model

```mermaid
flowchart TB
  Attacker["Attacker or wrong-role user"]
  Browser["Browser"]
  Filters["AuthFilter / CsrfFilter"]
  Servlets["Servlets"]
  Services["Services"]
  Store["JSON store and uploaded files"]
  AI["Optional AI provider"]

  Attacker -->|"unauthenticated request"| Filters
  Attacker -->|"CSRF POST"| Filters
  Attacker -->|"malicious upload"| Servlets
  Attacker -->|"role bypass attempt"| Services
  Browser --> Filters --> Servlets --> Services --> Store
  Services -. "consented server-side request" .-> AI

## Security Threat Model

```mermaid
flowchart TB
  Attacker["Attacker or wrong-role user"]
  Browser["Browser"]
  Filters["AuthFilter / CsrfFilter"]
  Servlets["Servlets"]
  Services["Services"]
  Store["JSON store and uploaded files"]
  AI["Optional AI provider"]

  Attacker -->|"unauthenticated request"| Filters
  Attacker -->|"CSRF POST"| Filters
  Attacker -->|"malicious upload"| Servlets
  Attacker -->|"role bypass attempt"| Services
  Browser --> Filters --> Servlets --> Services --> Store
  Services -. "consented server-side request" .-> AI
```

| Threat | Attack example | Existing control | Additional recommendation |
|---|---|---|---|
| Spoofing | Access portal with no session | `AuthFilter` login redirect | Use HTTPS and secure cookies in production |
| Elevation of privilege | TA posts to admin route | Role checks in admin servlets/services | Add browser E2E role tests |
| CSRF | Hidden form submits application decision | `CsrfFilter` for unsafe methods | Keep token on every new form/fetch |
| Tampering | Modify JSON file while app runs | Atomic writes and corruption checks | Do not edit data files manually during runtime |
| Information disclosure | Download another TA's resume snapshot | Ownership checks and controlled file helpers | Add specific negative download tests if route expands |
| Malicious upload | Executable disguised as PDF | MIME/signature/size validation | Virus scanning would be needed for production |
| Repudiation | Admin changes user without record | Audit logs for high-impact actions | Periodic audit export review |
| Secret leakage | Qwen key committed or rendered | Env/system property only; ignore `API-KEY` | Run secret scan before submission |
| Prompt injection / AI misuse | Resume asks AI to ignore instructions | AI output is advisory and constrained | Show user-facing warnings and keep final decision human |

## Authentication And Authorization Controls

| Control | Current evidence | Review expectation |
|---|---|---|
| Session-based authentication | `LoginServlet`, `AuthService`, `AuthFilter` | Protected route requires valid `currentUser` |
| Session hardening | Login creates fresh session | Session id rotation occurs after login |
| Role-based access | `UserRole` and route/service checks | TA, MO, Admin cannot cross sensitive boundaries |
| Ownership checks | MO can review own vacancies; TA can manage own applications/resumes | Tests cover wrong-owner cases |
| Admin-only database page | `DbDemoServlet` route `/admin/database` and `requireAdmin` | Entry is not public and should stay under `/admin` |

## Data Protection

| Data type | Sensitivity | Protection |
|---|---|---|
| Password hash | High | Hash through `PasswordUtil`; plain password only transient |
| Resume files | High | Stored under configured data dir; validated on upload; snapshot for application review |
| Application status | Medium/high | Role and ownership checks; audit where relevant |
| Workload records | Medium | Admin/MO context controls and workload aggregation |
| Audit logs | Medium | Admin search/export; avoid storing full AI prompts/outputs |
| AI inputs/outputs | High | Explicit consent, minimal audit metadata, optional provider |

## AI Ethics Policy

| Principle | Policy for QM HIRE | Current evidence |
|---|---|---|
| Transparency | Users must know when AI assistance is used | AI UI and guard require explicit consent |
| Human accountability | MO makes final offer/reject decisions | AI methods are recommendation/advice only |
| Privacy | Send only necessary data and do not persist complete prompts/model outputs in audit logs | `AiRequestGuard`, `AI_REQUEST` audit metadata |
| Fairness | AI score must not be the only basis for ranking or hiring | `MatchingService` combines rule-based logic and workload context |
| Contestability | Users and MO can inspect underlying application data and make manual decisions | Application detail, resume snapshot and status workflow |
| Reliability | AI provider failures must not block core recruitment | Rule-based fallback and optional AI configuration |
| Security | API key stays server-side | `QwenAiService` reads env/system properties |

## Secure Development Checklist

| Item | Required action |
|---|---|
| New public route | Confirm it is intentionally unauthenticated; otherwise protect with `AuthFilter` and role checks |
| New POST/PUT/DELETE action | Include CSRF token and negative test |
| New file operation | Normalize path, restrict to data dir, validate type/size, test traversal attempts |
| New admin action | Check Admin role and append audit log where state changes |
| New MO action | Check MO role and job ownership |
| New TA action | Check TA role and resource ownership |
| New AI action | Require consent, log `AI_REQUEST`, avoid storing full sensitive prompt/output |
| New config/secret | Read server-side, document env var, add ignore/secret scan consideration |

## Quality Management Plan

| Activity | Frequency | Evidence |
|---|---|---|
| Automated regression tests | Before commit and final push | `mvn test` output |
| Static search for high-risk patterns | Before final submission | Review searches in implementation doc |
| Documentation alignment | Before final submission | `docs/08-coursework-documentation-traceability.md` |
| Risk review | At milestone/final review | Risk register updates |
| Security review | Before enabling new endpoint or upload/AI feature | Checklist and tests |
| Manual demo rehearsal | Before presentation/video | User/deployment doc script |

## Known Limitations

| Limitation | Impact | Accepted because | Future improvement |
|---|---|---|---|
| JSON file persistence is single-JVM oriented | Not production-safe under multi-node deployment | Coursework demo scope and no external DB constraint | Replace repository implementation with RDBMS |
| No full browser E2E suite | Some JSP navigation regressions may be manual-only | Broad service/filter/servlet tests exist | Add Playwright/Selenium tests |
| No production email/password recovery | Users rely on admin password reset | Avoids insecure public reset flow in coursework scope | Add tokenized email reset with expiry |
| No enterprise identity provider | Demo accounts and local auth only | Course deployment simplicity | Integrate SSO/OAuth for production |
| AI fairness not externally audited | AI ranking may carry provider bias | AI is optional/advisory and final decisions are human | Add bias checks and explainability review |
