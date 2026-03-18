<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
                            <h2 class="text-slate-900 text-3xl font-black tracking-tight">Resume Management</h2>
                            <p class="text-slate-500 text-sm mt-2">Upload, update, and manage your resumes for different TA positions.</p>
                        </div>
                        <button class="bg-primary text-white px-5 py-2.5 rounded-lg text-sm font-bold flex items-center gap-2 hover:bg-primary/90 transition-all shadow-sm">
                            <span class="material-symbols-outlined text-sm">upload_file</span>
                            Upload Resume
                        </button>
                    </div>

                    <!-- Main Content Grid -->
                    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
                        <!-- Left Column: Resume List -->
                        <div class="lg:col-span-2 space-y-6">
                            
                            <!-- Resume Card 1 -->
                            <div class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow relative group">
                                <div class="absolute top-6 right-6 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                    <button class="p-2 text-slate-400 hover:text-primary bg-slate-50 rounded-lg transition-colors" title="Download">
                                        <span class="material-symbols-outlined text-xl">download</span>
                                    </button>
                                    <button class="p-2 text-slate-400 hover:text-red-500 bg-slate-50 rounded-lg transition-colors" title="Delete">
                                        <span class="material-symbols-outlined text-xl">delete</span>
                                    </button>
                                </div>
                                
                                <div class="flex items-start gap-4">
                                    <div class="w-14 h-14 rounded-xl bg-red-50 flex items-center justify-center text-red-500 shrink-0">
                                        <span class="material-symbols-outlined text-3xl">picture_as_pdf</span>
                                    </div>
                                    <div class="flex-1">
                                        <div class="flex items-center gap-3 mb-1">
                                            <h3 class="text-lg font-bold text-slate-900">Academic_CV_2024.pdf</h3>
                                            <span class="px-2 py-0.5 bg-green-100 text-green-700 text-[10px] font-bold uppercase rounded-full tracking-wider">Default</span>
                                        </div>
                                        <p class="text-sm text-slate-500 mb-4">Uploaded on Oct 24, 2023 • 1.2 MB</p>
                                        
                                        <div class="flex flex-wrap gap-2">
                                            <span class="text-xs font-medium text-slate-600 bg-slate-100 px-2.5 py-1 rounded-md">Computer Science</span>
                                            <span class="text-xs font-medium text-slate-600 bg-slate-100 px-2.5 py-1 rounded-md">Research</span>
                                            <button class="text-xs font-bold text-primary hover:underline px-2.5 py-1 flex items-center gap-1">
                                                <span class="material-symbols-outlined text-[14px]">add</span>
                                                Add Tag
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between text-sm">
                                    <span class="text-slate-500">Used in <strong class="text-slate-900">3</strong> active applications</span>
                                    <button class="text-primary font-bold hover:underline">View Applications</button>
                                </div>
                            </div>

                            <!-- Resume Card 2 -->
                            <div class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow relative group">
                                <div class="absolute top-6 right-6 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                                    <button class="p-2 text-slate-400 hover:text-primary bg-slate-50 rounded-lg transition-colors" title="Download">
                                        <span class="material-symbols-outlined text-xl">download</span>
                                    </button>
                                    <button class="p-2 text-slate-400 hover:text-red-500 bg-slate-50 rounded-lg transition-colors" title="Delete">
                                        <span class="material-symbols-outlined text-xl">delete</span>
                                    </button>
                                </div>
                                
                                <div class="flex items-start gap-4">
                                    <div class="w-14 h-14 rounded-xl bg-blue-50 flex items-center justify-center text-blue-500 shrink-0">
                                        <span class="material-symbols-outlined text-3xl">description</span>
                                    </div>
                                    <div class="flex-1">
                                        <div class="flex items-center gap-3 mb-1">
                                            <h3 class="text-lg font-bold text-slate-900">Industry_Resume_Tech.docx</h3>
                                        </div>
                                        <p class="text-sm text-slate-500 mb-4">Uploaded on Sep 12, 2023 • 845 KB</p>
                                        
                                        <div class="flex flex-wrap gap-2">
                                            <span class="text-xs font-medium text-slate-600 bg-slate-100 px-2.5 py-1 rounded-md">Software Engineering</span>
                                            <span class="text-xs font-medium text-slate-600 bg-slate-100 px-2.5 py-1 rounded-md">Industry</span>
                                            <button class="text-xs font-bold text-primary hover:underline px-2.5 py-1 flex items-center gap-1">
                                                <span class="material-symbols-outlined text-[14px]">add</span>
                                                Add Tag
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between text-sm">
                                    <span class="text-slate-500">Used in <strong class="text-slate-900">1</strong> active application</span>
                                    <button class="text-primary font-bold hover:underline">Set as Default</button>
                                </div>
                            </div>

                            <!-- Upload Area -->
                            <div class="border-2 border-dashed border-slate-300 rounded-xl p-10 text-center hover:bg-slate-50 transition-colors cursor-pointer group">
                                <div class="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center text-primary mx-auto mb-4 group-hover:scale-110 transition-transform">
                                    <span class="material-symbols-outlined text-3xl">cloud_upload</span>
                                </div>
                                <h3 class="text-lg font-bold text-slate-900 mb-2">Drag & drop your resume here</h3>
                                <p class="text-sm text-slate-500 mb-6">Supported formats: PDF, DOCX, DOC (Max 5MB)</p>
                                <button class="bg-white border border-slate-200 text-slate-700 px-6 py-2.5 rounded-lg text-sm font-bold hover:bg-slate-50 transition-colors shadow-sm">
                                    Browse Files
                                </button>
                            </div>
                        </div>

                        <!-- Right Column: Tips & Info -->
                        <div class="space-y-6">
                            <!-- AI Resume Review (Placeholder) -->
                            <div class="bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl p-6 text-white shadow-lg relative overflow-hidden">
                                <div class="absolute -right-4 -top-4 w-24 h-24 bg-white/10 rounded-full blur-2xl"></div>
                                <div class="flex items-center gap-2 mb-4 relative z-10">
                                    <span class="material-symbols-outlined">auto_awesome</span>
                                    <h3 class="font-bold text-lg">AI Resume Review</h3>
                                </div>
                                <p class="text-sm text-white/80 mb-6 relative z-10 leading-relaxed">
                                    Get instant feedback on your resume tailored for TA positions. Our AI analyzes keywords, formatting, and impact.
                                </p>
                                <button class="w-full bg-white text-indigo-600 font-bold py-2.5 rounded-lg text-sm hover:bg-opacity-90 transition-colors relative z-10 shadow-md">
                                    Analyze Default Resume
                                </button>
                            </div>

                            <!-- Tips Card -->
                            <div class="bg-white border border-slate-200 rounded-xl p-6 shadow-sm">
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
                </div>
            </main>
        </div>
    </div>
</body>
</html>
