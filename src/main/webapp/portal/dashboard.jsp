<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
                    <!-- Welcome Header -->
                    <div class="flex flex-col md:flex-row md:items-end justify-between gap-4">
                        <div>
                            <h2 class="text-primary text-4xl font-black tracking-tight mb-2">Welcome back, Alex!</h2>
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
                            <p class="text-primary text-4xl font-black">2</p>
                        </div>
                        <div class="flex flex-col gap-2 rounded-2xl bg-white p-6 border border-primary/5 shadow-sm hover:shadow-md transition-shadow border-l-4 border-l-primary">
                            <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 mb-2">
                                <span class="material-symbols-outlined">send</span>
                            </div>
                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">Submitted</p>
                            <p class="text-primary text-4xl font-black">4</p>
                        </div>
                        <div class="flex flex-col gap-2 rounded-2xl bg-white p-6 border border-primary/5 shadow-sm hover:shadow-md transition-shadow">
                            <div class="w-10 h-10 rounded-full bg-amber-100 flex items-center justify-center text-amber-600 mb-2">
                                <span class="material-symbols-outlined">pending_actions</span>
                            </div>
                            <p class="text-slate-500 text-sm font-semibold uppercase tracking-wider">Under Review</p>
                            <p class="text-primary text-4xl font-black">1</p>
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
                                    <div class="flex items-center gap-4 p-5 hover:bg-slate-50 transition-colors">
                                        <div class="w-10 h-10 rounded-xl bg-green-100 flex items-center justify-center text-green-600">
                                            <span class="material-symbols-outlined text-xl">check_circle</span>
                                        </div>
                                        <div class="flex-1">
                                            <p class="text-sm font-bold text-primary">Application for CS101 submitted</p>
                                            <p class="text-xs text-slate-500">Computer Science Department • Graduate Research Asst.</p>
                                        </div>
                                        <div class="text-right">
                                            <p class="text-xs font-semibold text-slate-400">Yesterday</p>
                                            <p class="text-[10px] font-bold text-green-600 bg-green-50 px-2 py-0.5 rounded-full inline-block mt-1">SUCCESS</p>
                                        </div>
                                    </div>
                                    <div class="flex items-center gap-4 p-5 hover:bg-slate-50 transition-colors">
                                        <div class="w-10 h-10 rounded-xl bg-blue-100 flex items-center justify-center text-blue-600">
                                            <span class="material-symbols-outlined text-xl">edit_document</span>
                                        </div>
                                        <div class="flex-1">
                                            <p class="text-sm font-bold text-primary">Resume "Academic_V2.pdf" updated</p>
                                            <p class="text-xs text-slate-500">Profile Management</p>
                                        </div>
                                        <div class="text-right">
                                            <p class="text-xs font-semibold text-slate-400">2 days ago</p>
                                        </div>
                                    </div>
                                    <div class="flex items-center gap-4 p-5 hover:bg-slate-50 transition-colors">
                                        <div class="w-10 h-10 rounded-xl bg-primary/10 flex items-center justify-center text-primary">
                                            <span class="material-symbols-outlined text-xl">visibility</span>
                                        </div>
                                        <div class="flex-1">
                                            <p class="text-sm font-bold text-primary">Application viewed by Recruiter</p>
                                            <p class="text-xs text-slate-500">Engineering Lab • Summer Internship</p>
                                        </div>
                                        <div class="text-right">
                                            <p class="text-xs font-semibold text-slate-400">3 days ago</p>
                                            <p class="text-[10px] font-bold text-primary bg-primary/5 px-2 py-0.5 rounded-full inline-block mt-1">REVIEWING</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Sidebar Actions / Reminders -->
                        <div class="space-y-6">
                            <h3 class="text-primary text-xl font-bold px-2">Action Required</h3>
                            <div class="bg-primary text-white rounded-2xl p-6 shadow-xl shadow-primary/20 relative overflow-hidden group">
                                <div class="absolute -right-4 -top-4 w-24 h-24 bg-white/10 rounded-full blur-2xl group-hover:scale-150 transition-transform"></div>
                                <h4 class="font-bold text-lg mb-2 relative z-10">Complete Bio-Data</h4>
                                <p class="text-sm text-white/70 mb-4 relative z-10">Your profile is 85% complete. Add your references to stand out to recruiters.</p>
                                <button class="w-full py-2.5 bg-white text-primary rounded-xl font-bold text-sm shadow-md hover:bg-slate-100 transition-colors relative z-10">Update Profile</button>
                            </div>

                            <div class="bg-white rounded-2xl p-6 border border-primary/5 shadow-sm">
                                <h4 class="font-bold text-primary mb-4">Upcoming Deadlines</h4>
                                <ul class="space-y-4">
                                    <li class="flex items-start gap-3">
                                        <div class="w-2 h-2 mt-1.5 rounded-full bg-red-500"></div>
                                        <div>
                                            <p class="text-sm font-bold text-slate-700">TA Application Spring</p>
                                            <p class="text-xs text-red-500 font-semibold">Ends in 2 days</p>
                                        </div>
                                    </li>
                                    <li class="flex items-start gap-3">
                                        <div class="w-2 h-2 mt-1.5 rounded-full bg-amber-500"></div>
                                        <div>
                                            <p class="text-sm font-bold text-slate-700">Library Assistant Pool</p>
                                            <p class="text-xs text-amber-600 font-semibold">Ends in 5 days</p>
                                        </div>
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
