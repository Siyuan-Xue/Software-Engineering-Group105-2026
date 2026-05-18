<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${language == 'zh' ? '申请人详情 - QM HIRE' : 'Applicant Details - QM HIRE'}</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
</head>
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
    <div class="relative flex min-h-screen w-full flex-col">
        <jsp:include page="/WEB-INF/jsp/components/header.jsp" />
        <div class="flex flex-1 overflow-hidden">
            <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />
            <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
                <div class="portal-page max-w-4xl">
                    <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                        <jsp:param name="containerClass" value="mb-6" />
                    </jsp:include>
                    <div class="portal-page-header mb-6">
                        <div>
                            <a href="${pageContext.request.contextPath}/applications" class="text-sm font-bold text-primary hover:underline inline-flex items-center gap-1 mb-2">
                                <span class="material-symbols-outlined text-sm">arrow_back</span>
                                ${language == 'zh' ? '返回申请列表' : 'Back to applications'}
                            </a>
                            <h2 class="portal-page-title">${language == 'zh' ? '申请人详情' : 'Applicant details'}</h2>
                            <p class="portal-page-copy"><c:out value="${vacancyTitle}"/> · <c:out value="${courseCode}"/></p>
                        </div>
                    </div>

                    <div class="portal-panel p-6 space-y-6">
                        <section>
                            <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500 mb-3">${language == 'zh' ? '申请人' : 'Applicant'}</h3>
                            <dl class="grid sm:grid-cols-2 gap-3 text-sm">
                                <div><dt class="text-slate-500">${language == 'zh' ? '姓名' : 'Name'}</dt><dd class="font-semibold text-slate-900"><c:out value="${applicantName}"/></dd></div>
                                <c:if test="${not empty applicantEmail}">
                                    <div><dt class="text-slate-500">${language == 'zh' ? '邮箱' : 'Email'}</dt><dd class="font-medium text-slate-800"><c:out value="${applicantEmail}"/></dd></div>
                                </c:if>
                                <c:if test="${not empty studentId}">
                                    <div><dt class="text-slate-500">${language == 'zh' ? '学号' : 'Student ID'}</dt><dd class="font-medium text-slate-800"><c:out value="${studentId}"/></dd></div>
                                </c:if>
                                <div><dt class="text-slate-500">${language == 'zh' ? '申请状态' : 'Status'}</dt><dd class="font-medium text-slate-800"><c:out value="${applicationStatus}"/></dd></div>
                                <div><dt class="text-slate-500">${language == 'zh' ? '提交日期' : 'Submitted'}</dt><dd class="font-medium text-slate-800"><c:out value="${appliedDate}"/></dd></div>
                            </dl>
                        </section>

                        <section>
                            <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500 mb-3">${language == 'zh' ? '简历' : 'Resume'}</h3>
                            <dl class="grid sm:grid-cols-2 gap-3 text-sm mb-4">
                                <div><dt class="text-slate-500">${language == 'zh' ? '简历标题' : 'Title'}</dt><dd class="font-semibold text-slate-900"><c:out value="${resumeTitle}"/></dd></div>
                                <c:if test="${not empty resumeDepartment}">
                                    <div><dt class="text-slate-500">${language == 'zh' ? '院系' : 'Department'}</dt><dd class="font-medium text-slate-800"><c:out value="${resumeDepartment}"/></dd></div>
                                </c:if>
                                <c:if test="${not empty resumeDegree}">
                                    <div><dt class="text-slate-500">${language == 'zh' ? '学位' : 'Degree'}</dt><dd class="font-medium text-slate-800"><c:out value="${resumeDegree}"/></dd></div>
                                </c:if>
                                <c:if test="${not empty resumeGpa}">
                                    <div><dt class="text-slate-500">GPA</dt><dd class="font-medium text-slate-800"><c:out value="${resumeGpa}"/></dd></div>
                                </c:if>
                            </dl>
                            <c:if test="${not empty resumeBio}">
                                <p class="text-sm text-slate-600 whitespace-pre-wrap mb-4"><c:out value="${resumeBio}"/></p>
                            </c:if>
                            <c:choose>
                                <c:when test="${resumeFileAvailable}">
                                    <a href="${pageContext.request.contextPath}/application/detail?applicationId=${applicationId}&mode=download"
                                       target="_blank" rel="noopener"
                                       class="portal-btn portal-btn-primary inline-flex">
                                        <span class="material-symbols-outlined text-sm">download</span>
                                        ${language == 'zh' ? '查看/下载简历文件' : 'View / download resume file'}
                                        <c:if test="${not empty resumeFileName}"> (<c:out value="${resumeFileName}"/>)</c:if>
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <p class="text-sm text-slate-500">${language == 'zh' ? '申请人未上传简历文件，仅可查看上方文字信息。' : 'No resume file was uploaded; only the profile text above is available.'}</p>
                                </c:otherwise>
                            </c:choose>
                        </section>

                        <c:if test="${not empty coverLetter}">
                            <section>
                                <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500 mb-3">${language == 'zh' ? '求职信 / 动机' : 'Cover letter / motivation'}</h3>
                                <p class="text-sm text-slate-700 whitespace-pre-wrap rounded-xl bg-slate-50 border border-slate-100 p-4"><c:out value="${coverLetter}"/></p>
                            </section>
                        </c:if>

                        <c:if test="${not empty moNotes}">
                            <section>
                                <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500 mb-3">${language == 'zh' ? '备注' : 'Notes'}</h3>
                                <p class="text-sm text-slate-600 whitespace-pre-wrap"><c:out value="${moNotes}"/></p>
                            </section>
                        </c:if>

                        <c:if test="${applicationStatus != 'Withdrawn' && applicationStatus != 'Accepted' && applicationStatus != 'Rejected' && applicationStatus != 'Declined'}">
                            <section class="border-t border-slate-100 pt-6">
                                <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500 mb-3">${language == 'zh' ? '处理申请' : 'Manage application'}</h3>
                                <div class="flex flex-wrap gap-3">
                                    <c:if test="${applicationStatus == 'Submitted'}">
                                        <form action="${pageContext.request.contextPath}/application/decision" method="POST">
                                            <input type="hidden" name="applicationId" value="${applicationId}"/>
                                            <input type="hidden" name="action" value="review"/>
                                            <input type="hidden" name="returnTo" value="detail"/>
                                            <button type="submit" class="portal-btn portal-btn-secondary text-sm">${language == 'zh' ? '开始审核' : 'Start review'}</button>
                                        </form>
                                    </c:if>
                                    <c:if test="${applicationStatus == 'Submitted' || applicationStatus == 'Under Review'}">
                                        <form action="${pageContext.request.contextPath}/application/decision" method="POST" onsubmit="return confirm('${language == 'zh' ? '确定直接录用该申请人？' : 'Accept this applicant?'}');">
                                            <input type="hidden" name="applicationId" value="${applicationId}"/>
                                            <input type="hidden" name="action" value="mo_accept"/>
                                            <input type="hidden" name="returnTo" value="detail"/>
                                            <button type="submit" class="portal-btn portal-btn-primary text-sm">${language == 'zh' ? '录用' : 'Accept'}</button>
                                        </form>
                                    </c:if>
                                    <form action="${pageContext.request.contextPath}/application/decision" method="POST" onsubmit="return confirmReject(this, '${language == 'zh' ? 'zh' : 'en'}');">
                                        <input type="hidden" name="applicationId" value="${applicationId}"/>
                                        <input type="hidden" name="action" value="reject"/>
                                        <input type="hidden" name="returnTo" value="detail"/>
                                        <input type="hidden" name="rejectionNote" value=""/>
                                        <button type="submit" class="portal-btn portal-btn-secondary text-sm text-red-700">${language == 'zh' ? '拒绝' : 'Reject'}</button>
                                    </form>
                                </div>
                            </section>
                        </c:if>
                    </div>
                </div>
            </main>
        </div>
    </div>
    <script>
        function confirmReject(form, lang) {
            var msg = lang === 'zh' ? '确定拒绝该申请？' : 'Reject this application?';
            if (!confirm(msg)) return false;
            var noteMsg = lang === 'zh' ? '可选：填写拒绝说明（留空则跳过）' : 'Optional rejection note (leave blank to skip)';
            var note = prompt(noteMsg, '');
            if (note === null) return false;
            var input = form.querySelector('input[name="rejectionNote"]');
            if (input) input.value = note;
            return true;
        }
    </script>
</body>
</html>
