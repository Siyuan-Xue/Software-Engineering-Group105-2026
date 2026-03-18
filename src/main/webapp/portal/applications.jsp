<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Applications - QM HIRE</title>
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
                            <h2 class="text-slate-900 text-3xl font-black tracking-tight">My Applications</h2>
                            <p class="text-slate-500 text-sm mt-2">Track the progress of your submitted module and program applications.</p>
                        </div>
                        <button class="bg-primary text-white px-5 py-2.5 rounded-lg text-sm font-bold flex items-center gap-2 hover:bg-primary/90 transition-all shadow-sm">
                            <span class="material-symbols-outlined text-sm">add</span>
                            New Application
                        </button>
                    </div>

                    <!-- Search and Filters -->
                    <div class="bg-white border border-slate-200 rounded-xl p-4 mb-6 shadow-sm">
                        <div class="flex flex-col lg:flex-row gap-4">
                            <div class="flex-1">
                                <label class="flex flex-col w-full">
                                    <div class="flex w-full items-center rounded-lg bg-slate-100 px-4 h-11 border border-transparent focus-within:border-primary/30 transition-all">
                                        <span class="material-symbols-outlined text-slate-400">search</span>
                                        <input class="w-full bg-transparent border-none focus:ring-0 text-slate-900 placeholder:text-slate-400 text-sm font-medium pl-3 outline-none" placeholder="Search by module name or department..." />
                                    </div>
                                </label>
                            </div>
                            <div class="flex flex-wrap gap-2">
                                <div class="relative group">
                                    <button class="flex h-11 items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors">
                                        <span>Status: All</span>
                                        <span class="material-symbols-outlined text-lg">expand_more</span>
                                    </button>
                                </div>
                                <div class="relative group">
                                    <button class="flex h-11 items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors">
                                        <span>Date: Latest</span>
                                        <span class="material-symbols-outlined text-lg">calendar_today</span>
                                    </button>
                                </div>
                                <button class="flex h-11 items-center gap-2 rounded-lg bg-slate-100 px-4 text-sm font-bold text-slate-500 hover:text-primary transition-colors">
                                    <span class="material-symbols-outlined text-lg">filter_list</span>
                                    Filters
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Applications Table -->
                    <div class="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
                        <div class="overflow-x-auto">
                            <table class="w-full text-left border-collapse">
                                <thead>
                                    <tr class="bg-slate-50 border-b border-slate-200">
                                        <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Module Name</th>
                                        <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Department</th>
                                        <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Submitted Date</th>
                                        <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Resume Used</th>
                                        <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500">Status</th>
                                        <th class="px-6 py-4 text-xs font-bold uppercase tracking-wider text-slate-500 text-right">Actions</th>
                                    </tr>
                                </thead>
                                <tbody class="divide-y divide-slate-100">
                                    <!-- Row 1 -->
                                    <tr class="hover:bg-slate-50/50 transition-colors">
                                        <td class="px-6 py-5">
                                            <div class="font-bold text-slate-900">CS50: Computer Science Fundamentals</div>
                                            <div class="text-xs text-slate-400 font-medium">Ref: #APP-2024-001</div>
                                        </td>
                                        <td class="px-6 py-5 text-sm text-slate-600 font-medium">Engineering & Tech</td>
                                        <td class="px-6 py-5 text-sm text-slate-600">Oct 12, 2023</td>
                                        <td class="px-6 py-5">
                                            <div class="flex items-center gap-2 text-xs font-medium text-primary bg-primary/5 px-2 py-1 rounded w-fit">
                                                <span class="material-symbols-outlined text-sm">description</span>
                                                Resume_V2.pdf
                                            </div>
                                        </td>
                                        <td class="px-6 py-5">
                                            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-blue-100 text-blue-700">
                                                Under Review
                                            </span>
                                        </td>
                                        <td class="px-6 py-5 text-right">
                                            <button class="text-slate-400 hover:text-primary transition-colors">
                                                <span class="material-symbols-outlined">more_vert</span>
                                            </button>
                                        </td>
                                    </tr>
                                    <!-- Row 2 -->
                                    <tr class="hover:bg-slate-50/50 transition-colors">
                                        <td class="px-6 py-5">
                                            <div class="font-bold text-slate-900">ECON101: Macroeconomics</div>
                                            <div class="text-xs text-slate-400 font-medium">Ref: #APP-2024-015</div>
                                        </td>
                                        <td class="px-6 py-5 text-sm text-slate-600 font-medium">Business School</td>
                                        <td class="px-6 py-5 text-sm text-slate-600">Sep 28, 2023</td>
                                        <td class="px-6 py-5">
                                            <div class="flex items-center gap-2 text-xs font-medium text-primary bg-primary/5 px-2 py-1 rounded w-fit">
                                                <span class="material-symbols-outlined text-sm">description</span>
                                                Academic_CV.pdf
                                            </div>
                                        </td>
                                        <td class="px-6 py-5">
                                            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-green-100 text-green-700">
                                                Accepted
                                            </span>
                                        </td>
                                        <td class="px-6 py-5 text-right">
                                            <button class="text-slate-400 hover:text-primary transition-colors">
                                                <span class="material-symbols-outlined">more_vert</span>
                                            </button>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                        
                        <!-- Pagination -->
                        <div class="px-6 py-4 bg-slate-50 border-t border-slate-200 flex items-center justify-between">
                            <p class="text-sm text-slate-500">Showing <span class="font-bold">1-2</span> of <span class="font-bold">2</span> applications</p>
                            <div class="flex gap-2">
                                <button class="p-1 rounded border border-slate-200 bg-white text-slate-400 hover:text-primary transition-colors disabled:opacity-50" disabled>
                                    <span class="material-symbols-outlined">chevron_left</span>
                                </button>
                                <button class="p-1 rounded border border-slate-200 bg-white text-slate-600 hover:text-primary transition-colors disabled:opacity-50" disabled>
                                    <span class="material-symbols-outlined">chevron_right</span>
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Informational Banner -->
                    <div class="mt-8 p-4 bg-primary/5 border border-primary/10 rounded-xl flex gap-4">
                        <div class="text-primary">
                            <span class="material-symbols-outlined">info</span>
                        </div>
                        <div>
                            <h4 class="text-sm font-bold text-slate-900">Expected Response Times</h4>
                            <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                                Most departments respond within 10-14 business days. If your status has been "Under Review" for more than 3 weeks, you can contact the department coordinator through the messaging tab.
                            </p>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
