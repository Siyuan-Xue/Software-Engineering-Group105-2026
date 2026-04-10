<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vacancies - QM HIRE</title>
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
<body class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
<div class="relative flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="portal-page">

                <!-- Page Header -->
                <div class="portal-page-header mb-2">
                    <div>
                        <h2 class="portal-page-title">Available Vacancies</h2>
                        <p class="portal-page-copy">Browse and apply for open Teaching Assistant positions across departments.</p>
                    </div>
                    <div class="flex gap-3">
                        <c:if test="${userRole == 'MO'}">
                            <button class="portal-btn portal-btn-primary" title="Post a new vacancy">
                                <span class="material-symbols-outlined text-sm">add</span>
                                Post Vacancy
                            </button>
                        </c:if>
                        <c:if test="${userRole == 'TA'}">
                            <button id="aiMatchBtn"
                                    onclick="openAIMatchDisclaimer()"
                                    class="portal-btn portal-btn-secondary"
                                    title="Use AI to score how well your resume matches each vacancy">
                                <span class="material-symbols-outlined text-sm">auto_awesome</span>
                                AI Match
                            </button>
                            <button class="portal-btn portal-btn-secondary" title="Get notified when new vacancies are posted">
                                <span class="material-symbols-outlined text-sm">notifications_active</span>
                                Job Alerts
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
                                           placeholder="Search by course code, title, or keywords..." />
                                </div>
                            </label>
                        </div>
                        <div class="flex flex-wrap gap-2">
                            <!-- Department -->
                            <div class="relative">
                                <select name="department" class="h-11 rounded-lg bg-white border border-slate-200 pl-4 pr-8 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors appearance-none">
                                    <option value="">All Departments</option>
                                    <option value="CS"   ${param.department == 'CS'   ? 'selected' : ''}>Computer Science</option>
                                    <option value="MATH" ${param.department == 'MATH' ? 'selected' : ''}>Mathematics</option>
                                </select>
                                <span class="material-symbols-outlined text-lg absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">expand_more</span>
                            </div>
                            <!-- Term (dynamic) -->
                            <div class="relative">
                                <select name="term" class="h-11 rounded-lg bg-white border border-slate-200 pl-4 pr-8 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors appearance-none">
                                    <option value="">All Terms</option>
                                    <c:forEach items="${termOptions}" var="termOpt">
                                        <option value="${fn:escapeXml(termOpt)}" ${param.term == termOpt ? 'selected' : ''}><c:out value="${termOpt}"/></option>
                                    </c:forEach>
                                </select>
                                <span class="material-symbols-outlined text-lg absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">expand_more</span>
                            </div>
                            <button type="submit" class="portal-btn portal-btn-primary">
                                <span class="material-symbols-outlined text-sm">search</span>
                                Search
                            </button>
                            <c:if test="${not empty param.keyword or not empty param.department or not empty param.term}">
                                <a href="${pageContext.request.contextPath}/vacancies" class="portal-btn portal-btn-secondary" title="Clear filters">
                                    <span class="material-symbols-outlined text-sm">close</span>
                                    Clear
                                </a>
                            </c:if>
                        </div>
                    </div>
                </form>

                <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                    <jsp:param name="containerClass" value="mb-6" />
                </jsp:include>

                <c:choose>
                    <c:when test="${pageState == 'loadError'}">
                        <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                            <jsp:param name="variant" value="error" />
                            <jsp:param name="icon" value="travel_explore" />
                            <jsp:param name="title" value="Vacancies unavailable" />
                            <jsp:param name="message" value="We couldn't load the vacancy list right now. Please refresh the page and try again." />
                            <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                            <jsp:param name="actionLabel" value="Try Again" />
                        </jsp:include>
                    </c:when>
                    <c:otherwise>

                        <!-- Result summary -->
                        <c:if test="${not empty vacancies or totalCount > 0}">
                            <div class="flex items-center justify-between mb-1 px-1">
                                <p class="text-sm text-slate-500">
                                    Showing
                                    <span class="font-semibold text-slate-700">${(currentPage - 1) * 6 + 1}–${(currentPage - 1) * 6 + fn:length(vacancies)}</span>
                                    of
                                    <span class="font-semibold text-slate-700">${totalCount}</span>
                                    vacancies
                                </p>
                                <c:if test="${totalPages > 1}">
                                    <span class="text-xs text-slate-400">Page ${currentPage} / ${totalPages}</span>
                                </c:if>
                            </div>
                        </c:if>

                        <!-- Vacancy List -->
                        <div class="space-y-4" id="vacancyList">
                            <c:choose>
                                <c:when test="${empty vacancies}">
                                    <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                                        <jsp:param name="icon" value="search_off" />
                                        <jsp:param name="title" value="No vacancies found" />
                                        <jsp:param name="message" value="Try adjusting your search filters or check back later for new opportunities." />
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
                                                            <c:out value="${vacancy.hoursPerWeek}"/> hrs/week
                                                        </div>
                                                        <div class="flex items-center gap-1.5 font-semibold text-emerald-600">
                                                            <span class="material-symbols-outlined text-[17px]">payments</span>
                                                            £<c:out value="${vacancy.hourlyRate}"/>/hr
                                                        </div>
                                                        <div class="flex items-center gap-1.5 font-medium text-amber-600">
                                                            <span class="material-symbols-outlined text-[17px]">event</span>
                                                            Deadline: <c:out value="${vacancy.deadline}"/>
                                                        </div>
                                                        <div class="flex items-center gap-1.5 text-slate-400">
                                                            <span class="material-symbols-outlined text-[17px]">person</span>
                                                            <c:out value="${vacancy.moduleOwner}"/>
                                                        </div>
                                                    </div>
                                                </div>

                                                <!-- Right actions -->
                                                <div class="flex flex-row md:flex-col justify-between items-end gap-3 shrink-0 border-t md:border-t-0 md:border-l border-slate-100 pt-4 md:pt-0 md:pl-6">
                                                    <c:choose>
                                                        <c:when test="${userRole == 'MO'}">
                                                            <div class="flex flex-col items-center gap-1.5">
                                                                <c:if test="${vacancy.isOwner}">
                                                                    <button type="button" class="portal-btn portal-btn-secondary whitespace-nowrap" title="Edit this vacancy">
                                                                        <span class="material-symbols-outlined text-sm">edit</span>
                                                                        Edit
                                                                    </button>
                                                                </c:if>
                                                            </div>
                                                            <a href="${pageContext.request.contextPath}/vacancy?vacancyId=${vacancy.vacancyId}"
                                                               class="portal-btn portal-btn-primary whitespace-nowrap">
                                                                View Details
                                                            </a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <!-- Favourite button + AI score badge (TA only) -->
                                                            <div class="flex flex-col items-center gap-1.5">
                                                                <button type="button"
                                                                        class="fav-btn ${vacancy.saved ? 'saved' : ''}"
                                                                        data-vacancy-id="${vacancy.vacancyId}"
                                                                        title="${vacancy.saved ? 'Remove from saved' : 'Save vacancy'}"
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
                                                                View & Apply
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
                    <h2 class="text-xl font-bold text-slate-900">AI Job Match</h2>
                    <p class="text-sm text-slate-500 mt-0.5">Please read before continuing</p>
                </div>
            </div>

            <div class="bg-amber-50 border border-amber-200 rounded-2xl p-4 mb-5 text-sm text-amber-900 leading-relaxed">
                <p class="font-bold mb-2">⚠️ Disclaimer — Read carefully:</p>
                <ul class="list-disc pl-4 space-y-1.5 text-amber-800">
                    <li>Match scores are generated by <strong>Qwen AI</strong> and are for <strong>reference only</strong>.</li>
                    <li>Scores may be inaccurate and <strong>do not</strong> guarantee or predict selection outcomes.</li>
                    <li>Your resume data is processed transiently and is not stored by the AI service.</li>
                    <li>For more accurate scores, ensure your resume profile is <strong>complete and up to date</strong> on the Resumes page.</li>
                </ul>
            </div>
            <p class="text-sm text-slate-500">
                By clicking <strong>"I Agree &amp; Match"</strong> you confirm you have read the above.
            </p>
        </div>
        <div class="px-7 py-5 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
            <button type="button" onclick="closeAIMatchDisclaimer()" class="portal-btn portal-btn-secondary">
                Cancel
            </button>
            <button type="button" onclick="agreeAndStartAIMatch()" class="portal-btn portal-btn-primary">
                <span class="material-symbols-outlined text-sm">auto_awesome</span>
                I Agree &amp; Match
            </button>
        </div>
    </div>
</div>

<script>
    const CTX = '${pageContext.request.contextPath}';

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
            if (res.status === 401) { alert('Please log in to save vacancies.'); return; }
            if (!res.ok) throw new Error('Server error');
            const data = await res.json();
            if (data.saved) { btn.classList.add('saved');    btn.title = 'Remove from saved'; }
            else             { btn.classList.remove('saved'); btn.title = 'Save vacancy'; }
        } catch (e) { console.error('Favorite toggle failed:', e); }
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
        showBanner('AI match scores loaded from cache. Click AI Match to refresh.', false);
    })();

    async function startAIMatch() {
        // Collect all job IDs visible on this page
        const slots = Array.from(document.querySelectorAll('.ai-score-slot'));
        if (!slots.length) { showBanner('No vacancies on this page to score.', false); return; }

        const ids = slots.map(s => s.dataset.jobId).filter(Boolean);

        // Show skeleton loading state
        slots.forEach(slot => {
            slot.innerHTML = '<div class="ai-badge-skeleton"></div>';
        });
        showBanner('AI is scoring your resume against these vacancies…', true);

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
                showBanner('Please log in to use AI Match.', false);
                return;
            }

            if (!data.ok) {
                clearSkeletons(slots);
                showBanner('AI Match failed: ' + (data.error || 'Unknown error'), false);
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
                ? 'AI match scores shown below each vacancy. Scores reflect your best-matched resume for each role.'
                : 'No resume found — upload one on the Resumes page for personalised scores.';
            showBanner(tip, false);

        } catch (e) {
            clearSkeletons(slots);
            showBanner('Network error: ' + e.message, false);
        } finally {
            if (btn) btn.disabled = false;
        }
    }

    function renderBadge(slot, score) {
        if (score < 0) {
            slot.innerHTML = '<span class="ai-badge score-unknown">?</span>';
            return;
        }
        let cls, label, icon;
        if (score >= 80)      { cls = 'score-excellent'; label = score + '%'; icon = '🌟'; }
        else if (score >= 60) { cls = 'score-good';      label = score + '%'; icon = '✓'; }
        else if (score >= 40) { cls = 'score-fair';      label = score + '%'; icon = '~'; }
        else                  { cls = 'score-low';       label = score + '%'; icon = '↓'; }

        slot.innerHTML =
            '<span class="ai-badge ' + cls + '" title="AI match score: ' + score + '/100">' +
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
