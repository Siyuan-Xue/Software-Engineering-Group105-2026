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
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
</head>
<body class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
    <div class="relative flex min-h-screen w-full flex-col">
        <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

        <div class="flex flex-1 overflow-hidden">
            <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

            <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
                <div class="portal-page portal-page--compact">
                    <c:set var="settingsState" value="${empty pageState ? 'normal' : pageState}" />
                    <c:set var="firstName" value="${userProfile.firstName}" />
                    <c:set var="lastName" value="${userProfile.lastName}" />
                    <c:set var="emailAddress" value="${empty userProfile.email ? 'Not provided yet' : userProfile.email}" />
                    <c:set var="studentId" value="${empty userProfile.studentId ? 'Not provided yet' : userProfile.studentId}" />
                    <c:set var="departmentName" value="${empty userProfile.department ? 'Department not set' : userProfile.department}" />
                    <c:set var="bioText" value="${empty userProfile.bio ? 'No bio has been added yet. This section is ready for backend-driven profile data.' : userProfile.bio}" />
                    <c:set var="profileDisplayName" value="Student User" />
                    <c:choose>
                        <c:when test="${not empty firstName and not empty lastName}">
                            <c:set var="profileDisplayName" value="${firstName} ${lastName}" />
                        </c:when>
                        <c:when test="${not empty firstName}">
                            <c:set var="profileDisplayName" value="${firstName}" />
                        </c:when>
                        <c:when test="${not empty lastName}">
                            <c:set var="profileDisplayName" value="${lastName}" />
                        </c:when>
                    </c:choose>
                    <c:set var="profileInitial" value="${fn:toUpperCase(fn:substring(profileDisplayName, 0, 1))}" />
                    <c:set var="notificationsEnabled" value="${userProfile.notificationsEnabled == true}" />

                    <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp" />

                    <c:choose>
                        <c:when test="${settingsState == 'loadError'}">
                            <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                                <jsp:param name="variant" value="error" />
                                <jsp:param name="icon" value="settings_alert" />
                                <jsp:param name="title" value="Settings unavailable" />
                                <jsp:param name="message" value="We couldn't load your account settings right now. Please refresh the page and try again." />
                                <jsp:param name="actionHref" value="${pageContext.request.contextPath}/settings" />
                                <jsp:param name="actionLabel" value="Try Again" />
                            </jsp:include>
                        </c:when>
                        <c:otherwise>
                            <div class="portal-page-header">
                                <div>
                                    <h2 class="portal-page-title">Settings</h2>
                                    <p class="portal-page-copy">Manage your account preferences, profile details, and communication settings.</p>
                                </div>
                                <div class="portal-summary-card">
                                    <p class="portal-kicker">Baseline Scope</p>
                                    <p class="mt-1 text-sm font-semibold text-slate-900">Profile data is now dynamic.</p>
                                    <p class="mt-1 text-xs leading-relaxed text-slate-500">This page is currently optimized for display and feedback states. Update actions can be attached once backend endpoints are finalized.</p>
                                </div>
                            </div>

                            <c:if test="${settingsState == 'updateSuccess' or settingsState == 'updateFailure'}">
                                <div class="portal-panel p-5 ${settingsState == 'updateSuccess' ? 'border-emerald-200 bg-emerald-50/80' : 'border-red-200 bg-red-50/80'}">
                                    <p class="portal-kicker ${settingsState == 'updateSuccess' ? 'text-emerald-700' : 'text-red-700'}">Update Status</p>
                                    <h3 class="mt-2 text-lg font-bold text-slate-900">
                                        <c:choose>
                                            <c:when test="${settingsState == 'updateSuccess'}">Settings updated successfully.</c:when>
                                            <c:otherwise>We couldn't save your latest changes.</c:otherwise>
                                        </c:choose>
                                    </h3>
                                    <p class="mt-1 text-sm leading-relaxed text-slate-600">
                                        <c:choose>
                                            <c:when test="${settingsState == 'updateSuccess'}">The latest profile information and preferences are now reflected on this page.</c:when>
                                            <c:otherwise>Please review the submitted values and try again once the corresponding backend update flow is ready.</c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </c:if>

                            <div class="grid grid-cols-1 gap-8 lg:grid-cols-[1.2fr_0.8fr]">
                                <div class="space-y-8">
                                    <section class="portal-panel overflow-hidden">
                                        <div class="border-b border-slate-100 p-6">
                                            <div class="flex items-center gap-4">
                                                <div class="flex h-16 w-16 items-center justify-center rounded-2xl bg-primary/10 text-2xl font-bold text-primary">
                                                    <c:out value="${profileInitial}" />
                                                </div>
                                                <div class="min-w-0">
                                                    <h3 class="truncate text-2xl font-black tracking-tight text-slate-900"><c:out value="${profileDisplayName}" /></h3>
                                                    <p class="mt-1 truncate text-sm text-slate-500"><c:out value="${departmentName}" /></p>
                                                    <p class="mt-2 inline-flex rounded-full bg-slate-100 px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-500">Read-only baseline view</p>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="grid grid-cols-1 gap-4 p-6 sm:grid-cols-2">
                                            <div class="rounded-xl border border-slate-200 bg-slate-50 p-4">
                                                <p class="text-[11px] font-bold uppercase tracking-[0.18em] text-slate-400">Email</p>
                                                <p class="mt-2 text-sm font-semibold text-slate-900"><c:out value="${emailAddress}" /></p>
                                            </div>
                                            <div class="rounded-xl border border-slate-200 bg-slate-50 p-4">
                                                <p class="text-[11px] font-bold uppercase tracking-[0.18em] text-slate-400">Student ID</p>
                                                <p class="mt-2 text-sm font-semibold text-slate-900"><c:out value="${studentId}" /></p>
                                            </div>
                                            <div class="rounded-xl border border-slate-200 bg-slate-50 p-4 sm:col-span-2">
                                                <p class="text-[11px] font-bold uppercase tracking-[0.18em] text-slate-400">Department</p>
                                                <p class="mt-2 text-sm font-semibold text-slate-900"><c:out value="${departmentName}" /></p>
                                            </div>
                                        </div>
                                    </section>

                                    <section class="portal-panel p-6">
                                        <div class="mb-4 flex items-center gap-3">
                                            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-100 text-blue-600">
                                                <span class="material-symbols-outlined">person</span>
                                            </div>
                                            <div>
                                                <h3 class="font-bold text-slate-900">Profile Bio</h3>
                                                <p class="text-sm text-slate-500">This section is ready for backend-driven personal details.</p>
                                            </div>
                                        </div>
                                        <div class="rounded-xl border border-slate-200 bg-slate-50 p-4">
                                            <p class="text-sm leading-relaxed text-slate-600"><c:out value="${bioText}" /></p>
                                        </div>
                                    </section>
                                </div>

                                <div class="space-y-6">
                                    <section class="portal-panel p-6">
                                        <div class="mb-4 flex items-center gap-3">
                                            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-emerald-100 text-emerald-600">
                                                <span class="material-symbols-outlined">notifications</span>
                                            </div>
                                            <div>
                                                <h3 class="font-bold text-slate-900">Communication Preferences</h3>
                                                <p class="text-sm text-slate-500">Current preference values coming from `userProfile`.</p>
                                            </div>
                                        </div>
                                        <div class="space-y-4">
                                            <div class="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50 p-4">
                                                <div>
                                                    <p class="text-sm font-bold text-slate-900">Notifications</p>
                                                    <p class="mt-1 text-xs text-slate-500">Recruiter replies, application updates, and reminders.</p>
                                                </div>
                                                <span class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-wider ${notificationsEnabled ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}">
                                                    <c:choose>
                                                        <c:when test="${notificationsEnabled}">Enabled</c:when>
                                                        <c:otherwise>Disabled</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                            <div class="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50 p-4">
                                                <div>
                                                    <p class="text-sm font-bold text-slate-900">Language</p>
                                                    <p class="mt-1 text-xs text-slate-500">Interface language will become editable once settings update actions are connected.</p>
                                                </div>
                                                <span class="rounded-full bg-white px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-primary ring-1 ring-slate-200">English</span>
                                            </div>
                                            <div class="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50 p-4">
                                                <div>
                                                    <p class="text-sm font-bold text-slate-900">Appearance</p>
                                                    <p class="mt-1 text-xs text-slate-500">Theme switching stays placeholder-only for the current baseline.</p>
                                                </div>
                                                <span class="rounded-full bg-slate-200 px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-600">Light</span>
                                            </div>
                                        </div>
                                    </section>

                                    <section class="portal-panel p-6">
                                        <div class="mb-4 flex items-center gap-3">
                                            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-amber-100 text-amber-600">
                                                <span class="material-symbols-outlined">info</span>
                                            </div>
                                            <div>
                                                <h3 class="font-bold text-slate-900">Implementation Note</h3>
                                                <p class="text-sm text-slate-500">This round focuses on consistent rendering and feedback states.</p>
                                            </div>
                                        </div>
                                        <p class="text-sm leading-relaxed text-slate-600">The current Settings page is intentionally read-only. It now consumes backend-provided profile fields and supports success, failure, and load-error feedback without inventing extra update flows before the contract is finalized.</p>
                                    </section>

                                    <section class="portal-panel portal-panel--danger p-6">
                                        <div class="mb-4 flex items-center gap-3">
                                            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-red-100 text-red-600">
                                                <span class="material-symbols-outlined">logout</span>
                                            </div>
                                            <div>
                                                <h3 class="font-bold text-red-600">Logout</h3>
                                                <p class="text-sm text-slate-500">Sign out of your account securely.</p>
                                            </div>
                                        </div>
                                        <form action="${pageContext.request.contextPath}/logout" method="POST">
                                            <button type="submit" class="portal-btn portal-btn-danger">
                                                Logout
                                            </button>
                                        </form>
                                    </section>
                                </div>
                            </div>

                            <div class="text-center text-xs text-slate-400">
                                <p>App Version 1.2.0 • Build 20241024</p>
                                <p class="mt-1">© 2024 University Recruitment System</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
