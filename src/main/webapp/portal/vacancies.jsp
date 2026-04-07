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
        .fav-btn { transition: transform .15s, color .15s; }
        .fav-btn:hover { transform: scale(1.2); }
        .fav-btn.saved .material-symbols-outlined { font-variation-settings: 'FILL' 1; color: #ef4444; }
        .fav-btn.saved:hover .material-symbols-outlined { color: #b91c1c; }
        .fav-btn:not(.saved) .material-symbols-outlined { color: #94a3b8; }
        .fav-btn:not(.saved):hover .material-symbols-outlined { color: #ef4444; }

        .vacancy-card { animation: fadeSlideUp .25s ease both; }
        @keyframes fadeSlideUp {
            from { opacity: 0; transform: translateY(8px); }
            to   { opacity: 1; transform: translateY(0); }
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
                        <button class="portal-btn portal-btn-secondary" title="Get notified when new vacancies are posted">
                            <span class="material-symbols-outlined text-sm">notifications_active</span>
                            Job Alerts
                        </button>
                    </div>
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
                                                <div class="flex flex-row md:flex-col justify-between items-end gap-4 shrink-0 border-t md:border-t-0 md:border-l border-slate-100 pt-4 md:pt-0 md:pl-6">
                                                    <!-- Favourite button -->
                                                    <button type="button"
                                                            class="fav-btn ${vacancy.saved ? 'saved' : ''}"
                                                            data-vacancy-id="${vacancy.vacancyId}"
                                                            title="${vacancy.saved ? 'Remove from saved' : 'Save vacancy'}"
                                                            onclick="toggleFavorite(this)">
                                                        <span class="material-symbols-outlined text-2xl">favorite</span>
                                                    </button>
                                                    <!-- Apply button -->
                                                    <a href="${pageContext.request.contextPath}/vacancy?vacancyId=${vacancy.vacancyId}"
                                                       class="portal-btn portal-btn-primary whitespace-nowrap">
                                                        View & Apply
                                                    </a>
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

<script>
    const CTX = '${pageContext.request.contextPath}';

    async function toggleFavorite(btn) {
        const vacancyId = btn.dataset.vacancyId;
        if (!vacancyId) return;

        btn.disabled = true;
        try {
            const fd = new FormData();
            fd.append('vacancyId', vacancyId);
            const res = await fetch(CTX + '/favorites', { method: 'POST', body: fd });
            if (res.status === 401) {
                alert('Please log in to save vacancies.');
                return;
            }
            if (!res.ok) throw new Error('Server error');
            const data = await res.json();
            if (data.saved) {
                btn.classList.add('saved');
                btn.title = 'Remove from saved';
            } else {
                btn.classList.remove('saved');
                btn.title = 'Save vacancy';
            }
        } catch (e) {
            console.error('Favorite toggle failed:', e);
        } finally {
            btn.disabled = false;
        }
    }

    // Keep filter page-reset on new searches
    document.querySelector('#filterForm').addEventListener('submit', function () {
        this.querySelector('[name="page"]').value = '1';
    });
</script>
</body>
</html>
