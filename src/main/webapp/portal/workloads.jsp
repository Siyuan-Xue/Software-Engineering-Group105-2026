<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${language == 'zh' ? '助教工作量 - QM HIRE' : 'TA Workloads - QM HIRE'}</title>
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
</head>
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
<div class="relative flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="portal-page">
                <c:set var="workloadsState" value="${empty pageState ? 'normal' : pageState}" />
                <c:set var="workloadsActiveFilters" value="${not empty param.keyword or not empty param.department}" />

                <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                    <jsp:param name="containerClass" value="mb-6" />
                </jsp:include>

                <div class="portal-page-header mb-6">
                    <div>
                        <h2 class="portal-page-title">${language == 'zh' ? '助教工作量' : 'TA Workloads'}</h2>
                        <p class="portal-page-copy">${language == 'zh' ? '查看各院系助教岗位分配和工时情况。' : 'Monitor TA assignments, hours, and estimated income across all departments.'}</p>
                    </div>
                </div>

                <c:if test="${workloadsState != 'loadError'}">
                    <form action="${pageContext.request.contextPath}/workloads" method="GET" class="portal-filter-bar mb-6">
                        <div class="flex flex-col lg:flex-row gap-4">
                            <div class="flex-1">
                                <label class="flex flex-col w-full">
                                    <div class="flex w-full items-center rounded-lg bg-slate-100 px-4 h-11 border border-transparent focus-within:border-primary/30 transition-all">
                                        <span class="material-symbols-outlined text-slate-400">search</span>
                                        <input type="text" name="keyword" value="${param.keyword}" class="w-full bg-transparent border-none focus:ring-0 text-slate-900 placeholder:text-slate-400 text-sm font-medium pl-3 outline-none" placeholder="${language == 'zh' ? '按助教姓名或学号搜索...' : 'Search by TA name or student ID...'}" />
                                    </div>
                                </label>
                            </div>
                            <div class="flex flex-wrap gap-2">
                                <div class="relative">
                                    <select name="department" class="flex h-11 items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors appearance-none pr-8 min-w-[10rem]">
                                        <option value="">${language == 'zh' ? '院系：全部' : 'Department: All'}</option>
                                        <c:forEach items="${departmentOptions}" var="dept">
                                            <option value="${dept}" ${param.department == dept ? 'selected' : ''}><c:out value="${dept}"/></option>
                                        </c:forEach>
                                    </select>
                                    <span class="material-symbols-outlined text-lg absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none text-slate-500">expand_more</span>
                                </div>
                                <button type="submit" class="flex h-11 items-center gap-2 rounded-lg bg-slate-100 px-4 text-sm font-bold text-slate-500 hover:text-primary transition-colors">
                                    <span class="material-symbols-outlined text-lg">filter_list</span>
                                    ${language == 'zh' ? '筛选' : 'Filter'}
                                </button>
                                <c:if test="${workloadsActiveFilters}">
                                    <a href="${pageContext.request.contextPath}/workloads" class="flex h-11 items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 text-sm font-bold text-slate-600 hover:text-primary transition-colors">
                                        ${language == 'zh' ? '清除' : 'Clear'}
                                    </a>
                                </c:if>
                            </div>
                        </div>
                    </form>
                </c:if>

                <c:choose>
                    <c:when test="${workloadsState == 'loadError'}">
                        <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                            <jsp:param name="variant" value="error" />
                            <jsp:param name="icon" value="group_off" />
                            <jsp:param name="title" value="${language == 'zh' ? '工作量暂不可用' : 'Workloads unavailable'}" />
                            <jsp:param name="message" value="${language == 'zh' ? '当前无法加载工作量数据，请刷新页面后重试。' : 'Unable to load workload data right now. Please refresh the page and try again.'}" />
                            <jsp:param name="actionHref" value="${pageContext.request.contextPath}/workloads" />
                            <jsp:param name="actionLabel" value="${language == 'zh' ? '重试' : 'Try Again'}" />
                        </jsp:include>
                    </c:when>
                    <c:when test="${workloadsState == 'empty'}">
                        <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                            <jsp:param name="icon" value="group" />
                            <jsp:param name="title" value="${language == 'zh' ? '暂无可统计的数据' : 'No aggregate data available'}" />
                            <jsp:param name="message" value="${language == 'zh' ? '当前没有任何被录用的申请记录，无法生成工作量报表。' : 'There are currently no accepted applications to generate workload statistics.'}" />
                            <jsp:param name="actionHref" value="${pageContext.request.contextPath}/dashboard" />
                            <jsp:param name="actionLabel" value="${language == 'zh' ? '返回仪表盘' : 'Back to Dashboard'}" />
                            <jsp:param name="actionStyle" value="secondary" />
                        </jsp:include>
                    </c:when>
                    <c:when test="${workloadsState == 'noSearchResults'}">
                        <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                            <jsp:param name="icon" value="filter_alt_off" />
                            <jsp:param name="title" value="${language == 'zh' ? '没有符合筛选条件的助教' : 'No TAs match your filters'}" />
                            <jsp:param name="message" value="${language == 'zh' ? '请尝试清除筛选条件或调整搜索关键词。' : 'Try clearing filters or adjusting your search to see more results.'}" />
                            <jsp:param name="actionHref" value="${pageContext.request.contextPath}/workloads" />
                            <jsp:param name="actionLabel" value="${language == 'zh' ? '清除筛选' : 'Clear filters'}" />
                            <jsp:param name="actionStyle" value="secondary" />
                        </jsp:include>
                    </c:when>
                    <c:otherwise>

                        <!-- Summary Cards -->
                        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                            <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex flex-col justify-between">
                                <p class="text-sm font-medium text-slate-500">${language == 'zh' ? '已录用助教总数' : 'Total Accepted TAs'}</p>
                                <p class="text-3xl font-bold text-slate-900 mt-2">${totalAcceptedTAs}</p>
                            </div>
                            <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex flex-col justify-between">
                                <p class="text-sm font-medium text-slate-500">${language == 'zh' ? '所有助教每周总工时' : 'Total Weekly Hours'}</p>
                                <p class="text-3xl font-bold text-accent mt-2">${totalWeeklyHours} <span class="text-lg font-normal text-slate-400">hrs</span></p>
                            </div>
                            <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex flex-col justify-between">
                                <p class="text-sm font-medium text-slate-500">${language == 'zh' ? '总预估支出' : 'Total Estimated Income'}</p>
                                <p class="text-3xl font-bold text-emerald-600 mt-2">£ ${totalEstimatedIncome}</p>
                            </div>
                            <div class="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex flex-col justify-between">
                                <p class="text-sm font-medium text-slate-500">${language == 'zh' ? '超负荷人数' : 'Overloaded TAs'}</p>
                                <p class="text-3xl font-bold text-red-600 mt-2">${overloadedTAs}</p>
                            </div>
                        </div>

                        <!-- Main Workload Table -->
                        <div class="portal-panel overflow-hidden whitespace-nowrap">
                            <table class="w-full text-left text-sm">
                                <thead class="bg-slate-50 border-b border-slate-100 text-slate-500">
                                    <tr>
                                        <th class="px-6 py-4 font-semibold">${language == 'zh' ? '助教信息' : 'TA Info'}</th>
                                        <th class="px-6 py-4 font-semibold">${language == 'zh' ? '在岗岗位数' : 'Accepted Vacancies'}</th>
                                        <th class="px-6 py-4 font-semibold">${language == 'zh' ? '每周总工时' : 'Weekly Hours'}</th>
                                        <th class="px-6 py-4 font-semibold">${language == 'zh' ? '总工时(8周)' : 'Total Workload (8 wk)'}</th>
                                        <th class="px-6 py-4 font-semibold">${language == 'zh' ? '总预估收入(8周)' : 'Est. Income (8 wk)'}</th>
                                        <th class="px-6 py-4 font-semibold">${language == 'zh' ? '状态' : 'Status'}</th>
                                        <th class="px-6 py-4 font-semibold text-right">${language == 'zh' ? '操作' : 'Action'}</th>
                                    </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                    <c:forEach items="${workloads}" var="wl" varStatus="loop">
                                        <tr class="hover:bg-slate-50 transition-colors">
                                            <td class="px-6 py-4">
                                                <div class="font-medium text-slate-900"><c:out value="${empty wl.taName ? 'Unknown TA' : wl.taName}"/></div>
                                                <div class="text-xs text-slate-500 mt-0.5"><c:out value="${empty wl.studentId ? wl.taEmail : wl.studentId}"/></div>
                                                <c:if test="${not empty wl.department}">
                                                    <div class="text-xs text-slate-400 mt-0.5"><c:out value="${wl.department}"/></div>
                                                </c:if>
                                            </td>
                                            <td class="px-6 py-4 font-medium text-slate-700">${wl.acceptedVacancyCount}</td>
                                            <td class="px-6 py-4 text-slate-600">${wl.totalWeeklyHours} hrs</td>
                                            <td class="px-6 py-4 text-slate-600">${wl.totalWorkloadHours} hrs</td>
                                            <td class="px-6 py-4 font-medium text-emerald-600">£ ${wl.totalEstimatedIncome}</td>
                                            <td class="px-6 py-4">
                                                <c:choose>
                                                    <c:when test="${wl.workloadStatus == 'Normal'}">
                                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800">Normal</span>
                                                    </c:when>
                                                    <c:when test="${wl.workloadStatus == 'Busy'}">
                                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800">Busy</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-700">Overloaded</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="px-6 py-4 text-right">
                                                <button onclick="toggleDetails('details-${loop.index}')" class="text-accent hover:text-accent/80 font-medium text-sm flex items-center justify-end w-full gap-1">
                                                    Details <span class="material-symbols-outlined text-[16px]">expand_more</span>
                                                </button>
                                            </td>
                                        </tr>
                                        <!-- Expandable Details Row -->
                                        <tr id="details-${loop.index}" class="hidden bg-slate-50/50">
                                            <td colspan="7" class="px-6 py-4">
                                                <div class="pl-4 border-l-2 border-accent/20 my-2">
                                                    <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-3">Assigned Vacancies Details</h4>
                                                    <div class="grid gap-3">
                                                        <c:forEach items="${wl.assignedVacancies}" var="vac">
                                                            <div class="bg-white p-3 rounded-lg border border-slate-200 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
                                                                <div>
                                                                    <div class="font-medium text-sm text-slate-900"><c:out value="${vac.title}"/> (<c:out value="${vac.courseCode}"/>)</div>
                                                                    <div class="text-xs text-slate-500 mt-1">Department: <c:out value="${vac.department}"/> • MO: <c:out value="${vac.moduleOwner}"/></div>
                                                                </div>
                                                                <div class="flex items-center gap-6 text-sm text-slate-600">
                                                                    <div class="text-center">
                                                                        <div class="text-xs text-slate-400">Weekly</div>
                                                                        <div class="font-medium">${vac.weeklyHours} hrs</div>
                                                                    </div>
                                                                    <div class="text-center">
                                                                        <div class="text-xs text-slate-400">Rate</div>
                                                                        <div class="font-medium">£${vac.hourlyRate}/hr</div>
                                                                    </div>
                                                                    <div class="text-center bg-slate-50 px-3 py-1.5 rounded border border-slate-100">
                                                                        <div class="text-[10px] uppercase text-slate-400 font-bold mb-0.5">Est. Total (8w)</div>
                                                                        <div class="font-bold text-emerald-600">£${vac.estimatedIncome}</div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </c:forEach>
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>
<script>
    function toggleDetails(id) {
        const row = document.getElementById(id);
        if (row.classList.contains('hidden')) {
            row.classList.remove('hidden');
        } else {
            row.classList.add('hidden');
        }
    }
</script>
</body>
</html>
