# 02 Analysis Model

## Course Basis
The goal of the analysis phase is not to describe how code is written, but to build a problem domain model: what the system boundary is, which use cases users use to achieve their goals, how key domain objects are related, and how business processes flow. The EBU6304 Analysis slides use the EBC method to classify objects into Boundary, Control, and Entity, and require converting requirements into discussable models via UML/diagrams.

| Slide | Document Deliverable |
|---|---|
| EBU6304_03 Requirements | Use cases derived from identified functional and non-functional requirements |
| EBU6304_04 User stories and prototyping | User stories mapped to use cases and activity flows |
| EBU6304_05 Analysis | EBC, domain model, activity diagram, class/ER analysis model |
| EBU6304_17 Revision | Traceability maintained between analysis model, design, implementation, and testing |

## System Context

```mermaid
flowchart LR
  TA["TA applicant"]
  MO["Module Organiser"]
  Admin["Administrator"]
  Browser["Browser / JSP UI"]
  System["QM HIRE web application"]
  Data[("JSON data files")]
  Files[("Resume uploads and application snapshots")]
  Qwen["Optional Qwen / DashScope API"]

  TA --> Browser
  MO --> Browser
  Admin --> Browser
  Browser --> System
  System --> Data
  System --> Files
  System -. "AI assistance when configured and consented" .-> Qwen
```

## Use Case Diagram

```mermaid
flowchart TB
  TA["TA applicant"]
  MO["Module Organiser"]
  Admin["Administrator"]

  subgraph TAUseCases["TA use cases"]
    UC1["Register / login"]
    UC2["Maintain profile and resumes"]
    UC3["Browse and favourite vacancies"]
    UC4["Submit application"]
    UC5["Track status and respond to offer"]
    UC6["Request optional AI help"]
  end

  subgraph MOUseCases["MO use cases"]
    UC7["Create and publish vacancy"]
    UC8["Maintain skill requirements"]
    UC9["Review applications"]
    UC10["Refresh match analysis"]
    UC11["Send offer or rejection"]
  end

  subgraph AdminUseCases["Admin use cases"]
    UC12["Manage users"]
    UC13["Manage skills"]
    UC14["View audit logs"]
    UC15["Monitor workloads"]
    UC16["Inspect JSON database demo"]
  end

  TA --> UC1
  TA --> UC2
  TA --> UC3
  TA --> UC4
  TA --> UC5
  TA --> UC6
  MO --> UC1
  MO --> UC7
  MO --> UC8
  MO --> UC9
  MO --> UC10
  MO --> UC11
  Admin --> UC1
  Admin --> UC12
  Admin --> UC13
  Admin --> UC14
  Admin --> UC15
  Admin --> UC16
```

## EBC Analysis

### Boundary Objects
Boundary objects handle interaction with users or external systems. The main boundaries in the current system are Servlets, JSPs, and the optional AI provider entry points.

| Boundary | Responsibility | Evidence |
|---|---|---|
| JSP pages | Render portal pages, forms, statuses, lists, and error messages | `src/main/webapp/portal/*.jsp`, `db-demo.jsp` |
| Auth / portal servlets | Receive HTTP requests, perform lightweight parsing and forwarding | `LoginServlet`, `RegisterServlet`, `DashboardServlet` |
| TA workflow servlets | Entry points for resume, vacancy, application, and messaging operations for TA | `ResumesServlet`, `VacanciesServlet`, `ApplicationSubmitServlet` |
| MO workflow servlets | Handle vacancy maintenance, application details, and matching analysis | `VacancyCreateServlet`, `VacancyEditServlet`, `ApplicationDetailServlet` |
| Admin servlets | Handle users, skills, auditing, database demo, and workloads | `AdminUsersServlet`, `AdminSkillsServlet`, `AdminAuditServlet`, `DbDemoServlet` |
| AI servlets | Receive AI requests after user consent | `AiMatchServlet`, `AiResumeRankServlet`, `AiTaCoverLetterServlet`, MO AI servlets |

### Control Objects
Control objects implement business rules, state transitions, matching calculations, and cross-entity coordination.

| Control | Responsibility | Evidence |
|---|---|---|
| `AuthService` | Authentication, password validation, user status checks | `src/main/java/com/bupt/ta/service/AuthService.java` |
| `TaAccountService` | TA self-registration rules | `TaAccountService.java` |
| `ResumeService` | Resume versioning, skills, and uploaded file association | `ResumeService.java` |
| `JobService` | Job lifecycle, MO ownership, and skill requirements | `JobService.java` |
| `ApplicationService` | Application submission, withdrawal, review, offer, accept/decline | `ApplicationService.java` |
| `MatchingService` | Rule-based matching, AI/fallback scoring, missing skills, and recommendation explanations | `MatchingService.java` |
| `AdminService` | User management, audit queries, workload aggregation, and rebalancing suggestions | `AdminService.java` |
| `DbDemoService` | Secure wrapper for repositories in the course database demo page | `DbDemoService.java` |
| `QwenAiService` | Qwen/DashScope request construction and response parsing | `QwenAiService.java` |

### Entity Objects
Entity objects represent persisted domain data, primarily located in `com.bupt.ta.domain.entity`.

| Entity | Meaning |
|---|---|
| `User` | TA, MO, Admin users |
| `Resume` | TA resume versions and uploaded file references |
| `Skill` | Skill catalog entry |
| `ResumeSkill` | Proficiency relationship between resume and skill |
| `Job` | TA vacancy |
| `JobRequirement` | Required skills for a job |
| `Application` | Job application and its status |
| `MatchScore` | Matching analysis between application/resume and job |
| `WorkloadRecord` | Workload generated by accepted applications |
| `Notification` | User notifications |
| `AuditLog` | Audit log entries |

## EBC Diagram

```mermaid
classDiagram
  class Boundary {
    <<boundary>>
    JSP pages
    Servlets
    AI endpoints
  }
  class Control {
    <<control>>
    AuthService
    ResumeService
    JobService
    ApplicationService
    MatchingService
    AdminService
  }
  class Entity {
    <<entity>>
    User
    Resume
    Job
    Application
    MatchScore
    AuditLog
  }
  class Persistence {
    <<control>>
    TaDatabase
    Repositories
    JsonTableStore
  }

  Boundary --> Control : delegates business actions
  Control --> Entity : validates and mutates
  Control --> Persistence : reads and writes
  Persistence --> Entity : stores snapshots
```

## Domain Model

```mermaid
classDiagram
  class User {
    UUID id
    String email
    UserRole role
    String fullName
    boolean active
  }
  class Resume {
    UUID id
    UUID userId
    String title
    DegreeLevel degreeLevel
    String uploadedFilePath
  }
  class Skill {
    UUID id
    String name
    SkillCategory category
  }
  class ResumeSkill {
    UUID resumeId
    UUID skillId
    ProficiencyLevel proficiency
    int yearsExperience
  }
  class Job {
    UUID id
    UUID ownerUserId
    String title
    JobStatus status
    int weeklyHours
    int slots
  }
  class JobRequirement {
    UUID jobId
    UUID skillId
    boolean required
    ProficiencyLevel minimumProficiency
  }
  class Application {
    UUID id
    UUID resumeId
    UUID jobId
    ApplicationStatus status
    String submittedResumePath
  }
  class MatchScore {
    UUID applicationId
    int ruleScore
    int aiScore
    int finalScore
    String recommendation
  }
  class WorkloadRecord {
    UUID userId
    UUID jobId
    UUID applicationId
    int weeklyHours
    WorkloadStatus status
  }
  class Notification {
    UUID userId
    NotificationType type
    boolean read
  }
  class AuditLog {
    UUID actorUserId
    AuditAction action
    EntityType entityType
    Instant operatedAt
  }

  User "1" --> "0..*" Resume : owns
  Resume "1" --> "0..*" ResumeSkill : has
  Skill "1" --> "0..*" ResumeSkill : classifies
  User "1" --> "0..*" Job : owns as MO
  Job "1" --> "0..*" JobRequirement : requires
  Skill "1" --> "0..*" JobRequirement : required by
  Resume "1" --> "0..*" Application : submitted with
  Job "1" --> "0..*" Application : receives
  Application "1" --> "0..1" MatchScore : analysed by
  Application "1" --> "0..1" WorkloadRecord : creates after acceptance
  User "1" --> "0..*" Notification : receives
  User "1" --> "0..*" AuditLog : acts
```

## ER / Data Model
JSON files are not relational databases, but data is still organized in a tabular structure. The ER diagram describes logical relationships; the actual files are mapped to `data/*.json` by `FileTaDatabase`.

```mermaid
erDiagram
  USER ||--o{ RESUME : owns
  USER ||--o{ JOB : owns
  USER ||--o{ NOTIFICATION : receives
  USER ||--o{ AUDIT_LOG : performs
  RESUME ||--o{ RESUME_SKILL : has
  SKILL ||--o{ RESUME_SKILL : appears_in
  JOB ||--o{ JOB_REQUIREMENT : has
  SKILL ||--o{ JOB_REQUIREMENT : required_as
  RESUME ||--o{ APPLICATION : submits
  JOB ||--o{ APPLICATION : receives
  APPLICATION ||--o| MATCH_SCORE : has
  APPLICATION ||--o| WORKLOAD_RECORD : creates
  JOB ||--o{ WORKLOAD_RECORD : contributes

  USER {
    uuid id PK
    string email
    string passwordHash
    string role
    boolean active
  }
  RESUME {
    uuid id PK
    uuid userId FK
    string title
    string uploadedFilePath
  }
  SKILL {
    uuid id PK
    string name
    string category
  }
  JOB {
    uuid id PK
    uuid ownerUserId FK
    string title
    string status
  }
  APPLICATION {
    uuid id PK
    uuid resumeId FK
    uuid jobId FK
    string status
    string submittedResumePath
  }
```

## Core Activity Diagrams

### TA Application Flow

```mermaid
flowchart TD
  A["TA logs in"] --> B["Browse open vacancies"]
  B --> C{"Has suitable resume?"}
  C -- "No" --> D["Create or upload resume"]
  C -- "Yes" --> E["Open vacancy detail"]
  D --> E
  E --> F["Select resume and write cover letter"]
  F --> G{"Validation passes?"}
  G -- "No" --> H["Show validation errors"]
  H --> F
  G -- "Yes" --> I["Save application and resume snapshot"]
  I --> J["Create notification / audit where applicable"]
  J --> K["TA tracks application status"]
```

### MO Review Flow

```mermaid
flowchart TD
  A["MO logs in"] --> B["Open own vacancy"]
  B --> C["View applications"]
  C --> D["Start review"]
  D --> E["Refresh match analysis"]
  E --> F["Inspect resume snapshot and scores"]
  F --> G{"Decision"}
  G -- "Offer" --> H["Send offer"]
  G -- "Reject" --> I["Reject with note"]
  H --> J["TA accepts or declines"]
  I --> K["Applicant notified"]
  J --> L{"Accepted?"}
  L -- "Yes" --> M["Create workload record"]
  L -- "No" --> N["Mark declined"]
```

### Admin User Management Flow

```mermaid
flowchart TD
  A["Admin logs in"] --> B["Open /admin/users"]
  B --> C{"Action"}
  C -- "Create" --> D["Validate email, role, password"]
  C -- "Edit" --> E["Validate profile and role rules"]
  C -- "Reset password" --> F["Set new password hash"]
  C -- "Deactivate" --> G["Check target is not protected admin"]
  D --> H["Persist user"]
  E --> H
  F --> H
  G --> H
  H --> I["Append audit log"]
  I --> J["Show result"]
```

## Analysis Risks

| Risk | Analysis observation | Mitigation in design/docs |
|---|---|---|
| User roles overlap in UI | TA, MO and Admin share login but have different permissions | Explicit RBAC, route table, role-specific use cases |
| Application state transitions can become inconsistent | Offer/accept/withdraw/reject paths affect Application, Notification, WorkloadRecord | State diagram and `ApplicationService` tests |
| JSON persistence may be mistaken for relational DB | Logical ER exists, but implementation is file-backed | Architecture doc states single-JVM constraint and atomic writes |
| AI might be treated as decision maker | AI produces recommendations and text only | Ethics doc requires consent, audit and human final decision |
```