<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Settings - QM HIRE</title>
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
                <div class="max-w-2xl mx-auto space-y-8">
                    <div>
                        <h2 class="text-3xl font-black text-slate-900 tracking-tight">Settings</h2>
                        <p class="text-slate-500 mt-2">Manage your account preferences and settings.</p>
                    </div>

                    <div class="bg-white rounded-2xl border border-slate-200 divide-y divide-slate-100 overflow-hidden shadow-sm">
                        <!-- Language Setting -->
                        <div class="p-6 flex items-center justify-between">
                            <div class="flex items-center gap-4">
                                <div class="w-10 h-10 rounded-xl bg-blue-100 flex items-center justify-center text-blue-600">
                                    <span class="material-symbols-outlined">language</span>
                                </div>
                                <div>
                                    <h3 class="font-bold text-slate-900">Language</h3>
                                    <p class="text-sm text-slate-500">Choose your preferred interface language.</p>
                                </div>
                            </div>
                            <div class="flex bg-slate-100 p-1 rounded-lg">
                                <button class="px-4 py-1.5 rounded-md text-sm font-bold transition-all bg-white text-primary shadow-sm">
                                    English
                                </button>
                                <button class="px-4 py-1.5 rounded-md text-sm font-bold transition-all text-slate-500 hover:text-slate-700">
                                    中文
                                </button>
                            </div>
                        </div>

                        <!-- Theme Setting (Placeholder) -->
                        <div class="p-6 flex items-center justify-between">
                            <div class="flex items-center gap-4">
                                <div class="w-10 h-10 rounded-xl bg-amber-100 flex items-center justify-center text-amber-600">
                                    <span class="material-symbols-outlined">dark_mode</span>
                                </div>
                                <div>
                                    <h3 class="font-bold text-slate-900">Appearance</h3>
                                    <p class="text-sm text-slate-500">Switch between light and dark themes.</p>
                                </div>
                            </div>
                            <button class="px-4 py-2 bg-slate-100 text-slate-700 rounded-lg text-sm font-bold hover:bg-slate-200 transition-colors">
                                Toggle Theme
                            </button>
                        </div>

                        <!-- Logout -->
                        <div class="p-6 flex items-center justify-between bg-red-50/30">
                            <div class="flex items-center gap-4">
                                <div class="w-10 h-10 rounded-xl bg-red-100 flex items-center justify-center text-red-600">
                                    <span class="material-symbols-outlined">logout</span>
                                </div>
                                <div>
                                    <h3 class="font-bold text-red-600">Logout</h3>
                                    <p class="text-sm text-slate-500">Sign out of your account securely.</p>
                                </div>
                            </div>
                            <a href="${pageContext.request.contextPath}/" class="px-6 py-2 bg-red-600 text-white rounded-lg text-sm font-bold hover:bg-red-700 transition-colors shadow-md shadow-red-600/20">
                                Logout
                            </a>
                        </div>
                    </div>

                    <div class="text-center text-xs text-slate-400">
                        <p>App Version 1.2.0 • Build 20241024</p>
                        <p class="mt-1">© 2024 University Recruitment System</p>
                    </div>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
