# 07 用户、部署与维护说明

## 课程依据

最终软件交付需要让评审者和维护者能够运行、验证和理解系统。EBU6304 的项目管理、实现、测试和复习内容都要求交付物不仅包含代码，还要包含用户手册、运行说明、配置说明、维护风险和验收路径。

| Slide | 本文档产物 |
|---|---|
| EBU6304_07 Implementation | 构建、配置、集成、部署说明 |
| EBU6304_08 Testing | 手动验收流程和运行验证 |
| EBU6304_10 Project Management | 交付、维护、风险沟通 |
| EBU6304_12 Risk and Quality Management | 故障排查和维护控制 |
| EBU6304_17 Revision | 最终提交材料一致性 |

## Quick Start

| Item | Value |
|---|---|
| Runtime | Java 17, Maven, Apache Tomcat 11 |
| Build artifact | `target/ta105.war` |
| Context path | `/ta105` |
| Default local URL | `http://localhost:8080/ta105/` |
| Data directory | `TA105_DATA_DIR` if set, otherwise local `data/` |
| Persistence | JSON files and uploaded resume files under data directory |
| Optional AI | Qwen/DashScope if `QWEN_API_KEY` is configured |

```bash
mvn test
mvn clean package
```

Deploy `target/ta105.war` to Tomcat 11 `webapps/`, then open `http://localhost:8080/ta105/`.

## Demo Accounts

When the configured data directory is empty, the seeder creates demo accounts. The shared coursework demo password is `password`.

| Role | Email | Main use |
|---|---|---|
| TA | `test@example.com` | Applicant workflows |
| MO | `mo@example.com` | Vacancy and application review |
| Admin | `admin@example.com` | User, skill, audit, workload and database maintenance |

## TA User Manual

| Task | Steps | Expected result |
|---|---|---|
| Register | Open `/register`, enter TA details, submit | A TA account is created and can log in |
| Login | Open `/login`, use TA credentials | Redirect to `/dashboard` |
| Manage resumes | Open `/resumes`, create/edit/upload/copy/delete allowed resumes | Resume list updates and uploaded file is saved under data dir |
| Browse vacancies | Open `/vacancies`, filter or favourite jobs | Open jobs are visible and favourites persist |
| Apply | Open a vacancy detail, choose resume, write cover letter, submit | Application appears in `/applications` |
| Track status | Open `/applications` | Status changes from pending/reviewing/offered/etc. are shown |
| Respond to offer | When offered, accept or decline from applications view | Application status updates and notifications are created |
| Optional AI help | Use AI controls only after accepting consent | AI returns advisory text or fallback message |

## MO User Manual

| Task | Steps | Expected result |
|---|---|---|
| Create vacancy | Open `/vacancy/create`, fill module, dates, hours, slots and requirements | Job is saved as draft/open based on action |
| Edit vacancy | Open own vacancy and use edit action | Only owning MO can update it |
| Review applications | Open applications for own vacancy | Applicant details and submitted resume snapshot are visible |
| Refresh match analysis | Start review, then refresh analysis | Rule/AI or fallback score and skill gaps are shown |
| Send decision | Choose offer or reject with note | Application status updates and applicant is notified |
| Use MO AI | Request ranking/advice with consent | AI output is advice only; MO still makes final decision |

## Admin User Manual

| Task | Route | Expected result |
|---|---|---|
| Manage users | `/admin/users` | Create TA/MO/Admin, edit profiles, reset passwords, deactivate/reactivate users |
| Manage skills | `/admin/skills` | Maintain the shared skill catalogue |
| View audit logs | `/admin/audit` | Search and export audit data |
| Monitor workloads | `/workloads` | Review accepted TA workload, capacity and rebalance suggestions |
| Inspect JSON database demo | `/admin/database` | Admin-only page for inspecting seeded JSON workflows |

### How To Enter `/admin/database`

1. Log in as an Admin user, for example `admin@example.com`.
2. Open `http://localhost:8080/ta105/admin/database` directly, or use the admin maintenance entry if shown in the portal.
3. Non-admin users must not be able to enter this page. The servlet route is intentionally under `/admin/database`, not a public demo route.

## Configuration Reference

| Variable / property | Required | Purpose |
|---|---|---|
| `TA105_DATA_DIR` | No | Absolute or relative path for JSON data and uploads |
| `QWEN_API_KEY` | No | Enables optional Qwen/DashScope AI features |
| `QWEN_MODEL` | No | Vision/multimodal resume model override |
| `QWEN_TEXT_MODEL` | No | Text model override for ranking, matching and drafting |

Never commit API keys, runtime `data/`, uploaded files, screenshots or local archives.

## Data Directory Layout

| Path | Meaning |
|---|---|
| `data/users.json` | User accounts and profile settings |
| `data/resumes.json` | Resume metadata |
| `data/jobs.json` | Vacancy records |
| `data/applications.json` | Applications and statuses |
| `data/match_scores.json` | Persisted match analysis |
| `data/workload_records.json` | Accepted TA workload records |
| `data/audit_logs.json` | Audit records |
| `data/resumes/uploads/` | Uploaded resume files |
| `data/applications/submissions/` | Application-time resume snapshots |

## Deployment Checklist

| Step | Command/action | Check |
|---|---|---|
| Clean test | `mvn test` | All tests pass |
| Package | `mvn clean package` | `target/ta105.war` exists |
| Configure data | Set `TA105_DATA_DIR` if needed | App can write to directory |
| Deploy | Copy WAR to Tomcat `webapps/` | Tomcat expands `/ta105` |
| Open app | Visit `/ta105/login` | Login page renders |
| Seed check | Use demo accounts on empty data dir | TA/MO/Admin can log in |
| Security smoke | Try admin route as TA | Access is denied |
| AI optional check | Leave `QWEN_API_KEY` unset | Core workflows still run |

## Maintenance Tasks

| Task | Frequency | Notes |
|---|---|---|
| Run tests | Before every push and final submission | `mvn test` |
| Back up data directory | Before manual data edits or demo reset | Copy entire `TA105_DATA_DIR` |
| Rotate demo password | Before any public deployment | Demo password is not production-safe |
| Review audit logs | After admin and hiring workflow demos | Search `/admin/audit` |
| Check ignored files | Before commit | `git status --short` and `git ls-files docs` |
| Refresh documentation | When routes, roles, setup or AI behavior changes | Update docs and README together |

## Troubleshooting

| Symptom | Likely cause | Action |
|---|---|---|
| Login page not found | WAR deployed under different context path | Check Tomcat app name and URL |
| Old page still appears | Tomcat kept exploded old WAR | Stop Tomcat, remove exploded `webapps/ta105/`, redeploy |
| Demo accounts missing | Existing data directory is not empty | Use documented account in that data set or reset data directory |
| Upload fails | File type, signature or size not allowed | Use valid PDF/DOC/DOCX within configured limit |
| AI feature says unavailable | `QWEN_API_KEY` absent or provider error | Continue with rule fallback or configure key server-side |
| Admin database page forbidden | Logged in as TA/MO or no session | Log in as Admin and open `/admin/database` |
| JSON load error | Data file manually corrupted | Restore backup or reset demo data directory |

## Final Submission Notes

- Submit source, tests, README and final Markdown docs.
- Do not submit `.local-docs-archive/`, `docs/slides/`, runtime `data/`, `target/`, screenshots or API keys.
- Keep root `README.md` as the practical build/run entry point and `docs/` as the final coursework engineering evidence.
