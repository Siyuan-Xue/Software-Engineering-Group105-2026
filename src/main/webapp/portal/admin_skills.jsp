<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${language == 'zh' ? '技能管理 - QM HIRE' : 'Skill Management - QM HIRE'}</title>
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
    <style>
        .skills-label {
            display: block;
            margin-bottom: 0.375rem;
            color: #64748b;
            font-size: 11px;
            font-weight: 800;
            letter-spacing: 0.14em;
            text-transform: uppercase;
        }
        .skills-input {
            width: 100%;
            border-radius: 0.875rem;
            border: 1px solid #e2e8f0;
            background: #fff;
            color: #0f172a;
            font-size: 0.875rem;
            outline: none;
            padding: 0.75rem 0.875rem;
        }
        .skills-input:focus {
            border-color: #94a3b8;
            box-shadow: 0 0 0 3px rgba(148, 163, 184, 0.16);
        }
        textarea.skills-input {
            min-height: 7rem;
            resize: vertical;
        }
    </style>
</head>
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
<div class="relative flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="portal-page">
                <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                    <jsp:param name="containerClass" value="mb-6" />
                </jsp:include>

                <c:set var="skillsState" value="${empty pageState ? 'normal' : pageState}" />
                <c:choose>
                    <c:when test="${skillsState == 'loadError'}">
                        <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                            <jsp:param name="variant" value="error" />
                            <jsp:param name="icon" value="category" />
                            <jsp:param name="title" value="${language == 'zh' ? '技能列表暂不可用' : 'Skills unavailable'}" />
                            <jsp:param name="message" value="${language == 'zh' ? '当前无法加载技能目录，请刷新页面后重试。' : 'Unable to load the skill catalogue right now. Please refresh and try again.'}" />
                            <jsp:param name="actionHref" value="${pageContext.request.contextPath}/admin/skills" />
                            <jsp:param name="actionLabel" value="${language == 'zh' ? '重试' : 'Try Again'}" />
                        </jsp:include>
                    </c:when>
                    <c:otherwise>

                <div class="portal-page-header mb-6">
                    <div>
                        <h2 class="portal-page-title">${language == 'zh' ? '技能管理' : 'Skill Management'}</h2>
                        <p class="portal-page-copy">${language == 'zh' ? '维护 TA 简历和 MO 岗位要求可共用的技能目录。' : 'Maintain the shared skill catalogue used by TA resumes and MO vacancy requirements.'}</p>
                    </div>
                    <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-bold text-slate-700 shadow-sm">
                        <span class="text-slate-400">${language == 'zh' ? '技能总数' : 'Total skills'}</span>
                        <span class="ml-2 text-slate-950"><c:out value="${fn:length(skills)}"/></span>
                    </div>
                </div>

                <div class="grid grid-cols-1 gap-6 lg:grid-cols-[0.72fr_1.28fr]">
                    <section class="portal-panel p-6">
                        <div class="mb-5 flex items-center gap-3">
                            <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-100 text-blue-700">
                                <span class="material-symbols-outlined">add_circle</span>
                            </div>
                            <div>
                                <h3 class="font-bold text-slate-900">${language == 'zh' ? '新增技能' : 'Create Skill'}</h3>
                                <p class="text-xs text-slate-500">${language == 'zh' ? '名称不区分大小写且不能重复。' : 'Names are case-insensitive and must be unique.'}</p>
                            </div>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/skills" method="POST" class="space-y-4">
                            <input type="hidden" name="action" value="create" />
                            <div>
                                <label class="skills-label">${language == 'zh' ? '技能名称' : 'Skill Name'} <span class="text-red-500">*</span></label>
                                <input class="skills-input" type="text" name="name" maxlength="80" required
                                       placeholder="${language == 'zh' ? '例如：React' : 'e.g. React'}" />
                            </div>
                            <div>
                                <label class="skills-label">${language == 'zh' ? '类别' : 'Category'} <span class="text-red-500">*</span></label>
                                <select class="skills-input" name="category" required>
                                    <c:forEach items="${skillCategories}" var="category">
                                        <option value="${category}"><c:out value="${category}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label class="skills-label">${language == 'zh' ? '描述' : 'Description'}</label>
                                <textarea class="skills-input" name="description" maxlength="280"
                                          placeholder="${language == 'zh' ? '简要说明该技能适用的教学或岗位场景。' : 'Briefly describe the teaching or vacancy context for this skill.'}"></textarea>
                            </div>
                            <button type="submit" class="portal-btn portal-btn-primary w-full">
                                <span class="material-symbols-outlined text-[18px]">save</span>
                                ${language == 'zh' ? '创建技能' : 'Create Skill'}
                            </button>
                        </form>
                    </section>

                    <section class="portal-panel overflow-hidden">
                        <div class="flex items-center justify-between border-b border-slate-100 px-6 py-5">
                            <div>
                                <h3 class="font-bold text-slate-900">${language == 'zh' ? '技能目录' : 'Skill Catalogue'}</h3>
                                <p class="mt-1 text-xs text-slate-500">${language == 'zh' ? '可编辑技能；已被简历或岗位引用的技能不能直接删除。' : 'Edit catalogue entries; skills used by resumes or vacancy requirements are protected from deletion.'}</p>
                            </div>
                            <span class="material-symbols-outlined text-slate-400">psychology</span>
                        </div>

                        <c:choose>
                            <c:when test="${empty skills}">
                                <div class="p-8">
                                    <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                        <jsp:param name="icon" value="psychology" />
                                        <jsp:param name="title" value="${language == 'zh' ? '暂无技能' : 'No skills yet'}" />
                                        <jsp:param name="message" value="${language == 'zh' ? '创建第一个技能后，TA 和 MO 就可以在后续数据中引用它。' : 'Create the first skill so TAs and MOs can reference it in later records.'}" />
                                    </jsp:include>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="overflow-x-auto">
                                    <table class="w-full text-left text-sm">
                                        <thead class="border-b border-slate-100 bg-slate-50 text-xs uppercase tracking-wider text-slate-500">
                                            <tr>
                                                <th class="px-6 py-4 font-bold">${language == 'zh' ? '技能' : 'Skill'}</th>
                                                <th class="px-6 py-4 font-bold">${language == 'zh' ? '类别' : 'Category'}</th>
                                                <th class="px-6 py-4 font-bold">${language == 'zh' ? '引用' : 'Usage'}</th>
                                                <th class="px-6 py-4 text-right font-bold">${language == 'zh' ? '操作' : 'Actions'}</th>
                                            </tr>
                                        </thead>
                                        <tbody class="divide-y divide-slate-100">
                                            <c:forEach items="${skills}" var="skill">
                                                <tr class="transition-colors hover:bg-slate-50">
                                                    <td class="px-6 py-4">
                                                        <p class="font-semibold text-slate-900"><c:out value="${skill.name}"/></p>
                                                        <p class="mt-1">
                                                            <span class="inline-flex rounded-full px-2.5 py-0.5 text-[11px] font-bold ${skill.active ? 'bg-green-100 text-green-700' : 'bg-slate-200 text-slate-700'}">
                                                                ${skill.active ? (language == 'zh' ? '启用' : 'Active') : (language == 'zh' ? '停用' : 'Inactive')}
                                                            </span>
                                                        </p>
                                                        <p class="mt-1 max-w-xl text-xs leading-5 text-slate-500">
                                                            <c:choose>
                                                                <c:when test="${not empty skill.description}"><c:out value="${skill.description}"/></c:when>
                                                                <c:otherwise>${language == 'zh' ? '暂无描述' : 'No description'}</c:otherwise>
                                                            </c:choose>
                                                        </p>
                                                    </td>
                                                    <td class="px-6 py-4">
                                                        <span class="inline-flex rounded-full bg-slate-100 px-3 py-1 text-xs font-bold text-slate-700">
                                                            <c:out value="${skill.category}"/>
                                                        </span>
                                                    </td>
                                                    <td class="px-6 py-4 text-slate-600">
                                                        <div class="flex flex-col gap-1 text-xs">
                                                            <span>${language == 'zh' ? '简历' : 'Resumes'}: <c:out value="${skill.resumeUseCount}"/></span>
                                                            <span>${language == 'zh' ? '岗位要求' : 'Requirements'}: <c:out value="${skill.requirementUseCount}"/></span>
                                                        </div>
                                                    </td>
                                                    <td class="px-6 py-4 text-right">
                                                        <div class="flex flex-col items-end gap-2">
                                                            <details class="w-full max-w-md text-left">
                                                                <summary class="ml-auto inline-flex min-h-10 cursor-pointer items-center justify-center gap-2 rounded-xl border border-slate-200 px-3 py-2 text-xs font-bold text-slate-700 transition-colors hover:bg-slate-50">
                                                                    <span class="material-symbols-outlined text-[16px]">edit</span>
                                                                    ${language == 'zh' ? '编辑' : 'Edit'}
                                                                </summary>
                                                                <form action="${pageContext.request.contextPath}/admin/skills" method="POST" class="mt-3 rounded-xl border border-slate-200 bg-white p-3 text-left shadow-sm">
                                                                    <input type="hidden" name="action" value="update" />
                                                                    <input type="hidden" name="skillId" value="${skill.id}" />
                                                                    <label class="skills-label">${language == 'zh' ? '技能名称' : 'Skill Name'}</label>
                                                                    <input class="skills-input mb-3" type="text" name="name" maxlength="80" value="${fn:escapeXml(skill.name)}" required />
                                                                    <label class="skills-label">${language == 'zh' ? '类别' : 'Category'}</label>
                                                                    <select class="skills-input mb-3" name="category" required>
                                                                        <c:forEach items="${skillCategories}" var="category">
                                                                            <option value="${category}" ${skill.category == category ? 'selected' : ''}><c:out value="${category}"/></option>
                                                                        </c:forEach>
                                                                    </select>
                                                                    <label class="skills-label">${language == 'zh' ? '描述' : 'Description'}</label>
                                                                    <textarea class="skills-input mb-3" name="description" maxlength="280"><c:out value="${skill.description}"/></textarea>
                                                                    <button type="submit" class="portal-btn portal-btn-primary text-xs">
                                                                        <span class="material-symbols-outlined text-[16px]">save</span>
                                                                        ${language == 'zh' ? '保存' : 'Save'}
                                                                    </button>
                                                                </form>
                                                            </details>
                                                            <form action="${pageContext.request.contextPath}/admin/skills" method="POST">
                                                                <input type="hidden" name="action" value="${skill.active ? 'deactivate' : 'activate'}" />
                                                                <input type="hidden" name="skillId" value="${skill.id}" />
                                                                <button type="submit" class="inline-flex min-h-10 items-center justify-center gap-2 rounded-xl border px-3 py-2 text-xs font-bold ${skill.active ? 'border-amber-200 text-amber-700 hover:bg-amber-50' : 'border-green-200 text-green-700 hover:bg-green-50'}">
                                                                    <span class="material-symbols-outlined text-[16px]">${skill.active ? 'visibility_off' : 'visibility'}</span>
                                                                    ${skill.active ? (language == 'zh' ? '停用' : 'Deactivate') : (language == 'zh' ? '启用' : 'Activate')}
                                                                </button>
                                                            </form>
                                                            <form action="${pageContext.request.contextPath}/admin/skills" method="POST"
                                                                  onsubmit="return confirm('${language == 'zh' ? '确定删除该技能？若仍被引用，系统会阻止删除。' : 'Delete this skill? The system will block deletion if it is still referenced.'}');">
                                                                <input type="hidden" name="action" value="delete" />
                                                                <input type="hidden" name="skillId" value="${skill.id}" />
                                                                <button type="submit" class="inline-flex min-h-10 items-center justify-center gap-2 rounded-xl border border-red-200 px-3 py-2 text-xs font-bold text-red-600 transition-colors hover:bg-red-50">
                                                                    <span class="material-symbols-outlined text-[16px]">delete</span>
                                                                    ${language == 'zh' ? '删除' : 'Delete'}
                                                                </button>
                                                            </form>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>
</body>
</html>
