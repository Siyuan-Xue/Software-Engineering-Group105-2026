<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${language == 'zh' ? '申请记录 - QM HIRE' : 'Applications - QM HIRE'}</title>
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
    <style>
        .qm-modal-overlay {
            display: none;
            position: fixed; inset: 0;
            background: rgba(15,23,42,0.55);
            align-items: center; justify-content: center;
            z-index: 9999; padding: 1rem;
        }
        .qm-modal-overlay.open { display: flex; }
        .qm-modal-box {
            background: #fff; border-radius: 1.5rem;
            box-shadow: 0 32px 64px -24px rgba(15,23,42,0.4);
            width: 100%; max-height: 90vh; overflow: hidden;
            display: flex; flex-direction: column;
        }
        .ai-prose h2 { font-size:1rem; font-weight:700; color:#1e293b; margin:1rem 0 .35rem; }
        .ai-prose ul { list-style:disc; padding-left:1.2rem; margin:.4rem 0; }
        .ai-prose li { margin:.25rem 0; color:#475569; line-height:1.55; }
        .ai-prose p { color:#475569; line-height:1.6; margin:.35rem 0; }
    </style>
</head>
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
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
                            <c:choose>
                                <c:when test="${userRole == 'MO'}">
                                    <h2 class="portal-page-title">${language == 'zh' ? '收到的申请' : 'Received Applications'}</h2>
                                    <p class="portal-page-copy">${language == 'zh' ? '查看投向你所发布岗位的申请。可使用「AI 推荐排序」对同一岗位的多名申请者生成浏览顺序建议；「AI 决策建议」针对单笔申请。使用前须阅读免责说明并点击同意；未配置 QWEN_API_KEY 时相关按钮不可用。' : 'Applications to your vacancies. Use “AI rank applicants” for suggested browse order when multiple people applied to the same role; “AI decision hints” per application. Read the notice and click agree before each call; without QWEN_API_KEY the buttons stay disabled.'}</p>
                                </c:when>
                                <c:otherwise>
                                    <h2 class="portal-page-title">${language == 'zh' ? '我的申请' : 'My Applications'}</h2>
                                    <p class="portal-page-copy">${language == 'zh' ? '跟踪你已提交的课程与项目申请进度。使用「AI 动机草稿」前须先阅读并同意隐私与免责说明。' : 'Track your submitted applications. Before using “AI motivation draft”, read and accept the privacy & disclaimer.'}</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <a href="${pageContext.request.contextPath}/vacancies" class="portal-btn portal-btn-primary">
                            <span class="material-symbols-outlined text-sm">search</span>
                            ${language == 'zh' ? '浏览岗位' : 'Browse Vacancies'}
                        </a>
                    </div>

                    <c:if test="${not qwenConfigured}">
                        <div class="mb-4 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
                            <span class="font-bold">${language == 'zh' ? 'AI 功能未启用' : 'AI features disabled'}</span> —
                            ${language == 'zh' ? '服务器未配置 QWEN_API_KEY，以下「AI」按钮将不可用。详见 README「Qwen API Key 配置指南」。' : 'QWEN_API_KEY is not set on the server; AI buttons below stay disabled. See README “Qwen API Key 配置指南”.'}
                        </div>
                    </c:if>

                    <!-- Search and Filters -->
                    <form action="${pageContext.request.contextPath}/applications" method="GET" class="portal-filter-bar">
                        <div class="flex flex-col lg:flex-row gap-4">
                            <div class="flex-1">
                                <label class="flex flex-col w-full">
                                    <div class="flex w-full items-center rounded-lg bg-slate-100 px-4 h-11 border border-transparent focus-within:border-primary/30 transition-all">
                                        <span class="material-symbols-outlined text-slate-400">search</span>
                                        <input type="text" name="keyword" value="${param.keyword}" class="w-full bg-transparent border-none focus:ring-0 text-slate-900 placeholder:text-slate-400 text-sm font-medium pl-3 outline-none" placeholder="${language == 'zh' ? '按课程名或院系搜索...' : 'Search by module name or department...'}" />
                                    </div>
                                </label>
                            </div>
                            <div class="flex flex-wrap gap-2">
                                <div class="relative group">
                                    <select name="status" class="flex h-11 items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors appearance-none pr-8">
                                        <option value="">${language == 'zh' ? '状态：全部' : 'Status: All'}</option>
                                        <option value="Submitted" ${param.status == 'Submitted' ? 'selected' : ''}>${language == 'zh' ? '已提交' : 'Submitted'}</option>
                                        <option value="Under Review" ${param.status == 'Under Review' ? 'selected' : ''}>${language == 'zh' ? '审核中' : 'Under Review'}</option>
                                        <option value="Offer Pending" ${param.status == 'Offer Pending' ? 'selected' : ''}>${language == 'zh' ? '待确认录用' : 'Offer Pending'}</option>
                                        <option value="Accepted" ${param.status == 'Accepted' ? 'selected' : ''}>${language == 'zh' ? '已录用' : 'Accepted'}</option>
                                        <option value="Rejected" ${param.status == 'Rejected' ? 'selected' : ''}>${language == 'zh' ? '已拒绝' : 'Rejected'}</option>
                                        <option value="Declined" ${param.status == 'Declined' ? 'selected' : ''}>${language == 'zh' ? '已拒绝录用' : 'Declined'}</option>
                                        <option value="Withdrawn" ${param.status == 'Withdrawn' ? 'selected' : ''}>${language == 'zh' ? '已撤回' : 'Withdrawn'}</option>
                                    </select>
                                    <span class="material-symbols-outlined text-lg absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-500">expand_more</span>
                                </div>
                                <div class="relative group">
                                    <select name="date" class="flex h-11 items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors appearance-none pr-8">
                                        <option value="latest" ${param.date == 'latest' || empty param.date ? 'selected' : ''}>${language == 'zh' ? '日期：最新' : 'Date: Latest'}</option>
                                        <option value="oldest" ${param.date == 'oldest' ? 'selected' : ''}>${language == 'zh' ? '日期：最早' : 'Date: Oldest'}</option>
                                    </select>
                                    <span class="material-symbols-outlined text-lg absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-500">calendar_today</span>
                                </div>
                                <button type="submit" class="flex h-11 items-center gap-2 rounded-lg bg-slate-100 px-4 text-sm font-bold text-slate-500 hover:text-primary transition-colors">
                                    <span class="material-symbols-outlined text-lg">filter_list</span>
                                    ${language == 'zh' ? '筛选' : 'Filter'}
                                </button>
                            </div>
                        </div>
                    </form>

                    <c:if test="${userRole == 'MO' && not empty moRankJobOptions}">
                        <div class="mb-5 rounded-2xl border border-slate-200 bg-white p-4 flex flex-col sm:flex-row sm:items-end gap-4 shadow-sm">
                            <div class="flex-1 min-w-0">
                                <label for="moRankJobSelect" class="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-1">${language == 'zh' ? '同一岗位多名申请者 · AI 辅助排序' : 'Multiple applicants per vacancy · AI ranking aid'}</label>
                                <select id="moRankJobSelect" class="w-full max-w-xl rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm font-medium text-slate-800">
                                    <c:forEach items="${moRankJobOptions}" var="rj">
                                        <option value="${rj.jobId}"><c:out value="${rj.title}"/> · ${rj.applicantCount}</option>
                                    </c:forEach>
                                </select>
                                <p class="text-xs text-slate-500 mt-2">${language == 'zh' ? '排序仅供参考，不代替院系录用流程；须先阅读免责说明并点击同意。' : 'Ranking is indicative only; read the notice and click agree before the request runs.'}</p>
                            </div>
                            <button type="button" id="moRankAiBtn"
                                    class="portal-btn portal-btn-secondary shrink-0 ${qwenConfigured ? '' : 'opacity-50 cursor-not-allowed'}"
                                    ${qwenConfigured ? '' : 'disabled="disabled"'}
                                    onclick="openMoRankDisclaimer()">
                                <span class="material-symbols-outlined text-sm">sort</span>
                                ${language == 'zh' ? 'AI 推荐排序' : 'AI rank applicants'}
                            </button>
                        </div>
                    </c:if>

                    <!-- Applications Table -->
                    <c:choose>
                        <c:when test="${applicationsState == 'loadError'}">
                            <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                                <jsp:param name="variant" value="error" />
                                <jsp:param name="icon" value="work_history" />
                                <jsp:param name="title" value="${language == 'zh' ? '申请列表暂不可用' : 'Applications unavailable'}" />
                                <jsp:param name="message" value="${language == 'zh' ? '当前无法加载你的申请记录，请刷新页面后重试。' : 'Unable to load your application history right now. Please refresh the page and try again.'}" />
                                <jsp:param name="actionHref" value="${pageContext.request.contextPath}/applications" />
                                <jsp:param name="actionLabel" value="${language == 'zh' ? '重试' : 'Try Again'}" />
                            </jsp:include>
                        </c:when>
                        <c:otherwise>
                            <div class="portal-panel overflow-hidden">
                                <div class="overflow-x-auto">
                                    <table class="w-full text-left border-collapse">
                                        <thead>
                                            <tr class="bg-slate-50 border-b border-slate-200">
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">${language == 'zh' ? '课程名称' : 'Module Name'}</th>
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">${language == 'zh' ? '院系' : 'Department'}</th>
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">${language == 'zh' ? '提交日期' : 'Submitted Date'}</th>
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">${language == 'zh' ? '使用简历' : 'Resume Used'}</th>
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">${language == 'zh' ? '状态' : 'Status'}</th>
                                                <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500 text-right">${language == 'zh' ? '操作' : 'Actions'}</th>
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
                                                                        <jsp:param name="title" value="${language == 'zh' ? '还没有申请记录' : 'No applications yet'}" />
                                                                        <jsp:param name="message" value="${language == 'zh' ? '你还没有提交任何申请，可以先浏览开放岗位。' : 'You have not submitted any applications. Browse open vacancies to get started.'}" />
                                                                        <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                                                        <jsp:param name="actionLabel" value="${language == 'zh' ? '浏览岗位' : 'Browse Vacancies'}" />
                                                                    </jsp:include>
                                                                </c:when>
                                                                <c:when test="${applicationsFilterNoResults}">
                                                                    <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                                        <jsp:param name="icon" value="filter_alt_off" />
                                                                        <jsp:param name="title" value="${language == 'zh' ? '没有符合筛选条件的申请' : 'No applications match your filters'}" />
                                                                        <jsp:param name="message" value="${language == 'zh' ? '请尝试清除筛选条件或调整搜索内容。' : 'Try clearing filters or adjusting your search to see more results.'}" />
                                                                        <jsp:param name="actionHref" value="${pageContext.request.contextPath}/applications" />
                                                                        <jsp:param name="actionLabel" value="${language == 'zh' ? '清除筛选' : 'Clear filters'}" />
                                                                    </jsp:include>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                                        <jsp:param name="icon" value="inbox" />
                                                                        <jsp:param name="title" value="${language == 'zh' ? '还没有申请记录' : 'No applications yet'}" />
                                                                        <jsp:param name="message" value="${language == 'zh' ? '你还没有提交任何申请，可以先浏览开放岗位。' : 'You have not submitted any applications. Browse open vacancies to get started.'}" />
                                                                        <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                                                        <jsp:param name="actionLabel" value="${language == 'zh' ? '浏览岗位' : 'Browse Vacancies'}" />
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
                                                                            ${language == 'zh' ? '已录用' : 'Accepted'}
                                                                        </span>
                                                                    </c:when>
                                                                    <c:when test="${app.status == 'Offer Pending'}">
                                                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-100 text-amber-700">
                                                                            ${language == 'zh' ? '待确认录用' : 'Offer Pending'}
                                                                        </span>
                                                                    </c:when>
                                                                    <c:when test="${app.status == 'Rejected'}">
                                                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-red-100 text-red-700">
                                                                            ${language == 'zh' ? '已拒绝' : 'Rejected'}
                                                                        </span>
                                                                    </c:when>
                                                                    <c:when test="${app.status == 'Declined'}">
                                                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-slate-200 text-slate-700">
                                                                            ${language == 'zh' ? '已拒绝录用' : 'Declined'}
                                                                        </span>
                                                                    </c:when>
                                                                    <c:when test="${app.status == 'Withdrawn'}">
                                                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-slate-200 text-slate-700">
                                                                            ${language == 'zh' ? '已撤回' : 'Withdrawn'}
                                                                        </span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold ${app.status == 'Submitted' ? 'bg-indigo-100 text-indigo-700' : 'bg-blue-100 text-blue-700'}">
                                                                            <c:choose>
                                                                                <c:when test="${app.status == 'Under Review'}">${language == 'zh' ? '审核中' : 'Under Review'}</c:when>
                                                                                <c:when test="${app.status == 'Submitted'}">${language == 'zh' ? '已提交' : 'Submitted'}</c:when>
                                                                                <c:otherwise><c:out value="${app.status}"/></c:otherwise>
                                                                            </c:choose>
                                                                        </span>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td class="px-6 py-5 text-right">
                                                                <div class="flex flex-col items-end gap-2">
                                                                <c:choose>
                                                                    <c:when test="${not empty app.vacancyId}">
                                                                        <a href="${pageContext.request.contextPath}/vacancy?vacancyId=${app.vacancyId}" class="text-sm font-bold text-primary hover:underline">${language == 'zh' ? '查看岗位' : 'View vacancy'}</a>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <a href="${pageContext.request.contextPath}/vacancies" class="text-sm font-bold text-slate-600 hover:text-primary hover:underline">${language == 'zh' ? '浏览岗位' : 'Browse vacancies'}</a>
                                                                    </c:otherwise>
                                                                </c:choose>
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
                                        <p class="text-sm text-slate-500">
                                            <c:choose>
                                                <c:when test="${language == 'zh'}">共显示 <strong class="text-slate-800">${fn:length(applications)}</strong> 条申请</c:when>
                                                <c:otherwise>Showing <strong class="text-slate-800">${fn:length(applications)}</strong> application<c:if test="${fn:length(applications) != 1}">s</c:if></c:otherwise>
                                            </c:choose>
                                        </p>
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
                            <h4 class="text-sm font-bold text-slate-900">${language == 'zh' ? '预计回复时间' : 'Expected Response Times'}</h4>
                            <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                                ${language == 'zh' ? '大多数院系会在 10-14 个工作日内回复。如果你的状态“审核中”超过 3 周，可以通过消息页面联系院系协调人。' : 'Most departments respond within 10-14 business days. If your status has been "Under Review" for more than 3 weeks, you can contact the department coordinator through the messaging tab.'}
                            </p>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <%-- MO: AI decision hints disclaimer --%>
    <div id="moAdviceDisc" class="qm-modal-overlay" onclick="if(event.target===this)closeMoAdviceDisc()">
        <div class="qm-modal-box max-w-lg" style="max-height:90vh">
            <div class="px-7 pt-7 pb-5 overflow-y-auto">
                <div class="flex items-start gap-4 mb-4">
                    <div class="w-12 h-12 rounded-2xl bg-violet-100 flex items-center justify-center shrink-0">
                        <span class="material-symbols-outlined text-2xl text-violet-600" style="font-variation-settings:'FILL' 1">policy</span>
                    </div>
                    <div>
                        <h2 class="text-xl font-bold text-slate-900">${language == 'zh' ? 'AI 决策建议 — 使用须知' : 'AI decision hints — please read'}</h2>
                        <p class="text-sm text-slate-500 mt-0.5">${language == 'zh' ? '继续前请确认你理解以下条款' : 'Confirm you understand the following before continuing'}</p>
                    </div>
                </div>
                <div class="bg-amber-50 border border-amber-200 rounded-2xl p-4 mb-4 text-sm text-amber-900 leading-relaxed">
                    <ul class="list-disc pl-4 space-y-1.5">
                        <li>${language == 'zh' ? '系统将把你的岗位说明、标签及该申请的简历摘要、求职信片段发送给第三方大模型（Qwen）进行分析。' : 'The service will send vacancy text, labels, and applicant resume/cover-letter excerpts to a third-party model (Qwen) for analysis.'}</li>
                        <li>${language == 'zh' ? '输出仅为参考，不构成录用、法律或合规意见；最终录用决定须由你方按院系政策作出。' : 'Output is guidance only — not hiring, legal, or compliance advice. Hiring decisions remain yours under institutional policy.'}</li>
                        <li>${language == 'zh' ? '模型可能产生错误或偏见，请勿单独依赖 AI 结果。' : 'Models may err or show bias; do not rely on AI output alone.'}</li>
                    </ul>
                </div>
                <p class="text-sm text-slate-500">${language == 'zh' ? '法律与隐私上的接受以本弹窗及下方确认按钮为准；AI 返回正文不构成对该接受的补充或替代。点击「同意并使用」即表示你已阅读并接受上述说明。' : 'Legal/privacy acceptance is this dialog plus the confirm button below; AI text does not supplement or replace it. By clicking “I agree & run” you confirm you accept the above.'}</p>
            </div>
            <div class="px-7 py-5 bg-slate-50 border-t border-slate-100 flex justify-end gap-3 shrink-0">
                <button type="button" onclick="closeMoAdviceDisc()" class="portal-btn portal-btn-secondary">${language == 'zh' ? '取消' : 'Cancel'}</button>
                <button type="button" onclick="agreeMoAdvice()" class="portal-btn portal-btn-primary">
                    <span class="material-symbols-outlined text-sm">auto_awesome</span>
                    ${language == 'zh' ? '同意并使用' : 'I agree & run'}
                </button>
            </div>
        </div>
    </div>

    <c:if test="${userRole == 'MO'}">
    <div id="moRankDisc" class="qm-modal-overlay" onclick="if(event.target===this)closeMoRankDisc()">
        <div class="qm-modal-box max-w-lg" style="max-height:90vh">
            <div class="px-7 pt-7 pb-5 overflow-y-auto">
                <div class="flex items-start gap-4 mb-4">
                    <div class="w-12 h-12 rounded-2xl bg-indigo-100 flex items-center justify-center shrink-0">
                        <span class="material-symbols-outlined text-2xl text-indigo-600" style="font-variation-settings:'FILL' 1">sort</span>
                    </div>
                    <div>
                        <h2 class="text-xl font-bold text-slate-900">${language == 'zh' ? 'AI 申请者排序 — 使用须知' : 'AI applicant ranking — please read'}</h2>
                        <p class="text-sm text-slate-500 mt-0.5">${language == 'zh' ? '对同一岗位的多名申请者生成浏览顺序建议' : 'Suggested browse order for multiple applicants to one vacancy'}</p>
                    </div>
                </div>
                <div class="bg-amber-50 border border-amber-200 rounded-2xl p-4 mb-4 text-sm text-amber-900 leading-relaxed">
                    <ul class="list-disc pl-4 space-y-1.5">
                        <li>${language == 'zh' ? '将把该岗位说明及每位申请者的简历/求职信摘要发往 Qwen，用于生成排序与简短理由。' : 'Vacancy text and each applicant’s resume/cover-letter excerpts are sent to Qwen for ordering and short notes.'}</li>
                        <li>${language == 'zh' ? '排序不构成录用或拒绝决定，院系政策与面试材料仍为准。' : 'Order is not an offer or rejection; policy and interviews prevail.'}</li>
                        <li>${language == 'zh' ? '法律与隐私接受以本弹窗及确认按钮为准。' : 'Legal/privacy acceptance is this dialog and the confirm button.'}</li>
                    </ul>
                </div>
                <p class="text-sm text-slate-500">${language == 'zh' ? '点击「同意并排序」后才会发起请求。' : 'The request runs only after you click “I agree & rank”.'}</p>
            </div>
            <div class="px-7 py-5 bg-slate-50 border-t border-slate-100 flex justify-end gap-3 shrink-0">
                <button type="button" onclick="closeMoRankDisc()" class="portal-btn portal-btn-secondary">${language == 'zh' ? '取消' : 'Cancel'}</button>
                <button type="button" onclick="agreeMoRankFetch()" class="portal-btn portal-btn-primary">
                    <span class="material-symbols-outlined text-sm">sort</span>
                    ${language == 'zh' ? '同意并排序' : 'I agree & rank'}
                </button>
            </div>
        </div>
    </div>
    </c:if>

    <%-- TA: motivation draft disclaimer --%>
    <div id="taMotivationDisc" class="qm-modal-overlay" onclick="if(event.target===this)closeTaMotivationDisc()">
        <div class="qm-modal-box max-w-lg" style="max-height:90vh">
            <div class="px-7 pt-7 pb-5 overflow-y-auto">
                <div class="flex items-start gap-4 mb-4">
                    <div class="w-12 h-12 rounded-2xl bg-sky-100 flex items-center justify-center shrink-0">
                        <span class="material-symbols-outlined text-2xl text-sky-600" style="font-variation-settings:'FILL' 1">policy</span>
                    </div>
                    <div>
                        <h2 class="text-xl font-bold text-slate-900">${language == 'zh' ? 'AI 动机草稿 — 使用须知' : 'AI motivation draft — please read'}</h2>
                        <p class="text-sm text-slate-500 mt-0.5">${language == 'zh' ? '继续前请确认你理解以下条款' : 'Confirm you understand the following before continuing'}</p>
                    </div>
                </div>
                <div class="bg-amber-50 border border-amber-200 rounded-2xl p-4 mb-4 text-sm text-amber-900 leading-relaxed">
                    <ul class="list-disc pl-4 space-y-1.5">
                        <li>${language == 'zh' ? '系统将把你所选简历的字段及目标岗位描述发送给第三方大模型（Qwen）以生成可编辑草稿。' : 'The service sends your selected resume fields and the target vacancy text to a third-party model (Qwen) to generate an editable draft.'}</li>
                        <li>${language == 'zh' ? '草稿仅供参考，你须自行核对事实、语气与院系要求后再提交申请。' : 'The draft is for reference; you must verify facts, tone, and requirements before submitting.'}</li>
                        <li>${language == 'zh' ? '模型可能产生不准确内容（幻觉），请勿未经审阅直接粘贴。' : 'Models may hallucinate; do not paste without reviewing.'}</li>
                    </ul>
                </div>
                <p class="text-sm text-slate-500">${language == 'zh' ? '法律与隐私上的接受以本弹窗及下方确认按钮为准；AI 返回正文不构成对该接受的补充或替代。点击「同意并生成」即表示你已阅读并接受上述说明。' : 'Legal/privacy acceptance is this dialog plus the confirm button below; AI text does not supplement or replace it. By clicking “I agree & generate” you confirm you accept the above.'}</p>
            </div>
            <div class="px-7 py-5 bg-slate-50 border-t border-slate-100 flex justify-end gap-3 shrink-0">
                <button type="button" onclick="closeTaMotivationDisc()" class="portal-btn portal-btn-secondary">${language == 'zh' ? '取消' : 'Cancel'}</button>
                <button type="button" onclick="agreeTaMotivation()" class="portal-btn portal-btn-primary">
                    <span class="material-symbols-outlined text-sm">edit_note</span>
                    ${language == 'zh' ? '同意并生成' : 'I agree & generate'}
                </button>
            </div>
        </div>
    </div>

    <%-- Shared AI result --%>
    <div id="appsAiResult" class="qm-modal-overlay" onclick="if(event.target===this)closeAppsAiResult()">
        <div class="qm-modal-box max-w-2xl" style="max-height:90vh">
            <div class="px-7 pt-6 pb-4 border-b border-slate-100 flex items-center justify-between shrink-0">
                <h2 id="appsAiResultTitle" class="text-xl font-bold text-slate-900">${language == 'zh' ? 'AI 结果' : 'AI result'}</h2>
                <button type="button" onclick="closeAppsAiResult()" class="text-slate-400 hover:text-slate-600 p-1">
                    <span class="material-symbols-outlined text-2xl">close</span>
                </button>
            </div>
            <div id="appsAiLoading" class="p-8 text-center text-sm text-slate-500" style="display:none">
                <div class="inline-block w-8 h-8 border-4 border-violet-500 border-t-transparent rounded-full animate-spin mb-3"></div>
                <p>${language == 'zh' ? '正在请求 AI，请稍候…' : 'Contacting AI, please wait…'}</p>
            </div>
            <div id="appsAiError" class="p-8 text-center text-sm text-red-700" style="display:none"></div>
            <div id="appsAiBody" class="ai-prose px-7 py-6 overflow-y-auto" style="display:none; max-height:60vh"></div>
            <div id="appsAiRankBody" class="px-7 py-6 overflow-y-auto text-sm" style="display:none; max-height:60vh"></div>
            <div class="px-7 py-4 bg-slate-50 border-t border-slate-100 shrink-0">
                <button type="button" onclick="closeAppsAiResult()" class="portal-btn portal-btn-secondary text-sm">${language == 'zh' ? '关闭' : 'Close'}</button>
            </div>
        </div>
    </div>

    <script>
        var CTX = '${pageContext.request.contextPath}';
        var QWEN_OK = ${qwenConfigured ? 'true' : 'false'};
        var _pendingMoApplicationId = null;
        var _pendingTaVacancyId = null;
        var _pendingTaResumeId = null;

        function openMoAdviceDisclaimer(applicationId) {
            if (!QWEN_OK) {
                alert('${language == 'zh' ? '未配置 QWEN_API_KEY，无法使用 AI。请在 Tomcat 的 setenv.bat / setenv.sh 或系统环境中配置后重启服务。详见 README。' : 'QWEN_API_KEY is not configured. Set it in Tomcat setenv or the environment and restart. See README.'}');
                return;
            }
            _pendingMoApplicationId = applicationId;
            document.getElementById('moAdviceDisc').classList.add('open');
        }
        function closeMoAdviceDisc() {
            document.getElementById('moAdviceDisc').classList.remove('open');
        }
        function openTaMotivationDisclaimer(vacancyId, resumeId) {
            if (!QWEN_OK) {
                alert('${language == 'zh' ? '未配置 QWEN_API_KEY，无法使用 AI。请在 Tomcat 的 setenv.bat / setenv.sh 或系统环境中配置后重启服务。详见 README。' : 'QWEN_API_KEY is not configured. Set it in Tomcat setenv or the environment and restart. See README.'}');
                return;
            }
            _pendingTaVacancyId = vacancyId;
            _pendingTaResumeId = resumeId;
            document.getElementById('taMotivationDisc').classList.add('open');
        }
        function closeTaMotivationDisc() {
            document.getElementById('taMotivationDisc').classList.remove('open');
        }

        function openAppsAiResult(title) {
            document.getElementById('appsAiResultTitle').textContent = title || '${language == 'zh' ? 'AI 结果' : 'AI result'}';
            document.getElementById('appsAiLoading').style.display = 'block';
            document.getElementById('appsAiError').textContent = '';
            document.getElementById('appsAiError').style.display = 'none';
            document.getElementById('appsAiBody').style.display = 'none';
            document.getElementById('appsAiBody').innerHTML = '';
            var rk = document.getElementById('appsAiRankBody');
            if (rk) { rk.style.display = 'none'; rk.innerHTML = ''; }
            document.getElementById('appsAiResult').classList.add('open');
        }
        function closeAppsAiResult() {
            document.getElementById('appsAiResult').classList.remove('open');
        }

        function escapeHtml(s) {
            return String(s == null ? '' : s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\"/g,'&quot;');
        }

        function openMoRankDisclaimer() {
            if (!QWEN_OK) {
                alert('${language == 'zh' ? '未配置 QWEN_API_KEY，无法使用 AI。请在 Tomcat 的 setenv.bat / setenv.sh 或系统环境中配置后重启服务。详见 README。' : 'QWEN_API_KEY is not configured. Set it in Tomcat setenv or the environment and restart. See README.'}');
                return;
            }
            var disc = document.getElementById('moRankDisc');
            if (!disc) return;
            var sel = document.getElementById('moRankJobSelect');
            if (!sel || !sel.value) return;
            disc.classList.add('open');
        }
        function closeMoRankDisc() {
            var m = document.getElementById('moRankDisc');
            if (m) m.classList.remove('open');
        }
        function agreeMoRankFetch() {
            closeMoRankDisc();
            var sel = document.getElementById('moRankJobSelect');
            if (!sel || !sel.value) return;
            openAppsAiResult('${language == 'zh' ? 'AI 申请者排序' : 'AI applicant ranking'}');
            var params = new URLSearchParams();
            params.append('jobId', sel.value);
            fetch(CTX + '/ai-mo-applicants-rank', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            }).then(function(r) {
                return r.json().then(function(data) { return { okHttp: r.ok, status: r.status, data: data }; });
            }).then(function(res) {
                document.getElementById('appsAiLoading').style.display = 'none';
                var data = res.data || {};
                if (!res.okHttp || !data.ok) {
                    var msg = data.error || ('HTTP ' + res.status + ' ${language == 'zh' ? '（请检查是否已配置 QWEN_API_KEY 或查看 Tomcat 日志）' : '(check QWEN_API_KEY or Tomcat logs)'}');
                    document.getElementById('appsAiError').textContent = msg;
                    document.getElementById('appsAiError').style.display = 'block';
                    return;
                }
                showMoRankResults(data);
            }).catch(function(e) {
                document.getElementById('appsAiLoading').style.display = 'none';
                document.getElementById('appsAiError').textContent = '${language == 'zh' ? '网络或解析错误：' : 'Network error: '}' + e.message;
                document.getElementById('appsAiError').style.display = 'block';
            });
        }
        function showMoRankResults(data) {
            var rankEl = document.getElementById('appsAiRankBody');
            var body = document.getElementById('appsAiBody');
            body.style.display = 'none';
            var vt = data.vacancyTitle || '';
            document.getElementById('appsAiResultTitle').textContent = '${language == 'zh' ? 'AI 申请者排序' : 'AI applicant ranking'}' + (vt ? (' — ' + vt) : '');
            var intro = '${language == 'zh' ? '以下为模型建议的浏览顺序与匹配分，请结合材料与面试自行决定。' : 'Suggested order and fit scores from the model — decide using materials and interviews.'}';
            var html = '<p class="text-sm text-slate-500 mb-4">' + escapeHtml(intro) + '</p><ol class="list-decimal pl-5 space-y-4 text-slate-700">';
            (data.rankings || []).forEach(function(r) {
                var sc = (typeof r.fitScore === 'number' && r.fitScore >= 0) ? (r.fitScore + '/100') : '—';
                html += '<li><div class="font-bold text-slate-900">#' + r.rank + ' <span class="text-violet-700">' + escapeHtml(String(sc)) + '</span></div>';
                html += '<div class="text-sm text-slate-600 mt-1">' + escapeHtml(r.note || '') + '</div>';
                html += '<div class="text-xs text-slate-400 mt-0.5 font-mono break-all">' + escapeHtml(r.applicationId || '') + '</div></li>';
            });
            html += '</ol>';
            rankEl.innerHTML = html;
            rankEl.style.display = 'block';
        }

        function agreeMoAdvice() {
            closeMoAdviceDisc();
            if (!_pendingMoApplicationId) return;
            openAppsAiResult('${language == 'zh' ? 'AI 决策建议' : 'AI decision hints'}');
            var params = new URLSearchParams();
            params.append('applicationId', _pendingMoApplicationId);
            fetch(CTX + '/ai-mo-application-advice', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            }).then(function(r) {
                return r.json().then(function(data) { return { okHttp: r.ok, status: r.status, data: data }; });
            }).then(function(res) {
                document.getElementById('appsAiLoading').style.display = 'none';
                var data = res.data || {};
                if (!res.okHttp || !data.ok) {
                    document.getElementById('appsAiError').textContent = data.error || ('HTTP ' + res.status);
                    document.getElementById('appsAiError').style.display = 'block';
                    return;
                }
                document.getElementById('appsAiBody').innerHTML = mdToHtml(data.markdown || '');
                document.getElementById('appsAiBody').style.display = 'block';
            }).catch(function(e) {
                document.getElementById('appsAiLoading').style.display = 'none';
                document.getElementById('appsAiError').textContent = '${language == 'zh' ? '网络错误：' : 'Network error: '}' + e.message;
                document.getElementById('appsAiError').style.display = 'block';
            });
        }

        function agreeTaMotivation() {
            closeTaMotivationDisc();
            if (!_pendingTaVacancyId || !_pendingTaResumeId) return;
            openAppsAiResult('${language == 'zh' ? 'AI 动机草稿' : 'AI motivation draft'}');
            var params = new URLSearchParams();
            params.append('vacancyId', _pendingTaVacancyId);
            params.append('resumeId', _pendingTaResumeId);
            fetch(CTX + '/ai-ta-cover-letter', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            }).then(function(r) {
                return r.json().then(function(data) { return { okHttp: r.ok, status: r.status, data: data }; });
            }).then(function(res) {
                document.getElementById('appsAiLoading').style.display = 'none';
                var data = res.data || {};
                if (!res.okHttp || !data.ok) {
                    document.getElementById('appsAiError').textContent = data.error || ('HTTP ' + res.status);
                    document.getElementById('appsAiError').style.display = 'block';
                    return;
                }
                document.getElementById('appsAiBody').innerHTML = mdToHtml(data.markdown || '');
                document.getElementById('appsAiBody').style.display = 'block';
            }).catch(function(e) {
                document.getElementById('appsAiLoading').style.display = 'none';
                document.getElementById('appsAiError').textContent = '${language == 'zh' ? '网络错误：' : 'Network error: '}' + e.message;
                document.getElementById('appsAiError').style.display = 'block';
            });
        }

        function mdToHtml(text) {
            if (!text) return '';
            return String(text)
                .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
                .replace(/^[#]{1,3}\s+(.+)$/gm,'<h2>$1</h2>')
                .replace(/\*\*(.+?)\*\*/g,'<strong>$1</strong>')
                .replace(/^[-•*]\s+(.+)$/gm,'<li>$1</li>')
                .replace(/^\d+\.\s+(.+)$/gm,'<li>$1</li>')
                .replace(/(<li>[^]*?<\/li>\n?)+/g, function(m){ return '<ul>'+m+'</ul>'; })
                .replace(/\n{2,}/g,'</p><p>')
                .replace(/^(?!<[hup])(.+)$/gm,'<p>$1</p>');
        }
    </script>
</body>
</html>
