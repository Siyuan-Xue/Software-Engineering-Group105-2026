<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${language == 'zh' ? '编辑岗位 - QM HIRE' : 'Edit Vacancy - QM HIRE'}</title>
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
                    fontFamily: { sans: ['Inter', 'sans-serif'] }
                }
            }
        }
    </script>
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
</head>
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
<div class="relative flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />
    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />
        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="portal-page max-w-3xl">
                <c:choose>
                    <c:when test="${editReturnTo == 'list'}">
                        <a href="${pageContext.request.contextPath}/vacancies"
                           class="flex items-center gap-2 text-slate-500 hover:text-primary mb-6 transition-colors font-medium w-fit">
                            <span class="material-symbols-outlined">arrow_back</span>
                            ${language == 'zh' ? '返回岗位列表' : 'Back to Vacancies'}
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/vacancy?vacancyId=${fn:escapeXml(editVacancyId)}"
                           class="flex items-center gap-2 text-slate-500 hover:text-primary mb-6 transition-colors font-medium w-fit">
                            <span class="material-symbols-outlined">arrow_back</span>
                            ${language == 'zh' ? '返回岗位详情' : 'Back to Vacancy Detail'}
                        </a>
                    </c:otherwise>
                </c:choose>

                <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                    <jsp:param name="containerClass" value="mb-6" />
                </jsp:include>

                <div class="portal-panel p-8">
                    <h1 class="text-2xl font-black text-slate-900 mb-2">${language == 'zh' ? '编辑岗位' : 'Edit Vacancy'}</h1>
                    <p class="text-sm text-slate-500 mb-8">${language == 'zh' ? '保存后将按你的选择返回列表或详情页。' : 'After saving you will return to the list or detail page as selected below.'}</p>

                    <form action="${pageContext.request.contextPath}/vacancy/edit" method="POST" class="space-y-5">
                        <input type="hidden" name="vacancyId" value="${fn:escapeXml(editVacancyId)}"/>

                        <div>
                            <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '标题' : 'Title'} <span class="text-red-500">*</span></label>
                            <input type="text" name="title" required value="${fn:escapeXml(editTitle)}"
                                   class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"/>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '课程代码' : 'Course Code'}</label>
                                <input type="text" name="courseCode" value="${fn:escapeXml(editCourseCode)}"
                                       class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"/>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '学期' : 'Term'}</label>
                                <select name="term" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none">
                                    <c:forEach items="${termOptions}" var="termOpt">
                                        <option value="${fn:escapeXml(termOpt)}" ${editTerm == termOpt ? 'selected' : ''}><c:out value="${termOpt}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '岗位类型' : 'Type'}</label>
                                <select name="type" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none">
                                    <option value="MODULE_SUPPORT" ${editType == 'MODULE_SUPPORT' ? 'selected' : ''}>${language == 'zh' ? '课程支持' : 'Module support'}</option>
                                    <option value="INVIGILATION" ${editType == 'INVIGILATION' ? 'selected' : ''}>${language == 'zh' ? '监考' : 'Invigilation'}</option>
                                    <option value="OTHER" ${editType == 'OTHER' ? 'selected' : ''}>${language == 'zh' ? '其他' : 'Other'}</option>
                                </select>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '名额' : 'Slots'}</label>
                                <input type="number" name="slots" min="1" value="${editSlots}"
                                       class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"/>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '开始日期' : 'Start Date'}</label>
                                <input type="date" name="startDate" value="${fn:escapeXml(editStartDate)}"
                                       class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"/>
                            </div>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '描述' : 'Description'}</label>
                            <textarea name="description" rows="4" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"><c:out value="${editDescription}"/></textarea>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '标签' : 'Labels'}</label>
                            <input type="text" name="labels" value="${fn:escapeXml(editLabels)}"
                                   class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"
                                   placeholder="${language == 'zh' ? '例如：Java, 实验课, 本科' : 'e.g. Java, lab, undergraduate'}"/>
                            <p class="text-xs text-slate-400 mt-1">${language == 'zh' ? '多个标签可用逗号、分号、竖线或换行分隔；最多 24 个，每个最长 48 字符。' : 'Separate tags with commas, semicolons, pipes, or newlines; up to 24 tags, 48 characters each.'}</p>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '每周工时' : 'Hours/Week'}</label>
                                <input type="number" name="hoursPerWeek" min="1" value="${editHoursPerWeek}"
                                       class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"/>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '时薪 (£)' : 'Hourly Rate (£)'}</label>
                                <input type="number" step="0.01" name="hourlyRate" min="0" value="${fn:escapeXml(editHourlyRate)}"
                                       class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"/>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '截止时间' : 'Deadline'}</label>
                                <input type="datetime-local" name="deadline" required value="${fn:escapeXml(editDeadline)}"
                                       class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"/>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '结束日期' : 'End Date'}</label>
                                <input type="date" name="endDate" value="${fn:escapeXml(editEndDate)}"
                                       class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none"/>
                            </div>
                        </div>
                        <div class="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                            <div class="mb-3 flex items-center justify-between gap-3">
                                <div>
                                    <label class="block text-sm font-bold text-slate-800">${language == 'zh' ? '技能要求' : 'Skill Requirements'}</label>
                                    <p class="mt-1 text-xs text-slate-500">${language == 'zh' ? '用于申请详情中的覆盖率、缺口和持久化匹配分析。' : 'Used for coverage, skill gaps, and persisted match analysis in applicant details.'}</p>
                                </div>
                                <span class="material-symbols-outlined text-slate-400">psychology</span>
                            </div>
                            <div class="space-y-3">
                                <c:forEach items="${jobRequirements}" var="jr" varStatus="jrs">
                                    <div class="grid grid-cols-1 gap-2 md:grid-cols-[1.2fr_1fr_0.6fr]">
                                        <select name="skillId" class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-700">
                                            <option value="">${language == 'zh' ? '选择技能' : 'Select skill'}</option>
                                            <c:forEach items="${skills}" var="skill">
                                                <c:if test="${skill.active or skill.id == jr.skillId}">
                                                    <option value="${skill.id}" ${skill.id == jr.skillId ? 'selected' : ''}><c:out value="${skill.name}"/></option>
                                                </c:if>
                                            </c:forEach>
                                        </select>
                                        <select name="minProficiency" class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-700">
                                            <c:forEach items="${proficiencyLevels}" var="level">
                                                <option value="${level}" ${jr.minProficiency == level ? 'selected' : ''}><c:out value="${level}"/></option>
                                            </c:forEach>
                                        </select>
                                        <label class="flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-bold text-slate-700">
                                            <input type="checkbox" name="requiredSkill" value="${jrs.index}" ${jr.required ? 'checked' : ''} />
                                            ${language == 'zh' ? '必需' : 'Required'}
                                        </label>
                                    </div>
                                </c:forEach>
                                <c:set var="baseRequirementIndex" value="${fn:length(jobRequirements)}" />
                                <c:forEach begin="0" end="2" var="extra">
                                    <div class="grid grid-cols-1 gap-2 md:grid-cols-[1.2fr_1fr_0.6fr]">
                                        <select name="skillId" class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-700">
                                            <option value="">${language == 'zh' ? '添加技能要求' : 'Add requirement'}</option>
                                            <c:forEach items="${skills}" var="skill">
                                                <c:if test="${skill.active}">
                                                    <option value="${skill.id}"><c:out value="${skill.name}"/></option>
                                                </c:if>
                                            </c:forEach>
                                        </select>
                                        <select name="minProficiency" class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-700">
                                            <c:forEach items="${proficiencyLevels}" var="level">
                                                <option value="${level}"><c:out value="${level}"/></option>
                                            </c:forEach>
                                        </select>
                                        <label class="flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-bold text-slate-700">
                                            <input type="checkbox" name="requiredSkill" value="${baseRequirementIndex + extra}" />
                                            ${language == 'zh' ? '必需' : 'Required'}
                                        </label>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '状态' : 'Status'}</label>
                            <select name="status" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none">
                                <option value="OPEN" ${editStatus == 'OPEN' ? 'selected' : ''}>${language == 'zh' ? '开放申请' : 'Open'}</option>
                                <option value="CLOSED" ${editStatus == 'CLOSED' ? 'selected' : ''}>${language == 'zh' ? '已关闭' : 'Closed'}</option>
                                <option value="DRAFT" ${editStatus == 'DRAFT' ? 'selected' : ''}>${language == 'zh' ? '草稿' : 'Draft'}</option>
                                <option value="CANCELLED" ${editStatus == 'CANCELLED' ? 'selected' : ''}>${language == 'zh' ? '已取消' : 'Cancelled'}</option>
                            </select>
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-slate-700 mb-1">${language == 'zh' ? '保存后跳转到' : 'After save, go to'}</label>
                            <select name="returnTo" class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none">
                                <option value="detail" ${editReturnTo != 'list' ? 'selected' : ''}>${language == 'zh' ? '岗位详情' : 'Vacancy detail'}</option>
                                <option value="list" ${editReturnTo == 'list' ? 'selected' : ''}>${language == 'zh' ? '岗位列表' : 'Vacancy list'}</option>
                            </select>
                        </div>

                        <div class="flex flex-wrap gap-3 pt-4 border-t border-slate-100">
                            <button type="submit" class="portal-btn portal-btn-primary">
                                <span class="material-symbols-outlined text-sm">save</span>
                                ${language == 'zh' ? '保存更改' : 'Save Changes'}
                            </button>
                            <c:choose>
                                <c:when test="${editReturnTo == 'list'}">
                                    <a href="${pageContext.request.contextPath}/vacancies" class="portal-btn portal-btn-secondary">${language == 'zh' ? '取消' : 'Cancel'}</a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/vacancy?vacancyId=${fn:escapeXml(editVacancyId)}" class="portal-btn portal-btn-secondary">${language == 'zh' ? '取消' : 'Cancel'}</a>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </form>
                </div>
            </div>
        </main>
    </div>
</div>
</body>
</html>
