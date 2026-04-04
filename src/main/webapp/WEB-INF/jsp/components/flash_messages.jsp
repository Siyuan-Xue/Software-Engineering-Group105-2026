<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty errorMessage or not empty successMessage}">
    <div class="space-y-4 ${param.containerClass}">
        <c:if test="${not empty errorMessage}">
            <div class="rounded-xl border border-red-200 bg-red-50 p-4 text-red-600" role="alert">
                <div class="flex items-start gap-3">
                    <span class="material-symbols-outlined shrink-0 text-red-500">error</span>
                    <p class="text-sm font-medium"><c:out value="${errorMessage}"/></p>
                </div>
            </div>
        </c:if>
        <c:if test="${not empty successMessage}">
            <div class="rounded-xl border border-green-200 bg-green-50 p-4 text-green-600" role="status">
                <div class="flex items-start gap-3">
                    <span class="material-symbols-outlined shrink-0 text-green-500">check_circle</span>
                    <p class="text-sm font-medium"><c:out value="${successMessage}"/></p>
                </div>
            </div>
        </c:if>
    </div>
</c:if>
