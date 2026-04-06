<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
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
                        accent: '#3b82f6',
                        'background-light': '#f8fafc',
                    },
                    fontFamily: {
                        sans: ['Inter', 'sans-serif'],
                    }
                }
            }
        }
    </script>
</head>
<body class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
<div class="relative flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="max-w-7xl mx-auto w-full space-y-8">
                <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
                    <div>
                        <h2 class="text-slate-900 text-3xl font-black tracking-tight">Sprint 1 JSON Database Demo</h2>
                        <p class="text-slate-500 text-sm mt-2">This page exercises the file-based repositories through a standard Servlet + JSP flow.</p>
                    </div>
                    <div class="bg-white border border-slate-200 rounded-xl px-4 py-3 shadow-sm">
                        <p class="text-xs font-bold uppercase tracking-wider text-slate-400">Data Directory</p>
                        <p class="text-sm font-medium text-slate-700 break-all"><c:out value="${dataDirectory}"/></p>
                    </div>
                </div>

                <c:if test="${not empty errorMessage}">
                    <div class="p-4 bg-red-50 border border-red-200 text-red-600 rounded-xl flex items-start gap-3">
                        <span class="material-symbols-outlined text-red-500 shrink-0">error</span>
                        <p class="text-sm font-medium"><c:out value="${errorMessage}"/></p>
                    </div>
                </c:if>
                <c:if test="${not empty successMessage}">
                    <div class="p-4 bg-green-50 border border-green-200 text-green-600 rounded-xl flex items-start gap-3">
                        <span class="material-symbols-outlined text-green-500 shrink-0">check_circle</span>
                        <p class="text-sm font-medium"><c:out value="${successMessage}"/></p>
                    </div>
                </c:if>

                <div class="flex flex-wrap gap-3">
                    <a href="#users-section" class="px-4 py-2 rounded-lg bg-white border border-slate-200 text-sm font-bold text-slate-700 shadow-sm hover:bg-slate-50">Users</a>
                    <a href="#resumes-section" class="px-4 py-2 rounded-lg bg-white border border-slate-200 text-sm font-bold text-slate-700 shadow-sm hover:bg-slate-50">Resumes</a>
                    <a href="#jobs-section" class="px-4 py-2 rounded-lg bg-white border border-slate-200 text-sm font-bold text-slate-700 shadow-sm hover:bg-slate-50">Jobs</a>
                    <a href="#applications-section" class="px-4 py-2 rounded-lg bg-white border border-slate-200 text-sm font-bold text-slate-700 shadow-sm hover:bg-slate-50">Applications</a>
                </div>

                <section id="users-section" class="grid grid-cols-1 xl:grid-cols-[420px_minmax(0,1fr)] gap-6">
                    <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
                        <div class="flex items-center justify-between gap-3 mb-6">
                            <div>
                                <h3 class="text-xl font-black text-slate-900">User Form</h3>
                                <p class="text-sm text-slate-500">Create or update Sprint 1 users.</p>
                            </div>
                            <c:if test="${editEntity == 'user'}">
                                <a href="${pageContext.request.contextPath}/db-demo#users-section" class="text-sm font-bold text-primary hover:underline">Cancel edit</a>
                            </c:if>
                        </div>
                        <form action="${pageContext.request.contextPath}/db-demo#users-section" method="POST" class="space-y-4">
                            <input type="hidden" name="entity" value="user" />
                            <input type="hidden" name="operation" value="${editEntity == 'user' ? 'update' : 'create'}" />
                            <input type="hidden" name="id" value="${editingUser.id}" />

                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Email</label>
                                <input type="email" name="email" value="${editingUser.email}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Password Hash</label>
                                <input type="text" name="passwordHash" value="${editingUser.passwordHash}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Full Name</label>
                                <input type="text" name="fullName" value="${editingUser.fullName}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Phone</label>
                                <input type="text" name="phone" value="${editingUser.phone}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Role</label>
                                <select name="role" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select role</option>
                                    <c:forEach items="${roles}" var="role">
                                        <option value="${role}" <c:if test="${editEntity == 'user' && editingUser.role == role}">selected="selected"</c:if>><c:out value="${role}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <button type="submit" class="w-full bg-primary text-white font-bold py-3 rounded-xl shadow-sm hover:bg-primary/90 transition-colors">
                                <c:choose>
                                    <c:when test="${editEntity == 'user'}">Update User</c:when>
                                    <c:otherwise>Create User</c:otherwise>
                                </c:choose>
                            </button>
                        </form>
                    </div>

                    <div class="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
                        <div class="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
                            <h3 class="text-lg font-black text-slate-900">Users</h3>
                            <span class="text-sm text-slate-500"><c:out value="${users.size()}"/> record(s)</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="w-full text-left text-sm">
                                <thead class="bg-slate-50 text-slate-500 uppercase text-xs tracking-wider">
                                <tr>
                                    <th class="px-6 py-3">User</th>
                                    <th class="px-6 py-3">Role</th>
                                    <th class="px-6 py-3">Active</th>
                                    <th class="px-6 py-3">Updated</th>
                                    <th class="px-6 py-3 text-right">Actions</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:choose>
                                    <c:when test="${empty users}">
                                        <tr><td colspan="5" class="px-6 py-6 text-center text-slate-500">No users yet.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${users}" var="user">
                                            <tr>
                                                <td class="px-6 py-4">
                                                    <div class="font-semibold text-slate-900"><c:out value="${user.fullName}"/></div>
                                                    <div class="text-xs text-slate-500"><c:out value="${user.email}"/></div>
                                                    <div class="text-[11px] text-slate-400 mt-1"><c:out value="${user.id}"/></div>
                                                </td>
                                                <td class="px-6 py-4"><c:out value="${user.role}"/></td>
                                                <td class="px-6 py-4">
                                                    <span class="inline-flex px-2 py-1 rounded-full text-xs font-bold ${user.active ? 'bg-green-100 text-green-700' : 'bg-slate-200 text-slate-600'}">
                                                        <c:out value="${user.active ? 'ACTIVE' : 'INACTIVE'}"/>
                                                    </span>
                                                </td>
                                                <td class="px-6 py-4 text-xs text-slate-500"><c:out value="${user.updatedAt}"/></td>
                                                <td class="px-6 py-4">
                                                    <div class="flex justify-end gap-2">
                                                        <a href="${pageContext.request.contextPath}/db-demo?editEntity=user&editId=${user.id}#users-section" class="px-3 py-2 rounded-lg border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-50">Edit</a>
                                                        <c:if test="${user.active}">
                                                            <form action="${pageContext.request.contextPath}/db-demo#users-section" method="POST">
                                                                <input type="hidden" name="entity" value="user" />
                                                                <input type="hidden" name="operation" value="deactivate" />
                                                                <input type="hidden" name="id" value="${user.id}" />
                                                                <button type="submit" class="px-3 py-2 rounded-lg bg-red-50 text-red-600 text-xs font-bold hover:bg-red-100">Deactivate</button>
                                                            </form>
                                                        </c:if>
                                                        <c:if test="${not user.active}">
                                                            <form action="${pageContext.request.contextPath}/db-demo#users-section" method="POST">
                                                                <input type="hidden" name="entity" value="user" />
                                                                <input type="hidden" name="operation" value="activate" />
                                                                <input type="hidden" name="id" value="${user.id}" />
                                                                <button type="submit" class="px-3 py-2 rounded-lg bg-green-50 text-green-700 text-xs font-bold hover:bg-green-100">Activate</button>
                                                            </form>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <section id="resumes-section" class="grid grid-cols-1 xl:grid-cols-[420px_minmax(0,1fr)] gap-6">
                    <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
                        <div class="flex items-center justify-between gap-3 mb-6">
                            <div>
                                <h3 class="text-xl font-black text-slate-900">Resume Form</h3>
                                <p class="text-sm text-slate-500">Users can own multiple resumes.</p>
                            </div>
                            <c:if test="${editEntity == 'resume'}">
                                <a href="${pageContext.request.contextPath}/db-demo#resumes-section" class="text-sm font-bold text-primary hover:underline">Cancel edit</a>
                            </c:if>
                        </div>
                        <form action="${pageContext.request.contextPath}/db-demo#resumes-section" method="POST" class="space-y-4">
                            <input type="hidden" name="entity" value="resume" />
                            <input type="hidden" name="operation" value="${editEntity == 'resume' ? 'update' : 'create'}" />
                            <input type="hidden" name="id" value="${editingResume.id}" />

                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Owner</label>
                                <select name="userId" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select user</option>
                                    <c:forEach items="${users}" var="user">
                                        <option value="${user.id}" <c:if test="${editEntity == 'resume' && editingResume.userId == user.id}">selected="selected"</c:if>>
                                            <c:out value="${user.fullName}"/> (<c:out value="${user.role}"/>)
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Title</label>
                                <input type="text" name="title" value="${editingResume.title}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                            </div>
                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Department</label>
                                    <input type="text" name="department" value="${editingResume.department}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                                </div>
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Degree Level</label>
                                    <select name="degreeLevel" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                        <option value="">Select degree</option>
                                        <c:forEach items="${degreeLevels}" var="degreeLevel">
                                            <option value="${degreeLevel}" <c:if test="${editEntity == 'resume' && editingResume.degreeLevel == degreeLevel}">selected="selected"</c:if>><c:out value="${degreeLevel}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">GPA</label>
                                    <input type="number" step="0.01" min="0" max="4.00" name="gpa" value="${editingResume.gpa}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                                </div>
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Max Weekly Hours</label>
                                    <input type="number" min="1" name="maxWeeklyHours" value="${editEntity == 'resume' ? editingResume.maxWeeklyHours : 20}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                                </div>
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Bio</label>
                                <textarea name="bio" rows="3" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"><c:out value="${editingResume.bio}"/></textarea>
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Availability JSON</label>
                                <textarea name="availabilityJson" rows="3" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"><c:out value="${editingResume.availabilityJson}"/></textarea>
                            </div>
                            <button type="submit" class="w-full bg-primary text-white font-bold py-3 rounded-xl shadow-sm hover:bg-primary/90 transition-colors">
                                <c:choose>
                                    <c:when test="${editEntity == 'resume'}">Update Resume</c:when>
                                    <c:otherwise>Create Resume</c:otherwise>
                                </c:choose>
                            </button>
                        </form>
                    </div>

                    <div class="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
                        <div class="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
                            <h3 class="text-lg font-black text-slate-900">Resumes</h3>
                            <span class="text-sm text-slate-500"><c:out value="${resumes.size()}"/> record(s)</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="w-full text-left text-sm">
                                <thead class="bg-slate-50 text-slate-500 uppercase text-xs tracking-wider">
                                <tr>
                                    <th class="px-6 py-3">Resume</th>
                                    <th class="px-6 py-3">Owner</th>
                                    <th class="px-6 py-3">Degree</th>
                                    <th class="px-6 py-3">Updated</th>
                                    <th class="px-6 py-3 text-right">Actions</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:choose>
                                    <c:when test="${empty resumes}">
                                        <tr><td colspan="5" class="px-6 py-6 text-center text-slate-500">No resumes yet.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${resumes}" var="resume">
                                            <tr>
                                                <td class="px-6 py-4">
                                                    <div class="font-semibold text-slate-900"><c:out value="${resume.title}"/></div>
                                                    <div class="text-xs text-slate-500"><c:out value="${resume.department}"/></div>
                                                    <div class="text-[11px] text-slate-400 mt-1"><c:out value="${resume.id}"/></div>
                                                </td>
                                                <td class="px-6 py-4">
                                                    <div class="font-medium text-slate-900"><c:out value="${userLabelsById[resume.userId]}"/></div>
                                                    <div class="text-xs text-slate-400"><c:out value="${resume.userId}"/></div>
                                                </td>
                                                <td class="px-6 py-4"><c:out value="${resume.degreeLevel}"/></td>
                                                <td class="px-6 py-4 text-xs text-slate-500"><c:out value="${resume.updatedAt}"/></td>
                                                <td class="px-6 py-4">
                                                    <div class="flex justify-end gap-2">
                                                        <a href="${pageContext.request.contextPath}/db-demo?editEntity=resume&editId=${resume.id}#resumes-section" class="px-3 py-2 rounded-lg border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-50">Edit</a>
                                                        <form action="${pageContext.request.contextPath}/db-demo#resumes-section" method="POST">
                                                            <input type="hidden" name="entity" value="resume" />
                                                            <input type="hidden" name="operation" value="delete" />
                                                            <input type="hidden" name="id" value="${resume.id}" />
                                                            <button type="submit" class="px-3 py-2 rounded-lg bg-red-50 text-red-600 text-xs font-bold hover:bg-red-100">Delete</button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <section id="jobs-section" class="grid grid-cols-1 xl:grid-cols-[420px_minmax(0,1fr)] gap-6">
                    <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
                        <div class="flex items-center justify-between gap-3 mb-6">
                            <div>
                                <h3 class="text-xl font-black text-slate-900">Job Form</h3>
                                <p class="text-sm text-slate-500">Only MO and ADMIN users can post jobs.</p>
                            </div>
                            <c:if test="${editEntity == 'job'}">
                                <a href="${pageContext.request.contextPath}/db-demo#jobs-section" class="text-sm font-bold text-primary hover:underline">Cancel edit</a>
                            </c:if>
                        </div>
                        <form action="${pageContext.request.contextPath}/db-demo#jobs-section" method="POST" class="space-y-4">
                            <input type="hidden" name="entity" value="job" />
                            <input type="hidden" name="operation" value="${editEntity == 'job' ? 'update' : 'create'}" />
                            <input type="hidden" name="id" value="${editingJob.id}" />

                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Posted By</label>
                                <select name="postedBy" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select recruiter</option>
                                    <c:forEach items="${recruiterUsers}" var="user">
                                        <option value="${user.id}" <c:if test="${editEntity == 'job' && editingJob.postedBy == user.id}">selected="selected"</c:if>><c:out value="${user.fullName}"/> (<c:out value="${user.role}"/>)</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Title</label>
                                <input type="text" name="title" value="${editingJob.title}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                            </div>
                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Module Code</label>
                                    <input type="text" name="moduleCode" value="${editingJob.moduleCode}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                                </div>
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Type</label>
                                    <select name="type" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                        <option value="">Select type</option>
                                        <c:forEach items="${jobTypes}" var="jobType">
                                            <option value="${jobType}" <c:if test="${editEntity == 'job' && editingJob.type == jobType}">selected="selected"</c:if>><c:out value="${jobType}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Status</label>
                                    <select name="status" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                        <option value="">Select status</option>
                                        <c:forEach items="${jobStatuses}" var="jobStatus">
                                            <option value="${jobStatus}" <c:if test="${editEntity == 'job' && editingJob.status == jobStatus}">selected="selected"</c:if>><c:out value="${jobStatus}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Deadline</label>
                                    <input type="datetime-local" name="deadline" value="${editingJobDeadlineValue}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                                </div>
                            </div>
                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Required Hours</label>
                                    <input type="number" min="1" name="requiredHours" value="${editEntity == 'job' ? editingJob.requiredHours : 1}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                                </div>
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Slots</label>
                                    <input type="number" min="1" name="slots" value="${editEntity == 'job' ? editingJob.slots : 1}" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                                </div>
                            </div>
                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Start Date</label>
                                    <input type="date" name="startDate" value="${editingJob.startDate}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                                </div>
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">End Date</label>
                                    <input type="date" name="endDate" value="${editingJob.endDate}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                                </div>
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Description</label>
                                <textarea name="description" rows="4" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"><c:out value="${editingJob.description}"/></textarea>
                            </div>
                            <button type="submit" class="w-full bg-primary text-white font-bold py-3 rounded-xl shadow-sm hover:bg-primary/90 transition-colors">
                                <c:choose>
                                    <c:when test="${editEntity == 'job'}">Update Job</c:when>
                                    <c:otherwise>Create Job</c:otherwise>
                                </c:choose>
                            </button>
                        </form>
                    </div>

                    <div class="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
                        <div class="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
                            <h3 class="text-lg font-black text-slate-900">Jobs</h3>
                            <span class="text-sm text-slate-500"><c:out value="${jobs.size()}"/> record(s)</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="w-full text-left text-sm">
                                <thead class="bg-slate-50 text-slate-500 uppercase text-xs tracking-wider">
                                <tr>
                                    <th class="px-6 py-3">Job</th>
                                    <th class="px-6 py-3">Poster</th>
                                    <th class="px-6 py-3">Status</th>
                                    <th class="px-6 py-3">Deadline</th>
                                    <th class="px-6 py-3 text-right">Actions</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:choose>
                                    <c:when test="${empty jobs}">
                                        <tr><td colspan="5" class="px-6 py-6 text-center text-slate-500">No jobs yet.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${jobs}" var="job">
                                            <tr>
                                                <td class="px-6 py-4">
                                                    <div class="font-semibold text-slate-900"><c:out value="${job.title}"/></div>
                                                    <div class="text-xs text-slate-500"><c:out value="${job.type}"/> • <c:out value="${job.moduleCode}"/></div>
                                                    <div class="text-[11px] text-slate-400 mt-1"><c:out value="${job.id}"/></div>
                                                </td>
                                                <td class="px-6 py-4">
                                                    <div class="font-medium text-slate-900"><c:out value="${userLabelsById[job.postedBy]}"/></div>
                                                    <div class="text-xs text-slate-400"><c:out value="${job.postedBy}"/></div>
                                                </td>
                                                <td class="px-6 py-4"><c:out value="${job.status}"/></td>
                                                <td class="px-6 py-4 text-xs text-slate-500"><c:out value="${job.deadline}"/></td>
                                                <td class="px-6 py-4">
                                                    <div class="flex justify-end gap-2">
                                                        <a href="${pageContext.request.contextPath}/db-demo?editEntity=job&editId=${job.id}#jobs-section" class="px-3 py-2 rounded-lg border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-50">Edit</a>
                                                        <form action="${pageContext.request.contextPath}/db-demo#jobs-section" method="POST">
                                                            <input type="hidden" name="entity" value="job" />
                                                            <input type="hidden" name="operation" value="delete" />
                                                            <input type="hidden" name="id" value="${job.id}" />
                                                            <button type="submit" class="px-3 py-2 rounded-lg bg-red-50 text-red-600 text-xs font-bold hover:bg-red-100">Delete</button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>

                <section id="applications-section" class="grid grid-cols-1 xl:grid-cols-[420px_minmax(0,1fr)] gap-6">
                    <div class="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
                        <div class="flex items-center justify-between gap-3 mb-6">
                            <div>
                                <h3 class="text-xl font-black text-slate-900">Application Form</h3>
                                <p class="text-sm text-slate-500">Link resumes to jobs and exercise status changes.</p>
                            </div>
                            <c:if test="${editEntity == 'application'}">
                                <a href="${pageContext.request.contextPath}/db-demo#applications-section" class="text-sm font-bold text-primary hover:underline">Cancel edit</a>
                            </c:if>
                        </div>
                        <form action="${pageContext.request.contextPath}/db-demo#applications-section" method="POST" class="space-y-4">
                            <input type="hidden" name="entity" value="application" />
                            <input type="hidden" name="operation" value="${editEntity == 'application' ? 'update' : 'create'}" />
                            <input type="hidden" name="id" value="${editingApplication.id}" />

                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Resume</label>
                                <select name="resumeId" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select resume</option>
                                    <c:forEach items="${resumes}" var="resume">
                                        <option value="${resume.id}" <c:if test="${editEntity == 'application' && editingApplication.resumeId == resume.id}">selected="selected"</c:if>><c:out value="${resume.title}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Job</label>
                                <select name="jobId" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select job</option>
                                    <c:forEach items="${jobs}" var="job">
                                        <option value="${job.id}" <c:if test="${editEntity == 'application' && editingApplication.jobId == job.id}">selected="selected"</c:if>><c:out value="${job.title}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Status</label>
                                <select name="status" required class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Select status</option>
                                    <c:forEach items="${applicationStatuses}" var="applicationStatus">
                                        <option value="${applicationStatus}" <c:if test="${editEntity == 'application' && editingApplication.status == applicationStatus}">selected="selected"</c:if>><c:out value="${applicationStatus}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Reviewed By</label>
                                <select name="reviewedBy" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30">
                                    <option value="">Optional reviewer</option>
                                    <c:forEach items="${recruiterUsers}" var="user">
                                        <option value="${user.id}" <c:if test="${editEntity == 'application' && editingApplication.reviewedBy == user.id}">selected="selected"</c:if>><c:out value="${user.fullName}"/> (<c:out value="${user.role}"/>)</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Reviewed At</label>
                                    <input type="datetime-local" name="reviewedAt" value="${editingApplicationReviewedAtValue}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                                </div>
                                <div>
                                    <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">TA Responded At</label>
                                    <input type="datetime-local" name="taRespondedAt" value="${editingApplicationTaRespondedAtValue}" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30" />
                                </div>
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">Cover Letter</label>
                                <textarea name="coverLetter" rows="3" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"><c:out value="${editingApplication.coverLetter}"/></textarea>
                            </div>
                            <div>
                                <label class="block text-xs font-black uppercase tracking-wider text-slate-400 mb-2">MO Notes</label>
                                <textarea name="moNotes" rows="3" class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none focus:border-primary/30"><c:out value="${editingApplication.moNotes}"/></textarea>
                            </div>
                            <button type="submit" class="w-full bg-primary text-white font-bold py-3 rounded-xl shadow-sm hover:bg-primary/90 transition-colors">
                                <c:choose>
                                    <c:when test="${editEntity == 'application'}">Update Application</c:when>
                                    <c:otherwise>Create Application</c:otherwise>
                                </c:choose>
                            </button>
                        </form>
                    </div>

                    <div class="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
                        <div class="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
                            <h3 class="text-lg font-black text-slate-900">Applications</h3>
                            <span class="text-sm text-slate-500"><c:out value="${applications.size()}"/> record(s)</span>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="w-full text-left text-sm">
                                <thead class="bg-slate-50 text-slate-500 uppercase text-xs tracking-wider">
                                <tr>
                                    <th class="px-6 py-3">Application</th>
                                    <th class="px-6 py-3">Resume</th>
                                    <th class="px-6 py-3">Job</th>
                                    <th class="px-6 py-3">Status</th>
                                    <th class="px-6 py-3 text-right">Actions</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:choose>
                                    <c:when test="${empty applications}">
                                        <tr><td colspan="5" class="px-6 py-6 text-center text-slate-500">No applications yet.</td></tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${applications}" var="application">
                                            <tr>
                                                <td class="px-6 py-4">
                                                    <div class="font-semibold text-slate-900"><c:out value="${application.id}"/></div>
                                                    <div class="text-xs text-slate-500"><c:out value="${application.updatedAt}"/></div>
                                                </td>
                                                <td class="px-6 py-4">
                                                    <div class="font-medium text-slate-900"><c:out value="${resumeLabelsById[application.resumeId]}"/></div>
                                                    <div class="text-xs text-slate-400"><c:out value="${application.resumeId}"/></div>
                                                </td>
                                                <td class="px-6 py-4">
                                                    <div class="font-medium text-slate-900"><c:out value="${jobLabelsById[application.jobId]}"/></div>
                                                    <div class="text-xs text-slate-400"><c:out value="${application.jobId}"/></div>
                                                </td>
                                                <td class="px-6 py-4"><c:out value="${application.status}"/></td>
                                                <td class="px-6 py-4">
                                                    <div class="flex justify-end gap-2">
                                                        <a href="${pageContext.request.contextPath}/db-demo?editEntity=application&editId=${application.id}#applications-section" class="px-3 py-2 rounded-lg border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-50">Edit</a>
                                                        <form action="${pageContext.request.contextPath}/db-demo#applications-section" method="POST">
                                                            <input type="hidden" name="entity" value="application" />
                                                            <input type="hidden" name="operation" value="delete" />
                                                            <input type="hidden" name="id" value="${application.id}" />
                                                            <button type="submit" class="px-3 py-2 rounded-lg bg-red-50 text-red-600 text-xs font-bold hover:bg-red-100">Delete</button>
                                                        </form>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </section>
            </div>
        </main>
    </div>
</div>
</body>
</html>
