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

                <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                    <jsp:param name="containerClass" value="mb-6" />
                </jsp:include>

                <div class="portal-page-header mb-6">
                    <div>
                        <h2 class="portal-page-title">${language == 'zh' ? '助教工作量' : 'TA Workloads'}</h2>
                        <p class="portal-page-copy">${language == 'zh' ? '查看各院系助教岗位分配和工时情况。' : 'Monitor Teaching Assistant assignments and hours across all departments.'}</p>
                    </div>
                </div>

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
                    <c:when test="${empty workloads}">
                        <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                            <jsp:param name="icon" value="group" />
                            <jsp:param name="title" value="${language == 'zh' ? '暂无工作量记录' : 'No workload records yet'}" />
                            <jsp:param name="message" value="${language == 'zh' ? '当前学期还没有形成可汇总的工作量记录。等岗位录用和分配产生后，这里会显示真实聚合结果。' : 'No workload records are available for the current term yet. Real aggregates will appear here once hiring and assignment records are created.'}" />
                            <jsp:param name="actionHref" value="${pageContext.request.contextPath}/dashboard" />
                            <jsp:param name="actionLabel" value="${language == 'zh' ? '返回仪表盘' : 'Back to Dashboard'}" />
                            <jsp:param name="actionStyle" value="secondary" />
                        </jsp:include>
                    </c:when>
                    <c:otherwise>
                        <div class="portal-panel overflow-hidden">
                            <table class="w-full text-left text-sm">
                                <thead class="bg-slate-50 border-b border-slate-100 text-slate-500">
                                    <tr>
                                        <th class="px-6 py-4 font-semibold">${language == 'zh' ? '助教姓名' : 'TA Name'}</th>
                                        <th class="px-6 py-4 font-semibold">${language == 'zh' ? '院系' : 'Department'}</th>
                                        <th class="px-6 py-4 font-semibold">${language == 'zh' ? '在岗岗位数' : 'Active Jobs'}</th>
                                        <th class="px-6 py-4 font-semibold">${language == 'zh' ? '每周总工时' : 'Total Hours/Week'}</th>
                                        <th class="px-6 py-4 font-semibold">${language == 'zh' ? '状态' : 'Status'}</th>
                                    </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                    <c:forEach items="${workloads}" var="wl">
                                        <tr class="hover:bg-slate-50 transition-colors">
                                            <td class="px-6 py-4 font-medium text-slate-900"><c:out value="${wl.taName}"/></td>
                                            <td class="px-6 py-4 text-slate-600"><c:out value="${wl.department}"/></td>
                                            <td class="px-6 py-4 text-slate-600"><c:out value="${wl.activeJobsCount}"/></td>
                                            <td class="px-6 py-4 text-slate-600"><c:out value="${wl.totalHoursPerWeek}"/> ${language == 'zh' ? '小时' : 'hrs'}</td>
                                            <td class="px-6 py-4">
                                                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${wl.status == 'Overloaded' ? 'bg-red-100 text-red-700' : 'bg-emerald-100 text-emerald-800'}">
                                                    <c:choose>
                                                        <c:when test="${wl.status == 'Overloaded'}">${language == 'zh' ? '超负荷' : 'Overloaded'}</c:when>
                                                        <c:otherwise>${language == 'zh' ? '正常' : 'Active'}</c:otherwise>
                                                    </c:choose>
                                                </span>
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
</body>
</html>
