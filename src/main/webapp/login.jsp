<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - QM HIRE</title>
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
<body class="bg-background-light font-sans text-slate-900 min-h-screen flex items-center justify-center p-4">
    <div class="w-full max-w-md bg-white rounded-3xl shadow-xl border border-slate-100 p-8 relative overflow-hidden">
        <div class="absolute top-0 right-0 w-32 h-32 bg-accent/10 rounded-bl-full -z-10"></div>
        
        <div class="flex items-center gap-3 mb-8">
            <div class="flex items-center justify-center w-10 h-10 rounded-xl bg-primary text-white shadow-md">
                <span class="material-symbols-outlined text-2xl">work</span>
            </div>
            <div class="flex items-center gap-1">
                <span class="font-black text-2xl tracking-tighter text-primary">QM</span>
                <span class="font-light text-2xl tracking-tight text-slate-500">HIRE</span>
            </div>
        </div>

        <h2 class="text-3xl font-black mb-2">Welcome back</h2>
        <p class="text-slate-500 mb-8">Sign in to access your TA portal</p>

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

        <form action="${pageContext.request.contextPath}/login" method="POST" class="space-y-5">
            <div>
                <label class="block text-xs font-black text-slate-400 uppercase tracking-wider mb-2">University Email</label>
                <div class="relative">
                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">mail</span>
                    <input type="email" name="email" class="w-full bg-slate-50 border border-slate-200 rounded-xl py-3 pl-10 pr-4 focus:ring-2 focus:ring-accent/20 focus:border-accent outline-none transition-all" placeholder="student@university.edu" required />
                </div>
            </div>

            <div>
                <div class="flex items-center justify-between mb-2">
                    <label class="block text-xs font-black text-slate-400 uppercase tracking-wider">Password</label>
                    <a href="#" class="text-xs font-bold text-accent hover:underline">Forgot?</a>
                </div>
                <div class="relative">
                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">lock</span>
                    <input type="password" name="password" class="w-full bg-slate-50 border border-slate-200 rounded-xl py-3 pl-10 pr-4 focus:ring-2 focus:ring-accent/20 focus:border-accent outline-none transition-all" placeholder="••••••••" required />
                </div>
            </div>

            <button type="submit" class="w-full bg-primary text-white font-black py-4 rounded-xl shadow-lg shadow-primary/20 hover:scale-[1.02] active:scale-[0.98] transition-all flex items-center justify-center gap-2 mt-4">
                SIGN IN
                <span class="material-symbols-outlined text-sm">arrow_forward</span>
            </button>
        </form>

        <div class="mt-8 text-center">
            <p class="text-sm text-slate-500">Don't have an account? <a href="#" class="font-bold text-primary hover:underline">Request access</a></p>
        </div>
    </div>
</body>
</html>
