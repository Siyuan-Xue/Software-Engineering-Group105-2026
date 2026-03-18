<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String currentPath = request.getRequestURI();
%>
<aside class="w-64 border-r border-primary/10 bg-white hidden lg:flex flex-col p-4 shrink-0">
    <nav class="flex flex-col gap-1">
        <a href="${pageContext.request.contextPath}/portal/dashboard.jsp" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors <%= currentPath.contains("dashboard") ? "bg-primary text-white shadow-sm" : "text-slate-600 hover:bg-slate-50" %>">
            <span class="material-symbols-outlined <%= currentPath.contains("dashboard") ? "!fill-1" : "" %>">dashboard</span>
            <span class="text-sm font-medium">Dashboard</span>
        </a>
        <a href="${pageContext.request.contextPath}/portal/applications.jsp" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors <%= currentPath.contains("applications") ? "bg-primary text-white shadow-sm" : "text-slate-600 hover:bg-slate-50" %>">
            <span class="material-symbols-outlined <%= currentPath.contains("applications") ? "!fill-1" : "" %>">work_history</span>
            <span class="text-sm font-medium">Applications</span>
        </a>
        <a href="${pageContext.request.contextPath}/portal/resumes.jsp" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors <%= currentPath.contains("resumes") ? "bg-primary text-white shadow-sm" : "text-slate-600 hover:bg-slate-50" %>">
            <span class="material-symbols-outlined <%= currentPath.contains("resumes") ? "!fill-1" : "" %>">article</span>
            <span class="text-sm font-medium">Resumes</span>
        </a>
        <a href="${pageContext.request.contextPath}/portal/vacancies.jsp" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors <%= currentPath.contains("vacancies") ? "bg-primary text-white shadow-sm" : "text-slate-600 hover:bg-slate-50" %>">
            <span class="material-symbols-outlined <%= currentPath.contains("vacancies") ? "!fill-1" : "" %>">search</span>
            <span class="text-sm font-medium">Vacancies</span>
        </a>
        <a href="${pageContext.request.contextPath}/portal/messages.jsp" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors <%= currentPath.contains("messages") ? "bg-primary text-white shadow-sm" : "text-slate-600 hover:bg-slate-50" %>">
            <span class="material-symbols-outlined <%= currentPath.contains("messages") ? "!fill-1" : "" %>">chat</span>
            <span class="text-sm font-medium">Messages</span>
        </a>
        
        <div class="my-4 border-t border-slate-100"></div>
        <a href="${pageContext.request.contextPath}/portal/settings.jsp" class="flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors <%= currentPath.contains("settings") ? "bg-primary text-white shadow-sm" : "text-slate-600 hover:bg-slate-50" %>">
            <span class="material-symbols-outlined <%= currentPath.contains("settings") ? "!fill-1" : "" %>">settings</span>
            <span class="text-sm font-medium">Settings</span>
        </a>
    </nav>
    
    <div class="mt-auto bg-primary/5 rounded-xl p-4 border border-primary/10">
        <p class="text-xs font-bold text-primary uppercase tracking-wider mb-2">Profile Strength</p>
        <div class="w-full bg-slate-200 h-1.5 rounded-full overflow-hidden">
            <div class="bg-primary h-full w-[85%]"></div>
        </div>
        <p class="text-xs mt-2 text-slate-500">85% complete. Add a cover letter to reach 100%.</p>
    </div>
</aside>
