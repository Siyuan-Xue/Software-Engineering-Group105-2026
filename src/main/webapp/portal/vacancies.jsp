<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                <div class="max-w-6xl mx-auto w-full">
                    <!-- Page Header -->
                    <div class="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-8">
                        <div>
                            <h2 class="text-slate-900 text-3xl font-black tracking-tight">Available Vacancies</h2>
                            <p class="text-slate-500 text-sm mt-2">Browse and apply for open Teaching Assistant positions across departments.</p>
                        </div>
                        <div class="flex gap-3">
                            <button class="bg-white border border-slate-200 text-slate-700 px-4 py-2.5 rounded-lg text-sm font-bold flex items-center gap-2 hover:bg-slate-50 transition-colors shadow-sm">
                                <span class="material-symbols-outlined text-sm">notifications_active</span>
                                Job Alerts
                            </button>
                        </div>
                    </div>

                    <!-- Search and Filters -->
                    <form action="${pageContext.request.contextPath}/vacancies" method="GET" class="bg-white border border-slate-200 rounded-xl p-4 mb-8 shadow-sm">
                        <div class="flex flex-col lg:flex-row gap-4">
                            <div class="flex-1">
                                <label class="flex flex-col w-full">
                                    <div class="flex w-full items-center rounded-lg bg-slate-100 px-4 h-11 border border-transparent focus-within:border-primary/30 transition-all">
                                        <span class="material-symbols-outlined text-slate-400">search</span>
                                        <input type="text" name="keyword" value="${param.keyword}" class="w-full bg-transparent border-none focus:ring-0 text-slate-900 placeholder:text-slate-400 text-sm font-medium pl-3 outline-none" placeholder="Search by course code, title, or keywords..." />
                                    </div>
                                </label>
                            </div>
                            <div class="flex flex-wrap gap-2">
                                <div class="relative group">
                                    <select name="department" class="flex h-11 items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors appearance-none pr-8">
                                        <option value="">Department</option>
                                        <option value="CS" ${param.department == 'CS' ? 'selected' : ''}>Computer Science</option>
                                        <option value="MATH" ${param.department == 'MATH' ? 'selected' : ''}>Mathematics</option>
                                    </select>
                                    <span class="material-symbols-outlined text-lg absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none">expand_more</span>
                                </div>
                                <div class="relative group">
                                    <select name="term" class="flex h-11 items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors appearance-none pr-8">
                                        <option value="">Term</option>
                                        <option value="Fall 2024" ${param.term == 'Fall 2024' ? 'selected' : ''}>Fall 2024</option>
                                        <option value="Spring 2025" ${param.term == 'Spring 2025' ? 'selected' : ''}>Spring 2025</option>
                                    </select>
                                    <span class="material-symbols-outlined text-lg absolute right-2 top-1/2 -translate-y-1/2 pointer-events-none">expand_more</span>
                                </div>
                                <button type="submit" class="flex h-11 items-center gap-2 rounded-lg bg-primary px-6 text-sm font-bold text-white hover:bg-primary/90 transition-colors shadow-md">
                                    Search
                                </button>
                            </div>
                        </div>
                    </form>

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

                    <!-- Vacancy List -->
                    <div class="space-y-4">
                        <c:choose>
                            <c:when test="${empty vacancies}">
                                <div class="bg-white border border-slate-200 rounded-xl p-12 text-center shadow-sm">
                                    <span class="material-symbols-outlined text-6xl text-slate-300 mb-4">search_off</span>
                                    <h3 class="text-xl font-bold text-slate-900 mb-2">No vacancies found</h3>
                                    <p class="text-slate-500">Try adjusting your search filters or check back later for new opportunities.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${vacancies}" var="vacancy">
                                    <!-- Vacancy Card -->
                                    <div class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow group">
                                        <div class="flex flex-col md:flex-row gap-6">
                                            <div class="flex-1">
                                                <div class="flex items-center gap-3 mb-2">
                                                    <span class="px-2.5 py-1 bg-primary/10 text-primary text-xs font-bold uppercase rounded-md tracking-wider"><c:out value="${vacancy.courseCode}"/></span>
                                                    <a href="${pageContext.request.contextPath}/vacancy?vacancyId=${vacancy.vacancyId}" class="text-xl font-bold text-slate-900 hover:text-primary transition-colors cursor-pointer"><c:out value="${vacancy.title}"/></a>
                                                </div>
                                                <p class="text-sm text-slate-600 mb-4 line-clamp-2 leading-relaxed">
                                                    <c:out value="${vacancy.description}"/>
                                                </p>
                                                <div class="flex flex-wrap items-center gap-4 text-sm text-slate-500">
                                                    <div class="flex items-center gap-1.5">
                                                        <span class="material-symbols-outlined text-[18px]">domain</span>
                                                        <c:out value="${vacancy.department}"/>
                                                    </div>
                                                    <div class="flex items-center gap-1.5">
                                                        <span class="material-symbols-outlined text-[18px]">schedule</span>
                                                        <c:out value="${vacancy.hoursPerWeek}"/> hrs/week
                                                    </div>
                                                    <div class="flex items-center gap-1.5">
                                                        <span class="material-symbols-outlined text-[18px]">payments</span>
                                                        $<c:out value="${vacancy.hourlyRate}"/>/hr
                                                    </div>
                                                    <div class="flex items-center gap-1.5 text-amber-600 font-medium">
                                                        <span class="material-symbols-outlined text-[18px]">event</span>
                                                        Deadline: <c:out value="${vacancy.deadline}"/>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="flex flex-col justify-between items-end gap-4 shrink-0 border-t md:border-t-0 md:border-l border-slate-100 pt-4 md:pt-0 md:pl-6">
                                                <button class="text-slate-400 hover:text-red-500 transition-colors" title="Save Vacancy">
                                                    <span class="material-symbols-outlined text-2xl">favorite_border</span>
                                                </button>
                                                <a href="${pageContext.request.contextPath}/vacancy?vacancyId=${vacancy.vacancyId}" class="w-full md:w-auto bg-primary text-white px-6 py-2.5 rounded-lg font-bold text-sm shadow-md hover:bg-primary/90 transition-colors text-center">
                                                    Apply Now
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Load More -->
                    <c:if test="${not empty vacancies}">
                        <div class="mt-8 text-center">
                            <button class="bg-white border border-slate-200 text-slate-700 px-6 py-3 rounded-lg text-sm font-bold hover:bg-slate-50 transition-colors shadow-sm inline-flex items-center gap-2">
                                <span class="material-symbols-outlined text-lg">sync</span>
                                Load More Vacancies
                            </button>
                        </div>
                    </c:if>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
