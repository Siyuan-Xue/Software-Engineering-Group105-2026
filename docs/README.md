# QM HIRE Software Engineering Final Documentation Index

This document directory is the final deliverable-oriented document set for the EBU6304 Software Engineering course. It replaces earlier process documents such as sprint records, interface communication drafts, and staged acceptance notes; those process materials have been moved to the locally ignored directory `.local-docs-archive/2026-05-19-process-docs/` and are no longer tracked as remote deliverables.

## Document Scope

QM HIRE is the TA recruitment system developed by Group 105. It covers TA registration, vacancy browsing, resume management, application submission, MO review, offer handling, workload monitoring, administrator maintenance, audit logs, and optional AI assistance. The final documentation focuses on the software engineering evidence required by the course: requirements, analysis, design, architecture, implementation, testing, risk, quality, security, ethics, deployment, and maintenance.

## Document List

| File | Main content | Related course topics | Review purpose |
|---|---|---|---|
| [01-requirements-and-user-stories.md](01-requirements-and-user-stories.md) | stakeholders, glossary, functional requirements, non-functional requirements, user stories, acceptance criteria, requirements traceability matrix | Requirements, User stories and prototyping, Agile | To judge whether system goals, scope, acceptance criteria, and implementation evidence are consistent |
| [02-analysis-models.md](02-analysis-models.md) | EBC analysis, use case diagram, domain model, ER/data model, core activity diagrams | Analysis, EBC, UML analysis models | To judge whether the problem-domain modelling fully covers the TA recruitment business |
| [03-architecture-and-design.md](03-architecture-and-design.md) | 4+1 view, component/package diagrams, deployment diagram, sequence diagrams, state diagram, SOLID, design patterns | Design, Software Architecture, Design Principles, Design Patterns | To judge whether the design is maintainable, evolvable, and has clear responsibilities |
| [04-implementation-and-code-quality.md](04-implementation-and-code-quality.md) | implementation conventions, module responsibilities, configuration, exceptions, secure coding, Repository/Facade, code review checklist | Implementation, Secure Development, SOLID | To guide code review and future maintenance |
| [05-testing-and-quality-assurance.md](05-testing-and-quality-assurance.md) | testing strategy, unit/integration/system/acceptance/security/performance/regression testing, report templates | Testing, Quality Management | To demonstrate test coverage and quality gates |
| [06-risk-security-and-ai-ethics.md](06-risk-security-and-ai-ethics.md) | risk register, threat model, security controls, AI consent/privacy/fairness/human decision boundaries | Risk and Quality Management, Secure Development, Ethics and AI | To demonstrate that risk, security, and AI ethics are explicitly managed |
| [07-user-deployment-and-maintenance.md](07-user-deployment-and-maintenance.md) | user manual, administrator entry points, runtime deployment, data directory, demo accounts, troubleshooting, maintenance notes | Project Management, Implementation, Final delivery | To support reviewers in running, demonstrating, and maintaining the system |
| [08-coursework-documentation-traceability.md](08-coursework-documentation-traceability.md) | coverage matrix mapping 17 slide sets to documents/diagrams, diagram inventory, deliverable completeness checks | Revision, all course topics | To demonstrate that the required documents and diagrams from the slides have been covered |

## Diagram Format

All diagrams are embedded in Markdown using Mermaid, including:

- use case diagram
- EBC / domain class diagram
- ER diagram
- activity diagrams
- sequence diagrams
- state diagram
- component / package diagram
- deployment diagram
- 4+1 view summary

## Boundary Between Source Code and Documentation

These documents describe the current state of the code and do not modify business logic. The main evidence in the current implementation is located in:

- `src/main/java/com/bupt/ta/web/servlet/`: Servlet routes and controller entry points
- `src/main/java/com/bupt/ta/service/`: business services for recruitment, resumes, authentication, matching, administration, AI, and related logic
- `src/main/java/com/bupt/ta/db/`: JSON database Facade, Repository, and Store
- `src/main/java/com/bupt/ta/domain/`: domain entities, enums, and value objects
- `src/main/webapp/`: JSP pages, shared components, and static resources
- `src/test/java/`: unit, integration, security-assistance, and Servlet tests

## Original Course Materials

The EBU6304 PDFs in `docs/slides/` are the local source materials for the course. They are used to organise the documentation basis, but they are not tracked as Git remote deliverable assets, in order to avoid committing large files and original classroom materials into the repository.