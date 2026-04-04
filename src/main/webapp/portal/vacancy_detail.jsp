<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vacancy Detail - QM HIRE</title>
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
                <div class="max-w-4xl mx-auto">
                    <a href="${pageContext.request.contextPath}/vacancies" class="flex items-center gap-2 text-slate-500 hover:text-primary mb-6 transition-colors font-medium w-fit">
                        <span class="material-symbols-outlined">arrow_back</span>
                        Back to Vacancies
                    </a>

                    <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                        <jsp:param name="containerClass" value="mb-6" />
                    </jsp:include>

                    <c:choose>
                        <c:when test="${empty vacancy}">
                            <div class="bg-white border border-slate-200 rounded-xl p-12 text-center shadow-sm">
                                <span class="material-symbols-outlined text-6xl text-slate-300 mb-4">error_outline</span>
                                <h3 class="text-xl font-bold text-slate-900 mb-2">Vacancy Not Found</h3>
                                <p class="text-slate-500">The vacancy you are looking for does not exist or has been removed.</p>
                                <a href="${pageContext.request.contextPath}/vacancies" class="inline-block mt-6 bg-primary text-white px-6 py-2.5 rounded-lg font-bold text-sm shadow-md hover:bg-primary/90 transition-colors">
                                    Browse Vacancies
                                </a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm">
                                <div class="p-8 border-b border-slate-100">
                                    <div class="flex flex-col md:flex-row justify-between items-start gap-4">
                                        <div>
                                            <span class="px-3 py-1 bg-primary/10 text-primary text-xs font-bold uppercase rounded-md tracking-wider mb-3 inline-block">
                                                <c:out value="${vacancy.courseCode}"/>
                                            </span>
                                            <h2 class="text-3xl font-black text-slate-900 tracking-tight"><c:out value="${vacancy.title}"/></h2>
                                            <p class="text-lg text-slate-500 mt-1"><c:out value="${vacancy.department}"/></p>
                                        </div>
                                        <button onclick="document.getElementById('applyModal').classList.remove('hidden')" class="bg-primary text-white px-8 py-3 rounded-xl font-bold shadow-lg shadow-primary/20 hover:scale-105 transition-transform">
                                            Apply Now
                                        </button>
                                    </div>

                                    <div class="grid grid-cols-2 md:grid-cols-4 gap-6 mt-8">
                                        <div class="space-y-1">
                                            <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">Hours</p>
                                            <p class="text-sm font-bold text-slate-700"><c:out value="${vacancy.hoursPerWeek}"/> hrs/week</p>
                                        </div>
                                        <div class="space-y-1">
                                            <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">Pay Rate</p>
                                            <p class="text-sm font-bold text-slate-700">$<c:out value="${vacancy.hourlyRate}"/>/hr</p>
                                        </div>
                                        <div class="space-y-1">
                                            <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">Deadline</p>
                                            <p class="text-sm font-bold text-red-500"><c:out value="${vacancy.deadline}"/></p>
                                        </div>
                                        <div class="space-y-1">
                                            <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">Module Owner</p>
                                            <p class="text-sm font-bold text-slate-700"><c:out value="${vacancy.moduleOwner}"/></p>
                                        </div>
                                    </div>
                                </div>

                                <div class="p-8 space-y-8">
                                    <section>
                                        <h3 class="text-xl font-bold text-slate-900 mb-4">Description</h3>
                                        <p class="text-slate-600 leading-relaxed">
                                            <c:out value="${vacancy.description}"/>
                                        </p>
                                    </section>

                                    <section>
                                        <h3 class="text-xl font-bold text-slate-900 mb-4">Requirements</h3>
                                        <ul class="space-y-3">
                                            <c:forEach items="${vacancy.requirements}" var="requirement">
                                                <li class="flex gap-3 text-slate-600">
                                                    <span class="material-symbols-outlined text-primary text-lg">check_circle</span>
                                                    <span class="text-sm"><c:out value="${requirement}"/></span>
                                                </li>
                                            </c:forEach>
                                        </ul>
                                    </section>
                                </div>
                            </div>

                            <!-- Apply Modal -->
                            <div id="applyModal" class="hidden fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
                                <div class="bg-white rounded-2xl w-full max-w-md p-8 shadow-2xl border border-slate-200">
                                    <h3 class="text-2xl font-black text-slate-900 mb-2">Select Resume</h3>
                                    <p class="text-slate-500 text-sm mb-6">Choose which resume you want to use for this application.</p>
                                    
                                    <form action="${pageContext.request.contextPath}/application" method="POST">
                                        <input type="hidden" name="vacancyId" value="${vacancy.vacancyId}">
                                        <div class="space-y-3 mb-8">
                                            <c:choose>
                                                <c:when test="${empty resumeList}">
                                                    <p class="text-sm text-slate-500 italic">No resumes found. Please upload a resume first.</p>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach items="${resumeList}" var="resume" varStatus="status">
                                                        <label class="w-full p-4 rounded-xl border-2 border-slate-100 hover:border-primary/30 text-left transition-all flex items-center gap-4 cursor-pointer">
                                                            <input type="radio" name="resumeId" value="${resume.resumeId}" class="w-5 h-5 text-primary border-slate-300 focus:ring-primary" <c:if test="${status.first}">required="required"</c:if>>
                                                            <div class="flex-1">
                                                                <p class="text-sm font-bold text-slate-900"><c:out value="${resume.resumeName}"/></p>
                                                            </div>
                                                        </label>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

                                        <div class="flex gap-3">
                                            <button type="button" onclick="document.getElementById('applyModal').classList.add('hidden')" class="flex-1 py-3 bg-slate-100 text-slate-600 rounded-xl font-bold text-sm hover:bg-slate-200 transition-colors">
                                                Cancel
                                            </button>
                                            <button type="submit" class="flex-1 py-3 bg-primary text-white rounded-xl font-bold text-sm shadow-lg shadow-primary/20 hover:bg-primary/90 transition-colors" <c:if test="${empty resumeList}">disabled="disabled"</c:if>>
                                                Confirm Apply
                                            </button>
                                        </div>
                                    </form>
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
