<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${i18n['settings.pageTitle']}</title>
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
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
<div class="relative flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />
    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="portal-page portal-page--compact">

                <%-- ── Derived display values ─────────────────────────────────── --%>
                <c:set var="displayName" value="${empty userProfile.fullName ? i18n['common.studentUser'] : userProfile.fullName}" />
                <c:set var="initial"     value="${fn:toUpperCase(fn:substring(displayName, 0, 1))}" />
                <c:set var="deptLabel"   value="${empty userProfile.department or userProfile.department == 'None' ? i18n['common.departmentNotSet'] : userProfile.department}" />
                <c:set var="state"       value="${empty pageState ? 'normal' : pageState}" />
                <c:set var="notifOn"     value="${userProfile.notificationsEnabled == true}" />
                <c:set var="departmentValue" value="${userProfile.department == 'None' ? '' : userProfile.department}" />
                <c:set var="preferredLanguage" value="${empty userProfile.preferredLanguage ? 'en' : userProfile.preferredLanguage}" />
                <c:set var="preferredAppearance" value="${empty userProfile.preferredAppearance ? 'light' : userProfile.preferredAppearance}" />

                <%-- ── Page header ──────────────────────────────────────────────── --%>
                <div class="portal-page-header mb-6">
                    <div>
                        <h2 class="portal-page-title">${i18n['settings.title']}</h2>
                        <p class="portal-page-copy">${i18n['settings.copy']}</p>
                    </div>
                </div>

                <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                    <jsp:param name="containerClass" value="mb-6" />
                </jsp:include>

                <c:choose>
                    <c:when test="${state == 'loadError'}">
                        <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                            <jsp:param name="variant" value="error" />
                            <jsp:param name="icon" value="settings_alert" />
                            <jsp:param name="title" value="Settings unavailable" />
                            <jsp:param name="message" value="We couldn't load your account settings right now. Please refresh the page or try again in a moment." />
                            <jsp:param name="actionHref" value="${pageContext.request.contextPath}/settings" />
                            <jsp:param name="actionLabel" value="Try Again" />
                        </jsp:include>
                    </c:when>
                    <c:otherwise>
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
                                            ${i18n['common.activeAccount']}
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="grid grid-cols-1 gap-3 p-6 sm:grid-cols-2">
                                <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4">
                                    <p class="settings-label">${i18n['common.email']}</p>
                                    <p class="text-sm font-semibold text-slate-900 break-all"><c:out value="${userProfile.email}"/></p>
                                </div>
                                <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4">
                                    <p class="settings-label">${i18n['common.phone']}</p>
                                    <p class="text-sm font-semibold text-slate-900">
                                        <c:choose>
                                            <c:when test="${not empty userProfile.phone}"><c:out value="${userProfile.phone}"/></c:when>
                                            <c:otherwise><span class="text-slate-400 font-normal">${i18n['common.notProvided']}</span></c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4">
                                    <p class="settings-label">${i18n['common.studentId']}</p>
                                    <p class="text-sm font-semibold text-slate-900">
                                        <c:choose>
                                            <c:when test="${not empty userProfile.studentId}"><c:out value="${userProfile.studentId}"/></c:when>
                                            <c:otherwise><span class="text-slate-400 font-normal">${i18n['common.notProvided']}</span></c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4">
                                    <p class="settings-label">${i18n['common.department']}</p>
                                    <p class="text-sm font-semibold text-slate-900"><c:out value="${deptLabel}"/></p>
                                </div>
                                <c:if test="${not empty userProfile.bio}">
                                    <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4 sm:col-span-2">
                                        <p class="settings-label">${i18n['common.bio']}</p>
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
                                    <h3 class="font-bold text-slate-900">${i18n['settings.editProfileTitle']}</h3>
                                    <p class="text-xs text-slate-500">${i18n['settings.editProfileCopy']}</p>
                                </div>
                            </div>

                            <form action="${pageContext.request.contextPath}/settings" method="POST" class="space-y-4">
                                <input type="hidden" name="action" value="updateProfile">

                                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div>
                                        <label class="settings-label">${i18n['common.fullName']} <span class="text-red-400">*</span></label>
                                        <input type="text" name="fullName"
                                               value="<c:out value='${userProfile.fullName}'/>"
                                               class="settings-input" required />
                                    </div>
                                    <div>
                                        <label class="settings-label">${i18n['common.phone']}</label>
                                        <input type="text" name="phone"
                                               value="<c:out value='${userProfile.phone}'/>"
                                               class="settings-input" placeholder="${i18n['settings.phonePlaceholder']}" />
                                    </div>
                                </div>

                                <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div>
                                        <label class="settings-label">${i18n['common.department']}</label>
                                        <input type="text" name="department"
                                               value="<c:out value='${departmentValue}'/>"
                                               class="settings-input" placeholder="${i18n['settings.departmentPlaceholder']}" />
                                    </div>
                                    <div>
                                        <label class="settings-label">${i18n['common.studentId']}</label>
                                        <input type="text" name="studentId"
                                               value="<c:out value='${userProfile.studentId}'/>"
                                               class="settings-input" placeholder="${i18n['settings.studentIdPlaceholder']}" />
                                    </div>
                                </div>

                                <div>
                                    <label class="settings-label">${i18n['common.bio']}</label>
                                    <textarea name="bio" rows="3" class="settings-input"
                                              placeholder="${i18n['settings.bioPlaceholder']}"><c:out value="${userProfile.bio}"/></textarea>
                                </div>

                                <label class="flex items-center gap-3 cursor-pointer rounded-xl border border-slate-200 bg-slate-50/60 p-3 hover:bg-slate-50 transition-colors">
                                    <input type="checkbox" name="notificationsEnabled"
                                           class="h-4 w-4 rounded border-slate-300 accent-slate-900"
                                           <c:if test="${notifOn}">checked</c:if> />
                                    <div>
                                        <p class="text-sm font-semibold text-slate-800">${i18n['settings.enableNotifications']}</p>
                                        <p class="text-xs text-slate-500">${i18n['settings.notificationsCopy']}</p>
                                    </div>
                                </label>

                                <button type="submit" class="portal-btn portal-btn-primary w-full justify-center">
                                    <span class="material-symbols-outlined text-sm">save</span>
                                    ${i18n['settings.saveProfile']}
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
                                    <h3 class="font-bold text-slate-900">${i18n['settings.preferencesTitle']}</h3>
                                    <p class="text-xs text-slate-500">${i18n['settings.preferencesCopy']}</p>
                                </div>
                            </div>
                            <div class="space-y-3">
                                <div class="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50/60 px-4 py-3">
                                    <div>
                                        <p class="text-sm font-semibold text-slate-900">${i18n['common.notifications']}</p>
                                        <p class="text-xs text-slate-500">${i18n['settings.notificationsCopyShort']}</p>
                                    </div>
                                    <c:choose>
                                        <c:when test="${notifOn}">
                                            <span class="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-emerald-700">
                                                <span class="material-symbols-outlined text-[13px]">check</span>${i18n['common.on']}
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="rounded-full bg-slate-200 px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-600">${i18n['common.off']}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <form action="${pageContext.request.contextPath}/settings" method="POST" class="rounded-xl border border-slate-200 bg-slate-50/60 px-4 py-4 space-y-3">
                                    <input type="hidden" name="action" value="updatePreferences">
                                    <div>
                                        <p class="text-sm font-semibold text-slate-900">${i18n['common.language']}</p>
                                        <p class="text-xs text-slate-500">${i18n['settings.languageCopy']}</p>
                                    </div>
                                    <select name="preferredLanguage" class="settings-input">
                                        <option value="en" ${preferredLanguage == 'en' ? 'selected' : ''}>${i18n['common.english']}</option>
                                        <option value="zh" ${preferredLanguage == 'zh' ? 'selected' : ''}>${i18n['common.chinese']}</option>
                                    </select>
                                    <div>
                                        <p class="text-sm font-semibold text-slate-900">${i18n['common.appearance']}</p>
                                        <p class="text-xs text-slate-500">${i18n['settings.appearanceCopy']}</p>
                                    </div>
                                    <select name="preferredAppearance" class="settings-input">
                                        <option value="light" ${preferredAppearance == 'light' ? 'selected' : ''}>${i18n['common.light']}</option>
                                        <option value="dark" ${preferredAppearance == 'dark' ? 'selected' : ''}>${i18n['common.dark']}</option>
                                    </select>
                                    <button type="submit" class="portal-btn portal-btn-secondary w-full justify-center">
                                        <span class="material-symbols-outlined text-sm">palette</span>
                                        ${i18n['settings.savePreferences']}
                                    </button>
                                </form>
                                <div class="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50/60 px-4 py-3">
                                    <div>
                                        <p class="text-sm font-semibold text-slate-900">${i18n['common.appearance']}</p>
                                    </div>
                                    <span class="rounded-full bg-slate-200 px-3 py-1 text-[11px] font-bold uppercase tracking-wider text-slate-600">
                                        <c:choose>
                                            <c:when test="${preferredAppearance == 'dark'}">${i18n['common.dark']}</c:when>
                                            <c:otherwise>${i18n['common.light']}</c:otherwise>
                                        </c:choose>
                                    </span>
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
                                    <h3 class="font-bold text-slate-900">${i18n['settings.changePasswordTitle']}</h3>
                                    <p class="text-xs text-slate-500">${i18n['settings.changePasswordCopy']}</p>
                                </div>
                            </div>

                            <form action="${pageContext.request.contextPath}/settings" method="POST"
                                  class="space-y-3" id="pwdForm">
                                <input type="hidden" name="action" value="changePassword">

                                <div>
                                    <label class="settings-label">${i18n['common.currentPassword']}</label>
                                    <input type="password" name="currentPassword" autocomplete="current-password"
                                           class="settings-input" placeholder="••••••••" required />
                                </div>
                                <div>
                                    <label class="settings-label">${i18n['common.newPassword']}</label>
                                    <input type="password" name="newPassword" id="newPwd" autocomplete="new-password"
                                           class="settings-input" placeholder="••••••••" required minlength="8" />
                                </div>
                                <div>
                                    <label class="settings-label">${i18n['common.confirmNewPassword']}</label>
                                    <input type="password" name="confirmPassword" id="confirmPwd" autocomplete="new-password"
                                           class="settings-input" placeholder="••••••••" required minlength="8" />
                                </div>
                                <p id="pwdMismatch" class="hidden text-xs font-semibold text-red-500">
                                    ${i18n['settings.passwordMismatch']}
                                </p>

                                <button type="submit" class="portal-btn portal-btn-secondary w-full justify-center mt-1"
                                        id="pwdSubmitBtn">
                                    <span class="material-symbols-outlined text-sm">key</span>
                                    ${i18n['settings.updatePassword']}
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
                                    <h3 class="font-bold text-red-600">${i18n['settings.signOutTitle']}</h3>
                                    <p class="text-xs text-slate-500">${i18n['settings.signOutCopy']}</p>
                                </div>
                            </div>
                            <form action="${pageContext.request.contextPath}/logout" method="POST">
                                <button type="submit" class="portal-btn portal-btn-danger w-full justify-center">
                                    <span class="material-symbols-outlined text-sm">logout</span>
                                    ${i18n['common.logout']}
                                </button>
                            </form>
                        </section>

                        <p class="text-center text-xs text-slate-400">
                            ${i18n['settings.footer']}
                        </p>
                    </div>
                </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>

<script>
    const newPwd = document.getElementById('newPwd');
    const confirmPwd = document.getElementById('confirmPwd');
    const mismatch = document.getElementById('pwdMismatch');
    const submitBtn = document.getElementById('pwdSubmitBtn');
    const passwordForm = document.getElementById('pwdForm');

    if (newPwd && confirmPwd && mismatch && submitBtn && passwordForm) {
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

        passwordForm.addEventListener('submit', function(e) {
            if (newPwd.value !== confirmPwd.value) {
                e.preventDefault();
                mismatch.classList.remove('hidden');
            }
        });
    }
</script>
</body>
</html>
