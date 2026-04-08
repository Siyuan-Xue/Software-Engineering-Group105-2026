<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Settings - QM HIRE</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: { primary: '#0f172a', accent: '#3b82f6', 'background-light': '#f8fafc' },
                    fontFamily: { sans: ['Inter', 'sans-serif'] }
                }
            }
        }
    </script>
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
    <style>
        .settings-label {
            display: block;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 0.14em;
            text-transform: uppercase;
            color: #64748b;
            margin-bottom: 6px;
        }
        .settings-input {
            width: 100%;
            border-radius: 0.75rem;
            border: 1px solid #e2e8f0;
            padding: 0.55rem 0.875rem;
            font-size: 0.875rem;
            color: #0f172a;
            background: #fff;
            outline: none;
            transition: border-color .15s;
        }
        .settings-input:focus { border-color: #94a3b8; box-shadow: 0 0 0 3px rgba(148,163,184,.15); }
        .settings-input[readonly] { background: #f8fafc; color: #64748b; cursor: not-allowed; }
        textarea.settings-input { resize: vertical; min-height: 80px; }
    </style>
</head>
<body class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
<div class="relative flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />
    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="portal-page portal-page--compact">

                <%-- ── Derived display values ─────────────────────────────────── --%>
                <c:set var="displayName" value="${empty userProfile.fullName ? 'Student User' : userProfile.fullName}" />
                <c:set var="initial"     value="${fn:toUpperCase(fn:substring(displayName, 0, 1))}" />
                <c:set var="deptLabel"   value="${empty userProfile.department or userProfile.department == 'None' ? 'Department not set' : userProfile.department}" />
                <c:set var="state"       value="${empty pageState ? 'normal' : pageState}" />
                <c:set var="notifOn"     value="${userProfile.notificationsEnabled == true}" />

                <%-- ── Page header ──────────────────────────────────────────────── --%>
                <div class="portal-page-header mb-6">
                    <div>
                        <h2 class="portal-page-title">Settings</h2>
                        <p class="portal-page-copy">Manage your profile, security settings, and communication preferences.</p>
                    </div>
                </div>

                <%-- ── Toast banners ─────────────────────────────────────────────── --%>
                <c:if test="${state == 'updateSuccess'}">
                    <div class="mb-6 flex items-center gap-3 rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-4">
                        <span class="material-symbols-outlined text-emerald-600">check_circle</span>
                        <p class="text-sm font-semibold text-emerald-800"><c:out value="${successMessage}"/></p>
                    </div>
                </c:if>
                <c:if test="${state == 'updateFailure'}">
                    <div class="mb-6 flex items-center gap-3 rounded-2xl border border-red-200 bg-red-50 px-5 py-4">
                        <span class="material-symbols-outlined text-red-500">error</span>
                        <p class="text-sm font-semibold text-red-700"><c:out value="${errorMessage}"/></p>
                    </div>
                </c:if>
                <c:if test="${state == 'pwdSuccess'}">
                    <div class="mb-6 flex items-center gap-3 rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-4">
                        <span class="material-symbols-outlined text-emerald-600">lock_open</span>
                        <p class="text-sm font-semibold text-emerald-800"><c:out value="${successMessage}"/></p>
                    </div>
                </c:if>
                <c:if test="${state == 'pwdFailure'}">
                    <div class="mb-6 flex items-center gap-3 rounded-2xl border border-red-200 bg-red-50 px-5 py-4">
                        <span class="material-symbols-outlined text-red-500">lock</span>
                        <p class="text-sm font-semibold text-red-700"><c:out value="${errorMessage}"/></p>
                    </div>
                </c:if>

                <div class="grid grid-cols-1 gap-8 lg:grid-cols-[1.3fr_0.7fr]">

                    <%-- ════════════ LEFT COLUMN ════════════ --%>
                    <div class="space-y-8">

                        <%-- ── Profile card ─────────────────────────────────────── --%>
                        <section class="portal-panel overflow-hidden">
                            <div class="border-b border-slate-100 bg-gradient-to-r from-slate-50 to-white p-6">
                                <div class="flex items-center gap-5">
                                    <div class="flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl text-2xl font-black text-white"
                                         style="background:#0f172a;">
                                        <c:out value="${initial}"/>
                                    </div>
                                    <div class="min-w-0">
                                        <h3 class="truncate text-2xl font-black tracking-tight text-slate-900">
                                            <c:out value="${displayName}"/>
                                        </h3>
                                        <p class="mt-0.5 truncate text-sm text-slate-500"><c:out value="${deptLabel}"/></p>
                                        <span class="mt-2 inline-flex items-center gap-1 rounded-full bg-emerald-100 px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-emerald-700">
                                            <span class="material-symbols-outlined text-[14px]">verified</span>
                                            Active account
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="grid grid-cols-1 gap-3 p-6 sm:grid-cols-2">
                                <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4">
                                    <p class="settings-label">Email</p>
                                    <p class="text-sm font-semibold text-slate-900 break-all"><c:out value="${userProfile.email}"/></p>
                                </div>
                                <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4">
                                    <p class="settings-label">Phone</p>
                                    <p class="text-sm font-semibold text-slate-900">
                                        <c:choose>
                                            <c:when test="${not empty userProfile.phone}"><c:out value="${userProfile.phone}"/></c:when>
                                            <c:otherwise><span class="text-slate-400 font-normal">Not provided</span></c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4">
                                    <p class="settings-label">Student ID</p>
                                    <p class="text-sm font-semibold text-slate-900">
                                        <c:choose>
                                            <c:when test="${not empty userProfile.studentId}"><c:out value="${userProfile.studentId}"/></c:when>
                                            <c:otherwise><span class="text-slate-400 font-normal">Not provided</span></c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4">
                                    <p class="settings-label">Department</p>
                                    <p class="text-sm font-semibold text-slate-900"><c:out value="${deptLabel}"/></p>
                                </div>
                                <c:if test="${not empty userProfile.bio}">
                                    <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4 sm:col-span-2">
                                        <p class="settings-label">Bio</p>
                                        <p class="text-sm leading-relaxed text-slate-700"><c:out value="${userProfile.bio}"/></p>
                                    </div>
                                </c:if>
                            </div>
                        </section>

                        <%-- ── Edit profile form ──────────────────────────────────── --%>
                        <section class="portal-panel p-6">
                            <div class="mb-5 flex items-center gap-3">
                                <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-100 text-blue-600">
                                    <span class="material-symbols-outlined">edit</span>
                                </div>
                                <div>
                                    <h3 class="font-bold text-slate-900">Edit Profile</h3>
                                    <p class="text-xs text-slate-500">All changes are saved permanently to your account.</p>
                                </div>
                            </div>

                            <form action="${pageContext.request.contextPath}/settings" method="POST" class="space-y-4">
                                <input type="hidden" name="action" value="updateProfile">

                                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div>
                                        <label class="settings-label">Full Name <span class="text-red-400">*</span></label>
                                        <input type="text" name="fullName"
                                               value="<c:out value='${userProfile.fullName}'/>"
                                               class="settings-input" required />
                                    </div>
                                    <div>
                                        <label class="settings-label">Phone</label>
                                        <input type="text" name="phone"
                                               value="<c:out value='${userProfile.phone}'/>"
                                               class="settings-input" placeholder="+44 7700 900000" />
                                    </div>
                                </div>

                                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div>
                                        <label class="settings-label">Department</label>
                                        <input type="text" name="department"
                                               value="${userProfile.department == 'None' ? '' : userProfile.department}"
                                               class="settings-input" placeholder="e.g. Computer Science" />
                                    </div>
                                    <div>
                                        <label class="settings-label">Student ID</label>
                                        <input type="text" name="studentId"
                                               value="<c:out value='${userProfile.studentId}'/>"
                                               class="settings-input" placeholder="e.g. 220012345" />
                                    </div>
                                </div>

                                <div>
                                    <label class="settings-label">Bio</label>
                                    <textarea name="bio" rows="3" class="settings-input"
                                              placeholder="Tell us a little about yourself, your background, and teaching interests..."><c:out value="${userProfile.bio}"/></textarea>
                                </div>

                                <label class="flex items-center gap-3 cursor-pointer rounded-xl border border-slate-200 bg-slate-50/60 p-3 hover:bg-slate-50 transition-colors">
                                    <input type="checkbox" name="notificationsEnabled"
                                           class="h-4 w-4 rounded border-slate-300 accent-slate-900"
                                           <c:if test="${notifOn}">checked</c:if> />
                                    <div>
                                        <p class="text-sm font-semibold text-slate-800">Enable notifications</p>
                                        <p class="text-xs text-slate-500">Recruiter replies, application updates, and reminders.</p>
                                    </div>
                                </label>

                                <button type="submit" class="portal-btn portal-btn-primary w-full justify-center">
                                    <span class="material-symbols-outlined text-sm">save</span>
                                    Save Profile
                                </button>
                            </form>
                        </section>
                    </div>

                    <%-- ════════════ RIGHT COLUMN ════════════ --%>
                    <div class="space-y-6">

                        <%-- ── Communication preferences (read-only display) ─────── --%>
                        <section class="portal-panel p-6">
                            <div class="mb-4 flex items-center gap-3">
                                <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-100 text-emerald-600">
                                    <span class="material-symbols-outlined">notifications</span>
                                </div>
                                <div>
                                    <h3 class="font-bold text-slate-900">Preferences</h3>
                                    <p class="text-xs text-slate-500">Current account settings.</p>
                                </div>
                            </div>
                            <div class="space-y-3">
                                <div class="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50/60 px-4 py-3">
                                    <div>
                                        <p class="text-sm font-semibold text-slate-900">Notifications</p>
                                        <p class="text-xs text-slate-500">Updates &amp; reminders</p>
                                    </div>
                                    <c:choose>
                                        <c:when test="${notifOn}">
                                            <span class="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-emerald-700">
                                                <span class="material-symbols-outlined text-[13px]">check</span>On
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rounded-full bg-slate-200 px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-600">Off</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50/60 px-4 py-3">
                                    <div>
                                        <p class="text-sm font-semibold text-slate-900">Language</p>
                                    </div>
                                    <span class="rounded-full bg-white ring-1 ring-slate-200 px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-700">English</span>
                                </div>
                                <div class="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50/60 px-4 py-3">
                                    <div>
                                        <p class="text-sm font-semibold text-slate-900">Appearance</p>
                                    </div>
                                    <span class="rounded-full bg-slate-200 px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-600">Light</span>
                                </div>
                            </div>
                        </section>

                        <%-- ── Change password form ──────────────────────────────── --%>
                        <section class="portal-panel p-6">
                            <div class="mb-5 flex items-center gap-3">
                                <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-100 text-amber-600">
                                    <span class="material-symbols-outlined">lock</span>
                                </div>
                                <div>
                                    <h3 class="font-bold text-slate-900">Change Password</h3>
                                    <p class="text-xs text-slate-500">Minimum 8 characters required.</p>
                                </div>
                            </div>

                            <form action="${pageContext.request.contextPath}/settings" method="POST"
                                  class="space-y-3" id="pwdForm">
                                <input type="hidden" name="action" value="changePassword">

                                <div>
                                    <label class="settings-label">Current Password</label>
                                    <input type="password" name="currentPassword" autocomplete="current-password"
                                           class="settings-input" placeholder="••••••••" required />
                                </div>
                                <div>
                                    <label class="settings-label">New Password</label>
                                    <input type="password" name="newPassword" id="newPwd" autocomplete="new-password"
                                           class="settings-input" placeholder="••••••••" required minlength="8" />
                                </div>
                                <div>
                                    <label class="settings-label">Confirm New Password</label>
                                    <input type="password" name="confirmPassword" id="confirmPwd" autocomplete="new-password"
                                           class="settings-input" placeholder="••••••••" required minlength="8" />
                                </div>
                                <p id="pwdMismatch" class="hidden text-xs font-semibold text-red-500">
                                    Passwords do not match.
                                </p>

                                <button type="submit" class="portal-btn portal-btn-secondary w-full justify-center mt-1"
                                        id="pwdSubmitBtn">
                                    <span class="material-symbols-outlined text-sm">key</span>
                                    Update Password
                                </button>
                            </form>
                        </section>

                        <%-- ── Danger zone: logout ───────────────────────────────── --%>
                        <section class="portal-panel portal-panel--danger p-6">
                            <div class="mb-4 flex items-center gap-3">
                                <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-red-100 text-red-600">
                                    <span class="material-symbols-outlined">logout</span>
                                </div>
                                <div>
                                    <h3 class="font-bold text-red-600">Sign Out</h3>
                                    <p class="text-xs text-slate-500">End your current session securely.</p>
                                </div>
                            </div>
                            <form action="${pageContext.request.contextPath}/logout" method="POST">
                                <button type="submit" class="portal-btn portal-btn-danger w-full justify-center">
                                    <span class="material-symbols-outlined text-sm">logout</span>
                                    Logout
                                </button>
                            </form>
                        </section>

                        <p class="text-center text-xs text-slate-400">
                            QM HIRE v1.2 &nbsp;·&nbsp; © 2026 Queen Mary University
                        </p>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<script>
    // Client-side confirm-password match check
    const newPwd     = document.getElementById('newPwd');
    const confirmPwd = document.getElementById('confirmPwd');
    const mismatch   = document.getElementById('pwdMismatch');
    const submitBtn  = document.getElementById('pwdSubmitBtn');

    function checkMatch() {
        if (confirmPwd.value && newPwd.value !== confirmPwd.value) {
            mismatch.classList.remove('hidden');
            submitBtn.disabled = true;
        } else {
            mismatch.classList.add('hidden');
            submitBtn.disabled = false;
        }
    }

    newPwd.addEventListener('input', checkMatch);
    confirmPwd.addEventListener('input', checkMatch);

    document.getElementById('pwdForm').addEventListener('submit', function(e) {
        if (newPwd.value !== confirmPwd.value) {
            e.preventDefault();
            mismatch.classList.remove('hidden');
        }
    });
</script>
</body>
</html>
