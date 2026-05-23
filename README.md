# QM HIRE · TA105

QM HIRE is a Java web application for the BUPT x QMUL Software Engineering Group 105 final coursework. It supports Teaching Assistant recruitment from vacancy discovery through application review, offer handling, workload monitoring, audit logging, and optional Qwen/DashScope AI assistance.

| Item | Current project state |
|---|---|
| Artifact | `target/ta105.war` |
| Context path | `/ta105` |
| Runtime | Java 17, Maven, Apache Tomcat 11 |
| Web stack | Jakarta Servlet 6, JSP, JSTL, Tailwind CDN |
| Persistence | Jackson JSON files under `data/`; no external database |
| Optional AI provider | Alibaba DashScope Qwen models, with rule-based fallback |
| Latest local verification | `mvn test` passed `133/133` tests |

---

## Coursework Submission Alignment

The handout requires the final software ZIP to include source code, test programs, code documentation such as JavaDocs, a user manual with key screenshots, and README setup/run instructions. This repository is organised to make those materials easy to inspect.

| Handout requirement | Project artifact |
|---|---|
| Source code | `src/main/java/`, `src/main/webapp/` |
| Test programs | `src/test/java/` |
| Code documentation | Generate with `mvn javadoc:javadoc`; output is `target/reports/apidocs/index.html` |
| User manual and deployment notes | `docs/07-user-deployment-and-maintenance.md` |
| Setup, configuration, and run instructions | This README plus `docs/README.md` |
| Final software ZIP | Submit as `Software_group105.zip` on QM+ |
| Final report | Submit separately as `Report_group105.pdf` |

Before final GitHub review, push all local `main` commits so the remote repository shows the latest software, tests, documentation, and README updates.

---

## Features

### Teaching Assistant

- Register and sign in as a TA applicant.
- Browse, filter, and favourite published vacancies.
- Create, upload, duplicate, edit, and delete resumes when allowed.
- Bind resume skills with proficiency and years of experience.
- Apply with an existing resume or a newly uploaded resume file; submitted files are snapshotted for MO review.
- Track applications, withdraw pending applications, and accept or decline offers.
- Use optional AI assistance for resume review, vacancy matching, resume ranking, and cover-letter drafting when `QWEN_API_KEY` is configured.
- See applicant-facing skill-gap feedback only; internal match scores and hiring recommendations are reserved for MO review.

### Module Organiser

- Create, edit, draft, publish, and cancel vacancies.
- Maintain vacancy metadata including labels, deadlines, job type, slots, start/end dates, and skill requirements.
- Review applications through the intended workflow: start review, refresh persisted match analysis, inspect applicant detail, download submitted resume files, send offers, or reject applications.
- View MO-only match analysis fields: rule score, AI/fallback score, final score, recommendation, explanation, skill coverage, missing required skills, and workload remaining hours.
- Refresh match analysis only after the application has entered review.
- Use optional AI assistance for applicant ranking and offer/reject advice; final decisions remain human-controlled.

### Administrator

- Manage TA, MO, and Admin users.
- Create accounts, edit profiles, reset passwords, and deactivate/reactivate non-admin users.
- Manage the skills catalogue with reference protection for skills already used by resumes or job requirements.
- Monitor accepted TA workloads, weekly hours, capacity, estimated income, and workload status.
- View rule-based workload rebalance suggestions that highlight high-utilisation TAs and possible lower-load alternatives.
- Search audit logs and export audit data as CSV.

### Platform, Security, And Reliability

- Role-based access control for TA, MO, and Admin workflows.
- Session hardening on login with session id rotation.
- Global CSRF protection for unsafe requests.
- File upload validation for allowed extensions, MIME type, file signature, and upload size.
- Public password reset no longer changes passwords directly; administrators reset passwords from Admin Users.
- AI requests require explicit user consent and create `AI_REQUEST` audit entries without storing prompts or full model outputs.
- Core recruitment flows continue without Qwen because persisted match analysis has a rule-based fallback.
- Runtime data is stored in JSON files through repository interfaces and the `TaDatabase` facade.

---

## Architecture Overview

QM HIRE is a single WAR application deployed to Tomcat. Browser requests go through filters and servlets, business rules live in services, and persistence is accessed through `TaDatabase` and JSON repositories. API keys are read server-side only and are never sent to the browser.

```mermaid
flowchart TB
  Browser[JSP pages in browser]
  Filters[AuthFilter + CsrfFilter + EncodingFilter]
  Servlets[Jakarta Servlets]
  Services[Service layer]
  DB[TaDatabase facade]
  Repos[JSON repositories]
  Files[(data/*.json + uploaded files)]
  Qwen[Optional Qwen/DashScope API]

  Browser --> Filters --> Servlets --> Services --> DB --> Repos --> Files
  Services -.->|server-side only| Qwen
```

Key design choices:

- JSON persistence satisfies the coursework no-external-database constraint and is suitable for single-JVM demonstration.
- Application submissions keep resume file snapshots so MO review is based on the file submitted at application time.
- AI is optional and guarded by consent, server-side validation, audit logging, and rule-based fallback.
- Tests cover services, repositories, utilities, filters, security helpers, and key servlets.

---

## Quick Start

### Prerequisites

- JDK 17 or later
- Maven 3.9 or later
- Apache Tomcat 11

### Build, Test, And Generate Documentation

```bash
git clone <repository-url>
cd <project-folder>

mvn test
mvn clean package
mvn javadoc:javadoc
```

Expected outputs:

- Tests: current local verification is `133/133` passing.
- WAR: `target/ta105.war`.
- JavaDocs: `target/reports/apidocs/index.html`.

### Deploy To Tomcat

1. Copy `target/ta105.war` to the Tomcat `webapps/` directory.
2. Start or restart Tomcat 11.
3. Open `http://localhost:8080/ta105/`.
4. If Tomcat does not pick up a redeployed WAR, stop Tomcat and remove the exploded `webapps/ta105/` directory before restarting.

### Run The Website Locally

Use this checklist when starting the website for local testing, screenshots, or the final video.

**Option A: command-line Tomcat**

```bash
# 1. Build the WAR from the project root.
mvn clean package

# 2. Copy the WAR to Tomcat. Replace CATALINA_HOME with your Tomcat directory.
cp target/ta105.war "$CATALINA_HOME/webapps/ta105.war"

# 3. Optional: use a dedicated local data directory for the demo.
export TA105_DATA_DIR="$PWD/data"

# 4. Start Tomcat.
"$CATALINA_HOME/bin/startup.sh"

# 5. Open the website.
open http://localhost:8080/ta105/
```

For Windows, use the same build command, copy `target\ta105.war` into `%CATALINA_HOME%\webapps\ta105.war`, then run `%CATALINA_HOME%\bin\startup.bat` and open `http://localhost:8080/ta105/`.

**Option B: IntelliJ IDEA with local Tomcat**

1. Open this repository in IntelliJ IDEA.
2. Confirm the Project SDK is Java 17.
3. Create a Tomcat Server run configuration.
4. Add the `ta105:war exploded` or `ta105.war` artifact.
5. Set the application context to `/ta105`.
6. Optionally add `TA105_DATA_DIR=<absolute-path-to-project>/data` as an environment variable.
7. Start the configuration and open `http://localhost:8080/ta105/`.

This approach is well suited for day-to-day debugging: configure local Tomcat, deploy the `ta105` WAR or exploded artifact, and confirm the context path is `/ta105`.

**Local run checks**

- Login page opens at `/ta105/login`.
- Demo accounts work after seeding: `test@example.com`, `mo@example.com`, `admin@example.com`; password is `password`.
- Uploaded resume files are written under `data/resumes/uploads/`.
- Application snapshot files are written under `data/applications/submissions/`.
- If a new WAR is not reflected, stop Tomcat and delete the exploded `webapps/ta105/` folder before restarting.
- After a successful local run, you should be able to sign in with the demo accounts and see JSON data and uploaded files written under `data/` when submitting resumes or applications.

### Demo Accounts

When the configured `data/` directory is empty, the seeder creates demo accounts. The demo password is `password`.

| Role | Email |
|---|---|
| TA | `test@example.com` |
| MO | `mo@example.com` |
| Admin | `admin@example.com` |

### Data Directory

By default, runtime data is stored under `./data`. To use another directory:

```bash
export TA105_DATA_DIR=/absolute/path/to/data
```

or pass the JVM property:

```bash
-Dta105.data.dir=/absolute/path/to/data
```

Stored runtime files include:

- JSON database files under `data/*.json`.
- Resume uploads under `data/resumes/uploads/`.
- Application submission snapshots under `data/applications/submissions/`.

Seeder data is created only when all business tables are empty. To reset a demo, stop Tomcat, back up `data/`, clear the business JSON files, and restart.

---

## Optional AI Configuration

AI features use Qwen/DashScope and are optional. Without `QWEN_API_KEY`, direct Qwen buttons are disabled and persisted match analysis still works through rule-based fallback.

| Variable | Required | Description |
|---|---|---|
| `QWEN_API_KEY` | Required for direct AI calls | DashScope API key |
| `QWEN_MODEL` | No | Vision/multimodal resume model |
| `QWEN_TEXT_MODEL` | No | Text model for ranking, matching, and drafting |

Windows Tomcat `bin/setenv.bat`:

```bat
set "QWEN_API_KEY=sk-your-key-here"
```

Linux or macOS:

```bash
export QWEN_API_KEY=sk-your-key-here
```

For consent rules, fallback behaviour, and troubleshooting, see `docs/06-risk-security-and-ai-ethics.md` and `docs/07-user-deployment-and-maintenance.md`.

---

## Documentation

| Document | Purpose |
|---|---|
| `docs/README.md` | Final coursework documentation index |
| `docs/01-requirements-and-user-stories.md` | Requirements, user stories, acceptance criteria, and traceability |
| `docs/02-analysis-models.md` | EBC analysis, use case, domain, ER, and activity diagrams |
| `docs/03-architecture-and-design.md` | 4+1 architecture, component/package/deployment diagrams, sequences, state, SOLID, patterns |
| `docs/04-implementation-and-code-quality.md` | Implementation conventions, security coding rules, persistence quality, code review checklist |
| `docs/05-testing-and-quality-assurance.md` | Test strategy, QA gates, black-box/white-box/security/regression testing |
| `docs/06-risk-security-and-ai-ethics.md` | Risk register, threat model, security controls, AI consent and ethics |
| `docs/07-user-deployment-and-maintenance.md` | User manual, admin entry points, deployment, data directory, troubleshooting |
| `docs/08-coursework-documentation-traceability.md` | EBU6304 slide-to-document and diagram traceability |

The documentation index tracks final deliverable Markdown documents only; course slides, screenshots, large files, and local process documentation archives are not tracked as remote deliverables.

---

## Important Routes

All paths are under the `/ta105` context.

| Area | Routes |
|---|---|
| Authentication | `/login`, `/logout`, `/register`, `/forgot-password` |
| TA portal | `/dashboard`, `/resumes`, `/vacancies`, `/vacancy`, `/applications`, `/messages`, `/settings`, `/favorites` |
| Application workflow | `/application`, `/application/detail`, `/application/decision`, `/match-analysis` |
| MO vacancy management | `/vacancy/create`, `/vacancy/edit` |
| Admin | `/admin/users`, `/admin/skills`, `/admin/audit`, `/admin/database`, `/workloads` |
| Optional AI | `/ai-match`, `/ai-resume-rank`, `/ai-mo-applicants-rank`, `/ai-mo-application-advice`, `/ai-ta-cover-letter` |

For example, the login page is available at `http://localhost:8080/ta105/login`.

---

## Project Structure

```text
.
├── README.md
├── pom.xml
├── Software_group105.zip              # final QM+ software package when prepared locally
├── docs/
│   ├── README.md
│   ├── user-manual.md
│   ├── screenshots/
│   ├── guides/
│   └── ta-recruitment-system/
├── src/
│   ├── main/java/com/bupt/ta/
│   │   ├── bootstrap/
│   │   ├── config/
│   │   ├── db/
│   │   ├── domain/
│   │   ├── dto/
│   │   ├── i18n/
│   │   ├── service/
│   │   ├── util/
│   │   └── web/
│   ├── main/webapp/
│   └── test/java/com/bupt/ta/
└── target/
    ├── ta105.war
    └── reports/apidocs/
```

`target/` and local ZIP artifacts are build/submission outputs. If they are absent after cloning, regenerate them with the commands in Quick Start.

---

## Development And Verification

Recommended final verification:

```bash
mvn test
mvn clean package
mvn javadoc:javadoc
```

Current local result:

- `mvn test`: `133` tests, `0` failures, `0` errors.
- `mvn clean package`: creates `target/ta105.war`.
- `mvn javadoc:javadoc`: creates `target/reports/apidocs/index.html`.

Before final submission, re-run the test, package, and JavaDocs commands above and confirm the GitHub remote includes the latest commits.

Development conventions:

- Use `DatabaseProvider.get(servletContext)` to obtain `TaDatabase` in servlets.
- Keep business rules in services rather than JSP pages.
- Use `RedirectUrls.withQueryParam` for redirects with messages.
- Enforce role and ownership checks on the server side.
- Protect unsafe requests with CSRF tokens.
- Require explicit AI consent before sending personal data to Qwen.
- Do not commit real credentials, real personal data, or production API keys.

---

## Team

BUPT x QMUL Software Engineering - Group 105

| Name | QM ID | BUPT ID | GitHub email |
|---|---|---|---|
| Wanran Sun | 231223254 | 2023213626 | 112358wan@gmail.com |
| Xiankun Jiang | 231223542 | 2023213655 | jp2023213655@qmul.ac.uk |
| Siyuan Xue | 231223564 | 2023213657 | jp2023213657@qmul.ac.uk |
| Yutong Wu | 231223575 | 2023213658 | serovia@126.com |
| Xiaoxiao Ma | 231223715 | 2023213672 | maxiaoxiao@bupt.edu.cn |
| Rui Ma | 231223151 | 2023213616 | 940874485@qq.com |

Teaching assistant for the course: Wang Ruijia, `wang_ruijia@bupt.edu.cn`.

---

## Academic Use

This repository is developed for coursework and demonstration. Do not use production credentials or real personal data in shared environments.
