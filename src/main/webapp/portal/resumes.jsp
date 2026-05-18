<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${language == 'zh' ? '简历 - QM HIRE' : 'Resumes - QM HIRE'}</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&display=swap" rel="stylesheet"/>
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
    <style>
        /* ── upload zone ──────────────────────────────────────────────── */
        .upload-zone { transition: all 0.25s ease; border: 2px dashed #e2e8f0; border-radius: 1.25rem; }
        .upload-zone:hover { border-color: #94a3b8; background: #f8fafc; }
        .upload-zone.dragover { background: #eff6ff; border-color: #3b82f6; transform: scale(1.01); }

        /* ── modals – use display:flex/none toggled via JS, NOT Tailwind hidden ── */
        .qm-modal-overlay {
            display: none;          /* controlled by JS only */
            position: fixed; inset: 0;
            background: rgba(15,23,42,0.55);
            align-items: center; justify-content: center;
            z-index: 9999; padding: 1rem;
        }
        .qm-modal-overlay.open { display: flex; }
        .qm-modal-box {
            background: #fff; border-radius: 1.5rem;
            box-shadow: 0 32px 64px -24px rgba(15,23,42,0.4);
            width: 100%; overflow: hidden;
            animation: modalPop .28s cubic-bezier(.34,1.4,.64,1) both;
        }
        @keyframes modalPop {
            from { transform: scale(0.92) translateY(16px); opacity: 0; }
            to   { transform: scale(1)    translateY(0);    opacity: 1; }
        }

        /* ── AI result rendering ──────────────────────────────────────── */
        .ai-prose h2 { font-size:1rem; font-weight:700; color:#1e293b; margin:1.1rem 0 .35rem; padding-bottom:.25rem; border-bottom:1px solid #f1f5f9; }
        .ai-prose strong { font-weight:600; color:#1e293b; }
        .ai-prose ul  { list-style:disc; padding-left:1.4rem; margin:.4rem 0 .6rem; }
        .ai-prose li  { margin:.3rem 0; color:#475569; line-height:1.6; }
        .ai-prose p   { color:#475569; line-height:1.65; margin:.4rem 0; }

        /* ── skeleton ─────────────────────────────────────────────────── */
        .skeleton {
            background: linear-gradient(90deg,#f1f5f9 25%,#e2e8f0 50%,#f1f5f9 75%);
            background-size: 200% 100%;
            animation: shimmer 1.4s infinite;
            border-radius: 6px;
        }
        @keyframes shimmer { from{background-position:200% 0} to{background-position:-200% 0} }

        /* ── resume card ──────────────────────────────────────────────── */
        .resume-card { transition: box-shadow .2s, transform .2s; }
        .resume-card:hover { box-shadow: 0 8px 30px -12px rgba(15,23,42,.18); transform: translateY(-1px); }

        /* file-type icon colours */
        .icon-pdf  { background:#fee2e2; color:#dc2626; }
        .icon-doc  { background:#dbeafe; color:#2563eb; }
        .icon-img  { background:#d1fae5; color:#059669; }
        .icon-txt  { background:#fef9c3; color:#ca8a04; }
        .icon-def  { background:#f1f5f9; color:#64748b; }

        /* card action buttons always visible */
        .card-action-btn {
            display: inline-flex; align-items: center; justify-content: center;
            width: 34px; height: 34px; border-radius: 10px;
            transition: background .15s, color .15s;
        }
        .card-action-edit  { color:#3b82f6; background:#eff6ff; }
        .card-action-edit:hover  { background:#dbeafe; }
        .card-action-del   { color:#ef4444; background:#fff1f2; }
        .card-action-del:hover   { background:#fee2e2; }
    </style>
</head>
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900">
<div class="flex min-h-screen w-full flex-col">
    <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

    <div class="flex flex-1 overflow-hidden">
        <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

        <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
            <div class="portal-page max-w-6xl">

                <%-- flash messages --%>
                <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                    <jsp:param name="containerClass" value="mb-4" />
                </jsp:include>

                <c:if test="${pageState == 'uploadSuccess'}">
                    <div class="flex items-center gap-3 rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-4 mb-2">
                        <span class="material-symbols-outlined text-emerald-500">check_circle</span>
                        <p class="text-sm font-semibold text-emerald-800">${language == 'zh' ? '简历上传成功，请在下方检查并更新信息。' : 'Resume uploaded. Review and update the details below.'}</p>
                    </div>
                </c:if>
                <c:if test="${pageState == 'uploadFailure'}">
                    <div class="flex items-center gap-3 rounded-2xl border border-red-200 bg-red-50 px-5 py-4 mb-2">
                        <span class="material-symbols-outlined text-red-500">error</span>
                        <p class="text-sm font-semibold text-red-800">${language == 'zh' ? '上传失败。' : 'Upload failed.'}<c:if test="${not empty errorMessage}"> <c:out value="${errorMessage}"/></c:if></p>
                    </div>
                </c:if>

                <%-- page header --%>
                <div class="portal-page-header mb-4">
                    <div>
                        <h1 class="portal-page-title">${language == 'zh' ? '我的简历' : 'My Resumes'}</h1>
                        <p class="portal-page-copy">${language == 'zh' ? '上传简历、获取 AI 优化建议，并跟踪你的助教申请。' : 'Upload your CV, get AI coaching, and track your TA applications.'}</p>
                    </div>
                </div>

                <%-- hidden upload form + file input --%>
                <form id="uploadForm" action="${pageContext.request.contextPath}/resumes"
                      method="POST" enctype="multipart/form-data" class="hidden">
                    <input type="hidden" name="action" value="upload"/>
                </form>
                <input type="file" id="resumeFileInput" name="resumeFile" form="uploadForm"
                       accept=".pdf,.doc,.docx,.jpg,.jpeg,.png,.txt"
                       class="sr-only"/>
                <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3 mb-3">
                    <label for="uploadLabelsInput" class="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-1">${language == 'zh' ? '简历标签（可选）' : 'Resume labels (optional)'}</label>
                    <input type="text" id="uploadLabelsInput"
                           class="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:ring-2 focus:ring-blue-200 outline-none"
                           placeholder="${language == 'zh' ? '如：Java, 助教, 本科 — 用逗号分隔' : 'e.g. Java, TA, undergraduate — comma separated'}"/>
                </div>

                <c:choose>
                    <c:when test="${pageState == 'loadError'}">
                        <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                            <jsp:param name="variant" value="error"/>
                            <jsp:param name="icon" value="description"/>
                            <jsp:param name="title" value="${language == 'zh' ? '简历暂不可用' : 'Resumes unavailable'}"/>
                            <jsp:param name="message" value="${language == 'zh' ? '当前无法加载你的简历，请刷新页面后重试。' : 'Unable to load your resumes. Please refresh and try again.'}"/>
                            <jsp:param name="actionHref" value="${pageContext.request.contextPath}/resumes"/>
                            <jsp:param name="actionLabel" value="${language == 'zh' ? '重试' : 'Try Again'}"/>
                        </jsp:include>
                    </c:when>
                    <c:otherwise>
                        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">

                            <%-- ── LEFT: resume list + upload zone ─────────────── --%>
                            <div class="lg:col-span-2 space-y-4">

                                <c:choose>
                                    <c:when test="${empty resumes}">
                                        <div class="flex flex-col items-center justify-center rounded-3xl border-2 border-dashed border-slate-200 bg-white/60 py-20 text-center">
                                            <div class="w-20 h-20 rounded-full bg-slate-100 flex items-center justify-center mb-5">
                                                <span class="material-symbols-outlined text-4xl text-slate-300">description</span>
                                            </div>
                                            <p class="text-lg font-bold text-slate-700 mb-1">${language == 'zh' ? '还没有简历' : 'No resumes yet'}</p>
                                            <p class="text-sm text-slate-400 mb-6 max-w-xs leading-relaxed">${language == 'zh' ? '上传第一份简历后即可开始匹配助教岗位。' : 'Upload your first resume to start matching with TA positions.'}</p>
                                            <label for="resumeFileInput" class="portal-btn portal-btn-primary cursor-pointer">
                                                <span class="material-symbols-outlined text-sm">upload_file</span>
                                                ${language == 'zh' ? '上传第一份简历' : 'Upload Your First Resume'}
                                            </label>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach items="${resumes}" var="r">
                                            <%-- determine file type for icon --%>
                                            <c:set var="iconCls" value="icon-def"/>
                                            <c:set var="iconName" value="description"/>
                                            <c:if test="${not empty r.originalFileName}">
                                                <c:choose>
                                                    <c:when test="${fn:containsIgnoreCase(r.originalFileName,'.pdf')}">
                                                        <c:set var="iconCls" value="icon-pdf"/>
                                                        <c:set var="iconName" value="picture_as_pdf"/>
                                                    </c:when>
                                                    <c:when test="${fn:containsIgnoreCase(r.originalFileName,'.jpg') or fn:containsIgnoreCase(r.originalFileName,'.jpeg') or fn:containsIgnoreCase(r.originalFileName,'.png')}">
                                                        <c:set var="iconCls" value="icon-img"/>
                                                        <c:set var="iconName" value="image"/>
                                                    </c:when>
                                                    <c:when test="${fn:containsIgnoreCase(r.originalFileName,'.doc')}">
                                                        <c:set var="iconCls" value="icon-doc"/>
                                                        <c:set var="iconName" value="article"/>
                                                    </c:when>
                                                    <c:when test="${fn:containsIgnoreCase(r.originalFileName,'.txt')}">
                                                        <c:set var="iconCls" value="icon-txt"/>
                                                        <c:set var="iconName" value="text_snippet"/>
                                                    </c:when>
                                                </c:choose>
                                            </c:if>

                                            <div class="portal-panel resume-card">
                                                <%-- card body --%>
                                                <div class="p-5 flex items-start gap-4">
                                                    <%-- file-type icon --%>
                                                    <div class="w-13 h-13 shrink-0 rounded-2xl ${iconCls} flex items-center justify-center"
                                                         style="width:52px;height:52px">
                                                        <span class="material-symbols-outlined text-2xl"
                                                              style="font-variation-settings:'FILL' 1"><c:out value="${iconName}"/></span>
                                                    </div>

                                                    <div class="flex-1 min-w-0">
                                                        <div class="flex items-start justify-between gap-3">
                                                            <div class="min-w-0">
                                                                <h3 class="text-base font-bold text-slate-900 leading-tight truncate">
                                                                    <c:out value="${r.title}"/>
                                                                </h3>
                                                                <%-- badges --%>
                                                                <div class="flex flex-wrap items-center gap-2 mt-1.5">
                                                                    <c:if test="${not empty r.degreeLevel}">
                                                                        <span class="text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-violet-100 text-violet-700">
                                                                            <c:out value="${r.degreeLevel}"/>
                                                                        </span>
                                                                    </c:if>
                                                                    <c:if test="${not empty r.department}">
                                                                        <span class="text-[11px] font-semibold text-slate-500">
                                                                            <c:out value="${r.department}"/>
                                                                        </span>
                                                                    </c:if>
                                                                    <c:if test="${not empty r.gpa}">
                                                                        <span class="text-[11px] font-semibold text-slate-400">
                                                                            GPA <strong class="text-slate-600"><c:out value="${r.gpa}"/></strong>
                                                                        </span>
                                                                    </c:if>
                                                                    <c:forEach items="${r.labels}" var="rlb">
                                                                        <span class="text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-sky-100 text-sky-800"><c:out value="${rlb}"/></span>
                                                                    </c:forEach>
                                                                </div>
                                                            </div>
                                                            <%-- action buttons – always visible --%>
                                                            <div class="flex gap-1.5 shrink-0">
                                                                <button type="button"
                                                                        class="card-action-btn card-action-edit"
                                                                        title="${language == 'zh' ? '编辑简历' : 'Edit resume'}"
                                                                        data-resume-id="${r.id}"
                                                                        data-title="${fn:escapeXml(r.title)}"
                                                                        data-dept="${fn:escapeXml(r.department)}"
                                                                        data-degree="${r.degreeLevel}"
                                                                        data-gpa="${r.gpa}"
                                                                        data-hours="${r.maxWeeklyHours}"
                                                                        data-bio="${fn:escapeXml(r.bio)}"
                                                                        data-resume-labels='<c:forEach items="${r.labels}" var="lb" varStatus="vs"><c:if test="${!vs.first}">|</c:if><c:out value="${lb}"/></c:forEach>'
                                                                        data-availability-json='${fn:escapeXml(availabilityJsonByResumeId[r.id])}'
                                                                        onclick="openEditModalFromBtn(this)">
                                                                    <span class="material-symbols-outlined text-[18px]">edit</span>
                                                                </button>
                                                                <button type="button"
                                                                        class="card-action-btn card-action-del"
                                                                        title="${language == 'zh' ? '删除简历' : 'Delete resume'}"
                                                                        onclick="deleteResume('${r.id}')">
                                                                    <span class="material-symbols-outlined text-[18px]">delete</span>
                                                                </button>
                                                                <button type="button"
                                                                        class="card-action-btn bg-slate-100 text-slate-600 hover:bg-slate-200"
                                                                        title="${language == 'zh' ? '复制简历' : 'Duplicate resume'}"
                                                                        onclick="duplicateResume('${r.id}')">
                                                                    <span class="material-symbols-outlined text-[18px]">content_copy</span>
                                                                </button>
                                                            </div>
                                                        </div>

                                                        <c:if test="${not empty r.bio}">
                                                            <p class="text-sm text-slate-500 mt-2.5 line-clamp-2 leading-relaxed">
                                                                <c:out value="${r.bio}"/>
                                                            </p>
                                                        </c:if>

                                                        <c:if test="${not empty r.originalFileName}">
                                                            <div class="mt-2 flex items-center gap-1 text-[11px] text-slate-400">
                                                                <span class="material-symbols-outlined text-[13px]">attach_file</span>
                                                                <span><c:out value="${r.originalFileName}"/></span>
                                                            </div>
                                                        </c:if>
                                                        <c:set var="resumeSkillsForCard" value="${resumeSkillViewsByResumeId[r.id]}" />
                                                        <div class="mt-3">
                                                            <c:choose>
                                                                <c:when test="${not empty resumeSkillsForCard}">
                                                                    <div class="flex flex-wrap gap-2">
                                                                        <c:forEach items="${resumeSkillsForCard}" var="rs">
                                                                            <span class="rounded-full bg-slate-100 px-3 py-1 text-[11px] font-bold text-slate-700">
                                                                                <c:out value="${rs.name}"/> · <c:out value="${rs.proficiency}"/> · <c:out value="${rs.yearsExp}"/>y
                                                                            </span>
                                                                        </c:forEach>
                                                                    </div>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <p class="text-xs text-slate-400">${language == 'zh' ? '尚未绑定技能' : 'No skills linked yet'}</p>
                                                                </c:otherwise>
                                                            </c:choose>
                                                            <details class="mt-3 rounded-xl border border-slate-100 bg-slate-50 p-3">
                                                                <summary class="cursor-pointer text-xs font-bold text-primary">
                                                                    ${language == 'zh' ? '管理简历技能' : 'Manage resume skills'}
                                                                </summary>
                                                                <form action="${pageContext.request.contextPath}/resumes" method="POST" class="mt-3 space-y-3">
                                                                    <input type="hidden" name="action" value="skills" />
                                                                    <input type="hidden" name="resumeId" value="${r.id}" />
                                                                    <c:forEach items="${resumeSkillsForCard}" var="rs">
                                                                        <div class="grid grid-cols-1 gap-2 md:grid-cols-[1.2fr_1fr_0.6fr]">
                                                                            <select name="skillId" class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-700">
                                                                                <option value="">${language == 'zh' ? '选择技能' : 'Select skill'}</option>
                                                                                <c:forEach items="${skills}" var="skill">
                                                                                    <c:if test="${skill.active or skill.id == rs.skillId}">
                                                                                        <option value="${skill.id}" ${skill.id == rs.skillId ? 'selected' : ''}><c:out value="${skill.name}"/></option>
                                                                                    </c:if>
                                                                                </c:forEach>
                                                                            </select>
                                                                            <select name="proficiency" class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-700">
                                                                                <c:forEach items="${proficiencyLevels}" var="level">
                                                                                    <option value="${level}" ${rs.proficiency == level ? 'selected' : ''}><c:out value="${level}"/></option>
                                                                                </c:forEach>
                                                                            </select>
                                                                            <input name="yearsExp" type="number" min="0" max="40" value="${rs.yearsExp}" class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-700" />
                                                                        </div>
                                                                    </c:forEach>
                                                                    <c:forEach begin="1" end="3">
                                                                        <div class="grid grid-cols-1 gap-2 md:grid-cols-[1.2fr_1fr_0.6fr]">
                                                                            <select name="skillId" class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-700">
                                                                                <option value="">${language == 'zh' ? '添加技能' : 'Add skill'}</option>
                                                                                <c:forEach items="${skills}" var="skill">
                                                                                    <c:if test="${skill.active}">
                                                                                        <option value="${skill.id}"><c:out value="${skill.name}"/></option>
                                                                                    </c:if>
                                                                                </c:forEach>
                                                                            </select>
                                                                            <select name="proficiency" class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-700">
                                                                                <c:forEach items="${proficiencyLevels}" var="level">
                                                                                    <option value="${level}"><c:out value="${level}"/></option>
                                                                                </c:forEach>
                                                                            </select>
                                                                            <input name="yearsExp" type="number" min="0" max="40" value="0" class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-medium text-slate-700" />
                                                                        </div>
                                                                    </c:forEach>
                                                                    <button type="submit" class="portal-btn portal-btn-primary text-xs">
                                                                        <span class="material-symbols-outlined text-sm">save</span>
                                                                        ${language == 'zh' ? '保存技能' : 'Save skills'}
                                                                    </button>
                                                                </form>
                                                            </details>
                                                        </div>
                                                    </div>
                                                </div>

                                                <%-- card footer --%>
                                                <div class="border-t border-slate-100 px-5 py-3 flex items-center justify-between bg-slate-50/60 rounded-b-2xl">
                                                    <div class="flex items-center gap-4 text-xs text-slate-400">
                                                        <span class="flex items-center gap-1">
                                                            <span class="material-symbols-outlined text-[13px]">schedule</span>
                                                            ${language == 'zh' ? '最多 ' : 'Max '}<strong class="text-slate-600"><c:out value="${r.maxWeeklyHours}"/></strong> ${language == 'zh' ? '小时/周' : 'hrs/wk'}
                                                        </span>
                                                        <span class="flex items-center gap-1">
                                                            <span class="material-symbols-outlined text-[13px]">event_available</span>
                                                            <strong class="text-slate-600"><c:out value="${fn:length(r.availabilitySlots)}"/></strong> ${language == 'zh' ? '个可用时段' : 'availability slots'}
                                                        </span>
                                                        <span>${language == 'zh' ? '更新于：' : 'Updated: '}<c:out value="${r.updatedAtDisplay}"/></span>
                                                    </div>
                                                    <%-- card AI button: pass this specific resume's ID --%>
                                                    <button type="button"
                                                            data-resume-id="${r.id}"
                                                            data-resume-title="${fn:escapeXml(r.title)}"
                                                            onclick="openDisclaimerModal(this.dataset.resumeId, this.dataset.resumeTitle)"
                                                            class="inline-flex items-center gap-1.5 text-xs font-bold text-violet-600 hover:text-violet-800 transition-colors">
                                                        <span class="material-symbols-outlined text-[14px]" style="font-variation-settings:'FILL' 1">auto_awesome</span>
                                                        ${language == 'zh' ? 'AI 分析' : 'AI Review'}
                                                    </button>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>

                                <%-- drag-and-drop upload zone --%>
                                <div id="dropZone" class="upload-zone cursor-pointer group p-10 text-center"
                                     onclick="document.getElementById('resumeFileInput').click()">
                                    <div class="w-16 h-16 rounded-full bg-blue-50 flex items-center justify-center text-blue-500 mx-auto mb-4 group-hover:scale-110 transition-transform">
                                        <span class="material-symbols-outlined text-3xl">cloud_upload</span>
                                    </div>
                                    <p class="font-bold text-slate-800 mb-1">${language == 'zh' ? '拖拽文件到此处或点击上传' : 'Drag & drop or click to upload'}</p>
                                    <p class="text-sm text-slate-400 mb-4">PDF · DOCX · JPG · PNG · TXT &nbsp;·&nbsp; ${language == 'zh' ? '最大 15 MB' : 'Max 15 MB'}</p>
                                    <label for="resumeFileInput"
                                           class="portal-btn portal-btn-primary text-sm pointer-events-none inline-flex cursor-pointer">
                                        <span class="material-symbols-outlined text-sm">upload_file</span>
                                        ${language == 'zh' ? '上传简历' : 'Upload Resume'}
                                    </label>
                                </div>
                            </div>

                            <%-- ── RIGHT: sidebar ───────────────────────────────── --%>
                            <div class="space-y-5">

                                <%-- AI review card --%>
                                <div class="portal-panel portal-panel--accent p-6 relative overflow-hidden">
                                    <div class="absolute -right-8 -top-8 w-32 h-32 bg-violet-400/10 rounded-full blur-3xl pointer-events-none"></div>
                                    <div class="relative flex items-center gap-2 mb-3">
                                        <span class="material-symbols-outlined text-violet-600"
                                              style="font-variation-settings:'FILL' 1">auto_awesome</span>
                                        <h3 class="font-bold text-slate-900">${language == 'zh' ? 'AI 简历分析' : 'AI Resume Review'}</h3>
                                        <c:if test="${not qwenConfigured}">
                                            <span class="ml-auto text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-100 text-amber-700 whitespace-nowrap">${language == 'zh' ? '缺少 KEY' : 'KEY REQUIRED'}</span>
                                        </c:if>
                                    </div>
                                    <p class="text-sm text-slate-500 mb-5 leading-relaxed relative">
                                        ${language == 'zh' ? '获取针对助教岗位的个性化简历优化建议。上传 ' : 'Get personalised coaching on how to improve your resume for TA roles. Upload a '}<strong class="text-slate-700">${language == 'zh' ? 'PDF 或图片' : 'PDF or image'}</strong>${language == 'zh' ? ' 可获得更好的多模态分析效果。' : ' for the best multimodal analysis.'}
                                    </p>
                                    <%-- sidebar: no resumeId = analyse latest uploaded file --%>
                                    <button type="button" id="aiReviewSidebarBtn"
                                            onclick="openDisclaimerModal(null, '${language == 'zh' ? '最近上传的文件' : 'Latest Uploaded File'}')"
                                            class="portal-btn portal-btn-primary w-full justify-center relative">
                                        <span class="material-symbols-outlined text-sm">auto_awesome</span>
                                        ${language == 'zh' ? '分析并优化我的简历' : 'Analyse & Improve My Resume'}
                                    </button>
                                </div>

                                <%-- tips card --%>
                                <div class="portal-panel p-5">
                                    <h3 class="font-bold text-slate-900 mb-4 flex items-center gap-2 text-sm">
                                        <span class="material-symbols-outlined text-amber-500"
                                              style="font-variation-settings:'FILL' 1">lightbulb</span>
                                        ${language == 'zh' ? '助教简历建议' : 'Resume Tips for TAs'}
                                    </h3>
                                    <ul class="space-y-3.5">
                                        <li class="flex gap-3">
                                            <span class="material-symbols-outlined text-emerald-500 shrink-0 mt-0.5 text-lg"
                                                  style="font-variation-settings:'FILL' 1">check_circle</span>
                                            <div>
                                                <p class="text-sm font-semibold text-slate-800">${language == 'zh' ? '突出教学经历' : 'Highlight Teaching Experience'}</p>
                                                <p class="text-xs text-slate-500 mt-0.5 leading-relaxed">${language == 'zh' ? '辅导、带教或以往 TA 经历应尽量放在前面。' : 'Tutoring, mentoring, or previous TA roles — put these at the top.'}</p>
                                            </div>
                                        </li>
                                        <li class="flex gap-3">
                                            <span class="material-symbols-outlined text-emerald-500 shrink-0 mt-0.5 text-lg"
                                                  style="font-variation-settings:'FILL' 1">check_circle</span>
                                            <div>
                                                <p class="text-sm font-semibold text-slate-800">${language == 'zh' ? '列出相关课程' : 'List Relevant Coursework'}</p>
                                                <p class="text-xs text-slate-500 mt-0.5 leading-relaxed">${language == 'zh' ? '写出与你申请岗位相关的进阶课程。' : 'Advanced courses related to the module you are applying for.'}</p>
                                            </div>
                                        </li>
                                        <li class="flex gap-3">
                                            <span class="material-symbols-outlined text-emerald-500 shrink-0 mt-0.5 text-lg"
                                                  style="font-variation-settings:'FILL' 1">check_circle</span>
                                            <div>
                                                <p class="text-sm font-semibold text-slate-800">${language == 'zh' ? '量化你的成果' : 'Quantify Achievements'}</p>
                                                <p class="text-xs text-slate-500 mt-0.5 leading-relaxed">${language == 'zh' ? '例如：“辅导 12 名学生，平均成绩提升 8%”。' : '"Tutored 12 students, improved average grade by 8%".'}</p>
                                            </div>
                                        </li>
                                        <li class="flex gap-3">
                                            <span class="material-symbols-outlined text-amber-500 shrink-0 mt-0.5 text-lg"
                                                  style="font-variation-settings:'FILL' 1">tips_and_updates</span>
                                            <div>
                                                <p class="text-sm font-semibold text-slate-800">${language == 'zh' ? '上传文件可获得更好的 AI 结果' : 'Upload a File for Best AI Results'}</p>
                                                <p class="text-xs text-slate-500 mt-0.5 leading-relaxed">${language == 'zh' ? 'PDF 或图片能让 AI 直接分析你的实际文档内容。' : 'PDF or image lets the AI analyse your actual document visually.'}</p>
                                            </div>
                                        </li>
                                    </ul>
                                </div>

                                <%-- formats card --%>
                                <div class="portal-panel p-5">
                                    <h3 class="font-semibold text-slate-800 mb-3 text-sm">${language == 'zh' ? '支持格式' : 'Supported Formats'}</h3>
                                    <div class="grid grid-cols-3 gap-1.5 text-center text-xs font-bold">
                                        <span class="rounded-xl icon-pdf py-2">PDF</span>
                                        <span class="rounded-xl icon-doc py-2">DOCX</span>
                                        <span class="rounded-xl icon-doc py-2">DOC</span>
                                        <span class="rounded-xl icon-img py-2">JPG</span>
                                        <span class="rounded-xl icon-img py-2">PNG</span>
                                        <span class="rounded-xl icon-txt py-2">TXT</span>
                                    </div>
                                    <p class="text-[11px] text-slate-400 mt-3 text-center">${language == 'zh' ? '最大文件大小：15 MB' : 'Max file size: 15 MB'}</p>
                                </div>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>

            </div>
        </main>
    </div>
</div>

<%-- ═══════════════════════════════════════════════════════
     MODAL 1 — AI Disclaimer
     Use qm-modal-overlay + JS to toggle — NOT Tailwind hidden
     ═══════════════════════════════════════════════════════ --%>

<%-- ── Rename-after-upload modal ──────────────────────────────────────────── --%>
<div id="renameModal" class="qm-modal-overlay"
     onclick="if(event.target===this) closeRenameModal()">
    <div class="qm-modal-box max-w-md">
        <div class="px-7 pt-7 pb-4">
            <div class="flex items-start gap-4 mb-5">
                <div class="w-11 h-11 rounded-2xl bg-blue-100 flex items-center justify-center shrink-0">
                    <span class="material-symbols-outlined text-2xl text-blue-600"
                          style="font-variation-settings:'FILL' 1">drive_file_rename_outline</span>
                </div>
                <div>
                    <h2 class="text-lg font-bold text-slate-800">${language == 'zh' ? '给你的简历命名' : 'Name Your Resume'}</h2>
                    <p class="text-sm text-slate-500 mt-0.5">
                        ${language == 'zh' ? '文件上传成功，请给它起一个清晰的名称。' : 'File uploaded successfully. Give it a meaningful name.'}
                    </p>
                </div>
            </div>

            <p class="text-xs text-slate-400 mb-1 font-medium uppercase tracking-wide">${language == 'zh' ? '源文件' : 'Source file'}</p>
            <p id="renameFileInfo" class="text-sm text-slate-600 bg-slate-50 rounded-lg px-3 py-2 mb-5
                                          border border-slate-200 truncate font-mono"></p>

            <label class="block text-xs text-slate-400 mb-1 font-medium uppercase tracking-wide"
                   for="renameInput">${language == 'zh' ? '简历标题' : 'Resume title'}</label>
            <input id="renameInput" type="text" placeholder="${language == 'zh' ? '未命名' : 'Untitled'}"
                   class="w-full rounded-xl border border-slate-300 px-4 py-2.5 text-sm
                          focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent
                          text-slate-800 placeholder-slate-400" />
            <p class="text-xs text-slate-400 mt-1.5">${language == 'zh' ? '留空则保持为“未命名”。' : 'Leave blank to keep "Untitled".'}</p>

            <label class="block text-xs text-slate-400 mb-1 mt-4 font-medium uppercase tracking-wide"
                   for="renameLabelsInput">${language == 'zh' ? '标签（可选）' : 'Labels (optional)'}</label>
            <input id="renameLabelsInput" type="text"
                   class="w-full rounded-xl border border-slate-300 px-4 py-2.5 text-sm
                          focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent
                          text-slate-800 placeholder-slate-400"
                   placeholder="${language == 'zh' ? '逗号分隔，如：Java, 算法' : 'Comma-separated, e.g. Java, algorithms'}"/>
        </div>

        <div class="px-7 py-5 bg-slate-50 rounded-b-2xl flex justify-end gap-3">
            <button type="button" onclick="closeRenameModal()"
                    class="portal-btn portal-btn-secondary text-sm">
                ${language == 'zh' ? '跳过' : 'Skip'}
            </button>
            <button type="button" onclick="saveRename()"
                    class="portal-btn portal-btn-primary text-sm">
                <span class="material-symbols-outlined text-sm">check</span>
                ${language == 'zh' ? '保存名称' : 'Save Name'}
            </button>
        </div>
    </div>
</div>

<div id="disclaimerModal" class="qm-modal-overlay"
     onclick="if(event.target===this) closeDisclaimerModal()">
    <div class="qm-modal-box max-w-lg">
        <div class="px-7 pt-7 pb-5">
            <div class="flex items-start gap-4 mb-5">
                <div class="w-12 h-12 rounded-2xl bg-violet-100 flex items-center justify-center shrink-0">
                    <span class="material-symbols-outlined text-2xl text-violet-600"
                          style="font-variation-settings:'FILL' 1">policy</span>
                </div>
                <div>
                    <h2 class="text-xl font-bold text-slate-900">${language == 'zh' ? 'AI 简历分析' : 'AI Resume Review'}</h2>
                    <p class="text-sm text-slate-500 mt-0.5">${language == 'zh' ? '继续前请先阅读说明' : 'Please read the disclaimer before continuing'}</p>
                    <p id="disclaimerResumeLabel" class="text-xs font-semibold text-violet-600 mt-1 hidden"></p>
                </div>
            </div>

            <div class="bg-amber-50 border border-amber-200 rounded-2xl p-4 mb-5 text-sm text-amber-900 leading-relaxed">
                <p class="font-bold mb-2">${language == 'zh' ? '注意：请仔细阅读以下说明：' : 'Disclaimer — Read carefully:'}</p>
                <ul class="list-disc pl-4 space-y-1.5 text-amber-800">
                    <li>${language == 'zh' ? '本分析由 AI 模型（Qwen VL）生成，仅供参考。' : 'This analysis is generated by an AI model (Qwen VL) and is for reference only.'}</li>
                    <li>${language == 'zh' ? '结果可能不完整、不准确，或包含模型幻觉。' : 'Results may be incomplete, inaccurate, or contain hallucinations.'}</li>
                    <li>${language == 'zh' ? 'AI 建议不代表官方录用决定，也不构成任何保证。' : 'AI suggestions do not represent official hiring decisions or guarantees.'}</li>
                    <li>${language == 'zh' ? '上传文件仅会在分析过程中短暂处理。' : 'Uploaded files are processed transiently for analysis purposes only.'}</li>
                    <li>${language == 'zh' ? '上传 PDF 或图片格式的简历可获得更好的视觉分析效果。' : 'Upload a PDF or image of your resume for the best visual analysis.'}</li>
                </ul>
            </div>
            <p class="text-sm text-slate-500">
                ${language == 'zh' ? '法律与隐私上的接受以本弹窗及下方确认按钮为准；AI 分析结果不构成对该接受的补充或替代。点击“同意并分析”即表示你已阅读并理解以上内容。' : 'Legal/privacy acceptance is this dialog plus the button below; AI analysis does not supplement or replace it. By clicking "I Agree & Analyse" you confirm you have read and understood the above.'}
            </p>
        </div>
        <div class="px-7 py-5 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
            <button type="button" onclick="closeDisclaimerModal()" class="portal-btn portal-btn-secondary">
                ${language == 'zh' ? '取消' : 'Cancel'}
            </button>
            <button type="button" onclick="startAIReview()" class="portal-btn portal-btn-primary">
                <span class="material-symbols-outlined text-sm">auto_awesome</span>
                ${language == 'zh' ? '同意并分析' : 'I Agree & Analyse'}
            </button>
        </div>
    </div>
</div>

<%-- ═══════════════════════════════════════════════════════
     MODAL 2 — AI Result
     ═══════════════════════════════════════════════════════ --%>
<div id="aiResultModal" class="qm-modal-overlay"
     onclick="if(event.target===this) closeAIModal()">
    <div class="qm-modal-box max-w-2xl">
        <div class="px-7 pt-7 pb-5 border-b border-slate-100 flex items-center justify-between">
            <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-2xl text-violet-600"
                      style="font-variation-settings:'FILL' 1">auto_awesome</span>
                <div>
                    <h2 class="text-xl font-bold text-slate-900">${language == 'zh' ? 'AI 简历分析结果' : 'AI Resume Analysis'}</h2>
                    <p id="aiModelLabel" class="text-xs text-slate-400 mt-0.5"></p>
                </div>
            </div>
            <button onclick="closeAIModal()" class="text-slate-400 hover:text-slate-600 transition-colors p-1">
                <span class="material-symbols-outlined text-2xl">close</span>
            </button>
        </div>

        <%-- loading skeleton --%>
        <div id="aiLoadingState" class="p-7 space-y-3">
            <div class="skeleton h-4 w-2/3"></div>
            <div class="skeleton h-3.5 w-full"></div>
            <div class="skeleton h-3.5 w-5/6"></div>
            <div class="skeleton h-3.5 w-4/5"></div>
            <div class="skeleton h-3.5 w-full"></div>
            <div class="skeleton h-3.5 w-3/4 mt-4"></div>
            <div class="skeleton h-3.5 w-full"></div>
            <div class="skeleton h-3.5 w-5/6"></div>
            <p class="text-center text-xs text-slate-400 mt-4 animate-pulse">
                ${language == 'zh' ? 'AI 正在分析你的简历…这可能需要 15-30 秒。' : 'AI is analysing your resume… this may take 15-30 seconds.'}
            </p>
        </div>

        <%-- result --%>
        <div id="aiResultContent" class="ai-prose px-7 py-6 overflow-y-auto"
             style="display:none; max-height:60vh"></div>

        <%-- error --%>
        <div id="aiErrorState" class="p-7 text-center" style="display:none">
            <span class="material-symbols-outlined text-5xl text-red-300 mb-3 block"
                  style="font-variation-settings:'FILL' 1">error_circle</span>
            <p class="text-sm font-bold text-red-700 mb-1" id="aiErrorMessage"></p>
            <p class="text-xs text-slate-400">${language == 'zh' ? '请检查 API 配置，或稍后再试。' : 'Check your API configuration or try again later.'}</p>
        </div>

        <div class="px-7 py-4 bg-slate-50 border-t border-slate-100 flex items-center justify-between">
            <p class="text-[11px] text-slate-400">${language == 'zh' ? 'AI 生成内容 · 仅供参考' : 'AI-generated content · for reference only'}</p>
            <button onclick="closeAIModal()" class="portal-btn portal-btn-secondary text-sm">${language == 'zh' ? '关闭' : 'Close'}</button>
        </div>
    </div>
</div>

<%-- ═══════════════════════════════════════════════════════
     MODAL 3 — Edit Resume
     ═══════════════════════════════════════════════════════ --%>
<div id="editModal" class="qm-modal-overlay"
     onclick="if(event.target===this) closeEditModal()">
    <div class="qm-modal-box max-w-lg">
        <div class="px-7 pt-7 pb-5 border-b border-slate-100 flex items-center justify-between">
            <h2 class="text-xl font-bold text-slate-900">${language == 'zh' ? '编辑简历信息' : 'Edit Resume Details'}</h2>
            <button onclick="closeEditModal()" class="text-slate-400 hover:text-slate-600 p-1">
                <span class="material-symbols-outlined text-2xl">close</span>
            </button>
        </div>
        <form method="POST" action="${pageContext.request.contextPath}/resumes"
              class="px-7 py-6 space-y-4 overflow-y-auto" style="max-height:72vh">
            <input type="hidden" name="action" value="save"/>
            <input type="hidden" name="resumeId" id="editResumeId"/>

            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1.5">${language == 'zh' ? '标题' : 'Title'}</label>
                <input type="text" name="title" id="editTitle" required
                       class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-violet-300"/>
            </div>
            <div class="grid grid-cols-2 gap-4">
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-1.5">${language == 'zh' ? '院系' : 'Department'}</label>
                    <input type="text" name="department" id="editDept"
                           class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-violet-300"/>
                </div>
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-1.5">${language == 'zh' ? '学位层次' : 'Degree Level'}</label>
                    <select name="degreeLevel" id="editDegree"
                            class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-violet-300">
                        <option value="BACHELOR">${language == 'zh' ? '本科' : 'Bachelor'}</option>
                        <option value="MASTER">${language == 'zh' ? '硕士' : 'Master'}</option>
                        <option value="PHD">PhD</option>
                    </select>
                </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-1.5">GPA</label>
                    <input type="number" name="gpa" id="editGpa" step="0.01" min="0" max="4"
                           class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-violet-300"/>
                </div>
                <div>
                    <label class="block text-sm font-semibold text-slate-700 mb-1.5">${language == 'zh' ? '每周最多工时' : 'Max Hours / Week'}</label>
                    <input type="number" name="maxWeeklyHours" id="editHours" min="1" max="40"
                           class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-violet-300"/>
                </div>
            </div>
            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1.5">${language == 'zh' ? '个人陈述 / 简介' : 'Personal Statement / Bio'}</label>
                <textarea name="bio" id="editBio" rows="4" class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-violet-300 resize-y"></textarea>
            </div>
            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-2">${language == 'zh' ? '可用时间' : 'Availability'}</label>
                <div class="space-y-2">
                    <c:forEach begin="0" end="2" var="idx">
                        <div class="grid grid-cols-1 gap-2 sm:grid-cols-[1.2fr_1fr_1fr]">
                            <select name="availabilityDay" id="editAvailDay${idx}"
                                    class="rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-violet-300">
                                <option value="">${language == 'zh' ? '选择星期' : 'Select day'}</option>
                                <c:forEach items="${daysOfWeek}" var="day">
                                    <option value="${day}"><c:out value="${day}"/></option>
                                </c:forEach>
                            </select>
                            <input type="time" name="availabilityStart" id="editAvailStart${idx}"
                                   class="rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-violet-300"/>
                            <input type="time" name="availabilityEnd" id="editAvailEnd${idx}"
                                   class="rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-violet-300"/>
                        </div>
                    </c:forEach>
                </div>
            </div>
            <div>
                <label class="block text-sm font-semibold text-slate-700 mb-1.5">${language == 'zh' ? '标签' : 'Labels'}</label>
                <input type="text" name="resumeLabels" id="editResumeLabels"
                       class="w-full rounded-xl border border-slate-200 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-violet-300"
                       placeholder="${language == 'zh' ? '逗号或分号分隔' : 'Comma or semicolon separated'}"/>
            </div>
            <div class="pt-1 flex justify-end gap-3">
                <button type="button" onclick="closeEditModal()" class="portal-btn portal-btn-secondary">${language == 'zh' ? '取消' : 'Cancel'}</button>
                <button type="submit" class="portal-btn portal-btn-primary">
                    <span class="material-symbols-outlined text-sm">save</span>
                    ${language == 'zh' ? '保存修改' : 'Save Changes'}
                </button>
            </div>
        </form>
    </div>
</div>

<%-- ═══════════════════════════════════════════════════════
     SCRIPTS
     ═══════════════════════════════════════════════════════ --%>
<script>
    // ── file upload via fetch → JSON response → rename modal ──────────────────
    (function () {
        var input = document.getElementById('resumeFileInput');
        var zone  = document.getElementById('dropZone');
        if (!input || !zone) return;

        // Save original dropzone HTML so we can restore it on error
        var zoneOriginalHTML = zone.innerHTML;

        function showZoneLoading(fileName) {
            zone.innerHTML = '<div class="py-6 text-center">'
                + '<div class="inline-block w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mb-3"></div>'
                + '<p class="text-sm font-semibold text-slate-700">${language == 'zh' ? '正在上传' : 'Uploading'} <em>' + fileName + '</em>…</p>'
                + '<p class="text-xs text-slate-400 mt-1">${language == 'zh' ? '请稍候' : 'Please wait'}</p>'
                + '</div>';
        }

        function restoreZone() {
            zone.innerHTML = zoneOriginalHTML;
        }

        function uploadFile(file) {
            if (!file) return;
            showZoneLoading(file.name);

            var fd = new FormData();
            fd.append('action', 'upload');
            fd.append('resumeFile', file);
            var ulab = document.getElementById('uploadLabelsInput');
            if (ulab && ulab.value && ulab.value.trim()) {
                fd.append('labels', ulab.value.trim());
            }

            fetch('${pageContext.request.contextPath}/resumes', { method: 'POST', body: fd })
                .then(function (r) { return r.json(); })
                .then(function (data) {
                    restoreZone();
                    // Reset input so the same file can be re-selected if needed
                    input.value = '';
                    if (!data.ok) {
                        alert('${language == 'zh' ? '上传失败：' : 'Upload failed: '}' + (data.error || '${language == 'zh' ? '未知错误' : 'Unknown error'}'));
                        return;
                    }
                    // Show rename modal so user can give the resume a meaningful name
                    openRenameModal(data.resumeId, data.originalFileName);
                })
                .catch(function (err) {
                    restoreZone();
                    input.value = '';
                    alert('${language == 'zh' ? '上传失败（网络错误），请重试。' : 'Upload failed (network error). Please try again.'}');
                });
        }

        input.addEventListener('change', function () {
            if (input.files && input.files.length > 0) uploadFile(input.files[0]);
        });

        ['dragenter', 'dragover'].forEach(function (evt) {
            zone.addEventListener(evt, function (e) {
                e.preventDefault();
                zone.classList.add('dragover');
            });
        });
        zone.addEventListener('dragleave', function () { zone.classList.remove('dragover'); });
        zone.addEventListener('drop', function (e) {
            e.preventDefault();
            zone.classList.remove('dragover');
            var files = e.dataTransfer && e.dataTransfer.files;
            if (files && files.length) uploadFile(files[0]);
        });
    })();

    // ── Rename modal (shown right after a successful file upload) ────────────────
    var _renameResumeId = null;

    function openRenameModal(resumeId, originalFileName) {
        _renameResumeId = resumeId;
        var nameInput   = document.getElementById('renameInput');
        var fileInfo    = document.getElementById('renameFileInfo');
        var labIn       = document.getElementById('renameLabelsInput');
        if (nameInput)  { nameInput.value = '${language == 'zh' ? '未命名' : 'Untitled'}'; }
        if (fileInfo)   { fileInfo.textContent = originalFileName || ''; }
        if (labIn)      { labIn.value = ''; }
        document.getElementById('renameModal').classList.add('open');
        if (nameInput) { nameInput.focus(); nameInput.select(); }
    }

    function closeRenameModal() {
        document.getElementById('renameModal').classList.remove('open');
        // Resume already saved with title "Untitled" — just reload to show the card
        window.location.href = '${pageContext.request.contextPath}/resumes';
    }

    function saveRename() {
        if (!_renameResumeId) { closeRenameModal(); return; }
        var title = (document.getElementById('renameInput').value || '').trim() || '${language == 'zh' ? '未命名' : 'Untitled'}';
        var form  = document.createElement('form');
        form.method = 'POST';
        form.action = '${pageContext.request.contextPath}/resumes';
        function addHidden(name, val) {
            var i = document.createElement('input');
            i.type = 'hidden'; i.name = name; i.value = val;
            form.appendChild(i);
        }
        addHidden('action',   'rename');
        addHidden('resumeId', _renameResumeId);
        addHidden('title',    title);
        var rl = document.getElementById('renameLabelsInput');
        if (rl && rl.value && rl.value.trim()) {
            addHidden('labels', rl.value.trim());
        }
        document.body.appendChild(form);
        form.submit();
    }

    document.getElementById('renameInput') &&
        document.getElementById('renameInput').addEventListener('keydown', function (e) {
            if (e.key === 'Enter') saveRename();
        });

    // ── Modal helpers (use qm-modal-overlay.open, NOT Tailwind hidden) ─────────
    var _currentReviewResumeId = null;  // null = sidebar (latest file), string = specific card

    function openDisclaimerModal(resumeId, resumeTitle) {
        _currentReviewResumeId = resumeId || null;
        var lbl = document.getElementById('disclaimerResumeLabel');
        if (lbl) {
            if (resumeTitle) {
                lbl.textContent = '${language == 'zh' ? '正在分析：' : 'Analysing: '}' + resumeTitle;
                lbl.classList.remove('hidden');
            } else {
                lbl.classList.add('hidden');
            }
        }
        document.getElementById('disclaimerModal').classList.add('open');
    }
    function closeDisclaimerModal() {
        document.getElementById('disclaimerModal').classList.remove('open');
    }
    function openAIModal() {
        var m = document.getElementById('aiResultModal');
        m.classList.add('open');
        document.getElementById('aiLoadingState').style.display  = '';
        document.getElementById('aiResultContent').style.display = 'none';
        document.getElementById('aiErrorState').style.display    = 'none';
    }
    function closeAIModal() {
        document.getElementById('aiResultModal').classList.remove('open');
    }
    function openEditModal(id, title, dept, degree, gpa, hours, bio, labelsJoined, availabilityJson) {
        document.getElementById('editResumeId').value = id;
        document.getElementById('editTitle').value    = title  || '';
        document.getElementById('editDept').value     = dept   || '';
        document.getElementById('editDegree').value   = degree || 'BACHELOR';
        document.getElementById('editGpa').value      = gpa    || '';
        document.getElementById('editHours').value    = hours  || '15';
        document.getElementById('editBio').value      = bio    || '';
        var lr = document.getElementById('editResumeLabels');
        if (lr) {
            lr.value = (labelsJoined || '').split('|').join(', ');
        }
        fillAvailabilityRows(availabilityJson || '[]');
        document.getElementById('editModal').classList.add('open');
    }
    function closeEditModal() {
        document.getElementById('editModal').classList.remove('open');
    }

    // ── Edit from data-* attributes (safe with special chars) ─────────────────
    function openEditModalFromBtn(btn) {
        var d = btn.dataset;
        openEditModal(d.resumeId, d.title, d.dept, d.degree, d.gpa, d.hours, d.bio,
            d.resumeLabels || '', d.availabilityJson || '[]');
    }

    function fillAvailabilityRows(rawJson) {
        var slots = [];
        try { slots = JSON.parse(rawJson || '[]') || []; } catch (e) { slots = []; }
        for (var i = 0; i < 3; i++) {
            var slot = slots[i] || {};
            var day = document.getElementById('editAvailDay' + i);
            var start = document.getElementById('editAvailStart' + i);
            var end = document.getElementById('editAvailEnd' + i);
            if (day) day.value = slot.dayOfWeek || '';
            if (start) start.value = normalizeTime(slot.startTime);
            if (end) end.value = normalizeTime(slot.endTime);
        }
    }

    function normalizeTime(value) {
        if (!value) return '';
        return String(value).substring(0, 5);
    }

    // ── AI Review ─────────────────────────────────────────────────────────────
    function startAIReview() {
        closeDisclaimerModal();
        openAIModal();
        callAIReview();
    }

    function callAIReview() {
        var fd = new FormData();
        fd.append('action', 'aiReview');
        if (_currentReviewResumeId) {
            fd.append('resumeId', _currentReviewResumeId);
        }

        fetch('${pageContext.request.contextPath}/resumes', { method: 'POST', body: fd })
            .then(function(r) {
                if (!r.ok && r.status !== 200) throw new Error('HTTP ' + r.status);
                return r.json();
            })
            .then(function(data) {
                document.getElementById('aiLoadingState').style.display = 'none';
                if (!data.ok) {
                    showAIError(data.error || '${language == 'zh' ? '服务器返回未知错误。' : 'Unknown error from server.'}');
                    return;
                }
                if (data.model) {
                    var modeStr = data.hasFile ? '${language == 'zh' ? ' · 多模态分析' : ' · multimodal analysis'}' : '${language == 'zh' ? ' · 文本分析' : ' · text analysis'}';
                    var resumeStr = data.resumeTitle ? ' · ' + data.resumeTitle : '';
                    document.getElementById('aiModelLabel').textContent =
                        '${language == 'zh' ? '模型：' : 'Model: '}' + data.model + modeStr + resumeStr;
                }
                var content = document.getElementById('aiResultContent');
                content.innerHTML = mdToHtml(data.analysis || '');
                content.style.display = '';
            })
            .catch(function(err) {
                document.getElementById('aiLoadingState').style.display = 'none';
                showAIError('${language == 'zh' ? '网络或服务器错误：' : 'Network or server error: '}' + err.message);
            });
    }

    function showAIError(msg) {
        document.getElementById('aiErrorMessage').textContent = msg;
        document.getElementById('aiErrorState').style.display = '';
    }

    // Lightweight markdown → HTML
    function mdToHtml(text) {
        return text
            .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
            .replace(/^[#]{1,3}\s+(.+)$/gm,'<h2>$1</h2>')
            .replace(/\*\*(.+?)\*\*/g,'<strong>$1</strong>')
            .replace(/^[-•*]\s+(.+)$/gm,'<li>$1</li>')
            .replace(/^\d+\.\s+(.+)$/gm,'<li>$1</li>')
            .replace(/(<li>[^]*?<\/li>\n?)+/g, function(m){ return '<ul>'+m+'</ul>'; })
            .replace(/\n{2,}/g,'</p><p>')
            .replace(/^(?!<[hup])(.+)$/gm,'<p>$1</p>');
    }

    // ── Delete ─────────────────────────────────────────────────────────────────
    function deleteResume(id) {
        if (!confirm('${language == 'zh' ? '确定删除这份简历吗？此操作无法撤销。' : 'Delete this resume? This action cannot be undone.'}')) return;
        var f = document.createElement('form');
        f.method = 'POST';
        f.action = '${pageContext.request.contextPath}/resumes';
        f.innerHTML = '<input type="hidden" name="action" value="delete"><input type="hidden" name="resumeId" value="' + id + '">';
        document.body.appendChild(f);
        f.submit();
    }
    function duplicateResume(id) {
        var f = document.createElement('form');
        f.method = 'POST';
        f.action = '${pageContext.request.contextPath}/resumes';
        f.innerHTML = '<input type="hidden" name="action" value="duplicate"><input type="hidden" name="resumeId" value="' + id + '">';
        document.body.appendChild(f);
        f.submit();
    }
</script>
</body>
</html>
