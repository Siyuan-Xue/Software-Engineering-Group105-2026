<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                <div class="max-w-6xl mx-auto w-full">
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
                </div>
                <div class="max-w-6xl mx-auto w-full h-[calc(100vh-120px)] flex bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm">
                    <!-- Chat List -->
                    <div class="w-80 border-r border-slate-200 flex flex-col">
                        <div class="p-4 border-b border-slate-200">
                            <h2 class="text-xl font-bold text-slate-900">Messages</h2>
                        </div>
                        <div class="flex-1 overflow-y-auto">
                            <c:choose>
                                <c:when test="${empty conversations}">
                                    <div class="p-6 text-center text-slate-500 text-sm">
                                        No conversations yet.
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${conversations}" var="conv">
                                        <!-- Chat Item -->
                                        <button class="w-full p-4 flex gap-3 text-left transition-colors hover:bg-slate-50 ${conv.id == activeConversation.id ? 'bg-primary/5 border-r-4 border-primary' : ''}">
                                            <img src="https://ui-avatars.com/api/?name=${conv.otherUserName}&background=random" alt="${conv.otherUserName}" class="w-12 h-12 rounded-full object-cover" />
                                            <div class="flex-1 min-w-0">
                                                <div class="flex justify-between items-baseline">
                                                    <h3 class="text-sm font-bold text-slate-900 truncate"><c:out value="${conv.otherUserName}"/></h3>
                                                    <span class="text-[10px] text-slate-500"><c:out value="${conv.lastMessageTime}"/></span>
                                                </div>
                                                <p class="text-xs text-slate-500 truncate mt-1"><c:out value="${conv.lastMessagePreview}"/></p>
                                            </div>
                                            <c:if test="${conv.unreadCount > 0}">
                                                <div class="w-5 h-5 bg-primary rounded-full flex items-center justify-center text-[10px] text-white font-bold">
                                                    <c:out value="${conv.unreadCount}"/>
                                                </div>
                                            </c:if>
                                        </button>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Chat Window -->
                    <div class="flex-1 flex flex-col">
                        <c:choose>
                            <c:when test="${empty activeConversation}">
                                <div class="flex-1 flex items-center justify-center text-slate-400">
                                    <div class="text-center">
                                        <span class="material-symbols-outlined text-4xl mb-2">chat</span>
                                        <p>Select a conversation to start messaging</p>
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <!-- Chat Header -->
                                <div class="p-4 border-b border-slate-200 flex items-center gap-3">
                                    <img src="https://ui-avatars.com/api/?name=${activeConversation.otherUserName}&background=random" alt="${activeConversation.otherUserName}" class="w-10 h-10 rounded-full object-cover" />
                                    <div>
                                        <h3 class="text-sm font-bold text-slate-900"><c:out value="${activeConversation.otherUserName}"/></h3>
                                        <span class="text-xs text-green-500 font-medium">Online</span>
                                    </div>
                                </div>

                                <!-- Messages Area -->
                                <div class="flex-1 overflow-y-auto p-6 space-y-4 bg-slate-50/50">
                                    <c:forEach items="${activeConversation.messages}" var="msg">
                                        <c:choose>
                                            <c:when test="${msg.isSystemMessage}">
                                                <!-- System Message -->
                                                <div class="flex gap-3 flex-row">
                                                    <div class="max-w-[70%] p-3 rounded-2xl text-sm bg-slate-200 text-slate-700 mx-auto text-center rounded-lg italic">
                                                        <c:out value="${msg.content}"/>
                                                        <div class="text-[10px] mt-1 text-slate-400">
                                                            <c:out value="${msg.time}"/>
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:when>
                                            <c:when test="${msg.isFromCurrentUser}">
                                                <!-- Message from current user -->
                                                <div class="flex gap-3 flex-row-reverse">
                                                    <div class="max-w-[70%] p-3 rounded-2xl text-sm bg-primary text-white rounded-br-none">
                                                        <c:out value="${msg.content}"/>
                                                        <div class="text-[10px] mt-1 text-white/70 text-right">
                                                            <c:out value="${msg.time}"/>
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <!-- Message from other user -->
                                                <div class="flex gap-3 flex-row">
                                                    <img src="https://ui-avatars.com/api/?name=${activeConversation.otherUserName}&background=random" alt="${activeConversation.otherUserName}" class="w-8 h-8 rounded-full object-cover self-end" />
                                                    <div class="max-w-[70%] p-3 rounded-2xl text-sm bg-white text-slate-900 border border-slate-200 rounded-bl-none">
                                                        <c:out value="${msg.content}"/>
                                                        <div class="text-[10px] mt-1 text-slate-400">
                                                            <c:out value="${msg.time}"/>
                                                        </div>
                                                    </div>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </div>

                                <!-- Message Input -->
                                <div class="p-4 border-t border-slate-200">
                                    <form action="${pageContext.request.contextPath}/messages" method="POST" class="flex gap-2">
                                        <input type="hidden" name="conversationId" value="${activeConversation.id}" />
                                        <input
                                            type="text"
                                            name="messageContent"
                                            placeholder="Type a message..."
                                            class="flex-1 bg-slate-100 border-none rounded-xl px-4 py-2 text-sm focus:ring-2 focus:ring-primary outline-none"
                                            required
                                        />
                                        <button
                                            type="submit"
                                            class="w-10 h-10 bg-primary text-white rounded-xl flex items-center justify-center hover:bg-primary/90 transition-colors"
                                        >
                                            <span class="material-symbols-outlined">send</span>
                                        </button>
                                    </form>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
