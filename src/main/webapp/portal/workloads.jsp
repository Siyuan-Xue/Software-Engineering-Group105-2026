<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TA Workloads - QM HIRE</title>
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
<body class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
<div class="relative flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="portal-page">
                <div class="portal-page-header mb-6">
                    <div>
                        <h2 class="portal-page-title">TA Workloads</h2>
                        <p class="portal-page-copy">Monitor Teaching Assistant assignments and hours across all departments.</p>
                    </div>
                </div>

                <div class="portal-panel overflow-hidden">
                    <table class="w-full text-left text-sm">
                        <thead class="bg-slate-50 border-b border-slate-100 text-slate-500">
                            <tr>
                                <th class="px-6 py-4 font-semibold">TA Name</th>
                                <th class="px-6 py-4 font-semibold">Department</th>
                                <th class="px-6 py-4 font-semibold">Active Jobs</th>
                                <th class="px-6 py-4 font-semibold">Total Hours/Week</th>
                                <th class="px-6 py-4 font-semibold">Status</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-100">
                            <c:forEach items="${workloads}" var="wl">
                                <tr class="hover:bg-slate-50 transition-colors">
                                    <td class="px-6 py-4 font-medium text-slate-900"><c:out value="${wl.taName}"/></td>
                                    <td class="px-6 py-4 text-slate-600"><c:out value="${wl.department}"/></td>
                                    <td class="px-6 py-4 text-slate-600"><c:out value="${wl.activeJobsCount}"/></td>
                                    <td class="px-6 py-4 text-slate-600"><c:out value="${wl.totalHoursPerWeek}"/> hrs</td>
                                    <td class="px-6 py-4">
                                        <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800">
                                            <c:out value="${wl.status}"/>
                                        </span>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty workloads}">
                                <tr>
                                    <td colspan="5" class="px-6 py-8 text-center text-slate-500">No workload data available.</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
