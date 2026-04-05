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
<c:set var="profileCompletion" value="${empty profileCompletionPercentage ? 0 : profileCompletionPercentage}" />
<aside class="hidden w-64 shrink-0 flex-col border-r border-primary/10 bg-white p-4 lg:flex">
    <nav class="flex flex-col gap-1">
        <a href="${contextPath}/dashboard" class="flex items-center gap-3 rounded-lg px-3 py-2.5 transition-colors ${dashboardActive ? 'bg-primary text-white shadow-sm' : 'text-slate-600 hover:bg-slate-50'}">
            <span class="material-symbols-outlined ${dashboardActive ? '!fill-1' : ''}">dashboard</span>
            <span class="text-sm font-medium">Dashboard</span>
        </a>
        <a href="${contextPath}/applications" class="flex items-center gap-3 rounded-lg px-3 py-2.5 transition-colors ${applicationsActive ? 'bg-primary text-white shadow-sm' : 'text-slate-600 hover:bg-slate-50'}">
            <span class="material-symbols-outlined ${applicationsActive ? '!fill-1' : ''}">work_history</span>
            <span class="text-sm font-medium">Applications</span>
        </a>
        <a href="${contextPath}/resumes" class="flex items-center gap-3 rounded-lg px-3 py-2.5 transition-colors ${resumesActive ? 'bg-primary text-white shadow-sm' : 'text-slate-600 hover:bg-slate-50'}">
            <span class="material-symbols-outlined ${resumesActive ? '!fill-1' : ''}">article</span>
            <span class="text-sm font-medium">Resumes</span>
        </a>
        <a href="${contextPath}/vacancies" class="flex items-center gap-3 rounded-lg px-3 py-2.5 transition-colors ${vacanciesActive ? 'bg-primary text-white shadow-sm' : 'text-slate-600 hover:bg-slate-50'}">
            <span class="material-symbols-outlined ${vacanciesActive ? '!fill-1' : ''}">search</span>
            <span class="text-sm font-medium">Vacancies</span>
        </a>
        <a href="${contextPath}/messages" class="flex items-center gap-3 rounded-lg px-3 py-2.5 transition-colors ${messagesActive ? 'bg-primary text-white shadow-sm' : 'text-slate-600 hover:bg-slate-50'}">
            <span class="material-symbols-outlined ${messagesActive ? '!fill-1' : ''}">chat</span>
            <span class="text-sm font-medium">Messages</span>
        </a>

        <div class="my-4 border-t border-slate-100"></div>

        <a href="${contextPath}/settings" class="flex items-center gap-3 rounded-lg px-3 py-2.5 transition-colors ${settingsActive ? 'bg-primary text-white shadow-sm' : 'text-slate-600 hover:bg-slate-50'}">
            <span class="material-symbols-outlined ${settingsActive ? '!fill-1' : ''}">settings</span>
            <span class="text-sm font-medium">Settings</span>
        </a>
    </nav>

    <div class="mt-auto rounded-xl border border-primary/10 bg-primary/5 p-4">
        <p class="mb-2 text-xs font-bold uppercase tracking-wider text-primary">Profile Strength</p>
        <div class="h-1.5 w-full overflow-hidden rounded-full bg-slate-200">
            <div class="h-full bg-primary" style="width: ${profileCompletion}%"></div>
        </div>
        <p class="mt-2 text-xs text-slate-500">
            <c:out value="${profileCompletion}" />% complete. Keep your profile updated so your applications stay ready to submit.
        </p>
    </div>
</aside>
