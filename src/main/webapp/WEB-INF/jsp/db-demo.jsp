<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DB Demo - QM HIRE</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary: '#0f172a',
                        accent: '#2563eb',
                        'background-light': '#f8fafc'
                    },
                    fontFamily: {
                        sans: ['Inter', 'sans-serif']
                    }
                }
            }
        }
    </script>
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
    <style>
        .demo-split {
            display: grid;
            gap: 1.5rem;
        }

        @media (min-width: 1280px) {
            .demo-split {
                grid-template-columns: minmax(28rem, 61.8%) minmax(0, 38.2%);
            }
        }
    </style>
</head>
<body data-theme="${appearance}" class="h-screen overflow-hidden bg-background-light font-sans text-slate-900">
<div class="relative flex h-screen w-full flex-col overflow-hidden">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

    <div class="flex min-h-0 flex-1 overflow-hidden">
        <main class="min-h-0 w-full overflow-y-auto overscroll-contain bg-background-light p-6 lg:p-10">
            <div class="mx-auto flex w-full max-w-7xl flex-col gap-8 pb-8">
                <section id="overview-section" class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                    <div class="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
                        <div class="max-w-3xl">
                            <p class="text-xs font-black uppercase tracking-[0.24em] text-slate-400">Unified Database Demo</p>
                            <h1 class="mt-2 text-3xl font-black tracking-tight text-slate-900">/admin/database now covers all 11 JSON tables</h1>
                            <p class="mt-3 text-sm leading-6 text-slate-600">
                                This page exercises the full frontend -> servlet -> service -> <code>TaDatabase</code> -> JSON file flow.
                                It never reads or writes <code>data/*.json</code> directly. Every write goes through
                                <code>DatabaseProvider.get(servletContext)</code> and the current repository/service APIs.
                            </p>
                            <p class="mt-3 text-sm leading-6 text-slate-600">
                                Fresh data directories are auto-seeded with three default users and baseline skills, so this page is usable on the very first run in any new location.
                            </p>
                        </div>
                        <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600 shadow-inner lg:w-[25rem]">
                            <p class="text-xs font-black uppercase tracking-[0.2em] text-slate-400">Data Directory</p>
                            <p class="mt-2 break-all font-medium text-slate-800"><c:out value="${dataDirectory}"/></p>
                            <p class="mt-3 text-xs leading-5 text-slate-500">
                                Suggested flow: confirm the seeded users and skills, create a resume, attach resume skills, create a job, attach job requirements, submit an application, then run match analysis and move the application through offer acceptance.
                            </p>
                        </div>
                    </div>

                    <c:if test="${not empty successMessage}">
                        <div class="mt-6 flex items-start gap-3 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
                            <span class="material-symbols-outlined text-emerald-600">check_circle</span>
                            <span><c:out value="${successMessage}"/></span>
                        </div>
                    </c:if>
                    <c:if test="${not empty errorMessage}">
                        <div class="mt-6 flex items-start gap-3 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                            <span class="material-symbols-outlined text-rose-600">error</span>
                            <span><c:out value="${errorMessage}"/></span>
                        </div>
                    </c:if>

                    <div class="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                        <c:forEach items="${tableCounts}" var="tableCount">
                            <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4">
                                <p class="text-xs font-black uppercase tracking-[0.2em] text-slate-400"><c:out value="${tableCount.tableName}"/></p>
                                <p class="mt-2 text-2xl font-black text-slate-900"><c:out value="${tableCount.rowCount}"/></p>
                                <p class="mt-1 text-xs text-slate-500"><c:out value="${tableCount.fileName}"/></p>
                            </div>
                        </c:forEach>
                    </div>
                </section>

                <section id="users-section" class="demo-split">
                    <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                        <div class="flex items-start justify-between gap-3">
                            <div>
                                <h2 class="text-xl font-black text-slate-900">User Form</h2>
                                <p class="mt-1 text-sm text-slate-500">Create or update a user through <code>db.users().save(...)</code>. The first run already seeds TA, MO, and Admin defaults.</p>
                            </div>
                            <c:if test="${editEntity == 'user'}">
                                <a href="${pageContext.request.contextPath}/admin/database#users-section" class="text-sm font-bold text-primary hover:underline">Cancel</a>
                            </c:if>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/database#users-section" method="post" class="mt-6 space-y-4">
                            <input type="hidden" name="operation" value="user-save" />
                            <input type="hidden" name="section" value="users-section" />
                            <input type="hidden" name="id" value="${editingUser.id}" />

                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Email</label>
                                <input type="email" name="email" value="${editingUser.email}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Plain Password</label>
                                <input type="password" name="plainPassword" <c:if test="${editEntity != 'user'}">required="required"</c:if> placeholder="${editEntity == 'user' ? 'Leave blank to keep current password' : 'Required for new users'}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Full Name</label>
                                <input type="text" name="fullName" value="${editingUser.fullName}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Role</label>
                                <select name="role" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select role</option>
                                    <c:forEach items="${roles}" var="role">
                                        <option value="${role}" ${editEntity == 'user' && editingUser.role == role ? 'selected' : ''}><c:out value="${role}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="grid gap-4 md:grid-cols-2">
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Phone</label>
                                    <input type="text" name="phone" value="${editingUser.phone}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Student ID</label>
                                    <input type="text" name="studentId" value="${editingUser.studentId}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Department</label>
                                <input type="text" name="department" value="${editingUser.department}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Bio</label>
                                <textarea name="bio" rows="3" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"><c:out value="${editingUser.bio}"/></textarea>
                            </div>
                            <label class="flex items-center gap-3 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-medium text-slate-700">
                                <input type="checkbox" name="active" value="true" ${editEntity != 'user' || editingUser.active ? 'checked' : ''}/>
                                Mark user as active
                            </label>
                            <button type="submit" class="w-full rounded-xl bg-primary px-4 py-3 text-sm font-black text-white transition hover:bg-slate-800">
                                ${editEntity == 'user' ? 'Update User' : 'Create User'}
                            </button>
                        </form>
                    </div>

                    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
                        <div class="flex items-center justify-between border-b border-slate-200 px-6 py-4">
                            <h2 class="text-lg font-black text-slate-900">Users</h2>
                            <span class="text-sm text-slate-500"><c:out value="${users.size()}"/> records</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="min-w-full text-left text-sm">
                                <thead class="bg-slate-50 text-xs uppercase tracking-[0.2em] text-slate-400">
                                <tr>
                                    <th class="px-6 py-3">Identity</th>
                                    <th class="px-6 py-3">Role</th>
                                    <th class="px-6 py-3">Profile</th>
                                    <th class="px-6 py-3 text-right">Action</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:forEach items="${users}" var="user">
                                    <tr>
                                        <td class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${user.fullName}"/></p>
                                            <p class="text-xs text-slate-500"><c:out value="${user.email}"/></p>
                                            <p class="mt-1 text-[11px] text-slate-400"><c:out value="${user.id}"/></p>
                                        </td>
                                        <td class="px-6 py-4"><c:out value="${user.role}"/></td>
                                        <td class="px-6 py-4 text-xs text-slate-500">
                                            <p>Active: <c:out value="${user.active}"/></p>
                                            <p>Department: <c:out value="${empty user.department ? '—' : user.department}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-right">
                                            <a href="${pageContext.request.contextPath}/admin/database?editEntity=user&editId=${user.id}#users-section" class="inline-flex rounded-lg border border-slate-200 px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50">Edit</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <section id="skills-section" class="demo-split">
                    <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                        <div class="flex items-start justify-between gap-3">
                            <div>
                                <h2 class="text-xl font-black text-slate-900">Skill Form</h2>
                                <p class="mt-1 text-sm text-slate-500">Create or update canonical skills through <code>db.skills().save(...)</code>. Fresh databases already start with a baseline skill set.</p>
                            </div>
                            <c:if test="${editEntity == 'skill'}">
                                <a href="${pageContext.request.contextPath}/admin/database#skills-section" class="text-sm font-bold text-primary hover:underline">Cancel</a>
                            </c:if>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/database#skills-section" method="post" class="mt-6 space-y-4">
                            <input type="hidden" name="operation" value="skill-save" />
                            <input type="hidden" name="section" value="skills-section" />
                            <input type="hidden" name="id" value="${editingSkill.id}" />

                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Skill Name</label>
                                <input type="text" name="name" value="${editingSkill.name}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Category</label>
                                <select name="category" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select category</option>
                                    <c:forEach items="${skillCategories}" var="category">
                                        <option value="${category}" ${editEntity == 'skill' && editingSkill.category == category ? 'selected' : ''}><c:out value="${category}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Description</label>
                                <textarea name="description" rows="4" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"><c:out value="${editingSkill.description}"/></textarea>
                            </div>
                            <button type="submit" class="w-full rounded-xl bg-primary px-4 py-3 text-sm font-black text-white transition hover:bg-slate-800">
                                ${editEntity == 'skill' ? 'Update Skill' : 'Create Skill'}
                            </button>
                        </form>
                    </div>

                    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
                        <div class="flex items-center justify-between border-b border-slate-200 px-6 py-4">
                            <h2 class="text-lg font-black text-slate-900">Skills</h2>
                            <span class="text-sm text-slate-500"><c:out value="${skills.size()}"/> records</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="min-w-full text-left text-sm">
                                <thead class="bg-slate-50 text-xs uppercase tracking-[0.2em] text-slate-400">
                                <tr>
                                    <th class="px-6 py-3">Skill</th>
                                    <th class="px-6 py-3">Category</th>
                                    <th class="px-6 py-3">Description</th>
                                    <th class="px-6 py-3 text-right">Action</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:forEach items="${skills}" var="skill">
                                    <tr>
                                        <td class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${skill.name}"/></p>
                                            <p class="mt-1 text-[11px] text-slate-400"><c:out value="${skill.id}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-slate-600"><c:out value="${skill.category}"/></td>
                                        <td class="px-6 py-4 text-xs text-slate-500"><c:out value="${empty skill.description ? '—' : skill.description}"/></td>
                                        <td class="px-6 py-4 text-right">
                                            <a href="${pageContext.request.contextPath}/admin/database?editEntity=skill&editId=${skill.id}#skills-section" class="inline-flex rounded-lg border border-slate-200 px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50">Edit</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <section id="resumes-section" class="demo-split">
                    <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                        <div class="flex items-start justify-between gap-3">
                            <div>
                                <h2 class="text-xl font-black text-slate-900">Resume Form</h2>
                                <p class="mt-1 text-sm text-slate-500">Writes through <code>ResumeService</code> and stores availability as structured JSON.</p>
                            </div>
                            <c:if test="${editEntity == 'resume'}">
                                <a href="${pageContext.request.contextPath}/admin/database#resumes-section" class="text-sm font-bold text-primary hover:underline">Cancel</a>
                            </c:if>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/database#resumes-section" method="post" class="mt-6 space-y-4">
                            <input type="hidden" name="operation" value="resume-save" />
                            <input type="hidden" name="section" value="resumes-section" />
                            <input type="hidden" name="id" value="${editingResume.id}" />

                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Owner</label>
                                <select name="userId" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select a TA user</option>
                                    <c:forEach items="${users}" var="user">
                                        <option value="${user.id}" ${editEntity == 'resume' && editingResume.userId == user.id ? 'selected' : ''}>
                                            <c:out value="${user.fullName}"/> (<c:out value="${user.role}"/>)
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Title</label>
                                <input type="text" name="title" value="${editingResume.title}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                            </div>
                            <div class="grid gap-4 md:grid-cols-2">
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Department</label>
                                    <input type="text" name="department" value="${editingResume.department}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Degree</label>
                                    <select name="degreeLevel" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                        <option value="">Select degree</option>
                                        <c:forEach items="${degreeLevels}" var="degree">
                                            <option value="${degree}" ${editEntity == 'resume' && editingResume.degreeLevel == degree ? 'selected' : ''}><c:out value="${degree}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="grid gap-4 md:grid-cols-2">
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">GPA</label>
                                    <input type="number" step="0.01" name="gpa" value="${editingResume.gpa}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Max Weekly Hours</label>
                                    <input type="number" min="1" name="maxWeeklyHours" value="${empty editingResume.maxWeeklyHours ? 20 : editingResume.maxWeeklyHours}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Bio</label>
                                <textarea name="bio" rows="3" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"><c:out value="${editingResume.bio}"/></textarea>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Availability Slots JSON</label>
                                <textarea name="availabilitySlotsJson" rows="5" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 font-mono text-xs outline-none focus:border-primary/30"><c:out value="${editingResumeAvailabilityJson}"/></textarea>
                                <p class="mt-2 text-xs text-slate-400">Example: <code>[{"dayOfWeek":"MONDAY","startTime":"09:00","endTime":"11:00"}]</code></p>
                            </div>
                            <button type="submit" class="w-full rounded-xl bg-primary px-4 py-3 text-sm font-black text-white transition hover:bg-slate-800">
                                ${editEntity == 'resume' ? 'Update Resume' : 'Create Resume'}
                            </button>
                        </form>
                    </div>

                    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
                        <div class="flex items-center justify-between border-b border-slate-200 px-6 py-4">
                            <h2 class="text-lg font-black text-slate-900">Resumes</h2>
                            <span class="text-sm text-slate-500"><c:out value="${resumes.size()}"/> records</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="min-w-full text-left text-sm">
                                <thead class="bg-slate-50 text-xs uppercase tracking-[0.2em] text-slate-400">
                                <tr>
                                    <th class="px-6 py-3">Resume</th>
                                    <th class="px-6 py-3">Owner</th>
                                    <th class="px-6 py-3">Availability</th>
                                    <th class="px-6 py-3 text-right">Action</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:forEach items="${resumes}" var="resume">
                                    <tr>
                                        <td class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${resume.title}"/></p>
                                            <p class="text-xs text-slate-500"><c:out value="${resume.degreeLevel}"/> · <c:out value="${empty resume.department ? '—' : resume.department}"/></p>
                                            <p class="mt-1 text-[11px] text-slate-400"><c:out value="${resume.id}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-slate-600"><c:out value="${userLabelsById[resume.userId]}"/></td>
                                        <td class="px-6 py-4 text-xs text-slate-500"><c:out value="${resume.availabilitySlots.size()}"/> slot(s)</td>
                                        <td class="px-6 py-4 text-right">
                                            <a href="${pageContext.request.contextPath}/admin/database?editEntity=resume&editId=${resume.id}#resumes-section" class="inline-flex rounded-lg border border-slate-200 px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50">Edit</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <section id="resume-skills-section" class="demo-split">
                    <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                        <div class="flex items-start justify-between gap-3">
                            <div>
                                <h2 class="text-xl font-black text-slate-900">Resume Skill Form</h2>
                                <p class="mt-1 text-sm text-slate-500">Bind a skill to a resume through <code>db.resumeSkills().save(...)</code>. This is the left half of the match model.</p>
                            </div>
                            <c:if test="${editEntity == 'resumeSkill'}">
                                <a href="${pageContext.request.contextPath}/admin/database#resume-skills-section" class="text-sm font-bold text-primary hover:underline">Cancel</a>
                            </c:if>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/database#resume-skills-section" method="post" class="mt-6 space-y-4">
                            <input type="hidden" name="operation" value="resume-skill-save" />
                            <input type="hidden" name="section" value="resume-skills-section" />
                            <input type="hidden" name="id" value="${editingResumeSkill.id}" />

                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Resume</label>
                                <select name="resumeId" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select resume</option>
                                    <c:forEach items="${resumes}" var="resume">
                                        <option value="${resume.id}" ${editEntity == 'resumeSkill' && editingResumeSkill.resumeId == resume.id ? 'selected' : ''}>
                                            <c:out value="${resume.title}"/> · <c:out value="${userLabelsById[resume.userId]}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Skill</label>
                                <select name="skillId" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select skill</option>
                                    <c:forEach items="${skills}" var="skill">
                                        <option value="${skill.id}" ${editEntity == 'resumeSkill' && editingResumeSkill.skillId == skill.id ? 'selected' : ''}>
                                            <c:out value="${skill.name}"/> · <c:out value="${skill.category}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="grid gap-4 md:grid-cols-2">
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Proficiency</label>
                                    <select name="proficiency" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                        <option value="">Select proficiency</option>
                                        <c:forEach items="${proficiencyLevels}" var="level">
                                            <option value="${level}" ${editEntity == 'resumeSkill' && editingResumeSkill.proficiency == level ? 'selected' : ''}><c:out value="${level}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Years of Experience</label>
                                    <input type="number" min="0" name="yearsExp" value="${empty editingResumeSkill.yearsExp ? 0 : editingResumeSkill.yearsExp}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                            </div>
                            <button type="submit" class="w-full rounded-xl bg-primary px-4 py-3 text-sm font-black text-white transition hover:bg-slate-800">
                                ${editEntity == 'resumeSkill' ? 'Update Resume Skill' : 'Create Resume Skill'}
                            </button>
                        </form>
                    </div>

                    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
                        <div class="flex items-center justify-between border-b border-slate-200 px-6 py-4">
                            <h2 class="text-lg font-black text-slate-900">Resume Skills</h2>
                            <span class="text-sm text-slate-500"><c:out value="${resumeSkills.size()}"/> records</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="min-w-full text-left text-sm">
                                <thead class="bg-slate-50 text-xs uppercase tracking-[0.2em] text-slate-400">
                                <tr>
                                    <th class="px-6 py-3">Resume</th>
                                    <th class="px-6 py-3">Skill</th>
                                    <th class="px-6 py-3">Level</th>
                                    <th class="px-6 py-3 text-right">Action</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:forEach items="${resumeSkills}" var="resumeSkill">
                                    <tr>
                                        <td class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${resumeLabelsById[resumeSkill.resumeId]}"/></p>
                                            <p class="mt-1 text-[11px] text-slate-400"><c:out value="${resumeSkill.id}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-slate-600"><c:out value="${skillLabelsById[resumeSkill.skillId]}"/></td>
                                        <td class="px-6 py-4 text-xs text-slate-500">
                                            <p><c:out value="${resumeSkill.proficiency}"/></p>
                                            <p class="mt-1"><c:out value="${resumeSkill.yearsExp}"/> year(s)</p>
                                        </td>
                                        <td class="px-6 py-4 text-right">
                                            <a href="${pageContext.request.contextPath}/admin/database?editEntity=resumeSkill&editId=${resumeSkill.id}#resume-skills-section" class="inline-flex rounded-lg border border-slate-200 px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50">Edit</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <section id="jobs-section" class="demo-split">
                    <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                        <div class="flex items-start justify-between gap-3">
                            <div>
                                <h2 class="text-xl font-black text-slate-900">Job Form</h2>
                                <p class="mt-1 text-sm text-slate-500">Writes through <code>JobService</code> and keeps the current status model.</p>
                            </div>
                            <c:if test="${editEntity == 'job'}">
                                <a href="${pageContext.request.contextPath}/admin/database#jobs-section" class="text-sm font-bold text-primary hover:underline">Cancel</a>
                            </c:if>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/database#jobs-section" method="post" class="mt-6 space-y-4">
                            <input type="hidden" name="operation" value="job-save" />
                            <input type="hidden" name="section" value="jobs-section" />
                            <input type="hidden" name="id" value="${editingJob.id}" />

                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Poster</label>
                                <select name="postedBy" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select MO/Admin</option>
                                    <c:forEach items="${recruiterUsers}" var="recruiter">
                                        <option value="${recruiter.id}" ${editEntity == 'job' && editingJob.postedBy == recruiter.id ? 'selected' : ''}>
                                            <c:out value="${recruiter.fullName}"/> (<c:out value="${recruiter.role}"/>)
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Title</label>
                                <input type="text" name="title" value="${editingJob.title}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                            </div>
                            <div class="grid gap-4 md:grid-cols-2">
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Type</label>
                                    <select name="type" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                        <option value="">Select type</option>
                                        <c:forEach items="${jobTypes}" var="jobType">
                                            <option value="${jobType}" ${editEntity == 'job' && editingJob.type == jobType ? 'selected' : ''}><c:out value="${jobType}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Status</label>
                                    <select name="status" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                        <c:forEach items="${jobStatuses}" var="jobStatus">
                                            <option value="${jobStatus}" ${(editEntity == 'job' && editingJob.status == jobStatus) || (editEntity != 'job' && jobStatus == 'OPEN') ? 'selected' : ''}><c:out value="${jobStatus}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="grid gap-4 md:grid-cols-2">
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Module Code</label>
                                    <input type="text" name="moduleCode" value="${editingJob.moduleCode}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Hourly Rate</label>
                                    <input type="number" step="0.01" name="hourlyRate" value="${empty editingJob.hourlyRate ? '20.00' : editingJob.hourlyRate}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                            </div>
                            <div class="grid gap-4 md:grid-cols-3">
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Required Hours</label>
                                    <input type="number" min="1" name="requiredHours" value="${empty editingJob.requiredHours ? 10 : editingJob.requiredHours}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Slots</label>
                                    <input type="number" min="1" name="slots" value="${empty editingJob.slots ? 1 : editingJob.slots}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Deadline</label>
                                    <input type="datetime-local" name="deadline" value="${empty editingJobDeadlineValue ? defaultDeadlineValue : editingJobDeadlineValue}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                            </div>
                            <div class="grid gap-4 md:grid-cols-2">
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Start Date</label>
                                    <input type="date" name="startDate" value="${editingJobStartDateValue}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">End Date</label>
                                    <input type="date" name="endDate" value="${editingJobEndDateValue}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"/>
                                </div>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Description</label>
                                <textarea name="description" rows="4" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"><c:out value="${editingJob.description}"/></textarea>
                            </div>
                            <button type="submit" class="w-full rounded-xl bg-primary px-4 py-3 text-sm font-black text-white transition hover:bg-slate-800">
                                ${editEntity == 'job' ? 'Update Job' : 'Create Job'}
                            </button>
                        </form>
                    </div>

                    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
                        <div class="flex items-center justify-between border-b border-slate-200 px-6 py-4">
                            <h2 class="text-lg font-black text-slate-900">Jobs</h2>
                            <span class="text-sm text-slate-500"><c:out value="${jobs.size()}"/> records</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="min-w-full text-left text-sm">
                                <thead class="bg-slate-50 text-xs uppercase tracking-[0.2em] text-slate-400">
                                <tr>
                                    <th class="px-6 py-3">Job</th>
                                    <th class="px-6 py-3">Poster</th>
                                    <th class="px-6 py-3">Status</th>
                                    <th class="px-6 py-3 text-right">Actions</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:forEach items="${jobs}" var="job">
                                    <tr>
                                        <td class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${job.title}"/></p>
                                            <p class="text-xs text-slate-500"><c:out value="${empty job.moduleCode ? 'No module code' : job.moduleCode}"/> · £<c:out value="${empty job.hourlyRate ? '20.00' : job.hourlyRate}"/>/hr</p>
                                            <p class="mt-1 text-[11px] text-slate-400"><c:out value="${job.id}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-slate-600"><c:out value="${userLabelsById[job.postedBy]}"/></td>
                                        <td class="px-6 py-4">
                                            <span class="inline-flex rounded-full px-2 py-1 text-xs font-bold ${job.status == 'OPEN' ? 'bg-emerald-100 text-emerald-700' : job.status == 'CANCELLED' ? 'bg-rose-100 text-rose-700' : 'bg-slate-100 text-slate-700'}">
                                                <c:out value="${job.status}"/>
                                            </span>
                                        </td>
                                        <td class="px-6 py-4">
                                            <div class="flex justify-end gap-2">
                                                <a href="${pageContext.request.contextPath}/admin/database?editEntity=job&editId=${job.id}#jobs-section" class="inline-flex rounded-lg border border-slate-200 px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50">Edit</a>
                                                <c:if test="${job.status != 'CANCELLED'}">
                                                    <form action="${pageContext.request.contextPath}/admin/database#jobs-section" method="post">
                                                        <input type="hidden" name="operation" value="job-cancel"/>
                                                        <input type="hidden" name="section" value="jobs-section"/>
                                                        <input type="hidden" name="jobId" value="${job.id}"/>
                                                        <button type="submit" class="rounded-lg bg-rose-50 px-3 py-2 text-xs font-bold text-rose-700 hover:bg-rose-100">Cancel</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <section id="job-requirements-section" class="demo-split">
                    <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                        <div class="flex items-start justify-between gap-3">
                            <div>
                                <h2 class="text-xl font-black text-slate-900">Job Requirement Form</h2>
                                <p class="mt-1 text-sm text-slate-500">Attach required or optional skills to a job through <code>db.jobRequirements().save(...)</code>. This is the right half of the match model.</p>
                            </div>
                            <c:if test="${editEntity == 'jobRequirement'}">
                                <a href="${pageContext.request.contextPath}/admin/database#job-requirements-section" class="text-sm font-bold text-primary hover:underline">Cancel</a>
                            </c:if>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/database#job-requirements-section" method="post" class="mt-6 space-y-4">
                            <input type="hidden" name="operation" value="job-requirement-save" />
                            <input type="hidden" name="section" value="job-requirements-section" />
                            <input type="hidden" name="id" value="${editingJobRequirement.id}" />

                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Job</label>
                                <select name="jobId" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select job</option>
                                    <c:forEach items="${jobs}" var="job">
                                        <option value="${job.id}" ${editEntity == 'jobRequirement' && editingJobRequirement.jobId == job.id ? 'selected' : ''}>
                                            <c:out value="${job.title}"/> · <c:out value="${job.status}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Skill</label>
                                <select name="skillId" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select skill</option>
                                    <c:forEach items="${skills}" var="skill">
                                        <option value="${skill.id}" ${editEntity == 'jobRequirement' && editingJobRequirement.skillId == skill.id ? 'selected' : ''}>
                                            <c:out value="${skill.name}"/> · <c:out value="${skill.category}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="grid gap-4 md:grid-cols-2">
                                <label class="flex items-center gap-3 rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-medium text-slate-700">
                                    <input type="checkbox" name="required" value="true" ${editEntity == 'jobRequirement' ? (editingJobRequirement.required ? 'checked' : '') : 'checked'}/>
                                    Mark as required
                                </label>
                                <div>
                                    <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Minimum Proficiency</label>
                                    <select name="minProficiency" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                        <option value="">Select level</option>
                                        <c:forEach items="${proficiencyLevels}" var="level">
                                            <option value="${level}" ${editEntity == 'jobRequirement' && editingJobRequirement.minProficiency == level ? 'selected' : ''}><c:out value="${level}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <button type="submit" class="w-full rounded-xl bg-primary px-4 py-3 text-sm font-black text-white transition hover:bg-slate-800">
                                ${editEntity == 'jobRequirement' ? 'Update Job Requirement' : 'Create Job Requirement'}
                            </button>
                        </form>
                    </div>

                    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
                        <div class="flex items-center justify-between border-b border-slate-200 px-6 py-4">
                            <h2 class="text-lg font-black text-slate-900">Job Requirements</h2>
                            <span class="text-sm text-slate-500"><c:out value="${jobRequirements.size()}"/> records</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="min-w-full text-left text-sm">
                                <thead class="bg-slate-50 text-xs uppercase tracking-[0.2em] text-slate-400">
                                <tr>
                                    <th class="px-6 py-3">Job</th>
                                    <th class="px-6 py-3">Skill</th>
                                    <th class="px-6 py-3">Rule</th>
                                    <th class="px-6 py-3 text-right">Action</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:forEach items="${jobRequirements}" var="requirement">
                                    <tr>
                                        <td class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${jobLabelsById[requirement.jobId]}"/></p>
                                            <p class="mt-1 text-[11px] text-slate-400"><c:out value="${requirement.id}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-slate-600"><c:out value="${skillLabelsById[requirement.skillId]}"/></td>
                                        <td class="px-6 py-4 text-xs text-slate-500">
                                            <p><c:out value="${requirement.required ? 'Required' : 'Optional'}"/></p>
                                            <p class="mt-1"><c:out value="${requirement.minProficiency}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-right">
                                            <a href="${pageContext.request.contextPath}/admin/database?editEntity=jobRequirement&editId=${requirement.id}#job-requirements-section" class="inline-flex rounded-lg border border-slate-200 px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50">Edit</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <section id="applications-section" class="demo-split">
                    <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                        <div>
                            <h2 class="text-xl font-black text-slate-900">Application Flow</h2>
                            <p class="mt-1 text-sm text-slate-500">Submit an application, then use the action buttons to drive status changes and watch linked tables update.</p>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/database#applications-section" method="post" class="mt-6 space-y-4">
                            <input type="hidden" name="operation" value="application-submit" />
                            <input type="hidden" name="section" value="applications-section" />

                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Resume</label>
                                <select name="resumeId" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select resume</option>
                                    <c:forEach items="${resumes}" var="resume">
                                        <option value="${resume.id}"><c:out value="${resume.title}"/> · <c:out value="${userLabelsById[resume.userId]}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Job</label>
                                <select name="jobId" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select open job</option>
                                    <c:forEach items="${jobs}" var="job">
                                        <option value="${job.id}"><c:out value="${job.title}"/> · <c:out value="${job.status}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Cover Letter</label>
                                <textarea name="coverLetter" rows="4" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"></textarea>
                            </div>
                            <button type="submit" class="w-full rounded-xl bg-primary px-4 py-3 text-sm font-black text-white transition hover:bg-slate-800">
                                Submit Application
                            </button>
                        </form>
                    </div>

                    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
                        <div class="flex items-center justify-between border-b border-slate-200 px-6 py-4">
                            <h2 class="text-lg font-black text-slate-900">Applications</h2>
                            <span class="text-sm text-slate-500"><c:out value="${applications.size()}"/> records</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="min-w-full text-left text-sm">
                                <thead class="bg-slate-50 text-xs uppercase tracking-[0.2em] text-slate-400">
                                <tr>
                                    <th class="px-6 py-3">Application</th>
                                    <th class="px-6 py-3">Resume / Job</th>
                                    <th class="px-6 py-3">Status</th>
                                    <th class="px-6 py-3 text-right">Actions</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:forEach items="${applications}" var="application">
                                    <tr>
                                        <td class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${application.status}"/></p>
                                            <p class="text-xs text-slate-500"><c:out value="${application.coverLetter}"/></p>
                                            <p class="mt-1 text-[11px] text-slate-400"><c:out value="${application.id}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-slate-600">
                                            <p>Resume: <c:out value="${resumeLabelsById[application.resumeId]}"/></p>
                                            <p class="mt-1">Job: <c:out value="${jobLabelsById[application.jobId]}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-slate-500">
                                            <p>Reviewed By: <c:out value="${empty application.reviewedBy ? '—' : application.reviewedBy}"/></p>
                                            <p class="mt-1">Responded At: <c:out value="${empty application.taRespondedAt ? '—' : application.taRespondedAt}"/></p>
                                        </td>
                                        <td class="px-6 py-4">
                                            <div class="flex flex-wrap justify-end gap-2">
                                                <c:if test="${application.status == 'PENDING'}">
                                                    <form action="${pageContext.request.contextPath}/admin/database#applications-section" method="post">
                                                        <input type="hidden" name="operation" value="application-review"/>
                                                        <input type="hidden" name="section" value="applications-section"/>
                                                        <input type="hidden" name="applicationId" value="${application.id}"/>
                                                        <button type="submit" class="rounded-lg border border-slate-200 px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50">Start Review</button>
                                                    </form>
                                                    <form action="${pageContext.request.contextPath}/admin/database#applications-section" method="post">
                                                        <input type="hidden" name="operation" value="application-withdraw"/>
                                                        <input type="hidden" name="section" value="applications-section"/>
                                                        <input type="hidden" name="applicationId" value="${application.id}"/>
                                                        <button type="submit" class="rounded-lg bg-slate-100 px-3 py-2 text-xs font-bold text-slate-700 hover:bg-slate-200">Withdraw</button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${application.status == 'REVIEWING'}">
                                                    <form action="${pageContext.request.contextPath}/admin/database#applications-section" method="post">
                                                        <input type="hidden" name="operation" value="application-offer"/>
                                                        <input type="hidden" name="section" value="applications-section"/>
                                                        <input type="hidden" name="applicationId" value="${application.id}"/>
                                                        <button type="submit" class="rounded-lg bg-emerald-50 px-3 py-2 text-xs font-bold text-emerald-700 hover:bg-emerald-100">Send Offer</button>
                                                    </form>
                                                    <form action="${pageContext.request.contextPath}/admin/database#applications-section" method="post" class="flex items-center gap-2">
                                                        <input type="hidden" name="operation" value="application-reject"/>
                                                        <input type="hidden" name="section" value="applications-section"/>
                                                        <input type="hidden" name="applicationId" value="${application.id}"/>
                                                        <input type="text" name="rejectionNote" placeholder="Optional rejection note" class="w-40 rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs outline-none focus:border-primary/30"/>
                                                        <button type="submit" class="rounded-lg bg-rose-50 px-3 py-2 text-xs font-bold text-rose-700 hover:bg-rose-100">Reject</button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${application.status == 'OFFER_PENDING'}">
                                                    <form action="${pageContext.request.contextPath}/admin/database#applications-section" method="post">
                                                        <input type="hidden" name="operation" value="application-accept"/>
                                                        <input type="hidden" name="section" value="applications-section"/>
                                                        <input type="hidden" name="applicationId" value="${application.id}"/>
                                                        <button type="submit" class="rounded-lg bg-emerald-50 px-3 py-2 text-xs font-bold text-emerald-700 hover:bg-emerald-100">Accept</button>
                                                    </form>
                                                    <form action="${pageContext.request.contextPath}/admin/database#applications-section" method="post">
                                                        <input type="hidden" name="operation" value="application-decline"/>
                                                        <input type="hidden" name="section" value="applications-section"/>
                                                        <input type="hidden" name="applicationId" value="${application.id}"/>
                                                        <button type="submit" class="rounded-lg bg-amber-50 px-3 py-2 text-xs font-bold text-amber-700 hover:bg-amber-100">Decline</button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <section id="match-scores-section" class="demo-split">
                    <div class="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                        <div>
                            <h2 class="text-xl font-black text-slate-900">Match Analysis</h2>
                            <p class="mt-1 text-sm text-slate-500">Runs <code>MatchingService.runAnalysis(...)</code> and writes to <code>match_scores</code>, <code>notifications</code>, and <code>audit_logs</code>.</p>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/database#match-scores-section" method="post" class="mt-6 space-y-4">
                            <input type="hidden" name="operation" value="match-score-refresh" />
                            <input type="hidden" name="section" value="match-scores-section" />

                            <div>
                                <label class="mb-2 block text-xs font-black uppercase tracking-[0.2em] text-slate-400">Application</label>
                                <select name="applicationId" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select application</option>
                                    <c:forEach items="${applications}" var="application">
                                        <option value="${application.id}">
                                            <c:out value="${resumeLabelsById[application.resumeId]}"/> -> <c:out value="${jobLabelsById[application.jobId]}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4 text-sm text-slate-600">
                                <p class="font-semibold text-slate-900">What this does</p>
                                <p class="mt-2 leading-6">The service compares <code>resume_skills</code> against <code>job_requirements</code>, computes coverage and score fields, and persists the refreshed analysis as a reusable row in <code>match_scores.json</code>.</p>
                            </div>
                            <button type="submit" class="w-full rounded-xl bg-primary px-4 py-3 text-sm font-black text-white transition hover:bg-slate-800">
                                Run / Refresh Match Analysis
                            </button>
                        </form>
                    </div>

                    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
                        <div class="flex items-center justify-between border-b border-slate-200 px-6 py-4">
                            <h2 class="text-lg font-black text-slate-900">Match Scores</h2>
                            <span class="text-sm text-slate-500"><c:out value="${matchScores.size()}"/> records</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="min-w-full text-left text-sm">
                                <thead class="bg-slate-50 text-xs uppercase tracking-[0.2em] text-slate-400">
                                <tr>
                                    <th class="px-6 py-3">Application</th>
                                    <th class="px-6 py-3">Scores</th>
                                    <th class="px-6 py-3">Coverage</th>
                                    <th class="px-6 py-3">AI</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:forEach items="${matchScores}" var="score">
                                    <tr>
                                        <td class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${applicationLabelsById[score.applicationId]}"/></p>
                                            <p class="mt-1 text-[11px] text-slate-400"><c:out value="${score.id}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-slate-600">
                                            <p>Rule: <c:out value="${score.ruleScore}"/></p>
                                            <p class="mt-1">AI: <c:out value="${score.aiScore}"/></p>
                                            <p class="mt-1 font-semibold text-slate-900">Final: <c:out value="${score.finalScore}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-slate-500">
                                            <p>Coverage: <c:out value="${score.skillCoveragePct}"/>%</p>
                                            <p class="mt-1">Missing required: <c:out value="${score.missingRequiredCount}"/></p>
                                            <p class="mt-1">Remaining hours: <c:out value="${score.workloadRemainingHours}"/></p>
                                        </td>
                                        <td class="px-6 py-4 text-xs text-slate-500">
                                            <p>Recommend: <c:out value="${score.aiRecommend}"/></p>
                                            <p class="mt-1"><c:out value="${empty score.aiExplanation ? '—' : score.aiExplanation}"/></p>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <section class="grid gap-6 xl:grid-cols-3">
                    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
                        <div class="border-b border-slate-200 px-6 py-4">
                            <h2 class="text-lg font-black text-slate-900">Recent Notifications</h2>
                            <p class="mt-1 text-sm text-slate-500">Written automatically by service-layer flows.</p>
                        </div>
                        <div class="divide-y divide-slate-100">
                            <c:choose>
                                <c:when test="${empty recentNotifications}">
                                    <div class="px-6 py-6 text-sm text-slate-500">No notifications yet.</div>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${recentNotifications}" var="notification">
                                        <div class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${notification.title}"/></p>
                                            <p class="mt-1 text-xs text-slate-600"><c:out value="${notification.message}"/></p>
                                            <p class="mt-2 text-[11px] text-slate-400"><c:out value="${userLabelsById[notification.userId]}"/> · <c:out value="${notification.entityType}"/> · <c:out value="${notification.createdAt}"/></p>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
                        <div class="border-b border-slate-200 px-6 py-4">
                            <h2 class="text-lg font-black text-slate-900">Recent Audit Logs</h2>
                            <p class="mt-1 text-sm text-slate-500">Shows tracked writes triggered by business actions.</p>
                        </div>
                        <div class="divide-y divide-slate-100">
                            <c:choose>
                                <c:when test="${empty recentAuditLogs}">
                                    <div class="px-6 py-6 text-sm text-slate-500">No audit logs yet.</div>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${recentAuditLogs}" var="auditLog">
                                        <div class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${auditLog.action}"/> · <c:out value="${auditLog.entityType}"/></p>
                                            <p class="mt-1 text-xs text-slate-600">Entity: <c:out value="${auditLog.entityId}"/></p>
                                            <p class="mt-2 text-[11px] text-slate-400"><c:out value="${auditLog.operatedAt}"/></p>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
                        <div class="border-b border-slate-200 px-6 py-4">
                            <h2 class="text-lg font-black text-slate-900">Recent Workload Records</h2>
                            <p class="mt-1 text-sm text-slate-500">Created after offer acceptance.</p>
                        </div>
                        <div class="divide-y divide-slate-100">
                            <c:choose>
                                <c:when test="${empty recentWorkloadRecords}">
                                    <div class="px-6 py-6 text-sm text-slate-500">No workload records yet.</div>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${recentWorkloadRecords}" var="workloadRecord">
                                        <div class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${userLabelsById[workloadRecord.taId]}"/></p>
                                            <p class="mt-1 text-xs text-slate-600"><c:out value="${jobLabelsById[workloadRecord.jobId]}"/></p>
                                            <p class="mt-1 text-xs text-slate-600"><c:out value="${workloadRecord.semester}"/> · <c:out value="${workloadRecord.status}"/></p>
                                            <p class="mt-2 text-[11px] text-slate-400">Hours: <c:out value="${workloadRecord.assignedHours}"/> · Application: <c:out value="${workloadRecord.applicationId}"/></p>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </section>
            </div>
        </main>
    </div>
</div>
</body>
</html>
