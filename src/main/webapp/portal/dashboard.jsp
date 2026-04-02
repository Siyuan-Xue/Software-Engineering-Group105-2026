<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - QM HIRE</title>
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
</head>
<body class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
    <div class="relative flex min-h-screen w-full flex-col">
        <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

        <div class="flex flex-1 overflow-hidden">
            <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

            <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
                <div class="max-w-5xl mx-auto space-y-8">
                    <%-- Error and Success Messages --%>
                    <c:if test="${not empty errorMessage}">
                        <div class="mb-6 p-4 bg-red-50 border border-red-200 text-red-600 rounded-xl flex items-start gap-3">
                            <span class="material-symbols-outlined text-red-500 shrink-0">error</span>
                            <p class="text-sm font-medium">${errorMessage}</p>
                        </div>
                    </c:if>
                    <c:if test="${not empty successMessage}">
                        <div class="mb-6 p-4 bg-green-50 border border-green-200 text-green-600 rounded-xl flex items-start gap-3">
                            <span class="material-symbols-outlined text-green-500 shrink-0">check_circle</span>
                            <p class="text-sm font-medium">${successMessage}</p>
                        </div>
                    </c:if>

                    <!-- Welcome Header -->
                    <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
                        <div>
                            <h2 class="text-primary text-4xl font-black tracking-tight mb-2">Welcome back, <c:out value="${userName != null ? userName : 'User'}"/>!</h2>
                            <p class="text-slate-500 text-lg">Here's a summary of your academic journey today.</p>
                        </div>
                        <div class="flex gap-3">
                            <a href="${pageContext.request.contextPath}/portal/resumes.jsp" class="flex items-center gap-2 bg-primary text-white px-5 py-2.5 rounded-xl font-bold text-sm shadow-lg shadow-primary/20 hover:scale-105 transition-transform">
                                <span class="material-symbols-outlined text-lg">upload</span>
                                Upload New Resume
                            </a>
                            <a href="${pageContext.request.contextPath}/portal/vacancies.jsp" class="flex items-center gap-2 bg-white border border-primary/10 text-primary px-5 py-2.5 rounded-xl font-bold text-sm shadow-sm hover:bg-slate-50 transition-colors">
                                <span class="material-symbols-outlined text-lg">search</span>
                                Find Vacancies
                            </a>
                        </div>
                    </div>

                    <!-- Stats Grid -->
                    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div class="flex flex-col gap-2 rounded-2xl bg-white p-6 border border-primary/5 shadow-sm hover:shadow-md transition-shadow">
                            <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 mb-2">
                                <span class="material-symbols-outlined">description</span>
                            </div>
                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">Saved Resumes</p>
                            <p class="text-primary text-4xl font-black"><c:out value="${savedResumesCount != null ? savedResumesCount : 0}"/></p>
                        </div>
                        <div class="flex flex-col gap-2 rounded-2xl bg-white p-6 border border-primary/5 shadow-sm hover:shadow-md transition-shadow border-l-4 border-l-primary">
                            <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 mb-2">
                                <span class="material-symbols-outlined">send</span>
                            </div>
                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">Submitted</p>
                            <p class="text-primary text-4xl font-black"><c:out value="${submittedApplicationsCount != null ? submittedApplicationsCount : 0}"/></p>
                        </div>
                        <div class="flex flex-col gap-2 rounded-2xl bg-white p-6 border border-primary/5 shadow-sm hover:shadow-md transition-shadow">
                            <div class="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 mb-2">
                                <span class="material-symbols-outlined">pending_actions</span>
                            </div>
                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">Under Review</p>
                            <p class="text-primary text-4xl font-black"><c:out value="${underReviewApplicationsCount != null ? underReviewApplicationsCount : 0}"/></p>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
                        <!-- Recent Activity -->
                        <div class="lg:col-span-2 space-y-4">
                            <div class="flex items-center justify-between px-2">
                                <h3 class="text-primary text-xl font-bold">Recent Activities</h3>
                                <button class="text-sm font-bold text-primary hover:underline">View All</button>
                            </div>
                            <div class="bg-white rounded-2xl border border-primary/5 shadow-sm overflow-hidden">
                                <div class="divide-y divide-primary/5">
                                    <c:choose>
                                        <c:when test="${empty recentActivities}">
                                            <div class="p-5 text-center text-slate-500 text-sm">
                                                No recent activities.
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach items="${recentActivities}" var="activity">
                                                <div class="flex items-center gap-4 p-5 hover:bg-slate-50 transition-colors">
                                                    <div class="w-10 h-10 rounded-xl ${activity.iconBgClass} flex items-center justify-center ${activity.iconColorClass}">
                                                        <span class="material-symbols-outlined text-xl"><c:out value="${activity.icon}"/></span>
                                                    </div>
                                                    <div class="flex-1">
                                                        <p class="text-sm font-bold text-primary"><c:out value="${activity.title}"/></p>
                                                        <p class="text-xs text-slate-500"><c:out value="${activity.description}"/></p>
                                                    </div>
                                                    <div class="text-right">
                                                        <p class="text-xs font-semibold text-slate-400"><c:out value="${activity.timeAgo}"/></p>
                                                        <c:if test="${not empty activity.statusBadge}">
                                                            <p class="text-[10px] font-bold ${activity.statusBadgeClass} px-2 py-0.5 rounded-full inline-block mt-1"><c:out value="${activity.statusBadge}"/></p>
                                                        </c:if>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>

                        <!-- Sidebar Actions / Reminders -->
                        <div class="space-y-6">
                            <h3 class="text-primary text-xl font-bold px-2">Action Required</h3>
                            <div class="bg-primary text-white rounded-2xl p-6 shadow-xl shadow-primary/20 relative overflow-hidden group">
                                <div class="absolute -right-4 -top-4 w-24 h-24 bg-white/10 rounded-full blur-2xl group-hover:scale-150 transition-transform"></div>
                                <h4 class="font-bold text-lg mb-2 relative z-10">Complete Bio-Data</h4>
                                <p class="text-sm text-white/70 mb-4 relative z-10">Your profile is <c:out value="${profileCompletionPercentage != null ? profileCompletionPercentage : 0}"/>% complete. Add your references to stand out to recruiters.</p>
                                <button class="w-full py-2.5 bg-white text-primary rounded-xl font-bold text-sm shadow-md hover:bg-slate-100 transition-colors relative z-10">Update Profile</button>
                            </div>

                            <div class="bg-white rounded-2xl p-6 border border-primary/5 shadow-sm">
                                <h4 class="font-bold text-primary mb-4">Upcoming Deadlines</h4>
                                <ul class="space-y-4">
                                    <c:choose>
                                        <c:when test="${empty upcomingDeadlines}">
                                            <li class="text-sm text-slate-500">No upcoming deadlines.</li>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach items="${upcomingDeadlines}" var="deadline">
                                                <li class="flex items-start gap-3">
                                                    <div class="w-2 h-2 mt-1.5 rounded-full ${deadline.colorClass}"></div>
                                                    <div>
                                                        <p class="text-sm font-bold text-slate-700"><c:out value="${deadline.title}"/></p>
                                                        <p class="text-xs ${deadline.textColorClass} font-semibold"><c:out value="${deadline.timeRemaining}"/></p>
                                                    </div>
                                                </li>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
