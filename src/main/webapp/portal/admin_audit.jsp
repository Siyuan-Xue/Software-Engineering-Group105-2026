<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${language == 'zh' ? '审计日志 - QM HIRE' : 'Audit Logs - QM HIRE'}</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
</head>
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
<div class="relative flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />
    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />
        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="portal-page">
                <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                    <jsp:param name="containerClass" value="mb-6" />
                </jsp:include>

                <div class="portal-page-header mb-6">
                    <div>
                        <h2 class="portal-page-title">${language == 'zh' ? '审计日志' : 'Audit Logs'}</h2>
                        <p class="portal-page-copy">${language == 'zh' ? '按操作者、动作、实体和日期核查关键系统变更，并导出 CSV。' : 'Review key system changes by operator, action, entity, and date, with CSV export.'}</p>
                    </div>
                    <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-bold text-slate-700 shadow-sm">
                        <span class="text-slate-400">${language == 'zh' ? '当前结果' : 'Results'}</span>
                        <span class="ml-2 text-slate-950"><c:out value="${fn:length(logs)}"/></span>
                    </div>
                </div>

                <form action="${pageContext.request.contextPath}/admin/audit" method="GET" class="portal-filter-bar">
                    <div class="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-6">
                        <select name="operatorId" class="rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-sm font-medium text-slate-700">
                            <option value="">${language == 'zh' ? '操作者：全部' : 'Operator: All'}</option>
                            <c:forEach items="${users}" var="user">
                                <option value="${user.id}" ${param.operatorId == user.id ? 'selected' : ''}><c:out value="${user.fullName}"/> · <c:out value="${user.role}"/></option>
                            </c:forEach>
                        </select>
                        <select name="action" class="rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-sm font-medium text-slate-700">
                            <option value="">${language == 'zh' ? '动作：全部' : 'Action: All'}</option>
                            <c:forEach items="${auditActions}" var="action">
                                <option value="${action}" ${param.action == action ? 'selected' : ''}><c:out value="${action}"/></option>
                            </c:forEach>
                        </select>
                        <select name="entityType" class="rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-sm font-medium text-slate-700">
                            <option value="">${language == 'zh' ? '实体：全部' : 'Entity: All'}</option>
                            <c:forEach items="${entityTypes}" var="entityType">
                                <option value="${entityType}" ${param.entityType == entityType ? 'selected' : ''}><c:out value="${entityType}"/></option>
                            </c:forEach>
                        </select>
                        <input type="date" name="from" value="${param.from}" class="rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-sm font-medium text-slate-700" />
                        <input type="date" name="to" value="${param.to}" class="rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-sm font-medium text-slate-700" />
                        <div class="flex gap-2">
                            <button type="submit" class="portal-btn portal-btn-primary flex-1 text-sm">
                                <span class="material-symbols-outlined text-sm">filter_list</span>
                                ${language == 'zh' ? '筛选' : 'Filter'}
                            </button>
                            <button type="submit" name="export" value="csv" class="portal-btn portal-btn-secondary text-sm">
                                <span class="material-symbols-outlined text-sm">download</span>
                                CSV
                            </button>
                        </div>
                    </div>
                </form>

                <section class="portal-panel overflow-hidden">
                    <c:choose>
                        <c:when test="${empty logs}">
                            <div class="p-8">
                                <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                    <jsp:param name="icon" value="fact_check" />
                                    <jsp:param name="title" value="${language == 'zh' ? '没有审计记录' : 'No audit logs'}" />
                                    <jsp:param name="message" value="${language == 'zh' ? '当前筛选条件下没有可显示的日志。' : 'No logs match the current filters.'}" />
                                    <jsp:param name="actionHref" value="${pageContext.request.contextPath}/admin/audit" />
                                    <jsp:param name="actionLabel" value="${language == 'zh' ? '清除筛选' : 'Clear filters'}" />
                                </jsp:include>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="overflow-x-auto">
                                <table class="w-full text-left text-sm">
                                    <thead class="border-b border-slate-100 bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                                    <tr>
                                        <th class="px-6 py-4 font-bold">${language == 'zh' ? '时间' : 'Time'}</th>
                                        <th class="px-6 py-4 font-bold">${language == 'zh' ? '操作者' : 'Operator'}</th>
                                        <th class="px-6 py-4 font-bold">${language == 'zh' ? '动作' : 'Action'}</th>
                                        <th class="px-6 py-4 font-bold">${language == 'zh' ? '实体' : 'Entity'}</th>
                                        <th class="px-6 py-4 font-bold">${language == 'zh' ? '详情' : 'Details'}</th>
                                    </tr>
                                    </thead>
                                    <tbody class="divide-y divide-slate-100">
                                    <c:forEach items="${logs}" var="log">
                                        <tr class="align-top hover:bg-slate-50">
                                            <td class="whitespace-nowrap px-6 py-4 text-slate-600"><c:out value="${log.operatedAt}"/></td>
                                            <td class="px-6 py-4">
                                                <p class="font-semibold text-slate-900"><c:out value="${empty log.operator ? log.operatorId : log.operator}"/></p>
                                                <p class="mt-1 max-w-64 break-all text-xs text-slate-400"><c:out value="${log.operatorId}"/></p>
                                            </td>
                                            <td class="px-6 py-4">
                                                <span class="inline-flex rounded-full bg-slate-100 px-3 py-1 text-xs font-bold text-slate-700"><c:out value="${log.action}"/></span>
                                            </td>
                                            <td class="px-6 py-4">
                                                <p class="font-semibold text-slate-800"><c:out value="${log.entityType}"/></p>
                                                <p class="mt-1 max-w-64 break-all text-xs text-slate-400"><c:out value="${log.entityId}"/></p>
                                            </td>
                                            <td class="px-6 py-4">
                                                <details class="max-w-xl">
                                                    <summary class="cursor-pointer text-xs font-bold text-primary">${language == 'zh' ? '查看 JSON' : 'View JSON'}</summary>
                                                    <div class="mt-3 grid gap-3 lg:grid-cols-2">
                                                        <div>
                                                            <p class="mb-1 text-[11px] font-bold uppercase tracking-wider text-slate-400">${language == 'zh' ? '旧值' : 'Old'}</p>
                                                            <pre class="max-h-48 overflow-auto rounded-xl bg-slate-900 p-3 text-xs text-slate-100"><c:out value="${log.oldValue}"/></pre>
                                                        </div>
                                                        <div>
                                                            <p class="mb-1 text-[11px] font-bold uppercase tracking-wider text-slate-400">${language == 'zh' ? '新值' : 'New'}</p>
                                                            <pre class="max-h-48 overflow-auto rounded-xl bg-slate-900 p-3 text-xs text-slate-100"><c:out value="${log.newValue}"/></pre>
                                                        </div>
                                                    </div>
                                                </details>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>
        </main>
    </div>
</div>
</body>
</html>
