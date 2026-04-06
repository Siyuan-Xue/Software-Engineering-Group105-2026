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
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
</head>
<body class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
    <div class="relative flex min-h-screen w-full flex-col">
        <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

        <div class="flex flex-1 overflow-hidden">
            <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

            <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
                <div class="portal-page">
                    <c:set var="dashboardState" value="${empty pageState ? 'normal' : pageState}" />
                    <c:set var="displayName" value="${empty userName ? 'User' : userName}" />
                    <c:set var="resumeCount" value="${empty savedResumesCount ? 0 : savedResumesCount}" />
                    <c:set var="submittedCount" value="${empty submittedApplicationsCount ? 0 : submittedApplicationsCount}" />
                    <c:set var="underReviewCount" value="${empty underReviewApplicationsCount ? 0 : underReviewApplicationsCount}" />
                    <c:set var="profileCompletion" value="${empty profileCompletionPercentage ? 0 : profileCompletionPercentage}" />
                    <c:set var="activitiesEmpty" value="${dashboardState == 'emptyActivities' or empty recentActivities}" />
                    <c:set var="deadlinesEmpty" value="${dashboardState == 'emptyDeadlines' or empty upcomingDeadlines}" />
                    <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp" />
                    <c:choose>
                        <c:when test="${dashboardState == 'loadError'}">
                            <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                                <jsp:param name="variant" value="error" />
                                <jsp:param name="icon" value="dashboard_customize" />
                                <jsp:param name="title" value="Dashboard unavailable" />
                                <jsp:param name="message" value="We couldn't load your dashboard right now. Please refresh the page or try again in a moment." />
                                <jsp:param name="actionHref" value="${pageContext.request.contextPath}/dashboard" />
                                <jsp:param name="actionLabel" value="Try Again" />
                            </jsp:include>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${activitiesEmpty or deadlinesEmpty}">
                                <div class="portal-callout border-amber-200 bg-amber-50/80 p-5">
                                    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                                        <div>
                                            <p class="text-xs font-bold uppercase tracking-[0.2em] text-amber-700">Dashboard Status</p>
                                            <c:choose>
                                                <c:when test="${activitiesEmpty and deadlinesEmpty}">
                                                    <h3 class="mt-2 text-lg font-bold text-slate-900">Your dashboard is ready to populate.</h3>
                                                    <p class="mt-1 text-sm leading-relaxed text-slate-600">Start browsing vacancies and updating your profile. Recent activity and deadlines will appear here as soon as your application journey begins.</p>
                                                </c:when>
                                                <c:when test="${activitiesEmpty}">
                                                    <h3 class="mt-2 text-lg font-bold text-slate-900">Activity feed is still empty.</h3>
                                                    <p class="mt-1 text-sm leading-relaxed text-slate-600">Once you apply, upload resumes, or receive updates from departments, your latest activity will appear here automatically.</p>
                                                </c:when>
                                                <c:otherwise>
                                                    <h3 class="mt-2 text-lg font-bold text-slate-900">No deadlines are due right now.</h3>
                                                    <p class="mt-1 text-sm leading-relaxed text-slate-600">You're all caught up for the moment. Keep an eye on new vacancies and upcoming application windows.</p>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <a href="${pageContext.request.contextPath}/vacancies" class="portal-btn portal-btn-primary">
                                            Explore Vacancies
                                        </a>
                                    </div>
                                </div>
                            </c:if>

                            <!-- Welcome Header -->
                            <div class="portal-page-header">
                                <div>
                                    <h2 class="portal-page-title">Welcome back, <c:out value="${displayName}"/>!</h2>
                                    <p class="portal-page-copy">
                                        <c:choose>
                                            <c:when test="${activitiesEmpty and deadlinesEmpty}">
                                                Build momentum by preparing your profile, uploading resumes, and exploring open TA opportunities.
                                            </c:when>
                                            <c:when test="${activitiesEmpty}">
                                                Your dashboard is set up. The activity feed will start filling up as soon as you take action in the portal.
                                            </c:when>
                                            <c:when test="${deadlinesEmpty}">
                                                Your application overview is in good shape. Watch this space for new deadlines and reminders.
                                            </c:when>
                                            <c:otherwise>
                                                Here's a summary of your academic journey today.
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="flex flex-wrap gap-3">
                                    <a href="${pageContext.request.contextPath}/resumes" class="portal-btn portal-btn-primary">
                                        <span class="material-symbols-outlined text-lg">upload</span>
                                        <c:choose>
                                            <c:when test="${resumeCount == 0}">Upload First Resume</c:when>
                                            <c:otherwise>Manage Resumes</c:otherwise>
                                        </c:choose>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/vacancies" class="portal-btn portal-btn-secondary text-primary">
                                        <span class="material-symbols-outlined text-lg">search</span>
                                        Browse Vacancies
                                    </a>
                                </div>
                            </div>

                            <!-- Stats Grid -->
                            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                                <div class="portal-stat-card flex flex-col gap-2">
                                    <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 mb-2">
                                        <span class="material-symbols-outlined">description</span>
                                    </div>
                                    <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">Saved Resumes</p>
                                    <p class="text-primary text-4xl font-black"><c:out value="${resumeCount}"/></p>
                                    <p class="text-xs text-slate-500">Keep at least one up-to-date resume ready for quick applications.</p>
                                </div>
                                <div class="portal-stat-card flex flex-col gap-2">
                                    <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 mb-2">
                                        <span class="material-symbols-outlined">send</span>
                                    </div>
                                    <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">Submitted</p>
                                    <p class="text-primary text-4xl font-black"><c:out value="${submittedCount}"/></p>
                                    <p class="text-xs text-slate-500">Every submitted application will feed the status and activity modules below.</p>
                                </div>
                                <div class="portal-stat-card flex flex-col gap-2">
                                    <div class="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 mb-2">
                                        <span class="material-symbols-outlined">pending_actions</span>
                                    </div>
                                    <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">Under Review</p>
                                    <p class="text-primary text-4xl font-black"><c:out value="${underReviewCount}"/></p>
                                    <p class="text-xs text-slate-500">Stay responsive so you don't miss follow-up requests from recruiters.</p>
                                </div>
                            </div>

                            <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
                                <!-- Recent Activity -->
                                <div class="lg:col-span-2 space-y-4">
                                    <div class="portal-section-bar">
                                        <h3 class="portal-section-title">Recent Activities</h3>
                                        <a href="${pageContext.request.contextPath}/applications" class="portal-section-link">View Applications</a>
                                    </div>
                                    <div class="portal-panel overflow-hidden">
                                        <div class="divide-y divide-primary/5">
                                            <c:choose>
                                                <c:when test="${activitiesEmpty}">
                                                    <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                        <jsp:param name="icon" value="history" />
                                                        <jsp:param name="title" value="No recent activities" />
                                                        <jsp:param name="message" value="Your latest applications, updates, and reminders will appear here once you start using the portal." />
                                                        <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                                        <jsp:param name="actionLabel" value="Browse Vacancies" />
                                                    </jsp:include>
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
                                    <div class="portal-section-bar">
                                        <h3 class="portal-section-title">Action Required</h3>
                                        <span class="rounded-full px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider ${profileCompletion < 100 ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700'}">
                                            <c:choose>
                                                <c:when test="${profileCompletion < 100}">Needs update</c:when>
                                                <c:otherwise>Ready</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="portal-panel portal-panel--accent relative overflow-hidden p-6 group">
                                        <div class="absolute -right-4 -top-4 w-24 h-24 bg-white/10 rounded-full blur-2xl group-hover:scale-150 transition-transform"></div>
                                        <h4 class="font-bold text-lg mb-2 relative z-10 text-slate-900">
                                            <c:choose>
                                                <c:when test="${profileCompletion < 100}">Complete Your Profile</c:when>
                                                <c:otherwise>Profile Ready</c:otherwise>
                                            </c:choose>
                                        </h4>
                                        <p class="text-sm text-slate-600 mb-4 relative z-10">
                                            <c:choose>
                                                <c:when test="${profileCompletion < 100}">
                                                    Your profile is <c:out value="${profileCompletion}"/>% complete. Add missing details so recruiters can review your information faster.
                                                </c:when>
                                                <c:otherwise>
                                                    Your profile is fully set up. Review your settings or keep your documents fresh before the next application cycle.
                                                </c:otherwise>
                                            </c:choose>
                                        </p>
                                        <a href="${pageContext.request.contextPath}/settings" class="portal-btn portal-btn-primary relative z-10 w-full">
                                            <c:choose>
                                                <c:when test="${profileCompletion < 100}">Update Profile</c:when>
                                                <c:otherwise>Review Settings</c:otherwise>
                                            </c:choose>
                                        </a>
                                    </div>

                                    <div class="portal-panel p-6">
                                        <h4 class="portal-section-title mb-4">Upcoming Deadlines</h4>
                                        <c:choose>
                                            <c:when test="${deadlinesEmpty}">
                                                <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                    <jsp:param name="icon" value="event_busy" />
                                                    <jsp:param name="title" value="No upcoming deadlines" />
                                                    <jsp:param name="message" value="New vacancy deadlines and reminders will be listed here when they are available." />
                                                    <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                                    <jsp:param name="actionLabel" value="See Open Roles" />
                                                    <jsp:param name="containerClass" value="px-0 py-4" />
                                                </jsp:include>
                                            </c:when>
                                            <c:otherwise>
                                                <ul class="space-y-4">
                                                    <c:forEach items="${upcomingDeadlines}" var="deadline">
                                                        <li class="flex items-start gap-3">
                                                            <div class="w-2 h-2 mt-1.5 rounded-full ${deadline.colorClass}"></div>
                                                            <div>
                                                                <p class="text-sm font-bold text-slate-700"><c:out value="${deadline.title}"/></p>
                                                                <p class="text-xs ${deadline.textColorClass} font-semibold"><c:out value="${deadline.timeRemaining}"/></p>
                                                            </div>
                                                        </li>
                                                    </c:forEach>
                                                </ul>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
