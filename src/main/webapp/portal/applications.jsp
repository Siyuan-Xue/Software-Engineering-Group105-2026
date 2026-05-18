<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Applications - QM HIRE</title>
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
                <div class="portal-page">
                    <c:set var="applicationsState" value="${empty pageState ? 'normal' : pageState}" />
                    <c:set var="applicationsActiveFilters" value="${not empty param.keyword or not empty param.status or param.date == 'oldest'}" />
                    <c:set var="applicationsFilterNoResults" value="${applicationsState == 'noFilterResults' or applicationsActiveFilters}" />

                    <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                        <jsp:param name="containerClass" value="mb-6" />
                    </jsp:include>

                    <!-- Page Header -->
                    <div class="portal-page-header mb-2">
                        <div>
                            <h2 class="portal-page-title">My Applications</h2>
                            <p class="portal-page-copy">Track the progress of your submitted module and program applications.</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/vacancies" class="portal-btn portal-btn-primary">
                            <span class="material-symbols-outlined text-sm">add</span>
                            New Application
                        </a>
                    </div>

                    <!-- Search and Filters -->
                    <form action="${pageContext.request.contextPath}/applications" method="GET" class="portal-filter-bar">
                        <div class="flex flex-col lg:flex-row gap-4">
                            <div class="flex-1">
                                <label class="flex flex-col w-full">
                                    <div class="flex w-full items-center rounded-lg bg-slate-100 px-4 h-11 border border-transparent focus-within:border-primary/30 transition-all">
                                        <span class="material-symbols-outlined text-slate-400">search</span>
                                        <input type="text" name="keyword" value="${param.keyword}" class="w-full bg-transparent border-none focus:ring-0 text-slate-900 placeholder:text-slate-400 text-sm font-medium pl-3 outline-none" placeholder="Search by module name or department..." />
                                    </div>
                                </label>
                            </div>
                            <div class="flex flex-wrap gap-2">
                                <div class="relative group">
                                    <select name="status" class="flex h-11 items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors appearance-none pr-8">
                                        <option value="">Status: All</option>
                                        <option value="Under Review" ${param.status == 'Under Review' ? 'selected' : ''}>Under Review</option>
                                        <option value="Accepted" ${param.status == 'Accepted' ? 'selected' : ''}>Accepted</option>
                                        <option value="Rejected" ${param.status == 'Rejected' ? 'selected' : ''}>Rejected</option>
                                    </select>
                                    <span class="material-symbols-outlined text-lg absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-500">expand_more</span>
                                </div>
                                <div class="relative group">
                                    <select name="date" class="flex h-11 items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors appearance-none pr-8">
                                        <option value="latest" ${param.date == 'latest' || empty param.date ? 'selected' : ''}>Date: Latest</option>
                                        <option value="oldest" ${param.date == 'oldest' ? 'selected' : ''}>Date: Oldest</option>
                                    </select>
                                    <span class="material-symbols-outlined text-lg absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-500">calendar_today</span>
                                </div>
                                <button type="submit" class="flex h-11 items-center gap-2 rounded-lg bg-slate-100 px-4 text-sm font-bold text-slate-500 hover:text-primary transition-colors">
                                    <span class="material-symbols-outlined text-lg">filter_list</span>
                                    Filter
                                </button>
                            </div>
                        </div>
                    </form>

                    <!-- Applications Table -->
                    <c:choose>
                        <c:when test="${applicationsState == 'loadError'}">
                            <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                                <jsp:param name="variant" value="error" />
                                <jsp:param name="icon" value="work_history" />
                                <jsp:param name="title" value="Applications unavailable" />
                                <jsp:param name="message" value="We couldn't load your application history right now. Please refresh the page and try again." />
                                <jsp:param name="actionHref" value="${pageContext.request.contextPath}/applications" />
                                <jsp:param name="actionLabel" value="Try Again" />
                            </jsp:include>
                        </c:when>
                        <c:otherwise>
                            <div class="portal-panel overflow-hidden">
                                <div class="overflow-x-auto">
                                    <table class="w-full text-left border-collapse">
                                        <thead>
                                            <tr class="bg-slate-50 border-b border-slate-200">
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Module Name</th>
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Department</th>
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Submitted Date</th>
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Resume Used</th>
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Status</th>
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500 text-right">Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody class="divide-y divide-slate-100">
                                            <c:choose>
                                                <c:when test="${empty applications}">
                                                    <tr>
                                                        <td colspan="6" class="px-6 py-8 text-center text-slate-500">
                                                            <c:choose>
                                                                <c:when test="${applicationsState == 'empty'}">
                                                                    <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                                        <jsp:param name="icon" value="inbox" />
                                                                        <jsp:param name="title" value="No applications yet" />
                                                                        <jsp:param name="message" value="You have not submitted any applications. Browse open vacancies to get started." />
                                                                        <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                                                        <jsp:param name="actionLabel" value="Browse Vacancies" />
                                                                    </jsp:include>
                                                                </c:when>
                                                                <c:when test="${applicationsFilterNoResults}">
                                                                    <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                                        <jsp:param name="icon" value="filter_alt_off" />
                                                                        <jsp:param name="title" value="No applications match your filters" />
                                                                        <jsp:param name="message" value="Try clearing filters or adjusting your search to see more results." />
                                                                        <jsp:param name="actionHref" value="${pageContext.request.contextPath}/applications" />
                                                                        <jsp:param name="actionLabel" value="Clear filters" />
                                                                    </jsp:include>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                                        <jsp:param name="icon" value="inbox" />
                                                                        <jsp:param name="title" value="No applications yet" />
                                                                        <jsp:param name="message" value="You have not submitted any applications. Browse open vacancies to get started." />
                                                                        <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                                                        <jsp:param name="actionLabel" value="Browse Vacancies" />
                                                                    </jsp:include>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                    </tr>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach items="${applications}" var="app">
                                                        <tr class="hover:bg-slate-50/50 transition-colors">
                                                            <td class="px-6 py-5">
                                                                <div class="font-bold text-slate-900"><c:out value="${app.vacancyTitle}"/></div>
                                                                <div class="text-xs text-slate-400 font-medium"><c:out value="${app.courseCode}"/> (Ref: #<c:out value="${app.applicationId}"/>)</div>
                                                            </td>
                                                            <td class="px-6 py-5 text-sm text-slate-600 font-medium">
                                                                <c:choose>
                                                                    <c:when test="${empty app.department}"><span class="text-slate-400">—</span></c:when>
                                                                    <c:otherwise><c:out value="${app.department}"/></c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td class="px-6 py-5 text-sm text-slate-600"><c:out value="${app.appliedDate}"/></td>
                                                            <td class="px-6 py-5">
                                                                <div class="flex items-center gap-2 text-xs font-medium text-primary bg-primary/5 px-2 py-1 rounded w-fit">
                                                                    <span class="material-symbols-outlined text-sm">description</span>
                                                                    <c:out value="${app.resumeName}"/>
                                                                </div>
                                                            </td>
                                                            <td class="px-6 py-5">
                                                                <c:choose>
                                                                    <c:when test="${app.status == 'Accepted'}">
                                                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-green-100 text-green-700">
                                                                            Accepted
                                                                        </span>
                                                                    </c:when>
                                                                    <c:when test="${app.status == 'Rejected'}">
                                                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-red-100 text-red-700">
                                                                            Rejected
                                                                        </span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-100 text-blue-700">
                                                                            <c:out value="${app.status}"/>
                                                                        </span>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td class="px-6 py-5 text-right">
                                                                <c:choose>
                                                                    <c:when test="${not empty app.vacancyId}">
                                                                        <a href="${pageContext.request.contextPath}/vacancy?vacancyId=${app.vacancyId}" class="text-sm font-bold text-primary hover:underline">View vacancy</a>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <a href="${pageContext.request.contextPath}/vacancies" class="text-sm font-bold text-slate-600 hover:text-primary hover:underline">Browse vacancies</a>
                                                                    </c:otherwise>
                                                                </c:choose>
<<<<<<< Updated upstream
=======
                                                                <c:if test="${userRole == 'MO' && app.status != 'Withdrawn' && app.status != 'Accepted' && app.status != 'Rejected' && app.status != 'Declined'}">
                                                                    <c:if test="${app.status == 'Submitted'}">
                                                                        <form action="${pageContext.request.contextPath}/application/decision" method="POST" class="inline">
                                                                            <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                                                            <input type="hidden" name="action" value="review"/>
                                                                            <button type="submit" class="text-xs font-bold text-blue-700 hover:underline">${language == 'zh' ? '开始审核' : 'Start review'}</button>
                                                                        </form>
                                                                    </c:if>
                                                                    <c:if test="${app.status == 'Submitted' || app.status == 'Under Review'}">
                                                                        <form action="${pageContext.request.contextPath}/application/decision" method="POST" class="inline" onsubmit="return confirm('${language == 'zh' ? '确定直接录用该申请人？' : 'Accept this applicant?'}');">
                                                                            <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                                                            <input type="hidden" name="action" value="mo_accept"/>
                                                                            <button type="submit" class="text-xs font-bold text-emerald-700 hover:underline">${language == 'zh' ? '录用' : 'Accept'}</button>
                                                                        </form>
                                                                    </c:if>
                                                                    <c:if test="${app.status != 'Offer Pending'}">
                                                                        <form action="${pageContext.request.contextPath}/application/decision" method="POST" class="inline" onsubmit="return confirmReject(this, '${language == 'zh' ? 'zh' : 'en'}');">
                                                                            <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                                                            <input type="hidden" name="action" value="reject"/>
                                                                            <input type="hidden" name="rejectionNote" value=""/>
                                                                            <button type="submit" class="text-xs font-bold text-red-700 hover:underline">${language == 'zh' ? '拒绝' : 'Reject'}</button>
                                                                        </form>
                                                                    </c:if>
                                                                </c:if>
                                                                <c:if test="${userRole == 'MO' && app.status != 'Withdrawn'}">
                                                                    <button type="button"
                                                                            class="inline-flex items-center gap-1 text-xs font-bold text-violet-700 hover:text-violet-900 disabled:opacity-40 disabled:pointer-events-none"
                                                                            data-app-id="${app.applicationId}"
                                                                            ${qwenConfigured ? '' : 'disabled="disabled"'}
                                                                            onclick="openMoAdviceDisclaimer(this.dataset.appId)">
                                                                        <span class="material-symbols-outlined text-[14px]" style="font-variation-settings:'FILL' 1">auto_awesome</span>
                                                                        ${language == 'zh' ? 'AI 决策建议' : 'AI decision hints'}
                                                                    </button>
                                                                </c:if>
                                                                <c:if test="${userRole == 'TA' && app.status == 'Offer Pending'}">
                                                                    <form action="${pageContext.request.contextPath}/application/decision" method="POST" class="inline">
                                                                        <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                                                        <input type="hidden" name="action" value="accept"/>
                                                                        <button type="submit" class="text-xs font-bold text-emerald-700 hover:underline">${language == 'zh' ? '接受录用' : 'Accept offer'}</button>
                                                                    </form>
                                                                    <form action="${pageContext.request.contextPath}/application/decision" method="POST" class="inline" onsubmit="return confirm('${language == 'zh' ? '确定拒绝该录用？' : 'Decline this offer?'}');">
                                                                        <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                                                        <input type="hidden" name="action" value="decline"/>
                                                                        <button type="submit" class="text-xs font-bold text-red-700 hover:underline">${language == 'zh' ? '拒绝录用' : 'Decline offer'}</button>
                                                                    </form>
                                                                </c:if>
                                                                <c:if test="${userRole == 'TA' && app.status != 'Withdrawn' && app.status != 'Accepted' && app.status != 'Rejected' && app.status != 'Declined'}">
                                                                    <form action="${pageContext.request.contextPath}/application/decision" method="POST" class="inline" onsubmit="return confirm('${language == 'zh' ? '确定撤回该申请？' : 'Withdraw this application?'}');">
                                                                        <input type="hidden" name="applicationId" value="${app.applicationId}"/>
                                                                        <input type="hidden" name="action" value="withdraw"/>
                                                                        <button type="submit" class="text-xs font-bold text-slate-600 hover:underline">${language == 'zh' ? '撤回申请' : 'Withdraw'}</button>
                                                                    </form>
                                                                </c:if>
                                                                <c:if test="${userRole == 'TA' && not empty app.vacancyId && not empty app.resumeId && app.status != 'Withdrawn'}">
                                                                    <button type="button"
                                                                            class="inline-flex items-center gap-1 text-xs font-bold text-sky-700 hover:text-sky-900 disabled:opacity-40 disabled:pointer-events-none"
                                                                            data-vacancy-id="${app.vacancyId}"
                                                                            data-resume-id="${app.resumeId}"
                                                                            ${qwenConfigured ? '' : 'disabled="disabled"'}
                                                                            onclick="openTaMotivationDisclaimer(this.dataset.vacancyId, this.dataset.resumeId)">
                                                                        <span class="material-symbols-outlined text-[14px]" style="font-variation-settings:'FILL' 1">edit_note</span>
                                                                        ${language == 'zh' ? 'AI 动机草稿' : 'AI motivation draft'}
                                                                    </button>
                                                                </c:if>
                                                                </div>
>>>>>>> Stashed changes
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                        </tbody>
                                    </table>
                                </div>

                                <c:if test="${not empty applications}">
                                    <div class="px-6 py-4 bg-slate-50 border-t border-slate-200">
                                        <p class="text-sm text-slate-500">Showing <strong class="text-slate-800">${fn:length(applications)}</strong> application<c:if test="${fn:length(applications) != 1}">s</c:if></p>
                                    </div>
                                </c:if>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <!-- Informational Banner -->
                    <div class="portal-callout flex gap-4">
                        <div class="text-primary">
                            <span class="material-symbols-outlined">info</span>
                        </div>
                        <div>
                            <h4 class="text-sm font-bold text-slate-900">Expected Response Times</h4>
                            <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                                Most departments respond within 10-14 business days. If your status has been "Under Review" for more than 3 weeks, you can contact the department coordinator through the messaging tab.
                            </p>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
