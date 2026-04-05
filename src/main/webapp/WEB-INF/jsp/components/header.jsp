<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="currentPath" value="${pageContext.request.requestURI}" />
<c:set var="headerDisplayName" value="Student User" />
<c:set var="headerSecondaryLabel" value="TA Applicant" />
<c:choose>
    <c:when test="${not empty userProfile.firstName and not empty userProfile.lastName}">
        <c:set var="headerDisplayName" value="${userProfile.firstName} ${userProfile.lastName}" />
    </c:when>
    <c:when test="${not empty userProfile.firstName}">
        <c:set var="headerDisplayName" value="${userProfile.firstName}" />
    </c:when>
    <c:when test="${not empty userProfile.lastName}">
        <c:set var="headerDisplayName" value="${userProfile.lastName}" />
    </c:when>
    <c:when test="${not empty userName}">
        <c:set var="headerDisplayName" value="${userName}" />
    </c:when>
</c:choose>
<c:if test="${not empty userRoleLabel}">
    <c:set var="headerSecondaryLabel" value="${userRoleLabel}" />
</c:if>
<c:if test="${not empty userProfile.department}">
    <c:set var="headerSecondaryLabel" value="${userProfile.department}" />
</c:if>
<c:if test="${not empty userSecondaryLabel}">
    <c:set var="headerSecondaryLabel" value="${userSecondaryLabel}" />
</c:if>
<c:set var="avatarLetter" value="${fn:toUpperCase(fn:substring(headerDisplayName, 0, 1))}" />
<c:set var="dashboardActive" value="${fn:contains(currentPath, 'dashboard')}" />
<c:set var="applicationsActive" value="${fn:contains(currentPath, 'applications')}" />
<c:set var="resumesActive" value="${fn:contains(currentPath, 'resumes')}" />
<c:set var="vacanciesActive" value="${fn:contains(currentPath, 'vacancies') or fn:contains(currentPath, 'vacancy')}" />
<c:set var="messagesActive" value="${fn:contains(currentPath, 'messages')}" />
<c:set var="settingsActive" value="${fn:contains(currentPath, 'settings')}" />
<header class="sticky top-0 z-50 border-b border-primary/10 bg-white/95 backdrop-blur">
    <div class="flex items-center justify-between gap-4 px-6 py-3 lg:px-10">
        <div class="flex items-center gap-6">
            <a href="${contextPath}/dashboard" class="flex items-center gap-3">
                <div class="flex h-9 w-9 items-center justify-center rounded-xl bg-primary text-white shadow-sm">
                    <span class="material-symbols-outlined text-xl">work</span>
                </div>
                <div class="flex items-center gap-1">
                    <span class="font-black text-lg tracking-tighter text-primary">QM</span>
                    <span class="font-light text-lg tracking-tight text-slate-500">HIRE</span>
                </div>
            </a>
            <form action="${contextPath}/vacancies" method="GET" class="hidden md:flex min-w-72">
                <label class="flex w-full items-center rounded-xl border border-primary/10 bg-slate-50 px-4 py-2.5 focus-within:border-primary/20">
                    <span class="material-symbols-outlined text-xl text-slate-400">search</span>
                    <input
                        type="text"
                        name="keyword"
                        value="${param.keyword}"
                        class="w-full border-none bg-transparent pl-3 text-sm text-slate-900 outline-none focus:ring-0"
                        placeholder="Search vacancies..."
                    />
                </label>
            </form>
        </div>

        <div class="flex items-center gap-4">
            <div class="hidden items-center gap-2 sm:flex">
                <button type="button" class="flex h-10 w-10 items-center justify-center rounded-lg bg-slate-50 text-slate-600 transition-colors hover:bg-primary/10" aria-label="Notifications">
                    <span class="material-symbols-outlined">notifications</span>
                </button>
                <a href="${contextPath}/settings" class="flex h-10 w-10 items-center justify-center rounded-lg bg-slate-50 text-slate-600 transition-colors hover:bg-primary/10" aria-label="Settings">
                    <span class="material-symbols-outlined">settings</span>
                </a>
            </div>
            <div class="hidden h-8 w-px bg-slate-200 sm:block"></div>
            <div class="flex items-center gap-3">
                <div class="hidden min-w-0 text-right sm:block">
                    <div class="truncate text-sm font-semibold text-slate-900"><c:out value="${headerDisplayName}" /></div>
                    <div class="truncate text-xs text-slate-500"><c:out value="${headerSecondaryLabel}" /></div>
                </div>
                <div class="flex h-10 w-10 items-center justify-center overflow-hidden rounded-full border border-primary/20 bg-primary/10 text-sm font-bold text-primary">
                    <c:choose>
                        <c:when test="${not empty userAvatarUrl}">
                            <img src="${userAvatarUrl}" alt="${headerDisplayName}" class="h-full w-full object-cover" />
                        </c:when>
                        <c:otherwise>
                            <span><c:out value="${avatarLetter}" /></span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>

    <nav class="border-t border-primary/10 px-4 py-3 lg:hidden">
        <div class="flex gap-2 overflow-x-auto">
            <a href="${contextPath}/dashboard" class="whitespace-nowrap rounded-full px-3 py-2 text-xs font-bold transition-colors ${dashboardActive ? 'bg-primary text-white' : 'bg-slate-100 text-slate-600'}">Dashboard</a>
            <a href="${contextPath}/applications" class="whitespace-nowrap rounded-full px-3 py-2 text-xs font-bold transition-colors ${applicationsActive ? 'bg-primary text-white' : 'bg-slate-100 text-slate-600'}">Applications</a>
            <a href="${contextPath}/resumes" class="whitespace-nowrap rounded-full px-3 py-2 text-xs font-bold transition-colors ${resumesActive ? 'bg-primary text-white' : 'bg-slate-100 text-slate-600'}">Resumes</a>
            <a href="${contextPath}/vacancies" class="whitespace-nowrap rounded-full px-3 py-2 text-xs font-bold transition-colors ${vacanciesActive ? 'bg-primary text-white' : 'bg-slate-100 text-slate-600'}">Vacancies</a>
            <a href="${contextPath}/messages" class="whitespace-nowrap rounded-full px-3 py-2 text-xs font-bold transition-colors ${messagesActive ? 'bg-primary text-white' : 'bg-slate-100 text-slate-600'}">Messages</a>
            <a href="${contextPath}/settings" class="whitespace-nowrap rounded-full px-3 py-2 text-xs font-bold transition-colors ${settingsActive ? 'bg-primary text-white' : 'bg-slate-100 text-slate-600'}">Settings</a>
        </div>
    </nav>
</header>
