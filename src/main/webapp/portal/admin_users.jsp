<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${language == 'zh' ? '用户管理 - QM HIRE' : 'User Management - QM HIRE'}</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
    <style>
        .admin-label { display:block; margin-bottom:.35rem; color:#64748b; font-size:11px; font-weight:800; letter-spacing:.14em; text-transform:uppercase; }
        .admin-input { width:100%; border-radius:.875rem; border:1px solid #e2e8f0; background:#fff; color:#0f172a; font-size:.875rem; outline:none; padding:.72rem .85rem; }
        .admin-input:focus { border-color:#94a3b8; box-shadow:0 0 0 3px rgba(148,163,184,.16); }
    </style>
</head>
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
<div class="relative flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />
    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />
        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="portal-page">
                <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                    <jsp:param name="containerClass" value="mb-6" />
                </jsp:include>

                <div class="portal-page-header mb-6">
                    <div>
                        <h2 class="portal-page-title">${language == 'zh' ? '用户管理' : 'User Management'}</h2>
                        <p class="portal-page-copy">${language == 'zh' ? '由管理员创建 TA、MO、Admin 账号，并停用或恢复非管理员账号。' : 'Create TA, MO, and Admin accounts, then deactivate or restore non-admin users.'}</p>
                    </div>
                    <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-bold text-slate-700 shadow-sm">
                        <span class="text-slate-400">${language == 'zh' ? '用户总数' : 'Total users'}</span>
                        <span class="ml-2 text-slate-950"><c:out value="${fn:length(users)}"/></span>
                    </div>
                </div>

                <div class="grid grid-cols-1 gap-6 xl:grid-cols-[0.7fr_1.3fr]">
                    <section class="portal-panel p-6">
                        <div class="mb-5 flex items-center gap-3">
                            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-100 text-blue-700">
                                <span class="material-symbols-outlined">person_add</span>
                            </div>
                            <div>
                                <h3 class="font-bold text-slate-900">${language == 'zh' ? '创建账号' : 'Create Account'}</h3>
                                <p class="text-xs text-slate-500">${language == 'zh' ? '邮箱不区分大小写且不能重复。' : 'Emails are case-insensitive and must be unique.'}</p>
                            </div>
                        </div>
                        <form action="${pageContext.request.contextPath}/admin/users" method="POST" class="space-y-4">
                            <input type="hidden" name="action" value="create" />
                            <div>
                                <label class="admin-label">Email *</label>
                                <input class="admin-input" type="email" name="email" required />
                            </div>
                            <div>
                                <label class="admin-label">${language == 'zh' ? '姓名' : 'Full Name'} *</label>
                                <input class="admin-input" type="text" name="fullName" maxlength="120" required />
                            </div>
                            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                                <div>
                                    <label class="admin-label">${language == 'zh' ? '角色' : 'Role'} *</label>
                                    <select class="admin-input" name="role" required>
                                        <c:forEach items="${userRoles}" var="role">
                                            <option value="${role}"><c:out value="${role}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div>
                                    <label class="admin-label">${language == 'zh' ? '初始密码' : 'Initial Password'} *</label>
                                    <input class="admin-input" type="password" name="password" minlength="6" required />
                                </div>
                            </div>
                            <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                                <div>
                                    <label class="admin-label">${language == 'zh' ? '院系' : 'Department'}</label>
                                    <input class="admin-input" type="text" name="department" maxlength="120" />
                                </div>
                                <div>
                                    <label class="admin-label">${language == 'zh' ? '学号/工号' : 'Student/Staff ID'}</label>
                                    <input class="admin-input" type="text" name="studentId" maxlength="80" />
                                </div>
                            </div>
                            <div>
                                <label class="admin-label">${language == 'zh' ? '电话' : 'Phone'}</label>
                                <input class="admin-input" type="text" name="phone" maxlength="60" />
                            </div>
                            <button type="submit" class="portal-btn portal-btn-primary w-full">
                                <span class="material-symbols-outlined text-[18px]">save</span>
                                ${language == 'zh' ? '创建账号' : 'Create Account'}
                            </button>
                        </form>
                    </section>

                    <section class="portal-panel overflow-hidden">
                        <div class="border-b border-slate-100 px-6 py-5">
                            <h3 class="font-bold text-slate-900">${language == 'zh' ? '账号列表' : 'Accounts'}</h3>
                            <p class="mt-1 text-xs text-slate-500">${language == 'zh' ? '编辑基本资料、重置密码，或停用非管理员账号。' : 'Edit profile fields, reset passwords, or deactivate non-admin accounts.'}</p>
                        </div>
                        <div class="overflow-x-auto">
                            <table class="w-full text-left text-sm">
                                <thead class="border-b border-slate-100 bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                                <tr>
                                    <th class="px-6 py-4 font-bold">${language == 'zh' ? '用户' : 'User'}</th>
                                    <th class="px-6 py-4 font-bold">${language == 'zh' ? '角色' : 'Role'}</th>
                                    <th class="px-6 py-4 font-bold">${language == 'zh' ? '状态' : 'Status'}</th>
                                    <th class="px-6 py-4 text-right font-bold">${language == 'zh' ? '操作' : 'Actions'}</th>
                                </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                <c:forEach items="${users}" var="user">
                                    <tr class="align-top transition-colors hover:bg-slate-50">
                                        <td class="px-6 py-4">
                                            <p class="font-semibold text-slate-900"><c:out value="${user.fullName}"/></p>
                                            <p class="mt-1 text-xs text-slate-500"><c:out value="${user.email}"/></p>
                                            <p class="mt-1 text-xs text-slate-400"><c:out value="${user.department}"/></p>
                                        </td>
                                        <td class="px-6 py-4">
                                            <span class="inline-flex rounded-full bg-slate-100 px-3 py-1 text-xs font-bold text-slate-700"><c:out value="${user.role}"/></span>
                                        </td>
                                        <td class="px-6 py-4">
                                            <span class="inline-flex rounded-full px-3 py-1 text-xs font-bold ${user.active ? 'bg-green-100 text-green-700' : 'bg-slate-200 text-slate-700'}">
                                                ${user.active ? (language == 'zh' ? '启用' : 'Active') : (language == 'zh' ? '停用' : 'Inactive')}
                                            </span>
                                        </td>
                                        <td class="px-6 py-4 text-right">
                                            <div class="flex flex-col items-end gap-2">
                                                <details class="w-full max-w-lg text-left">
                                                    <summary class="ml-auto inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-xl border border-slate-200 px-3 py-2 text-xs font-bold text-slate-700 transition-colors hover:bg-slate-50">
                                                        <span class="material-symbols-outlined text-[16px]">edit</span>
                                                        ${language == 'zh' ? '编辑' : 'Edit'}
                                                    </summary>
                                                    <form action="${pageContext.request.contextPath}/admin/users" method="POST" class="mt-3 grid grid-cols-1 gap-3 rounded-xl border border-slate-200 bg-white p-3 shadow-sm sm:grid-cols-2">
                                                        <input type="hidden" name="action" value="update" />
                                                        <input type="hidden" name="userId" value="${user.id}" />
                                                        <div>
                                                            <label class="admin-label">Email</label>
                                                            <input class="admin-input" type="email" name="email" value="${fn:escapeXml(user.email)}" required />
                                                        </div>
                                                        <div>
                                                            <label class="admin-label">${language == 'zh' ? '姓名' : 'Full Name'}</label>
                                                            <input class="admin-input" type="text" name="fullName" value="${fn:escapeXml(user.fullName)}" required />
                                                        </div>
                                                        <div>
                                                            <label class="admin-label">${language == 'zh' ? '角色' : 'Role'}</label>
                                                            <select class="admin-input" name="role" ${user.id == currentUserId ? 'disabled' : ''}>
                                                                <c:forEach items="${userRoles}" var="role">
                                                                    <option value="${role}" ${user.role == role ? 'selected' : ''}><c:out value="${role}"/></option>
                                                                </c:forEach>
                                                            </select>
                                                        </div>
                                                        <div>
                                                            <label class="admin-label">${language == 'zh' ? '新密码' : 'New Password'}</label>
                                                            <input class="admin-input" type="password" name="password" minlength="6" placeholder="${language == 'zh' ? '留空不修改' : 'Leave blank to keep'}" />
                                                        </div>
                                                        <div>
                                                            <label class="admin-label">${language == 'zh' ? '院系' : 'Department'}</label>
                                                            <input class="admin-input" type="text" name="department" value="${fn:escapeXml(user.department)}" />
                                                        </div>
                                                        <div>
                                                            <label class="admin-label">${language == 'zh' ? '学号/工号' : 'Student/Staff ID'}</label>
                                                            <input class="admin-input" type="text" name="studentId" value="${fn:escapeXml(user.studentId)}" />
                                                        </div>
                                                        <div class="sm:col-span-2">
                                                            <label class="admin-label">${language == 'zh' ? '电话' : 'Phone'}</label>
                                                            <input class="admin-input" type="text" name="phone" value="${fn:escapeXml(user.phone)}" />
                                                        </div>
                                                        <div class="sm:col-span-2">
                                                            <button type="submit" class="portal-btn portal-btn-primary text-xs">
                                                                <span class="material-symbols-outlined text-[16px]">save</span>
                                                                ${language == 'zh' ? '保存' : 'Save'}
                                                            </button>
                                                        </div>
                                                    </form>
                                                </details>
                                                <c:if test="${user.role != 'ADMIN'}">
                                                    <form action="${pageContext.request.contextPath}/admin/users" method="POST" class="inline">
                                                        <input type="hidden" name="action" value="${user.active ? 'deactivate' : 'activate'}" />
                                                        <input type="hidden" name="userId" value="${user.id}" />
                                                        <button type="submit" class="inline-flex min-h-10 items-center justify-center gap-2 rounded-xl border px-3 py-2 text-xs font-bold ${user.active ? 'border-amber-200 text-amber-700 hover:bg-amber-50' : 'border-green-200 text-green-700 hover:bg-green-50'}">
                                                            <span class="material-symbols-outlined text-[16px]">${user.active ? 'block' : 'check_circle'}</span>
                                                            ${user.active ? (language == 'zh' ? '停用' : 'Deactivate') : (language == 'zh' ? '启用' : 'Activate')}
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </section>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
