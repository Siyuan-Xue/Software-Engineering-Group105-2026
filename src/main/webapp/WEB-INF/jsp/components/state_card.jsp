<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="stateVariant" value="${empty param.variant ? 'empty' : param.variant}" />
<c:set var="stateIcon" value="${empty param.icon ? 'inbox' : param.icon}" />
<c:set var="actionStyle" value="${empty param.actionStyle ? 'primary' : param.actionStyle}" />
<c:choose>
    <c:when test="${stateVariant == 'error'}">
        <c:set var="iconWrapClass" value="bg-red-50 text-red-500" />
        <c:set var="titleClass" value="text-slate-900" />
    </c:when>
    <c:when test="${stateVariant == 'notFound'}">
        <c:set var="iconWrapClass" value="bg-amber-50 text-amber-500" />
        <c:set var="titleClass" value="text-slate-900" />
    </c:when>
    <c:otherwise>
        <c:set var="iconWrapClass" value="bg-slate-100 text-slate-400" />
        <c:set var="titleClass" value="text-slate-900" />
    </c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${actionStyle == 'secondary'}">
        <c:set var="actionClass" value="bg-white border border-slate-200 text-slate-700 hover:bg-slate-50" />
    </c:when>
    <c:otherwise>
        <c:set var="actionClass" value="bg-primary text-white shadow-md hover:bg-primary/90" />
    </c:otherwise>
</c:choose>
<div class="rounded-xl border border-slate-200 bg-white p-12 text-center shadow-sm ${param.containerClass}">
    <div class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full ${iconWrapClass}">
        <span class="material-symbols-outlined text-3xl"><c:out value="${stateIcon}" /></span>
    </div>
    <h3 class="text-xl font-bold ${titleClass}"><c:out value="${param.title}" /></h3>
    <c:if test="${not empty param.message}">
        <p class="mx-auto mt-2 max-w-xl text-sm leading-relaxed text-slate-500"><c:out value="${param.message}" /></p>
    </c:if>
    <c:if test="${not empty param.actionHref and not empty param.actionLabel}">
        <a href="${param.actionHref}" class="mt-6 inline-flex items-center gap-2 rounded-lg px-6 py-2.5 text-sm font-bold transition-colors ${actionClass}">
            <c:out value="${param.actionLabel}" />
        </a>
    </c:if>
</div>
