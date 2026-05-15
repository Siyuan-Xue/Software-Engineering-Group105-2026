<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${i18n['index.pageTitle']}</title>
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
                        'background-light': '#020617', // Dark theme for glass to pop
                        'background-dark': '#020617',
                        'glass-border': 'rgba(255, 255, 255, 0.15)',
                        'glass-bg': 'rgba(255, 255, 255, 0.03)',
                        'glass-highlight': 'rgba(255, 255, 255, 0.1)',
                    },
                    fontFamily: {
                        sans: ['Inter', 'sans-serif'],
                        display: ['Inter', 'sans-serif'],
                    },
                    animation: {
                        'float': 'float 8s ease-in-out infinite',
                        'float-delayed': 'float 8s ease-in-out 4s infinite',
                        'float-slow': 'float 12s ease-in-out 2s infinite',
                        'blob': 'blob 10s infinite',
                    },
                    keyframes: {
                        float: {
                            '0%, 100%': { transform: 'translateY(0) rotate(0deg)' },
                            '50%': { transform: 'translateY(-20px) rotate(2deg)' },
                        },
                        blob: {
                            '0%': { transform: 'translate(0px, 0px) scale(1)' },
                            '33%': { transform: 'translate(30px, -50px) scale(1.1)' },
                            '66%': { transform: 'translate(-20px, 20px) scale(0.9)' },
                            '100%': { transform: 'translate(0px, 0px) scale(1)' },
                        }
                    }
                }
            }
        }
    </script>
    <style>
        body { 
            font-family: 'Inter', sans-serif;
            background-color: #020617; /* Slate 950 */
            color: #f8fafc;
        }

        /* Liquid Glass Utilities */
        .liquid-glass {
            background: linear-gradient(135deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.01) 100%);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border: 1px solid rgba(255, 255, 255, 0.1);
            box-shadow: 
                0 8px 32px 0 rgba(0, 0, 0, 0.3),
                inset 0 1px 1px 0 rgba(255, 255, 255, 0.15),
                inset 0 -1px 1px 0 rgba(255, 255, 255, 0.05);
        }

        .liquid-glass-strong {
            background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.02) 100%);
            backdrop-filter: blur(30px);
            -webkit-backdrop-filter: blur(30px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            box-shadow: 
                0 16px 40px 0 rgba(0, 0, 0, 0.4),
                inset 0 1px 2px 0 rgba(255, 255, 255, 0.3);
        }

        .text-gradient {
            background-clip: text;
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-image: linear-gradient(to right, #ffffff, #94a3b8);
        }
        
        .text-gradient-accent {
            background-clip: text;
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-image: linear-gradient(to right, #60a5fa, #c084fc);
        }

        /* Ambient Glows */
        .ambient-blob {
            position: absolute;
            filter: blur(80px);
            z-index: 0;
            opacity: 0.6;
            border-radius: 50%;
        }

        /* Perspective Container for Images */
        .perspective-container {
            perspective: 2000px;
            transform-style: preserve-3d;
        }

        .hero-image-card {
            transform-style: preserve-3d;
            transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
        }
        
        .hero-image-card:hover {
            transform: translateZ(50px) rotateX(-5deg) rotateY(5deg);
        }
        
        .hero-image-card img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: inherit;
            opacity: 0.8;
            mix-blend-mode: overlay;
        }

    </style>
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
</head>
<body class="bg-background-light font-display text-slate-900 overflow-hidden" data-theme="${appearance}">

    <!-- Left Header: Brand Badge -->
    <div class="fixed top-6 left-6 lg:top-8 lg:left-12 z-50">
        <div class="flex items-center gap-2 bg-white/70 backdrop-blur-md px-4 py-2 rounded-full border border-slate-200/50 shadow-sm transition-all hover:bg-white/90 cursor-pointer">
            <span class="material-symbols-outlined text-slate-700 text-xl">school</span>
            <span class="font-black text-sm tracking-widest text-slate-800">QM HIRE</span>
        </div>
    </div>

    <!-- Right Top: Phase Indicators -->
    <div class="fixed top-8 right-6 lg:right-12 z-50 flex gap-2">
        <button id="indicator-1" class="phase-indicator w-8 h-1.5 rounded-full bg-slate-400 transition-all duration-300"></button>
        <button id="indicator-2" class="phase-indicator w-8 h-1.5 rounded-full bg-slate-300 transition-all duration-300"></button>
        <button id="indicator-3" class="phase-indicator w-8 h-1.5 rounded-full bg-slate-300 transition-all duration-300"></button>
        <button id="indicator-4" class="phase-indicator w-8 h-1.5 rounded-full bg-slate-300 transition-all duration-300"></button>
    </div>

    <!-- Snap Scrolling Container -->
    <main id="journey-container" class="h-screen w-full overflow-y-auto snap-y snap-mandatory scroll-smooth relative">

        <!-- Phase 1: Introduction -->
        <section id="phase-1" class="phase-section h-screen w-full snap-start relative flex items-center bg-slate-50">
            <!-- Right side background blend -->
            <div class="absolute inset-y-0 right-0 w-1/2 overflow-hidden pointer-events-none">
                <div class="absolute inset-0 bg-gradient-to-r from-slate-50 via-slate-50/80 to-transparent z-10 w-32 left-0"></div>
                <img src="${pageContext.request.contextPath}/images/bg.jpg" 
                     alt="Classroom Setup" 
                     class="w-full h-full object-cover object-left opacity-30 mix-blend-multiply flex transition-transform duration-[2s]" 
                />
            </div>
            
            <div class="container mx-auto px-6 lg:px-24 relative z-20">
                <div class="max-w-2xl translate-y-4 opacity-0 animate-content-in">
                    <div class="inline-flex items-center gap-2 px-3 py-1 bg-slate-200/50 rounded-full mb-8 text-slate-600 text-[10px] font-black uppercase tracking-widest">
                        PHASE 01 / INTRODUCTION
                    </div>
                    
                    <h1 class="text-6xl md:text-8xl font-black text-[#0f172a] leading-[1.0] mb-2 tracking-tight">
                        Your Impact<br/>
                        <span class="text-[#e11d48]">Starts Here.</span>
                    </h1>
                    
                    <p class="text-lg md:text-xl text-slate-600 mt-8 max-w-xl font-light leading-relaxed">
                        Join our academic excellence. The University TA Recruitment System is your gateway to shaping the next generation of scholars.
                    </p>
                </div>
            </div>

            <div class="absolute bottom-10 left-1/2 -translate-x-1/2 text-center animate-bounce">
                <div class="text-[10px] uppercase font-black tracking-widest text-[#94a3b8] mb-2">Scroll to explore the journey</div>
                <span class="material-symbols-outlined text-[#cbd5e1]">keyboard_arrow_down</span>
            </div>
            
            <!-- Optional subtle bottom text -->
            <div class="absolute bottom-8 left-6 lg:left-12 text-[#94a3b8] text-[10px] text-xs font-semibold tracking-wider">
                &copy; 2026 QM HIRE GROUP 105
            </div>
        </section>

        <!-- Phase 2: Benefits -->
        <section id="phase-2" class="phase-section h-screen w-full snap-start relative flex items-center bg-[#f8fafc]">
            <div class="container mx-auto px-6 lg:px-24">
                <div class="grid grid-cols-1 lg:grid-cols-12 gap-16 items-center">
                    
                    <!-- Left: Copy -->
                    <div class="col-span-1 lg:col-span-5 max-w-lg">
                        <div class="inline-flex items-center gap-2 px-3 py-1 bg-slate-200/50 rounded-full mb-8 text-slate-600 text-[10px] font-black uppercase tracking-widest">
                            PHASE 02 / BENEFITS
                        </div>
                        <h2 class="text-5xl md:text-6xl font-black text-[#0f172a] leading-tight mb-8">
                            Why become a<br/>Teaching Assistant?
                        </h2>
                        <p class="text-lg text-slate-600 font-light leading-relaxed">
                            Beyond the stipend, TA positions offer unparalleled professional growth, networking with faculty, and a chance to master your field through teaching.
                        </p>
                    </div>

                    <!-- Right: Cards -->
                    <div class="col-span-1 lg:col-span-7 grid grid-cols-1 sm:grid-cols-2 gap-6 relative">
                        <!-- Card 1 -->
                        <div class="bg-white p-8 rounded-3xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 hover:-translate-y-1 transition-all duration-300">
                            <span class="material-symbols-outlined text-[#e11d48] text-3xl mb-4">hub</span>
                            <h3 class="text-xl font-bold text-slate-900 mb-2">Centralized Hub</h3>
                            <p class="text-slate-500 text-sm leading-relaxed">Apply to multiple departments with a single profile.</p>
                        </div>
                        
                        <!-- Card 2 -->
                        <div class="bg-white p-8 rounded-3xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 hover:-translate-y-1 transition-all duration-300 sm:translate-y-6">
                            <span class="material-symbols-outlined text-[#e11d48] text-3xl mb-4">monitoring</span>
                            <h3 class="text-xl font-bold text-slate-900 mb-2">Merit-Based</h3>
                            <p class="text-slate-500 text-sm leading-relaxed">Transparent evaluation based on your academic record.</p>
                        </div>
                        
                        <!-- Card 3 -->
                        <div class="bg-white p-8 rounded-3xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-slate-100 hover:-translate-y-1 transition-all duration-300">
                            <span class="material-symbols-outlined text-[#e11d48] text-3xl mb-4">update</span>
                            <h3 class="text-xl font-bold text-slate-900 mb-2">Real-time Tracking</h3>
                            <p class="text-slate-500 text-sm leading-relaxed">Stay informed about your application status 24/7.</p>
                        </div>
                        
                        <!-- Card 4 (Highlighted) -->
                        <div class="bg-[#0f172a] p-8 rounded-3xl shadow-xl hover:-translate-y-1 transition-all duration-300 sm:translate-y-6 relative overflow-hidden">
                            <div class="absolute -right-4 -top-4 w-24 h-24 bg-blue-500/20 blur-2xl rounded-full"></div>
                            <span class="material-symbols-outlined text-[#38bdf8] text-3xl mb-4 relative z-10">verified</span>
                            <h3 class="text-xl font-bold text-white mb-2 relative z-10">Faculty Review</h3>
                            <p class="text-slate-300 text-sm leading-relaxed relative z-10">Direct connection to lead researchers and professors.</p>
                        </div>
                    </div>

                </div>
            </div>
        </section>

        <!-- Phase 3: Timeline -->
        <section id="phase-3" class="phase-section h-screen w-full snap-start relative flex flex-col items-center justify-center bg-[#f1f5f9]">
            <div class="text-center mb-16">
                <div class="inline-flex items-center gap-2 px-3 py-1 bg-slate-200/50 rounded-full mb-6 text-slate-600 text-[10px] font-black uppercase tracking-widest">
                    PHASE 03 / TIMELINE
                </div>
                <h2 class="text-5xl md:text-6xl font-black text-[#0f172a]">
                    Mark Your Calendar
                </h2>
            </div>

            <!-- Central Timeline Structure -->
            <div class="relative w-full max-w-4xl px-6">
                <!-- Vertical dashed line -->
                <div class="absolute left-1/2 -translate-x-1/2 top-0 bottom-0 w-px bg-slate-300/50"></div>
                
                <div class="flex flex-col gap-12 relative z-10">
                    
                    <!-- Node 1 -->
                    <div class="grid grid-cols-2 gap-8 items-center group">
                        <div class="text-right pr-12 relative">
                            <h3 class="text-2xl font-bold text-[#0f172a] mb-1">Portal Launch</h3>
                            <p class="text-slate-500 text-sm">General applications open for all faculties</p>
                            <!-- Circle marker inside right edge -->
                            <div class="absolute right-0 top-1/2 -translate-y-1/2 translate-x-1/2 w-10 h-10 rounded-full bg-[#e11d48] text-white flex items-center justify-center font-bold text-lg shadow-lg group-hover:scale-110 transition-transform">1</div>
                        </div>
                        <div class="pl-12">
                            <span class="px-3 py-1 bg-pink-100 text-[#e11d48] font-bold text-sm tracking-widest rounded">MARCH 15</span>
                        </div>
                    </div>

                    <!-- Node 2 -->
                    <div class="grid grid-cols-2 gap-8 items-center group">
                        <div class="text-right pr-12">
                            <span class="px-3 py-1 bg-slate-200 text-[#0f172a] font-bold text-sm tracking-widest rounded">APR 05</span>
                        </div>
                        <div class="pl-12 relative">
                            <h3 class="text-2xl font-bold text-[#0f172a] mb-1">Submission Cut-off</h3>
                            <p class="text-slate-500 text-sm">Final deadline for portfolio and document uploads</p>
                            <!-- Circle marker inside left edge -->
                            <div class="absolute left-0 top-1/2 -translate-y-1/2 -translate-x-1/2 w-10 h-10 rounded-full bg-[#0f172a] text-white flex items-center justify-center font-bold text-lg shadow-lg group-hover:scale-110 transition-transform">2</div>
                        </div>
                    </div>

                    <!-- Node 3 -->
                    <div class="grid grid-cols-2 gap-8 items-center group">
                        <div class="text-right pr-12 relative">
                            <h3 class="text-2xl font-bold text-[#0f172a] mb-1">Interview Cycle</h3>
                            <p class="text-slate-500 text-sm">Departmental interviews and technical screenings</p>
                            <!-- Circle marker inside right edge -->
                            <div class="absolute right-0 top-1/2 -translate-y-1/2 translate-x-1/2 w-10 h-10 rounded-full bg-slate-200 text-slate-600 flex items-center justify-center font-bold text-lg shadow border border-slate-300 group-hover:scale-110 transition-transform">3</div>
                        </div>
                        <div class="pl-12">
                            <span class="px-3 py-1 bg-slate-200 text-slate-500 font-bold text-sm tracking-widest rounded">APR 20</span>
                        </div>
                    </div>

                </div>
            </div>
        </section>

        <!-- Phase 4: Final Step -->
        <section id="phase-4" class="phase-section min-h-screen w-full snap-start relative flex items-center bg-[#071324] py-24 lg:py-0"> <!-- Deep Navy overflow protection -->
            <div class="container mx-auto px-6 lg:px-24">
                <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-16 items-center">
                    
                    <!-- Left Copy -->
                    <div class="max-w-xl">
                        <div class="inline-flex items-center gap-2 px-3 py-1 bg-white/5 border border-white/10 rounded-full mb-4 lg:mb-6 text-[#f43f5e] text-[10px] font-black uppercase tracking-widest">
                            PHASE 04 / FINAL STEP
                        </div>
                        
                        <h2 class="text-4xl md:text-5xl lg:text-6xl font-black text-white leading-tight mb-3 lg:mb-4">
                            Ready to Begin<br/>Your Journey?
                        </h2>
                        
                        <p class="text-sm lg:text-base text-slate-300 font-light leading-relaxed mb-6">
                            You have reviewed the requirements, checked the dates, and understood the impact. The portal is now ready for your application.
                        </p>
                        
                        <div class="flex flex-col sm:flex-row gap-3 lg:gap-4 mb-6">
                            <a href="${pageContext.request.contextPath}/login" class="px-5 lg:px-8 py-3 bg-[#e11d48] text-white font-bold rounded-xl hover:bg-[#be185d] transition-colors flex items-center justify-center gap-2 shadow-[0_0_20px_rgba(225,29,72,0.3)] min-w-[200px]">
                                LOGIN TO PORTAL <span class="material-symbols-outlined">arrow_forward</span>
                            </a>
        
                        </div>
                        
                        <div class="flex items-center gap-4 border-t border-white/10 pt-4">
                            <div class="w-10 h-10 shrink-0 rounded-full bg-white/5 border border-white/10 flex items-center justify-center text-[#e11d48]">
                                <span class="material-symbols-outlined text-sm">support_agent</span>
                            </div>
                            <div>
                                <div class="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Tech Support</div>
                                <div class="text-sm text-white focus:outline-none focus:underline"><a href="mailto:ta-support@university.edu">ta-support@university.edu</a></div>
                            </div>
                        </div>
                    </div>

                    <!-- Right Form Visualization -->
                    <div class="hidden lg:block relative perspective-container">
                        <div class="bg-white rounded-[2rem] p-6 lg:p-8 shadow-[0_20px_60px_-10px_rgba(0,0,0,0.5)] transform rotate-y-[-5deg] rotate-x-[2deg] translate-z-[10px]">
                            
                            <div class="absolute -top-6 -right-6 text-[#f1f5f9]">
                                <span class="material-symbols-outlined" style="font-size: 80px;">live_help</span>
                            </div>

                            <h3 class="text-2xl lg:text-3xl font-black text-[#0f172a] mb-6 relative z-10">Direct Inquiry</h3>
                            
                            <div class="space-y-4 lg:space-y-6 relative z-10">
                                <div>
                                    <label class="block text-[10px] font-black tracking-widest text-[#94a3b8] uppercase mb-1 lg:mb-2">Subject</label>
                                    <input type="text" value="Application Issue" class="w-full px-4 py-3 border border-slate-200 rounded-lg text-slate-700 outline-none" readonly>
                                </div>
                                <div>
                                    <label class="block text-[10px] font-black tracking-widest text-[#94a3b8] uppercase mb-1 lg:mb-2">Message</label>
                                    <textarea rows="3" placeholder="How can we help?" class="w-full px-4 py-3 bg-[#f8fafc] border border-slate-200 rounded-lg text-slate-700 outline-none resize-none"></textarea>
                                </div>
                                <button class="w-full py-4 bg-[#0f172a] text-white font-bold rounded-lg hover:bg-[#1e293b] transition-colors mt-2">
                                    SEND MESSAGE
                                </button>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </section>

    </main>

    <style>
        .animate-content-in {
            animation: contentIn 1s cubic-bezier(0.16, 1, 0.3, 1) forwards;
            animation-delay: 0.2s;
        }

        @keyframes contentIn {
            0% { opacity: 0; transform: translateY(30px); }
            100% { opacity: 1; transform: translateY(0); }
        }

        /* Hide scrollbar for cleaner look if desired */
        #journey-container::-webkit-scrollbar {
            width: 0;
            background: transparent;
        }
    </style>

    <script>
        document.addEventListener('DOMContentLoaded', () => {
            const container = document.getElementById('journey-container');
            const indicators = [
                document.getElementById('indicator-1'),
                document.getElementById('indicator-2'),
                document.getElementById('indicator-3'),
                document.getElementById('indicator-4')
            ];
            
            // Allow clicking indicators to scroll
            indicators.forEach((ind, index) => {
                if(ind) {
                   ind.addEventListener('click', () => {
                       const target = document.getElementById('phase-' + (index + 1));
                       if(target) {
                           target.scrollIntoView({ behavior: 'smooth' });
                       }
                   });
                }
            });

            // Intersection Observer to update active indicator
            const observerOptions = {
                root: container,
                threshold: 0.5 // trigger when 50% of the section is visible
            };

            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const phaseId = entry.target.id; 
                        const activeIndex = parseInt(phaseId.split('-')[1]) - 1;

                        indicators.forEach((ind, idx) => {
                            if (idx === activeIndex) {
                                // Active style
                                ind.style.opacity = '1';
                                ind.style.backgroundColor = (activeIndex === 3) ? '#f8fafc' : '#0f172a'; // Contrast changes on dark background
                            } else {
                                // Inactive style
                                ind.style.opacity = '0.3';
                                ind.style.backgroundColor = (activeIndex === 3) ? '#cbd5e1' : '#94a3b8';
                            }
                        });
                        
                        // Small parallax effect re-trigger logic could go here
                        const img = entry.target.querySelector('img');
                        if(img) {
                             img.style.transform = 'scale(1.03)';
                             setTimeout(() => { if(img) img.style.transform = 'scale(1)'; }, 100);
                        }
                    }
                });
            }, observerOptions);

            // Observe all phases
            document.querySelectorAll('.phase-section').forEach(section => {
                observer.observe(section);
            });
            
            // Initial styling for indicator 1
            if(indicators[0]){
                 indicators[0].style.backgroundColor = '#0f172a';
                 indicators[0].style.opacity = '1';
            }
        });
    </script>
</body>
</html>
