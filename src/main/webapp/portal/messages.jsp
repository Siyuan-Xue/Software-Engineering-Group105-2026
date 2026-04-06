<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
</head>
<body class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
    <div class="relative flex min-h-screen w-full flex-col">
        <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

        <div class="flex flex-1 overflow-hidden">
            <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

            <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
                <div class="portal-page">
                    <c:set var="messagesState" value="${empty pageState ? 'normal' : pageState}" />
                    <c:set var="showConversationEmpty" value="${messagesState == 'empty' or empty conversations}" />
                    <c:set var="showNoActiveConversation" value="${messagesState == 'noActiveConversation' or (not showConversationEmpty and empty activeConversation)}" />
                    <c:set var="selectedConversationId" value="${empty activeConversation.conversationId ? (empty activeConversation.id ? param.conversationId : activeConversation.id) : activeConversation.conversationId}" />
                    <c:set var="noActiveMessage" value="${empty param.conversationId ? 'Choose a conversation from the left to read messages and send replies.' : 'We could not open that conversation. Choose another thread from the list.'}" />

                    <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                        <jsp:param name="containerClass" value="mb-6" />
                    </jsp:include>

                    <c:choose>
                        <c:when test="${messagesState == 'loadError'}">
                            <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                                <jsp:param name="variant" value="error" />
                                <jsp:param name="icon" value="mark_chat_read" />
                                <jsp:param name="title" value="Messages unavailable" />
                                <jsp:param name="message" value="We couldn't load your conversations right now. Please try again after refreshing the page." />
                                <jsp:param name="actionHref" value="${pageContext.request.contextPath}/messages" />
                                <jsp:param name="actionLabel" value="Try Again" />
                            </jsp:include>
                        </c:when>
                        <c:otherwise>
                            <div class="portal-page-header">
                                <div>
                                    <h2 class="portal-page-title">Messages</h2>
                                    <p class="portal-page-copy">Stay in touch with recruiters and department contacts as your applications move forward.</p>
                                </div>
                                <div class="portal-summary-card">
                                    <p class="portal-kicker">Inbox Summary</p>
                                    <p class="mt-1 text-sm font-semibold text-slate-900">
                                        <c:choose>
                                            <c:when test="${showConversationEmpty}">
                                                0 active conversations
                                            </c:when>
                                            <c:otherwise>
                                                <c:out value="${fn:length(conversations)}" /> active conversations
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                    <p class="mt-1 text-xs text-slate-500">
                                        <c:choose>
                                            <c:when test="${showConversationEmpty}">
                                                New conversations will appear here once departments or recruiters contact you.
                                            </c:when>
                                            <c:when test="${showNoActiveConversation}">
                                                Select a conversation to read updates and send replies.
                                            </c:when>
                                            <c:otherwise>
                                                Conversation ready. Keep your replies clear and timely.
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </div>

                            <div class="portal-panel flex h-[calc(100vh-120px)] overflow-hidden">
                                <div class="flex w-80 flex-col border-r border-slate-200">
                                    <div class="border-b border-slate-200 p-4">
                                        <h3 class="text-base font-bold text-slate-900">Conversation List</h3>
                                        <p class="mt-1 text-xs text-slate-500">Open a thread to read updates and send replies.</p>
                                    </div>
                                    <div class="flex-1 overflow-y-auto">
                                        <c:choose>
                                            <c:when test="${showConversationEmpty}">
                                                <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                    <jsp:param name="icon" value="forum" />
                                                    <jsp:param name="title" value="No conversations yet" />
                                                    <jsp:param name="message" value="Messages from recruiters and departments will appear here once a conversation starts." />
                                                    <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                                    <jsp:param name="actionLabel" value="Browse Vacancies" />
                                                    <jsp:param name="containerClass" value="min-h-full" />
                                                </jsp:include>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach items="${conversations}" var="conv">
                                                    <c:set var="conversationId" value="${empty conv.conversationId ? conv.id : conv.conversationId}" />
                                                    <c:set var="contactName" value="${empty conv.contactName ? (empty conv.otherUserName ? 'Contact' : conv.otherUserName) : conv.contactName}" />
                                                    <c:set var="contactRole" value="${empty conv.contactRole ? 'Department Contact' : conv.contactRole}" />
                                                    <c:set var="contactAvatar" value="${conv.contactAvatar}" />
                                                    <c:set var="lastMessage" value="${empty conv.lastMessage ? (empty conv.lastMessagePreview ? 'No messages yet' : conv.lastMessagePreview) : conv.lastMessage}" />
                                                    <c:set var="lastMessageTime" value="${conv.lastMessageTime}" />
                                                    <c:set var="contactInitial" value="${fn:toUpperCase(fn:substring(contactName, 0, 1))}" />
                                                    <a href="${pageContext.request.contextPath}/messages?conversationId=${conversationId}" class="flex w-full gap-3 border-r-4 p-4 text-left transition-colors hover:bg-slate-50 ${conversationId == selectedConversationId ? 'border-primary bg-primary/5' : 'border-transparent'}">
                                                        <c:choose>
                                                            <c:when test="${not empty contactAvatar}">
                                                                <img src="${contactAvatar}" alt="${contactName}" class="h-12 w-12 rounded-full object-cover" />
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">
                                                                    <c:out value="${contactInitial}" />
                                                                </div>
                                                            </c:otherwise>
                                                        </c:choose>
                                                        <div class="min-w-0 flex-1">
                                                            <div class="flex items-baseline justify-between gap-3">
                                                                <div class="min-w-0">
                                                                    <h3 class="truncate text-sm font-bold text-slate-900"><c:out value="${contactName}" /></h3>
                                                                    <p class="truncate text-[11px] text-slate-400"><c:out value="${contactRole}" /></p>
                                                                </div>
                                                                <span class="shrink-0 text-[10px] text-slate-500"><c:out value="${lastMessageTime}" /></span>
                                                            </div>
                                                            <p class="mt-1 truncate text-xs text-slate-500"><c:out value="${lastMessage}" /></p>
                                                        </div>
                                                        <c:if test="${conv.unreadCount > 0}">
                                                            <div class="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-primary text-[10px] font-bold text-white">
                                                                <c:out value="${conv.unreadCount}" />
                                                            </div>
                                                        </c:if>
                                                    </a>
                                                </c:forEach>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <div class="flex flex-1 flex-col">
                                    <c:choose>
                                        <c:when test="${showConversationEmpty}">
                                            <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                <jsp:param name="icon" value="mark_chat_unread" />
                                                <jsp:param name="title" value="Inbox waiting for your first conversation" />
                                                <jsp:param name="message" value="Once a department reaches out or you begin a thread from an application, the full conversation view will appear here." />
                                                <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                                <jsp:param name="actionLabel" value="Find Opportunities" />
                                                <jsp:param name="containerClass" value="min-h-full" />
                                            </jsp:include>
                                        </c:when>
                                        <c:when test="${showNoActiveConversation}">
                                            <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                <jsp:param name="icon" value="chat" />
                                                <jsp:param name="title" value="No conversation selected" />
                                                <jsp:param name="message" value="${noActiveMessage}" />
                                                <jsp:param name="containerClass" value="min-h-full" />
                                            </jsp:include>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="activeConversationId" value="${empty activeConversation.conversationId ? activeConversation.id : activeConversation.conversationId}" />
                                            <c:set var="activeContactName" value="${empty activeConversation.contactName ? (empty activeConversation.otherUserName ? 'Contact' : activeConversation.otherUserName) : activeConversation.contactName}" />
                                            <c:set var="activeContactRole" value="${empty activeConversation.contactRole ? 'Department Contact' : activeConversation.contactRole}" />
                                            <c:set var="activeContactAvatar" value="${activeConversation.contactAvatar}" />
                                            <c:set var="activeContactInitial" value="${fn:toUpperCase(fn:substring(activeContactName, 0, 1))}" />

                                            <div class="border-b border-slate-200 p-4">
                                                <div class="flex items-center gap-3">
                                                    <c:choose>
                                                        <c:when test="${not empty activeContactAvatar}">
                                                            <img src="${activeContactAvatar}" alt="${activeContactName}" class="h-10 w-10 rounded-full object-cover" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <div class="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-sm font-bold text-primary">
                                                                <c:out value="${activeContactInitial}" />
                                                            </div>
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <div>
                                                        <h3 class="text-sm font-bold text-slate-900"><c:out value="${activeContactName}" /></h3>
                                                        <span class="text-xs font-medium text-slate-500"><c:out value="${activeContactRole}" /></span>
                                                    </div>
                                                </div>
                                                <p class="mt-3 text-xs leading-relaxed text-slate-500">Keep replies professional and concise. Important updates about interviews, documents, or timelines should stay in this thread.</p>
                                            </div>

                                            <div class="flex-1 space-y-4 overflow-y-auto bg-slate-50/50 p-6">
                                                <c:choose>
                                                    <c:when test="${empty activeConversation.messages}">
                                                        <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                            <jsp:param name="icon" value="chat_bubble" />
                                                            <jsp:param name="title" value="No messages in this thread yet" />
                                                            <jsp:param name="message" value="Start the conversation with a clear question or confirmation so the contact can respond quickly." />
                                                            <jsp:param name="containerClass" value="min-h-full" />
                                                        </jsp:include>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:forEach items="${activeConversation.messages}" var="msg">
                                                            <c:set var="messageTimestamp" value="${empty msg.timestamp ? msg.time : msg.timestamp}" />
                                                            <c:set var="isMine" value="${empty msg.isMine ? msg.isFromCurrentUser : msg.isMine}" />
                                                            <c:choose>
                                                                <c:when test="${msg.isSystemMessage}">
                                                                    <div class="flex gap-3">
                                                                        <div class="mx-auto max-w-[70%] rounded-lg bg-slate-200 p-3 text-center text-sm italic text-slate-700">
                                                                            <c:out value="${msg.content}" />
                                                                            <div class="mt-1 text-[10px] text-slate-400">
                                                                                <c:out value="${messageTimestamp}" />
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </c:when>
                                                                <c:when test="${isMine}">
                                                                    <div class="flex flex-row-reverse gap-3">
                                                                        <div class="max-w-[70%] rounded-2xl rounded-br-none bg-primary p-3 text-sm text-white">
                                                                            <c:out value="${msg.content}" />
                                                                            <div class="mt-1 text-right text-[10px] text-white/70">
                                                                                <c:out value="${messageTimestamp}" />
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <div class="flex gap-3">
                                                                        <c:choose>
                                                                            <c:when test="${not empty activeContactAvatar}">
                                                                                <img src="${activeContactAvatar}" alt="${activeContactName}" class="h-8 w-8 self-end rounded-full object-cover" />
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                <div class="flex h-8 w-8 shrink-0 self-end items-center justify-center rounded-full bg-primary/10 text-[11px] font-bold text-primary">
                                                                                    <c:out value="${activeContactInitial}" />
                                                                                </div>
                                                                            </c:otherwise>
                                                                        </c:choose>
                                                                        <div class="max-w-[70%] rounded-2xl rounded-bl-none border border-slate-200 bg-white p-3 text-sm text-slate-900">
                                                                            <c:out value="${msg.content}" />
                                                                            <div class="mt-1 text-[10px] text-slate-400">
                                                                                <c:out value="${messageTimestamp}" />
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </c:forEach>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>

                                            <div class="border-t border-slate-200 p-4">
                                                <form action="${pageContext.request.contextPath}/messages" method="POST" class="flex gap-2">
                                                    <input type="hidden" name="conversationId" value="${activeConversationId}" />
                                                    <input
                                                        type="text"
                                                        name="messageContent"
                                                        placeholder="Type a message..."
                                                        class="flex-1 rounded-xl border-none bg-slate-100 px-4 py-2 text-sm outline-none focus:ring-2 focus:ring-primary"
                                                        required
                                                    />
                                                    <button
                                                        type="submit"
                                                        class="flex h-10 w-10 items-center justify-center rounded-xl bg-primary text-white transition-colors hover:bg-primary/90"
                                                    >
                                                        <span class="material-symbols-outlined">send</span>
                                                    </button>
                                                </form>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
