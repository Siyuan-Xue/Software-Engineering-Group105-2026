<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Messages - QM HIRE</title>
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
                <div class="max-w-6xl mx-auto w-full h-[calc(100vh-120px)] flex bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm">
                    <!-- Chat List -->
                    <div class="w-80 border-r border-slate-200 flex flex-col">
                        <div class="p-4 border-b border-slate-200">
                            <h2 class="text-xl font-bold text-slate-900">Messages</h2>
                        </div>
                        <div class="flex-1 overflow-y-auto">
                            <!-- Chat Item 1 (Active) -->
                            <button class="w-full p-4 flex gap-3 text-left transition-colors hover:bg-slate-50 bg-primary/5 border-r-4 border-primary">
                                <img src="https://ui-avatars.com/api/?name=System&background=0D8ABC&color=fff" alt="System" class="w-12 h-12 rounded-full object-cover" />
                                <div class="flex-1 min-w-0">
                                    <div class="flex justify-between items-baseline">
                                        <h3 class="text-sm font-bold text-slate-900 truncate">System Messages</h3>
                                        <span class="text-[10px] text-slate-500">10:30 AM</span>
                                    </div>
                                    <p class="text-xs text-slate-500 truncate mt-1">Welcome to the platform!</p>
                                </div>
                                <div class="w-5 h-5 bg-primary rounded-full flex items-center justify-center text-[10px] text-white font-bold">
                                    1
                                </div>
                            </button>

                            <!-- Chat Item 2 -->
                            <button class="w-full p-4 flex gap-3 text-left transition-colors hover:bg-slate-50">
                                <img src="https://ui-avatars.com/api/?name=Dr+Smith&background=random" alt="Dr. Smith" class="w-12 h-12 rounded-full object-cover" />
                                <div class="flex-1 min-w-0">
                                    <div class="flex justify-between items-baseline">
                                        <h3 class="text-sm font-bold text-slate-900 truncate">CS101 - MO (Dr. Smith)</h3>
                                        <span class="text-[10px] text-slate-500">Yesterday</span>
                                    </div>
                                    <p class="text-xs text-slate-500 truncate mt-1">Your application has been received.</p>
                                </div>
                            </button>
                        </div>
                    </div>

                    <!-- Chat Window -->
                    <div class="flex-1 flex flex-col">
                        <!-- Chat Header -->
                        <div class="p-4 border-b border-slate-200 flex items-center gap-3">
                            <img src="https://ui-avatars.com/api/?name=System&background=0D8ABC&color=fff" alt="System" class="w-10 h-10 rounded-full object-cover" />
                            <div>
                                <h3 class="text-sm font-bold text-slate-900">System Messages</h3>
                                <span class="text-xs text-green-500 font-medium">Online</span>
                            </div>
                        </div>

                        <!-- Messages Area -->
                        <div class="flex-1 overflow-y-auto p-6 space-y-4 bg-slate-50/50">
                            <!-- System Message -->
                            <div class="flex gap-3 flex-row">
                                <div class="max-w-[70%] p-3 rounded-2xl text-sm bg-slate-200 text-slate-700 mx-auto text-center rounded-lg italic">
                                    Welcome to the platform! We are glad to have you here.
                                    <div class="text-[10px] mt-1 text-slate-400">
                                        10:30 AM
                                    </div>
                                </div>
                            </div>
                            
                            <!-- Example of a regular message from another user -->
                            <!--
                            <div class="flex gap-3 flex-row">
                                <img src="https://ui-avatars.com/api/?name=Dr+Smith&background=random" alt="Dr. Smith" class="w-8 h-8 rounded-full object-cover self-end" />
                                <div class="max-w-[70%] p-3 rounded-2xl text-sm bg-white text-slate-900 border border-slate-200 rounded-bl-none">
                                    Hello Alex. I saw your application for the CS101 TA position.
                                    <div class="text-[10px] mt-1 text-slate-400">
                                        Yesterday
                                    </div>
                                </div>
                            </div>
                            -->

                            <!-- Example of a message from the current user -->
                            <!--
                            <div class="flex gap-3 flex-row-reverse">
                                <div class="max-w-[70%] p-3 rounded-2xl text-sm bg-primary text-white rounded-br-none">
                                    Thank you, Dr. Smith. I am looking forward to it.
                                    <div class="text-[10px] mt-1 text-white/70 text-right">
                                        Yesterday
                                    </div>
                                </div>
                            </div>
                            -->
                        </div>

                        <!-- Message Input -->
                        <div class="p-4 border-t border-slate-200">
                            <form class="flex gap-2">
                                <input
                                    type="text"
                                    placeholder="Type a message..."
                                    class="flex-1 bg-slate-100 border-none rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-primary outline-none"
                                />
                                <button
                                    type="submit"
                                    class="w-10 h-10 bg-primary text-white rounded-xl flex items-center justify-center hover:bg-primary/90 transition-colors"
                                >
                                    <span class="material-symbols-outlined">send</span>
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
