<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Resumes - QM HIRE</title>
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
                    <c:set var="resumesState" value="${empty pageState ? 'normal' : pageState}" />

                    <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                        <jsp:param name="containerClass" value="mb-6" />
                    </jsp:include>

                    <c:if test="${resumesState == 'uploadSuccess'}">
                        <div class="portal-callout border-emerald-200 bg-emerald-50/80 p-4 mb-6" role="status">
                            <p class="text-sm font-medium text-emerald-800">Your resume was uploaded successfully.</p>
                        </div>
                    </c:if>
                    <c:if test="${resumesState == 'uploadFailure'}">
                        <div class="portal-callout border-red-200 bg-red-50/80 p-4 mb-6" role="alert">
                            <p class="text-sm font-medium text-red-800">Upload could not be completed. Check the message above or try again.</p>
                        </div>
                    </c:if>

                    <!-- Page Header -->
                    <div class="portal-page-header mb-2">
                        <div>
                            <h2 class="portal-page-title">Resume Management</h2>
                            <p class="portal-page-copy">Upload, update, and manage your resumes for different TA positions.</p>
                        </div>
                        <label for="resume_file_input" class="portal-btn portal-btn-primary cursor-pointer">
                            <span class="material-symbols-outlined text-sm">upload_file</span>
                            Upload Resume
                        </label>
                    </div>

                    <c:choose>
                        <c:when test="${resumesState == 'loadError'}">
                            <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                                <jsp:param name="variant" value="error" />
                                <jsp:param name="icon" value="description" />
                                <jsp:param name="title" value="Resumes unavailable" />
                                <jsp:param name="message" value="We couldn't load your saved resumes right now. Please refresh the page and try again." />
                                <jsp:param name="actionHref" value="${pageContext.request.contextPath}/resumes" />
                                <jsp:param name="actionLabel" value="Try Again" />
                            </jsp:include>
                        </c:when>
                        <c:otherwise>
                            <form id="resume_upload_form" action="${pageContext.request.contextPath}/resumes" method="POST" enctype="multipart/form-data" class="hidden" aria-hidden="true"></form>
                            <input type="file" name="resumeFile" id="resume_file_input" form="resume_upload_form"
                                   accept=".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                   class="sr-only w-px h-px opacity-0 absolute overflow-hidden"/>

                            <!-- Main Content Grid -->
                            <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
                                <!-- Left Column: Resume List -->
                                <div class="lg:col-span-2 space-y-6">

                                    <c:choose>
                                        <c:when test="${empty resumes}">
                                            <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                                                <jsp:param name="icon" value="description" />
                                                <jsp:param name="title" value="No resumes uploaded yet" />
                                                <jsp:param name="message" value="Upload your first resume to start applying for TA positions." />
                                            </jsp:include>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach items="${resumes}" var="resume">
                                                <!-- Resume Card -->
                                                <div class="portal-panel p-6 hover:shadow-md transition-shadow relative group">
                                                    <div class="absolute top-6 right-6 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                                        <button type="button" disabled class="p-2 text-slate-400 bg-slate-50 rounded-lg cursor-not-allowed opacity-60" title="Download not available yet — pending backend">
                                                            <span class="material-symbols-outlined text-xl">download</span>
                                                        </button>
                                                        <button type="button" disabled class="p-2 text-slate-400 bg-slate-50 rounded-lg cursor-not-allowed opacity-60" title="Delete not available yet — pending backend">
                                                            <span class="material-symbols-outlined text-xl">delete</span>
                                                        </button>
                                                    </div>

                                                    <div class="flex items-start gap-4">
                                                        <div class="w-14 h-14 rounded-xl bg-blue-50 flex items-center justify-center text-blue-500 shrink-0">
                                                            <span class="material-symbols-outlined text-3xl">description</span>
                                                        </div>
                                                        <div class="flex-1">
                                                            <div class="flex items-center gap-3 mb-1">
                                                                <h3 class="text-lg font-bold text-slate-900"><c:out value="${resume.resumeName}"/></h3>
                                                                <c:if test="${resume.isDefault}">
                                                                    <span class="px-2 py-0.5 bg-green-100 text-green-700 text-[10px] font-bold uppercase rounded-full tracking-wider">Default</span>
                                                                </c:if>
                                                            </div>
                                                            <p class="text-sm text-slate-500 mb-4">Uploaded on <c:out value="${resume.uploadDate}"/> • <c:out value="${resume.fileSize}"/></p>

                                                            <div class="flex flex-wrap gap-2">
                                                                <c:if test="${not empty resume.tags}">
                                                                    <c:forEach items="${resume.tags}" var="tag">
                                                                        <span class="text-xs font-medium text-slate-600 bg-slate-100 px-2.5 py-1 rounded-md"><c:out value="${tag}"/></span>
                                                                    </c:forEach>
                                                                </c:if>
                                                                <button type="button" disabled class="text-xs font-bold text-slate-400 cursor-not-allowed px-2.5 py-1 flex items-center gap-1" title="Tags editing not available yet — pending backend">
                                                                    <span class="material-symbols-outlined text-[14px]">add</span>
                                                                    Add Tag
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </div>

                                                    <div class="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between text-sm">
                                                        <span class="text-slate-500">Used in <strong class="text-slate-900"><c:out value="${resume.activeApplicationsCount}"/></strong> active application(s)</span>
                                                        <c:choose>
                                                            <c:when test="${resume.isDefault}">
                                                                <a href="${pageContext.request.contextPath}/applications" class="text-primary font-bold hover:underline">View Applications</a>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <button type="button" disabled class="text-slate-400 font-bold cursor-not-allowed" title="Set default not available yet — pending backend">Set as Default</button>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>

                                    <!-- Upload Area -->
                                    <div id="resume_drop_zone" class="portal-upload-surface cursor-pointer group">
                                        <div class="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center text-primary mx-auto mb-4 group-hover:scale-110 transition-transform">
                                            <span class="material-symbols-outlined text-3xl">cloud_upload</span>
                                        </div>
                                        <h3 class="text-lg font-bold text-slate-900 mb-2">Drag & drop your resume here</h3>
                                        <p class="text-sm text-slate-500 mb-6">Supported formats: PDF, DOCX, DOC (Max 5MB)</p>
                                        <label for="resume_file_input" class="portal-btn portal-btn-secondary cursor-pointer inline-flex">
                                            Browse Files
                                        </label>
                                    </div>
                                </div>

                                <!-- Right Column: Tips & Info -->
                                <div class="space-y-6">
                                    <!-- AI Resume Review (Placeholder) -->
                                    <div class="portal-panel portal-panel--accent p-6 relative overflow-hidden">
                                        <div class="absolute -right-4 -top-4 w-24 h-24 bg-primary/10 rounded-full blur-2xl"></div>
                                        <div class="flex items-center gap-2 mb-4 relative z-10">
                                            <span class="material-symbols-outlined text-primary">auto_awesome</span>
                                            <h3 class="font-bold text-lg text-slate-900">AI Resume Review</h3>
                                        </div>
                                        <p class="text-sm text-slate-600 mb-6 relative z-10 leading-relaxed">
                                            Get instant feedback on your resume tailored for TA positions. Our AI analyzes keywords, formatting, and impact.
                                        </p>
                                        <button type="button" disabled class="portal-btn portal-btn-primary relative z-10 w-full cursor-not-allowed opacity-60" title="Not available yet — pending backend">
                                            Analyze Default Resume
                                        </button>
                                    </div>

                                    <!-- Tips Card -->
                                    <div class="portal-panel p-6">
                                        <h3 class="font-bold text-slate-900 mb-4 flex items-center gap-2">
                                            <span class="material-symbols-outlined text-amber-500">lightbulb</span>
                                            Resume Tips for TAs
                                        </h3>
                                        <ul class="space-y-4">
                                            <li class="flex gap-3">
                                                <span class="material-symbols-outlined text-green-500 text-lg shrink-0">check_circle</span>
                                                <p class="text-sm text-slate-600 leading-relaxed">
                                                    <strong class="text-slate-900 block mb-0.5">Highlight Teaching Experience</strong>
                                                    Include any tutoring, mentoring, or previous TA roles prominently.
                                                </p>
                                            </li>
                                            <li class="flex gap-3">
                                                <span class="material-symbols-outlined text-green-500 text-lg shrink-0">check_circle</span>
                                                <p class="text-sm text-slate-600 leading-relaxed">
                                                    <strong class="text-slate-900 block mb-0.5">Relevant Coursework</strong>
                                                    List advanced courses related to the module you're applying for.
                                                </p>
                                            </li>
                                            <li class="flex gap-3">
                                                <span class="material-symbols-outlined text-green-500 text-lg shrink-0">check_circle</span>
                                                <p class="text-sm text-slate-600 leading-relaxed">
                                                    <strong class="text-slate-900 block mb-0.5">Keep it Concise</strong>
                                                    Aim for 1-2 pages maximum. Academic CVs can be longer if necessary.
                                                </p>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </main>
        </div>
    </div>
    <script>
        (function () {
            var input = document.getElementById('resume_file_input');
            var form = document.getElementById('resume_upload_form');
            var zone = document.getElementById('resume_drop_zone');
            if (!input || !form || !zone) return;
            input.addEventListener('change', function () {
                if (input.files && input.files.length) form.submit();
            });
            zone.addEventListener('dragover', function (e) {
                e.preventDefault();
                e.stopPropagation();
            });
            zone.addEventListener('drop', function (e) {
                e.preventDefault();
                e.stopPropagation();
                if (!e.dataTransfer || !e.dataTransfer.files || !e.dataTransfer.files.length) return;
                var dt = new DataTransfer();
                dt.items.add(e.dataTransfer.files[0]);
                input.files = dt.files;
                form.submit();
            });
        })();
    </script>
</body>
</html>
