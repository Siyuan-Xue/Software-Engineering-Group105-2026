<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${language == 'zh' ? '仪表盘 - QM HIRE' : 'Dashboard - QM HIRE'}</title>
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
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
    <div class="relative flex min-h-screen w-full flex-col">
        <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

        <div class="flex flex-1 overflow-hidden">
            <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

            <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
                <div class="portal-page">
                    <c:set var="dashboardState" value="${empty pageState ? 'normal' : pageState}" />
                    <c:set var="displayName" value="${empty userName ? (language == 'zh' ? '用户' : 'User') : userName}" />
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
                                <jsp:param name="title" value="${language == 'zh' ? '仪表盘暂不可用' : 'Dashboard unavailable'}" />
                                <jsp:param name="message" value="${language == 'zh' ? '当前无法加载仪表盘，请刷新页面后重试。' : 'Unable to load your dashboard right now. Please refresh the page or try again in a moment.'}" />
                                <jsp:param name="actionHref" value="${pageContext.request.contextPath}/dashboard" />
                                <jsp:param name="actionLabel" value="${language == 'zh' ? '重试' : 'Try Again'}" />
                            </jsp:include>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${activitiesEmpty or deadlinesEmpty}">
                                <div class="portal-callout border-amber-200 bg-amber-50/80 p-5">
                                    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                                        <div>
                                            <p class="text-xs font-bold uppercase tracking-[0.2em] text-amber-700">${language == 'zh' ? '仪表盘状态' : 'Dashboard Status'}</p>
                                            <c:choose>
                                                <c:when test="${activitiesEmpty and deadlinesEmpty}">
                                                    <h3 class="mt-2 text-lg font-bold text-slate-900">${language == 'zh' ? '你的仪表盘已准备就绪。' : 'Your dashboard is ready to populate.'}</h3>
                                                    <p class="mt-1 text-sm leading-relaxed text-slate-600">${language == 'zh' ? '开始浏览岗位并完善个人资料后，近期活动和截止日期就会显示在这里。' : 'Start browsing vacancies and updating your profile. Recent activity and deadlines will appear here as soon as your application journey begins.'}</p>
                                                </c:when>
                                                <c:when test="${activitiesEmpty}">
                                                    <h3 class="mt-2 text-lg font-bold text-slate-900">${language == 'zh' ? '活动记录仍为空。' : 'Activity feed is still empty.'}</h3>
                                                    <p class="mt-1 text-sm leading-relaxed text-slate-600">${language == 'zh' ? '当你提交申请、上传简历或收到院系更新后，最新动态会自动显示在这里。' : 'Once you apply, upload resumes, or receive updates from departments, your latest activity will appear here automatically.'}</p>
                                                </c:when>
                                                <c:otherwise>
                                                    <h3 class="mt-2 text-lg font-bold text-slate-900">${language == 'zh' ? '当前没有即将到期的事项。' : 'No deadlines are due right now.'}</h3>
                                                    <p class="mt-1 text-sm leading-relaxed text-slate-600">${language == 'zh' ? '你目前一切正常，请留意新的岗位和即将开放的申请窗口。' : 'You are all caught up for the moment. Keep an eye on new vacancies and upcoming application windows.'}</p>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <a href="${pageContext.request.contextPath}/vacancies" class="portal-btn portal-btn-primary">
                                            ${language == 'zh' ? '浏览岗位' : 'Explore Vacancies'}
                                        </a>
                                    </div>
                                </div>
                            </c:if>

                            <!-- Welcome Header -->
                            <div class="portal-page-header">
                                <div>
                                    <h2 class="portal-page-title">${language == 'zh' ? '欢迎回来，' : 'Welcome back, '}<c:out value="${displayName}"/>!</h2>
                                    <p class="portal-page-copy">
                                        <c:choose>
                                            <c:when test="${activitiesEmpty and deadlinesEmpty}">
                                                ${language == 'zh' ? '通过完善资料、上传简历并浏览开放岗位，开启你的申请流程。' : 'Build momentum by preparing your profile, uploading resumes, and exploring open TA opportunities.'}
                                            </c:when>
                                            <c:when test="${activitiesEmpty}">
                                                ${language == 'zh' ? '你的仪表盘已配置完成。只要你开始在系统中操作，活动记录就会逐步显示。' : 'Your dashboard is set up. The activity feed will start filling up as soon as you take action in the portal.'}
                                            </c:when>
                                            <c:when test="${deadlinesEmpty}">
                                                ${language == 'zh' ? '你的申请概览情况良好，请继续关注新的截止日期和提醒。' : 'Your application overview is in good shape. Watch this space for new deadlines and reminders.'}
                                            </c:when>
                                            <c:otherwise>
                                                ${language == 'zh' ? '这里是你今天的申请概览。' : 'Here is a summary of your academic journey today.'}
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="flex flex-wrap gap-3">
                                    <a href="${pageContext.request.contextPath}/resumes" class="portal-btn portal-btn-primary">
                                        <span class="material-symbols-outlined text-lg">upload</span>
                                        <c:choose>
                                            <c:when test="${resumeCount == 0}">${language == 'zh' ? '上传第一份简历' : 'Upload First Resume'}</c:when>
                                            <c:otherwise>${language == 'zh' ? '管理简历' : 'Manage Resumes'}</c:otherwise>
                                        </c:choose>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/vacancies" class="portal-btn portal-btn-secondary text-primary">
                                        <span class="material-symbols-outlined text-lg">search</span>
                                        ${language == 'zh' ? '浏览岗位' : 'Browse Vacancies'}
                                    </a>
                                </div>
                            </div>

                            <!-- Stats Grid -->
                            <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                                <c:choose>
                                    <c:when test="${userRole == 'MO'}">
                                        <div class="portal-stat-card flex flex-col gap-2">
                                            <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 mb-2">
                                                <span class="material-symbols-outlined">post_add</span>
                                            </div>
                                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">${language == 'zh' ? '已发布岗位' : 'Posted Vacancies'}</p>
                                            <p class="text-primary text-4xl font-black"><c:out value="${postedVacanciesCount != null ? postedVacanciesCount : 0}"/></p>
                                            <p class="text-xs text-slate-500">${language == 'zh' ? '你发布的岗位数量。' : 'Vacancies you have published.'}</p>
                                        </div>
                                        <div class="portal-stat-card flex flex-col gap-2">
                                            <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 mb-2">
                                                <span class="material-symbols-outlined">inbox</span>
                                            </div>
                                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">${language == 'zh' ? '收到的申请' : 'Received Applications'}</p>
                                            <p class="text-primary text-4xl font-black"><c:out value="${receivedApplicationsCount != null ? receivedApplicationsCount : 0}"/></p>
                                            <p class="text-xs text-slate-500">${language == 'zh' ? '你的岗位收到的申请总数。' : 'Total applications received for your vacancies.'}</p>
                                        </div>
                                        <div class="portal-stat-card flex flex-col gap-2">
                                            <div class="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 mb-2">
                                                <span class="material-symbols-outlined">pending_actions</span>
                                            </div>
                                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">${language == 'zh' ? '待审核' : 'Pending Review'}</p>
                                            <p class="text-primary text-4xl font-black"><c:out value="${underReviewCount != null ? underReviewCount : 0}"/></p>
                                            <p class="text-xs text-slate-500">${language == 'zh' ? '等待你审核的申请。' : 'Applications waiting for your review.'}</p>
                                        </div>
                                    </c:when>
                                    <c:when test="${userRole == 'ADMIN'}">
                                        <div class="portal-stat-card flex flex-col gap-2">
                                            <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 mb-2">
                                                <span class="material-symbols-outlined">group</span>
                                            </div>
                                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">${language == 'zh' ? '助教总数' : 'Total TAs'}</p>
                                            <p class="text-primary text-4xl font-black"><c:out value="${totalTAsCount != null ? totalTAsCount : 0}"/></p>
                                            <p class="text-xs text-slate-500">${language == 'zh' ? '已注册的助教人数。' : 'Registered Teaching Assistants.'}</p>
                                        </div>
                                        <div class="portal-stat-card flex flex-col gap-2">
                                            <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 mb-2">
                                                <span class="material-symbols-outlined">work</span>
                                            </div>
                                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">${language == 'zh' ? '开放岗位' : 'Active Vacancies'}</p>
                                            <p class="text-primary text-4xl font-black"><c:out value="${activeVacanciesCount != null ? activeVacanciesCount : 0}"/></p>
                                            <p class="text-xs text-slate-500">${language == 'zh' ? '当前开放的岗位数量。' : 'Currently open positions.'}</p>
                                        </div>
                                        <div class="portal-stat-card flex flex-col gap-2">
                                            <div class="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 mb-2">
                                                <span class="material-symbols-outlined">assignment_turned_in</span>
                                            </div>
                                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">${language == 'zh' ? '已分配助教' : 'Assigned TAs'}</p>
                                            <p class="text-primary text-4xl font-black">0</p>
                                            <p class="text-xs text-slate-500">${language == 'zh' ? '当前正在工作的助教。' : 'TAs currently working.'}</p>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="portal-stat-card flex flex-col gap-2">
                                            <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 mb-2">
                                                <span class="material-symbols-outlined">description</span>
                                            </div>
                                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">${language == 'zh' ? '已保存简历' : 'Saved Resumes'}</p>
                                            <p class="text-primary text-4xl font-black"><c:out value="${savedResumesCount != null ? savedResumesCount : 0}"/></p>
                                            <p class="text-xs text-slate-500">${language == 'zh' ? '至少保留一份最新简历，方便快速申请。' : 'Keep at least one up-to-date resume ready for quick applications.'}</p>
                                        </div>
                                        <div class="portal-stat-card flex flex-col gap-2">
                                            <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 mb-2">
                                                <span class="material-symbols-outlined">send</span>
                                            </div>
                                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">${language == 'zh' ? '已提交' : 'Submitted'}</p>
                                            <p class="text-primary text-4xl font-black"><c:out value="${submittedApplicationsCount != null ? submittedApplicationsCount : 0}"/></p>
                                            <p class="text-xs text-slate-500">${language == 'zh' ? '每一条已提交申请都会出现在下方状态和活动模块中。' : 'Every submitted application will feed the status and activity modules below.'}</p>
                                        </div>
                                        <div class="portal-stat-card flex flex-col gap-2">
                                            <div class="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 mb-2">
                                                <span class="material-symbols-outlined">pending_actions</span>
                                            </div>
                                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">${language == 'zh' ? '审核中' : 'Under Review'}</p>
                                            <p class="text-primary text-4xl font-black"><c:out value="${underReviewApplicationsCount != null ? underReviewApplicationsCount : 0}"/></p>
                                            <p class="text-xs text-slate-500">${language == 'zh' ? '保持关注，避免错过招聘方的后续联系。' : 'Stay responsive so you do not miss follow-up requests from recruiters.'}</p>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
                                <!-- Recent Activity -->
                                <div class="lg:col-span-2 space-y-4">
                                    <div class="portal-section-bar">
                                        <h3 class="portal-section-title">${language == 'zh' ? '近期活动' : 'Recent Activities'}</h3>
                                        <a href="${pageContext.request.contextPath}/applications" class="portal-section-link">${language == 'zh' ? '查看申请' : 'View Applications'}</a>
                                    </div>
                                    <div class="portal-panel overflow-hidden">
                                        <div class="divide-y divide-primary/5">
                                            <c:choose>
                                                <c:when test="${activitiesEmpty}">
                                                    <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                        <jsp:param name="icon" value="history" />
                                                        <jsp:param name="title" value="${language == 'zh' ? '暂无近期活动' : 'No recent activities'}" />
                                                        <jsp:param name="message" value="${language == 'zh' ? '当你开始使用系统后，最新申请、更新和提醒会显示在这里。' : 'Your latest applications, updates, and reminders will appear here once you start using the portal.'}" />
                                                        <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                                        <jsp:param name="actionLabel" value="${language == 'zh' ? '浏览岗位' : 'Browse Vacancies'}" />
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
                                        <h3 class="portal-section-title">${language == 'zh' ? '待处理事项' : 'Action Required'}</h3>
                                        <span class="rounded-full px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider ${profileCompletion < 100 ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700'}">
                                            <c:choose>
                                                <c:when test="${profileCompletion < 100}">${language == 'zh' ? '需更新' : 'Needs update'}</c:when>
                                                <c:otherwise>${language == 'zh' ? '已就绪' : 'Ready'}</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                    <div class="portal-panel portal-panel--accent relative overflow-hidden p-6 group">
                                        <div class="absolute -right-4 -top-4 w-24 h-24 bg-white/10 rounded-full blur-2xl group-hover:scale-150 transition-transform"></div>
                                        <h4 class="font-bold text-lg mb-2 relative z-10 text-slate-900">
                                            <c:choose>
                                                <c:when test="${profileCompletion < 100}">${language == 'zh' ? '完善你的资料' : 'Complete Your Profile'}</c:when>
                                                <c:otherwise>${language == 'zh' ? '资料已准备就绪' : 'Profile Ready'}</c:otherwise>
                                            </c:choose>
                                        </h4>
                                        <p class="text-sm text-slate-600 mb-4 relative z-10">
                                            <c:choose>
                                                <c:when test="${profileCompletion < 100}">
                                                    ${language == 'zh' ? '你的资料已完成 ' : 'Your profile is '}<c:out value="${profileCompletion}"/>%${language == 'zh' ? '。补充缺失信息可让招聘方更快查看你的资料。' : ' complete. Add missing details so recruiters can review your information faster.'}
                                                </c:when>
                                                <c:otherwise>
                                                    ${language == 'zh' ? '你的资料已完整设置，可以查看设置或在下一轮申请前更新材料。' : 'Your profile is fully set up. Review your settings or keep your documents fresh before the next application cycle.'}
                                                </c:otherwise>
                                            </c:choose>
                                        </p>
                                        <a href="${pageContext.request.contextPath}/settings" class="portal-btn portal-btn-primary relative z-10 w-full">
                                            <c:choose>
                                                <c:when test="${profileCompletion < 100}">${language == 'zh' ? '更新资料' : 'Update Profile'}</c:when>
                                                <c:otherwise>${language == 'zh' ? '查看设置' : 'Review Settings'}</c:otherwise>
                                            </c:choose>
                                        </a>
                                    </div>

                                    <div class="portal-panel p-6">
                                        <h4 class="portal-section-title mb-4">${language == 'zh' ? '即将到期' : 'Upcoming Deadlines'}</h4>
                                        <c:choose>
                                            <c:when test="${deadlinesEmpty}">
                                                <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                    <jsp:param name="icon" value="event_busy" />
                                                    <jsp:param name="title" value="${language == 'zh' ? '暂无即将到期事项' : 'No upcoming deadlines'}" />
                                                    <jsp:param name="message" value="${language == 'zh' ? '有新的岗位截止日期或提醒时，会显示在这里。' : 'New vacancy deadlines and reminders will be listed here when they are available.'}" />
                                                    <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                                    <jsp:param name="actionLabel" value="${language == 'zh' ? '查看开放岗位' : 'See Open Roles'}" />
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
