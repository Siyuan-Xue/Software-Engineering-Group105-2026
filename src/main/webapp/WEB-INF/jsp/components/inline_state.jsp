<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="stateVariant" value="${empty param.variant ? 'empty' : param.variant}" />
<c:set var="stateIcon" value="${empty param.icon ? 'inbox' : param.icon}" />
<c:choose>
    <c:when test="${stateVariant == 'error'}">
        <c:set var="iconWrapClass" value="bg-red-50 text-red-500" />
    </c:when>
    <c:when test="${stateVariant == 'notFound'}">
        <c:set var="iconWrapClass" value="bg-amber-50 text-amber-500" />
    </c:when>
    <c:otherwise>
        <c:set var="iconWrapClass" value="bg-slate-100 text-slate-400" />
    </c:otherwise>
</c:choose>
<div class="flex flex-col items-center justify-center px-6 py-8 text-center ${param.containerClass}">
    <div class="mb-3 flex h-12 w-12 items-center justify-center rounded-full ${iconWrapClass}">
        <span class="material-symbols-outlined text-2xl"><c:out value="${stateIcon}" /></span>
    </div>
    <h3 class="text-base font-bold text-slate-900"><c:out value="${param.title}" /></h3>
    <c:if test="${not empty param.message}">
        <p class="mt-2 max-w-md text-sm leading-relaxed text-slate-500"><c:out value="${param.message}" /></p>
    </c:if>
    <c:if test="${not empty param.actionHref and not empty param.actionLabel}">
        <a href="${param.actionHref}" class="mt-4 inline-flex items-center gap-2 rounded-lg bg-white px-4 py-2 text-sm font-bold text-slate-700 ring-1 ring-slate-200 transition-colors hover:bg-slate-50">
            <c:out value="${param.actionLabel}" />
        </a>
    </c:if>
</div>
