<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>QM HIRE - University TA Portal</title>
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
                        'background-dark': '#020617',
                    },
                    fontFamily: {
                        sans: ['Inter', 'sans-serif'],
                        display: ['Inter', 'sans-serif'],
                    }
                }
            }
        }
    </script>
    <style>
        body { font-family: 'Inter', sans-serif; }
    </style>
</head>
<body class="bg-background-light font-display text-slate-900 overflow-x-hidden">
    <div class="snap-y-container relative">
        <nav class="fixed top-0 left-0 w-full z-50 flex items-center justify-between px-8 py-6 pointer-events-none">
            <div class="flex items-center gap-3 bg-white/80 backdrop-blur-md px-4 py-2 rounded-full border border-slate-200 pointer-events-auto shadow-sm">
                <div class="flex items-center justify-center w-8 h-8 rounded-lg bg-primary text-white">
                    <span class="material-symbols-outlined text-xl">work</span>
                </div>
                <div class="flex items-center gap-1">
                    <span class="font-black text-lg tracking-tighter text-primary">QM</span>
                    <span class="font-light text-lg tracking-tight text-slate-500">HIRE</span>
                </div>
            </div>
        </nav>

        <section class="snap-section relative px-6 overflow-hidden min-h-screen flex items-center justify-center">
            <div class="absolute inset-0 z-0 opacity-10">
                <div class="absolute top-0 right-0 w-1/2 h-full bg-[url('https://lh3.googleusercontent.com/aida-public/AB6AXuDtl8dFnRszw8DDwL_Uo0aFgbmyQxXrbgiQ334vE5vllXD4lpQzltxggQhcNeYjQ0cY4XJGq-O5kEMqrvYWKwbKQHj8hr8A2ZK-UbHz2hX5_L70NMhdYM-48pZzFK9zr_Kcb-VjUpaZf2Exsdb4TjkqhyU0Gp-wdJ7F3NAaY5Q14GkpkGQu6vXhvyBqToNyUn-yPshAvKqnAj_DrJnlfWPsIqGgukckbwzD_0FrtogGRd8ZRkS5uXo2Uhm6siNfmIrUGA-TSE4Hxuo')] bg-cover bg-center"></div>
            </div>
            <div class="mx-auto max-w-4xl text-center relative z-10">
                <span class="inline-block px-4 py-1.5 rounded-full bg-primary/5 text-primary text-xs font-black uppercase tracking-widest mb-6">Phase 01 / Introduction</span>
                <h1 class="text-6xl md:text-8xl font-black text-slate-900 leading-[0.9] mb-8">
                    Your Impact <br/><span class="text-accent">Starts Here.</span>
                </h1>
                <p class="text-xl text-slate-600 max-w-2xl mx-auto mb-12">
                    Join our academic excellence. The University TA Recruitment System is your gateway to shaping the next generation of scholars.
                </p>
                <div class="mt-12 flex flex-col sm:flex-row gap-4 justify-center">
                    <a href="${pageContext.request.contextPath}/login" class="px-10 py-5 bg-accent text-white font-black text-lg rounded-xl shadow-2xl hover:scale-105 active:scale-95 transition-all flex items-center justify-center gap-3">
                        LOGIN TO PORTAL
                        <span class="material-symbols-outlined">arrow_forward</span>
                    </a>
                </div>
            </div>
        </section>
    </div>
</body>
</html>
