<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
                    <a href="${pageContext.request.contextPath}/portal/vacancies.jsp" class="flex items-center gap-2 text-slate-500 hover:text-primary mb-6 transition-colors font-medium w-fit">
                        <span class="material-symbols-outlined">arrow_back</span>
                        Back to Vacancies
                    </a>

                    <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm">
                        <div class="p-8 border-b border-slate-100">
                            <div class="flex flex-col md:flex-row justify-between items-start gap-4">
                                <div>
                                    <span class="px-3 py-1 bg-primary/10 text-primary text-xs font-bold uppercase rounded-md tracking-wider mb-3 inline-block">
                                        CS101
                                    </span>
                                    <h2 class="text-3xl font-black text-slate-900 tracking-tight">Introduction to Computer Science</h2>
                                    <p class="text-lg text-slate-500 mt-1">Computer Science</p>
                                </div>
                                <button onclick="document.getElementById('applyModal').classList.remove('hidden')" class="bg-primary text-white px-8 py-3 rounded-xl font-bold shadow-lg shadow-primary/20 hover:scale-105 transition-transform">
                                    Apply Now
                                </button>
                            </div>

                            <div class="grid grid-cols-2 md:grid-cols-4 gap-6 mt-8">
                                <div class="space-y-1">
                                    <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">Hours</p>
                                    <p class="text-sm font-bold text-slate-700">10-15 hrs/week</p>
                                </div>
                                <div class="space-y-1">
                                    <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">Pay Rate</p>
                                    <p class="text-sm font-bold text-slate-700">$25/hr</p>
                                </div>
                                <div class="space-y-1">
                                    <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">Deadline</p>
                                    <p class="text-sm font-bold text-red-500">Oct 15, 2024</p>
                                </div>
                                <div class="space-y-1">
                                    <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">Module Owner</p>
                                    <p class="text-sm font-bold text-slate-700">Dr. Smith</p>
                                </div>
                            </div>
                        </div>

                        <div class="p-8 space-y-8">
                            <section>
                                <h3 class="text-xl font-bold text-slate-900 mb-4">Description</h3>
                                <p class="text-slate-600 leading-relaxed">
                                    We are looking for enthusiastic Teaching Assistants to support our CS101 course. This is a foundational course that covers programming basics, algorithms, and data structures using Python. As a TA, you will play a crucial role in student success by providing guidance during labs and office hours.
                                </p>
                            </section>

                            <section>
                                <h3 class="text-xl font-bold text-slate-900 mb-4">Requirements</h3>
                                <ul class="space-y-3">
                                    <li class="flex gap-3 text-slate-600">
                                        <span class="material-symbols-outlined text-primary text-lg">check_circle</span>
                                        <span class="text-sm">Strong proficiency in Python programming</span>
                                    </li>
                                    <li class="flex gap-3 text-slate-600">
                                        <span class="material-symbols-outlined text-primary text-lg">check_circle</span>
                                        <span class="text-sm">Completed CS101 or equivalent with an A grade</span>
                                    </li>
                                    <li class="flex gap-3 text-slate-600">
                                        <span class="material-symbols-outlined text-primary text-lg">check_circle</span>
                                        <span class="text-sm">Excellent communication and interpersonal skills</span>
                                    </li>
                                    <li class="flex gap-3 text-slate-600">
                                        <span class="material-symbols-outlined text-primary text-lg">check_circle</span>
                                        <span class="text-sm">Ability to explain complex concepts in simple terms</span>
                                    </li>
                                    <li class="flex gap-3 text-slate-600">
                                        <span class="material-symbols-outlined text-primary text-lg">check_circle</span>
                                        <span class="text-sm">Prior tutoring or teaching experience is a plus</span>
                                    </li>
                                </ul>
                            </section>
                        </div>
                    </div>

                    <!-- Apply Modal -->
                    <div id="applyModal" class="hidden fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
                        <div class="bg-white rounded-2xl w-full max-w-md p-8 shadow-2xl border border-slate-200">
                            <h3 class="text-2xl font-black text-slate-900 mb-2">Select Resume</h3>
                            <p class="text-slate-500 text-sm mb-6">Choose which resume you want to use for this application.</p>
                            
                            <form action="${pageContext.request.contextPath}/portal/applications.jsp" method="POST">
                                <div class="space-y-3 mb-8">
                                    <label class="w-full p-4 rounded-xl border-2 border-slate-100 hover:border-primary/30 text-left transition-all flex items-center gap-4 cursor-pointer">
                                        <input type="radio" name="resumeId" value="1" class="w-5 h-5 text-primary border-slate-300 focus:ring-primary" required>
                                        <div class="flex-1">
                                            <p class="text-sm font-bold text-slate-900">Academic_CV_2024.pdf</p>
                                            <p class="text-xs text-slate-500">Uploaded Oct 24, 2023</p>
                                        </div>
                                    </label>
                                    <label class="w-full p-4 rounded-xl border-2 border-slate-100 hover:border-primary/30 text-left transition-all flex items-center gap-4 cursor-pointer">
                                        <input type="radio" name="resumeId" value="2" class="w-5 h-5 text-primary border-slate-300 focus:ring-primary">
                                        <div class="flex-1">
                                            <p class="text-sm font-bold text-slate-900">Industry_Resume_Tech.docx</p>
                                            <p class="text-xs text-slate-500">Uploaded Sep 12, 2023</p>
                                        </div>
                                    </label>
                                </div>

                                <div class="flex gap-3">
                                    <button type="button" onclick="document.getElementById('applyModal').classList.add('hidden')" class="flex-1 py-3 bg-slate-100 text-slate-600 rounded-xl font-bold text-sm hover:bg-slate-200 transition-colors">
                                        Cancel
                                    </button>
                                    <button type="submit" class="flex-1 py-3 bg-primary text-white rounded-xl font-bold text-sm shadow-lg shadow-primary/20 hover:bg-primary/90 transition-colors">
                                        Confirm Apply
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
