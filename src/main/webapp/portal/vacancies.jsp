<%--
  Vacancies 主列表页（前后端联调核心页之一）：列表数据由后端放入 request attribute `vacancies`，
  本页用 JSTL/EL 做动态渲染，便于与contract中的 keyword / department / term / userRole 字段对齐。
  角色相关按钮仅在视图层按 userRole 条件展示；最终能否发布/申请仍以后端校验为准。
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${language == 'zh' ? '岗位列表 - QM HIRE' : 'Vacancies - QM HIRE'}</title>
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
                    fontFamily: { sans: ['Inter', 'sans-serif'] }
                }
            }
        }
    </script>
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
    <style>
        /* ── modal overlay ───────────────────────────────────────────── */
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
            width: 100%; overflow: hidden;
            animation: modalPop .28s cubic-bezier(.34,1.4,.64,1) both;
        }
        @keyframes modalPop {
            from { transform: scale(0.92) translateY(16px); opacity:0; }
            to   { transform: scale(1)    translateY(0);    opacity:1; }
        }

        /* ── favourite button ─────────────────────────────────────────── */
        .fav-btn { transition: transform .15s, color .15s; }
        .fav-btn:hover { transform: scale(1.2); }
        .fav-btn.saved .material-symbols-outlined { font-variation-settings: 'FILL' 1; color: #ef4444; }
        .fav-btn.saved:hover .material-symbols-outlined { color: #b91c1c; }
        .fav-btn:not(.saved) .material-symbols-outlined { color: #94a3b8; }
        .fav-btn:not(.saved):hover .material-symbols-outlined { color: #ef4444; }

        /* ── card animation ──────────────────────────────────────────── */
        .vacancy-card { animation: fadeSlideUp .25s ease both; }
        @keyframes fadeSlideUp {
            from { opacity: 0; transform: translateY(8px); }
            to   { opacity: 1; transform: translateY(0); }
        }

        /* ── AI match badge ──────────────────────────────────────────── */
        .ai-badge {
            display: inline-flex; align-items: center; gap: 3px;
            font-size: 11px; font-weight: 700; letter-spacing: .3px;
            padding: 3px 8px; border-radius: 99px;
            transition: opacity .3s;
            white-space: nowrap;
        }
        .ai-badge-skeleton {
            width: 56px; height: 22px; border-radius: 99px;
            background: linear-gradient(90deg,#f1f5f9 25%,#e2e8f0 50%,#f1f5f9 75%);
            background-size: 200% 100%;
            animation: shimmer 1.2s infinite;
        }
        @keyframes shimmer { from{background-position:200% 0} to{background-position:-200% 0} }
        .score-excellent { background:#dcfce7; color:#16a34a; }
        .score-good      { background:#dbeafe; color:#2563eb; }
        .score-fair      { background:#fef9c3; color:#ca8a04; }
        .score-low       { background:#fee2e2; color:#dc2626; }
        .score-unknown   { background:#f1f5f9; color:#64748b; }

        /* ── AI match bar (page-level) ───────────────────────────────── */
        #aiMatchBanner {
            background: linear-gradient(135deg,#1e293b 0%,#334155 100%);
        }
    </style>
</head>
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
<div class="relative flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="portal-page">

                <!-- Page Header -->
                <div class="portal-page-header mb-2">
                    <div>
                        <h2 class="portal-page-title">${language == 'zh' ? '可申请岗位' : 'Available Vacancies'}</h2>
                        <p class="portal-page-copy">${language == 'zh' ? '浏览并申请各院系开放的助教岗位。' : 'Browse and apply for open Teaching Assistant positions across departments.'}</p>
                    </div>
                    <div class="flex gap-3">
                        <%-- MO：发布入口；TA：列表增强能力。仅展示层区分角色，权限以服务端为准。 --%>
                        <c:if test="${userRole == 'MO'}">
                            <button class="portal-btn portal-btn-primary" title="${language == 'zh' ? '发布新岗位' : 'Post a new vacancy'}" onclick="openCreateVacancyModal()">
                                <span class="material-symbols-outlined text-sm">add</span>
                                ${language == 'zh' ? '发布岗位' : 'Post Vacancy'}
                            </button>
                        </c:if>
                        <c:if test="${userRole == 'TA'}">
                            <button id="aiMatchBtn"
                                    onclick="openAIMatchDisclaimer()"
                                    class="portal-btn portal-btn-secondary"
                                    title="${language == 'zh' ? '使用 AI 评估你的简历与各岗位的匹配度' : 'Use AI to score how well your resume matches each vacancy'}">
                                <span class="material-symbols-outlined text-sm">auto_awesome</span>
                                ${language == 'zh' ? 'AI 匹配' : 'AI Match'}
                            </button>
                            <button class="portal-btn portal-btn-secondary" title="${language == 'zh' ? '有新岗位发布时接收提醒' : 'Get notified when new vacancies are posted'}">
                                <span class="material-symbols-outlined text-sm">notifications_active</span>
                                ${language == 'zh' ? '岗位提醒' : 'Job Alerts'}
                            </button>
                        </c:if>
                    </div>
                </div>

                <!-- AI Match Banner (shown while loading / after results) -->
                <div id="aiMatchBanner" style="display:none" class="rounded-2xl text-white px-5 py-3.5 mb-5 flex items-center gap-3 text-sm">
                    <span class="material-symbols-outlined text-lg shrink-0" style="FILL:1">auto_awesome</span>
                    <span id="aiMatchBannerText" class="flex-1"></span>
                    <button onclick="clearAIScores()" class="text-white/60 hover:text-white transition-colors">
                        <span class="material-symbols-outlined text-base">close</span>
                    </button>
                </div>

                <%-- GET 筛选：keyword / department / term 与接口契约一致，便于 Servlet 解析并与列表查询复用同一套参数名。 --%>
                <!-- Search and Filters -->
                <form id="filterForm" action="${pageContext.request.contextPath}/vacancies" method="GET" class="portal-filter-bar">
                    <input type="hidden" name="page" value="1">
                    <div class="flex flex-col lg:flex-row gap-4">
                        <div class="flex-1">
                            <label class="flex flex-col w-full">
                                <div class="flex w-full items-center rounded-lg bg-slate-100 px-4 h-11 border border-transparent focus-within:border-slate-300 transition-all">
                                    <span class="material-symbols-outlined text-slate-400">search</span>
                                    <input type="text" name="keyword" value="${fn:escapeXml(param.keyword)}"
                                           class="w-full bg-transparent border-none focus:ring-0 text-slate-900 placeholder:text-slate-400 text-sm font-medium pl-3 outline-none"
                                           placeholder="${language == 'zh' ? '按课程代码、标题或关键词搜索...' : 'Search by course code, title, or keywords...'}" />
                                </div>
                            </label>
                        </div>
                        <div class="flex flex-wrap gap-2">
                            <!-- Department -->
                            <div class="relative">
                                <select name="department" class="h-11 rounded-lg bg-white border border-slate-200 pl-4 pr-8 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors appearance-none">
                                    <option value="">${language == 'zh' ? '全部院系' : 'All Departments'}</option>
                                    <option value="CS"   ${param.department == 'CS'   ? 'selected' : ''}>${language == 'zh' ? '计算机' : 'Computer Science'}</option>
                                    <option value="MATH" ${param.department == 'MATH' ? 'selected' : ''}>${language == 'zh' ? '数学' : 'Mathematics'}</option>
                                </select>
                                <span class="material-symbols-outlined text-lg absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">expand_more</span>
                            </div>
                            <!-- Term (dynamic) -->
                            <div class="relative">
                                <select name="term" class="h-11 rounded-lg bg-white border border-slate-200 pl-4 pr-8 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors appearance-none">
                                    <option value="">${language == 'zh' ? '全部学期' : 'All Terms'}</option>
                                    <c:forEach items="${termOptions}" var="termOpt">
                                        <option value="${fn:escapeXml(termOpt)}" ${param.term == termOpt ? 'selected' : ''}><c:out value="${termOpt}"/></option>
                                    </c:forEach>
                                </select>
                                <span class="material-symbols-outlined text-lg absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">expand_more</span>
                            </div>
                            <button type="submit" class="portal-btn portal-btn-primary">
                                <span class="material-symbols-outlined text-sm">search</span>
                                ${language == 'zh' ? '搜索' : 'Search'}
                            </button>
                            <c:if test="${not empty param.keyword or not empty param.department or not empty param.term}">
                                <a href="${pageContext.request.contextPath}/vacancies" class="portal-btn portal-btn-secondary" title="${language == 'zh' ? '清除筛选' : 'Clear filters'}">
                                    <span class="material-symbols-outlined text-sm">close</span>
                                    ${language == 'zh' ? '清除' : 'Clear'}
                                </a>
                            </c:if>
                        </div>
                    </div>
                </form>

                <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                    <jsp:param name="containerClass" value="mb-6" />
                </jsp:include>

                <%-- loadError：后端异常；否则根据 vacancies 是否为空展示列表或空状态（空列表可能表示无数据或无匹配筛选，具体文案由产品与后端约定）。 --%>
                <c:choose>
                    <c:when test="${pageState == 'loadError'}">
                        <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                            <jsp:param name="variant" value="error" />
                            <jsp:param name="icon" value="travel_explore" />
                            <jsp:param name="title" value="${language == 'zh' ? '岗位列表暂不可用' : 'Vacancies unavailable'}" />
                            <jsp:param name="message" value="${language == 'zh' ? '当前无法加载岗位列表，请刷新页面后重试。' : 'Unable to load the vacancy list right now. Please refresh the page and try again.'}" />
                            <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                            <jsp:param name="actionLabel" value="${language == 'zh' ? '重试' : 'Try Again'}" />
                        </jsp:include>
                    </c:when>
                    <c:otherwise>

                        <!-- Result summary -->
                        <c:if test="${not empty vacancies or totalCount > 0}">
                            <div class="flex items-center justify-between mb-1 px-1">
                                <p class="text-sm text-slate-500">
                                    ${language == 'zh' ? '显示' : 'Showing'}
                                    <span class="font-semibold text-slate-700">${(currentPage - 1) * 6 + 1}–${(currentPage - 1) * 6 + fn:length(vacancies)}</span>
                                    ${language == 'zh' ? '，共' : 'of'}
                                    <span class="font-semibold text-slate-700">${totalCount}</span>
                                    ${language == 'zh' ? '个岗位' : 'vacancies'}
                                </p>
                                <c:if test="${totalPages > 1}">
                                    <span class="text-xs text-slate-400">${language == 'zh' ? '第' : 'Page '}${currentPage} / ${totalPages}${language == 'zh' ? '页' : ''}</span>
                                </c:if>
                            </div>
                        </c:if>

                        <%-- 列表数据来自 request attribute `vacancies`（契约字段 vacancyId 用于跳转详情）。 --%>
                        <!-- Vacancy List -->
                        <div class="space-y-4" id="vacancyList">
                            <c:choose>
                                <c:when test="${empty vacancies}">
                                    <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                                        <jsp:param name="icon" value="search_off" />
                                        <jsp:param name="title" value="${language == 'zh' ? '未找到岗位' : 'No vacancies found'}" />
                                        <jsp:param name="message" value="${language == 'zh' ? '请尝试调整搜索条件，或稍后再查看新的岗位机会。' : 'Try adjusting your search filters or check back later for new opportunities.'}" />
                                    </jsp:include>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${vacancies}" var="vacancy" varStatus="vs">
                                        <div class="portal-panel p-6 hover:shadow-md transition-shadow vacancy-card"
                                             style="animation-delay: ${vs.index * 40}ms">
                                            <div class="flex flex-col md:flex-row gap-6">
                                                <div class="flex-1 min-w-0">
                                                    <div class="flex items-center gap-3 mb-2 flex-wrap">
                                                        <span class="shrink-0 px-2.5 py-1 bg-slate-100 text-slate-600 text-xs font-bold uppercase rounded-md tracking-wider">
                                                            <c:out value="${vacancy.courseCode}"/>
                                                        </span>
                                                        <a href="${pageContext.request.contextPath}/vacancy?vacancyId=${vacancy.vacancyId}"
                                                           class="text-xl font-bold text-slate-900 hover:text-blue-600 transition-colors truncate">
                                                            <c:out value="${vacancy.title}"/>
                                                        </a>
                                                    </div>
                                                    <p class="text-sm text-slate-500 mb-4 line-clamp-2 leading-relaxed">
                                                        <c:out value="${vacancy.description}"/>
                                                    </p>
                                                    <div class="flex flex-wrap items-center gap-x-5 gap-y-2 text-sm text-slate-500">
                                                        <div class="flex items-center gap-1.5">
                                                            <span class="material-symbols-outlined text-[17px]">domain</span>
                                                            <c:out value="${vacancy.department}"/>
                                                        </div>
                                                        <div class="flex items-center gap-1.5">
                                                            <span class="material-symbols-outlined text-[17px]">schedule</span>
                                                            <c:out value="${vacancy.hoursPerWeek}"/> ${language == 'zh' ? '小时/周' : 'hrs/week'}
                                                        </div>
                                                        <div class="flex items-center gap-1.5 font-semibold text-emerald-600">
                                                            <span class="material-symbols-outlined text-[17px]">payments</span>
                                                            £<c:out value="${vacancy.hourlyRate}"/>/hr
                                                        </div>
                                                        <div class="flex items-center gap-1.5 font-medium text-amber-600">
                                                            <span class="material-symbols-outlined text-[17px]">event</span>
                                                            ${language == 'zh' ? '截止：' : 'Deadline: '}<c:out value="${vacancy.deadline}"/>
                                                        </div>
                                                        <div class="flex items-center gap-1.5 text-slate-400">
                                                            <span class="material-symbols-outlined text-[17px]">person</span>
                                                            <c:out value="${vacancy.moduleOwner}"/>
                                                        </div>
                                                    </div>
                                                </div>

                                                <%-- MO：仅 owner 显示编辑；详情链与 TA 一致使用 vacancyId。非 MO 走 TA 分支（收藏/申请入口）。 --%>
                                                <!-- Right actions -->
                                                <div class="flex flex-row md:flex-col justify-between items-end gap-3 shrink-0 border-t md:border-t-0 md:border-l border-slate-100 pt-4 md:pt-0 md:pl-6">
                                                    <c:choose>
                                                        <c:when test="${userRole == 'MO'}">
                                                            <div class="flex flex-col items-center gap-1.5">
                                                                <c:if test="${vacancy.owner}">
                                                                    <button type="button" class="portal-btn portal-btn-secondary whitespace-nowrap" title="${language == 'zh' ? '编辑该岗位' : 'Edit this vacancy'}">
                                                                        <span class="material-symbols-outlined text-sm">edit</span>
                                                                        ${language == 'zh' ? '编辑' : 'Edit'}
                                                                    </button>
                                                                </c:if>
                                                            </div>
                                                            <a href="${pageContext.request.contextPath}/vacancy?vacancyId=${vacancy.vacancyId}"
                                                               class="portal-btn portal-btn-primary whitespace-nowrap">
                                                                ${language == 'zh' ? '查看详情' : 'View Details'}
                                                            </a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <!-- Favourite button + AI score badge (TA only) -->
                                                            <div class="flex flex-col items-center gap-1.5">
                                                                <button type="button"
                                                                        class="fav-btn ${vacancy.saved ? 'saved' : ''}"
                                                                        data-vacancy-id="${vacancy.vacancyId}"
                                                                        title="${vacancy.saved ? (language == 'zh' ? '取消收藏' : 'Remove from saved') : (language == 'zh' ? '收藏岗位' : 'Save vacancy')}"
                                                                        onclick="toggleFavorite(this)">
                                                                    <span class="material-symbols-outlined text-2xl">favorite</span>
                                                                </button>
                                                                <!-- AI match score badge (populated by JS) -->
                                                                <div id="score-${vacancy.vacancyId}"
                                                                     class="ai-score-slot"
                                                                     data-job-id="${vacancy.vacancyId}">
                                                                    <%-- hidden initially; filled by startAIMatch() --%>
                                                                </div>
                                                            </div>
                                                            <!-- Apply button -->
                                                            <a href="${pageContext.request.contextPath}/vacancy?vacancyId=${vacancy.vacancyId}"
                                                               class="portal-btn portal-btn-primary whitespace-nowrap">
                                                                ${language == 'zh' ? '查看并申请' : 'View & Apply'}
                                                            </a>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <!-- Pagination -->
                        <c:if test="${totalPages > 1}">
                            <div class="mt-8 flex items-center justify-center gap-2">
                                <!-- Previous -->
                                <c:choose>
                                    <c:when test="${currentPage > 1}">
                                        <a href="${pageContext.request.contextPath}/vacancies?page=${currentPage - 1}&keyword=${fn:escapeXml(param.keyword)}&department=${fn:escapeXml(param.department)}&term=${fn:escapeXml(param.term)}"
                                           class="portal-btn portal-btn-secondary px-3">
                                            <span class="material-symbols-outlined text-lg">chevron_left</span>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="portal-btn portal-btn-secondary px-3 opacity-40 cursor-not-allowed">
                                            <span class="material-symbols-outlined text-lg">chevron_left</span>
                                        </span>
                                    </c:otherwise>
                                </c:choose>

                                <!-- Page numbers -->
                                <c:forEach begin="1" end="${totalPages}" var="p">
                                    <c:choose>
                                        <c:when test="${p == currentPage}">
                                            <span class="w-10 h-10 flex items-center justify-center rounded-xl text-sm font-bold text-white"
                                                  style="background:#0f172a;">${p}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${pageContext.request.contextPath}/vacancies?page=${p}&keyword=${fn:escapeXml(param.keyword)}&department=${fn:escapeXml(param.department)}&term=${fn:escapeXml(param.term)}"
                                               class="w-10 h-10 flex items-center justify-center rounded-xl text-sm font-semibold text-slate-600 border border-slate-200 bg-white hover:bg-slate-50 transition-colors">${p}</a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>

                                <!-- Next -->
                                <c:choose>
                                    <c:when test="${currentPage < totalPages}">
                                        <a href="${pageContext.request.contextPath}/vacancies?page=${currentPage + 1}&keyword=${fn:escapeXml(param.keyword)}&department=${fn:escapeXml(param.department)}&term=${fn:escapeXml(param.term)}"
                                           class="portal-btn portal-btn-secondary px-3">
                                            <span class="material-symbols-outlined text-lg">chevron_right</span>
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="portal-btn portal-btn-secondary px-3 opacity-40 cursor-not-allowed">
                                            <span class="material-symbols-outlined text-lg">chevron_right</span>
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:if>

                    </c:otherwise>
                </c:choose>

            </div>
        </main>
    </div>
</div>

<%-- ═══════════════════════════════════════════════════════
     AI Match Disclaimer Modal
     ═══════════════════════════════════════════════════════ --%>
<div id="aiMatchDisclaimerModal" class="qm-modal-overlay"
     onclick="if(event.target===this) closeAIMatchDisclaimer()">
    <div class="qm-modal-box max-w-md">
        <div class="px-7 pt-7 pb-5">
            <div class="flex items-start gap-4 mb-5">
                <div class="w-12 h-12 rounded-2xl bg-blue-100 flex items-center justify-center shrink-0">
                    <span class="material-symbols-outlined text-2xl text-blue-600"
                          style="font-variation-settings:'FILL' 1">policy</span>
                </div>
                <div>
                    <h2 class="text-xl font-bold text-slate-900">${language == 'zh' ? 'AI 岗位匹配' : 'AI Job Match'}</h2>
                    <p class="text-sm text-slate-500 mt-0.5">${language == 'zh' ? '继续前请先阅读' : 'Please read before continuing'}</p>
                </div>
            </div>

            <div class="bg-amber-50 border border-amber-200 rounded-2xl p-4 mb-5 text-sm text-amber-900 leading-relaxed">
                <p class="font-bold mb-2">${language == 'zh' ? '注意：请仔细阅读以下说明：' : 'Disclaimer — Read carefully:'}</p>
                <ul class="list-disc pl-4 space-y-1.5 text-amber-800">
                    <li>${language == 'zh' ? '匹配分数由 ' : 'Match scores are generated by '}<strong>Qwen AI</strong>${language == 'zh' ? ' 生成，仅供参考。' : ' and are for reference only.'}</li>
                    <li>${language == 'zh' ? '分数可能不准确，不能保证或预测录用结果。' : 'Scores may be inaccurate and do not guarantee or predict selection outcomes.'}</li>
                    <li>${language == 'zh' ? '你的简历数据仅在分析过程中短暂处理，不会被 AI 服务长期存储。' : 'Your resume data is processed transiently and is not stored by the AI service.'}</li>
                    <li>${language == 'zh' ? '为获得更准确的分数，请确保你在简历页面中的简历资料完整且最新。' : 'For more accurate scores, ensure your resume profile is complete and up to date on the Resumes page.'}</li>
                </ul>
            </div>
            <p class="text-sm text-slate-500">
                ${language == 'zh' ? '点击“同意并匹配”即表示你已阅读并理解以上内容。' : 'By clicking "I Agree & Match" you confirm you have read the above.'}
            </p>
        </div>
        <div class="px-7 py-5 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
            <button type="button" onclick="closeAIMatchDisclaimer()" class="portal-btn portal-btn-secondary">
                ${language == 'zh' ? '取消' : 'Cancel'}
            </button>
            <button type="button" onclick="agreeAndStartAIMatch()" class="portal-btn portal-btn-primary">
                <span class="material-symbols-outlined text-sm">auto_awesome</span>
                ${language == 'zh' ? '同意并匹配' : 'I Agree & Match'}
            </button>
        </div>
    </div>
</div>

<%-- ═══════════════════════════════════════════════════════
     Create Vacancy Modal (MO Only)
     ═══════════════════════════════════════════════════════ --%>
<c:if test="${userRole == 'MO'}">
<div id="createVacancyModal" class="qm-modal-overlay"
     onclick="if(event.target===this) closeCreateVacancyModal()">
    <div class="qm-modal-box max-w-2xl">
        <form action="${pageContext.request.contextPath}/vacancy/create" method="POST">
            <div class="px-7 pt-7 pb-5">
                <div class="flex items-start gap-4 mb-5">
                    <div class="w-12 h-12 rounded-2xl bg-blue-100 flex items-center justify-center shrink-0">
                        <span class="material-symbols-outlined text-2xl text-blue-600"
                              style="font-variation-settings:'FILL' 1">add_circle</span>
                    </div>
                    <div>
                        <h2 class="text-xl font-bold text-slate-900">${language == 'zh' ? '发布新岗位' : 'Post a New Vacancy'}</h2>
                        <p class="text-sm text-slate-500 mt-0.5">${language == 'zh' ? '填写新的助教岗位信息。' : 'Fill in the details for the new teaching assistant position.'}</p>
                    </div>
                </div>

                <div class="space-y-4">
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '标题' : 'Title'} <span class="text-red-500">*</span></label>
                        <input type="text" name="title" required class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none" placeholder="${language == 'zh' ? '例如：Java 导论助教' : 'e.g. Teaching Assistant for Intro to Java'}">
                    </div>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '课程代码' : 'Course Code'}</label>
                            <input type="text" name="courseCode" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none" placeholder="${language == 'zh' ? '例如：ECS414U' : 'e.g. ECS414U'}">
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '学期' : 'Term'}</label>
                            <select name="term" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none">
                                <c:forEach items="${termOptions}" var="termOpt">
                                    <option value="${fn:escapeXml(termOpt)}"><c:out value="${termOpt}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '描述' : 'Description'}</label>
                        <textarea name="description" rows="3" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none" placeholder="${language == 'zh' ? '简要描述岗位内容...' : 'Brief description of the role...'}"></textarea>
                    </div>
                    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div>
                            <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '每周工时' : 'Hours/Week'}</label>
                            <input type="number" name="hoursPerWeek" min="1" value="10" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none">
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '时薪 (£)' : 'Hourly Rate (£)'}</label>
                            <input type="number" step="0.01" name="hourlyRate" min="0" value="20.00" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none">
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '截止时间' : 'Deadline'}</label>
                            <input type="datetime-local" name="deadline" required class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none">
                        </div>
                    </div>
                </div>
            </div>
            <div class="px-7 py-5 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
                <button type="button" onclick="closeCreateVacancyModal()" class="portal-btn portal-btn-secondary">
                    ${language == 'zh' ? '取消' : 'Cancel'}
                </button>
                <button type="submit" class="portal-btn portal-btn-primary">
                    <span class="material-symbols-outlined text-sm">save</span>
                    ${language == 'zh' ? '创建岗位' : 'Create Vacancy'}
                </button>
            </div>
        </form>
    </div>
</div>
</c:if>

<script>
    const CTX = '${pageContext.request.contextPath}';

    // ── Create Vacancy Modal ──────────────────────────────────────────────────
    function openCreateVacancyModal() {
        document.getElementById('createVacancyModal').classList.add('open');
    }
    function closeCreateVacancyModal() {
        document.getElementById('createVacancyModal').classList.remove('open');
    }

    // ── AI Match Disclaimer ───────────────────────────────────────────────────
    function openAIMatchDisclaimer() {
        document.getElementById('aiMatchDisclaimerModal').classList.add('open');
    }
    function closeAIMatchDisclaimer() {
        document.getElementById('aiMatchDisclaimerModal').classList.remove('open');
    }
    function agreeAndStartAIMatch() {
        closeAIMatchDisclaimer();
        startAIMatch();
    }

    // ── Favourite toggle ──────────────────────────────────────────────────────
    async function toggleFavorite(btn) {
        const vacancyId = btn.dataset.vacancyId;
        if (!vacancyId) return;
        btn.disabled = true;
        try {
            const fd = new FormData();
            fd.append('vacancyId', vacancyId);
            const res = await fetch(CTX + '/favorites', { method: 'POST', body: fd });
            if (res.status === 401) { alert('${language == 'zh' ? '请先登录后再收藏岗位。' : 'Please log in to save vacancies.'}'); return; }
            if (!res.ok) throw new Error('${language == 'zh' ? '服务器错误' : 'Server error'}');
            const data = await res.json();
            if (data.saved) { btn.classList.add('saved');    btn.title = '${language == 'zh' ? '取消收藏' : 'Remove from saved'}'; }
            else             { btn.classList.remove('saved'); btn.title = '${language == 'zh' ? '收藏岗位' : 'Save vacancy'}'; }
        } catch (e) { console.error('${language == 'zh' ? '收藏切换失败：' : 'Favorite toggle failed:'}', e); }
        finally { btn.disabled = false; }
    }

    // ── AI Match ──────────────────────────────────────────────────────────────
    // Cache scores in sessionStorage so they survive pagination within a session.
    const SCORE_CACHE_KEY = 'aiMatchScores';

    function getScoreCache() {
        try { return JSON.parse(sessionStorage.getItem(SCORE_CACHE_KEY) || '{}'); }
        catch { return {}; }
    }
    function saveScoreCache(scores) {
        try { sessionStorage.setItem(SCORE_CACHE_KEY, JSON.stringify(scores)); }
        catch { /* quota exceeded – ignore */ }
    }

    // Apply cached scores to current page cards on load
    (function applyCachedScores() {
        const cache = getScoreCache();
        if (!Object.keys(cache).length) return;
        document.querySelectorAll('.ai-score-slot').forEach(slot => {
            const id = slot.dataset.jobId;
            if (id && cache[id] !== undefined) renderBadge(slot, cache[id]);
        });
        showBanner('${language == 'zh' ? '已从缓存加载 AI 匹配分数。点击 AI Match 可刷新。' : 'AI match scores loaded from cache. Click AI Match to refresh.'}', false);
    })();

    async function startAIMatch() {
        // Collect all job IDs visible on this page
        const slots = Array.from(document.querySelectorAll('.ai-score-slot'));
        if (!slots.length) { showBanner('${language == 'zh' ? '当前页面没有可评分的岗位。' : 'No vacancies on this page to score.'}', false); return; }

        const ids = slots.map(s => s.dataset.jobId).filter(Boolean);

        // Show skeleton loading state
        slots.forEach(slot => {
            slot.innerHTML = '<div class="ai-badge-skeleton"></div>';
        });
        showBanner('${language == 'zh' ? 'AI 正在评估你的简历与这些岗位的匹配度…' : 'AI is scoring your resume against these vacancies…'}', true);

        const btn = document.getElementById('aiMatchBtn');
        if (btn) btn.disabled = true;

        try {
            // URLSearchParams sends application/x-www-form-urlencoded,
            // which servlets without @MultipartConfig can read via getParameterValues()
            const params = new URLSearchParams();
            ids.forEach(id => params.append('ids[]', id));

            const res  = await fetch(CTX + '/ai-match', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            });
            const data = await res.json();

            if (res.status === 401) {
                clearSkeletons(slots);
                showBanner('${language == 'zh' ? '请先登录后再使用 AI 匹配。' : 'Please log in to use AI Match.'}', false);
                return;
            }

            if (!data.ok) {
                clearSkeletons(slots);
                showBanner('${language == 'zh' ? 'AI 匹配失败：' : 'AI Match failed: '}' + (data.error || '${language == 'zh' ? '未知错误' : 'Unknown error'}'), false);
                return;
            }

            // Render badges and merge into cache
            const cache = getScoreCache();
            slots.forEach(slot => {
                const id    = slot.dataset.jobId;
                const score = data.scores && data.scores[id] !== undefined ? data.scores[id] : -1;
                renderBadge(slot, score);
                if (score >= 0) cache[id] = score;
            });
            saveScoreCache(cache);

            const hasResume = data.hasResume;
            const tip = hasResume
                ? '${language == 'zh' ? 'AI 匹配分数已显示在各岗位下方，分数反映你最匹配该岗位的简历。' : 'AI match scores shown below each vacancy. Scores reflect your best-matched resume for each role.'}'
                : '${language == 'zh' ? '未找到简历，请先在简历页面上传以获取个性化评分。' : 'No resume found — upload one on the Resumes page for personalised scores.'}';
            showBanner(tip, false);

        } catch (e) {
            clearSkeletons(slots);
            showBanner('${language == 'zh' ? '网络错误：' : 'Network error: '}' + e.message, false);
        } finally {
            if (btn) btn.disabled = false;
        }
    }

    function renderBadge(slot, score) {
        if (score < 0) {
            slot.innerHTML = '<span class="ai-badge score-unknown">${language == 'zh' ? '未知' : '?'}</span>';
            return;
        }
        let cls, label, icon;
        if (score >= 80)      { cls = 'score-excellent'; label = score + '%'; icon = '🌟'; }
        else if (score >= 60) { cls = 'score-good';      label = score + '%'; icon = '✓'; }
        else if (score >= 40) { cls = 'score-fair';      label = score + '%'; icon = '~'; }
        else                  { cls = 'score-low';       label = score + '%'; icon = '↓'; }

        slot.innerHTML =
            '<span class="ai-badge ' + cls + '" title="${language == 'zh' ? 'AI 匹配得分' : 'AI match score'}: ' + score + '/100">' +
            icon + ' ' + label +
            '</span>';
    }

    function clearSkeletons(slots) {
        slots.forEach(s => { s.innerHTML = ''; });
    }

    function clearAIScores() {
        sessionStorage.removeItem(SCORE_CACHE_KEY);
        document.querySelectorAll('.ai-score-slot').forEach(s => { s.innerHTML = ''; });
        document.getElementById('aiMatchBanner').style.display = 'none';
    }

    function showBanner(text, loading) {
        const banner = document.getElementById('aiMatchBanner');
        const label  = document.getElementById('aiMatchBannerText');
        label.textContent = text;
        banner.style.display  = 'flex';
        banner.style.opacity  = loading ? '0.85' : '1';
    }

    // Keep filter page-reset on new searches
    document.querySelector('#filterForm').addEventListener('submit', function () {
        this.querySelector('[name="page"]').value = '1';
    });
</script>
</body>
</html>
