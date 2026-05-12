<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="currentPath" value="${pageContext.request.requestURI}" />
<c:set var="dashboardActive" value="${fn:contains(currentPath, 'dashboard')}" />
<c:set var="applicationsActive" value="${fn:contains(currentPath, 'applications')}" />
<c:set var="resumesActive" value="${fn:contains(currentPath, 'resumes')}" />
<c:set var="vacanciesActive" value="${fn:contains(currentPath, 'vacancies') or fn:contains(currentPath, 'vacancy')}" />
<c:set var="messagesActive" value="${fn:contains(currentPath, 'messages')}" />
<c:set var="settingsActive" value="${fn:contains(currentPath, 'settings')}" />
<c:set var="skillsActive" value="${fn:contains(currentPath, 'admin/skills')}" />
<c:set var="profileCompletion" value="${empty profileCompletionPercentage ? 0 : profileCompletionPercentage}" />
<aside class="hidden w-64 shrink-0 flex-col border-r border-slate-100 bg-white p-4 lg:flex">
    <nav class="flex flex-col gap-1">
        <a href="${contextPath}/dashboard" style="${dashboardActive ? 'background:#0f172a;color:#fff;' : ''}" class="flex items-center gap-3 rounded-xl px-3 py-2.5 transition-all text-slate-600 hover:bg-slate-50 hover:text-slate-900 ${dashboardActive ? 'shadow-sm' : ''}">
            <span class="material-symbols-outlined text-[20px]">dashboard</span>
            <span class="text-sm font-semibold">${i18n['common.dashboard']}</span>
        </a>
        
        <c:if test="${userRole == 'TA' or userRole == 'MO'}">
            <a href="${contextPath}/applications" style="${applicationsActive ? 'background:#0f172a;color:#fff;' : ''}" class="flex items-center gap-3 rounded-xl px-3 py-2.5 transition-all text-slate-600 hover:bg-slate-50 hover:text-slate-900 ${applicationsActive ? 'shadow-sm' : ''}">
                <span class="material-symbols-outlined text-[20px]">work_history</span>
                <span class="text-sm font-semibold">${i18n['common.applications']}</span>
            </a>
        </c:if>

        <c:if test="${userRole == 'TA'}">
            <a href="${contextPath}/resumes" style="${resumesActive ? 'background:#0f172a;color:#fff;' : ''}" class="flex items-center gap-3 rounded-xl px-3 py-2.5 transition-all text-slate-600 hover:bg-slate-50 hover:text-slate-900 ${resumesActive ? 'shadow-sm' : ''}">
                <span class="material-symbols-outlined text-[20px]">article</span>
                <span class="text-sm font-semibold">${i18n['common.resumes']}</span>
            </a>
        </c:if>

        <c:if test="${userRole == 'TA' or userRole == 'MO'}">
            <a href="${contextPath}/vacancies" style="${vacanciesActive ? 'background:#0f172a;color:#fff;' : ''}" class="flex items-center gap-3 rounded-xl px-3 py-2.5 transition-all text-slate-600 hover:bg-slate-50 hover:text-slate-900 ${vacanciesActive ? 'shadow-sm' : ''}">
                <span class="material-symbols-outlined text-[20px]">search</span>
                <span class="text-sm font-semibold">${i18n['common.vacancies']}</span>
            </a>
        </c:if>

        <c:if test="${userRole == 'ADMIN'}">
            <a href="${contextPath}/workloads" style="${fn:contains(currentPath, 'workloads') ? 'background:#0f172a;color:#fff;' : ''}" class="flex items-center gap-3 rounded-xl px-3 py-2.5 transition-all text-slate-600 hover:bg-slate-50 hover:text-slate-900 ${fn:contains(currentPath, 'workloads') ? 'shadow-sm' : ''}">
                <span class="material-symbols-outlined text-[20px]">group</span>
                <span class="text-sm font-semibold">${i18n['common.workloads']}</span>
            </a>
            <a href="${contextPath}/admin/skills" style="${skillsActive ? 'background:#0f172a;color:#fff;' : ''}" class="flex items-center gap-3 rounded-xl px-3 py-2.5 transition-all text-slate-600 hover:bg-slate-50 hover:text-slate-900 ${skillsActive ? 'shadow-sm' : ''}">
                <span class="material-symbols-outlined text-[20px]">psychology</span>
                <span class="text-sm font-semibold">${i18n['common.skills']}</span>
            </a>
        </c:if>

        <a href="${contextPath}/messages" style="${messagesActive ? 'background:#0f172a;color:#fff;' : ''}" class="flex items-center gap-3 rounded-xl px-3 py-2.5 transition-all text-slate-600 hover:bg-slate-50 hover:text-slate-900 ${messagesActive ? 'shadow-sm' : ''}">
            <span class="material-symbols-outlined text-[20px]">chat</span>
            <span class="text-sm font-semibold">${i18n['common.messages']}</span>
        </a>

        <div class="my-3 border-t border-slate-100"></div>

        <a href="${contextPath}/settings" style="${settingsActive ? 'background:#0f172a;color:#fff;' : ''}" class="flex items-center gap-3 rounded-xl px-3 py-2.5 transition-all text-slate-600 hover:bg-slate-50 hover:text-slate-900 ${settingsActive ? 'shadow-sm' : ''}">
            <span class="material-symbols-outlined text-[20px]">settings</span>
            <span class="text-sm font-semibold">${i18n['common.settings']}</span>
        </a>
    </nav>

    <div class="mt-auto rounded-xl border border-primary/10 bg-primary/5 p-4">
        <p class="mb-2 text-xs font-bold uppercase tracking-wider text-primary">${i18n['common.profileStrength']}</p>
        <div class="h-1.5 w-full overflow-hidden rounded-full bg-slate-200">
            <div class="h-full bg-primary" style="width: ${profileCompletion}%"></div>
        </div>
        <p class="mt-2 text-xs text-slate-500">
            <c:choose>
                <c:when test="${language == 'zh'}"><c:out value="${profileCompletion}" />% 已完成。请保持资料更新，方便随时提交申请。</c:when>
                <c:otherwise><c:out value="${profileCompletion}" />% complete. Keep your profile updated so your applications stay ready to submit.</c:otherwise>
            </c:choose>
        </p>
    </div>
</aside>
