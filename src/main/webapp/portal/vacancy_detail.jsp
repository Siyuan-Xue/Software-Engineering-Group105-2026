<%--
  单个 Vacancy 详情页：必须由 query 参数 vacancyId 定位资源；详情对象来自 request attribute `vacancy`。
  无 vacancy 时展示 notFound 状态页而非抛错，便于用户理解与返回列表（也是 TA/MO 差异化最明显的页面之一）。
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${language == 'zh' ? '岗位详情 - QM HIRE' : 'Vacancy Detail - QM HIRE'}</title>
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
    <style>
        .fav-btn { transition: transform .15s, color .15s; }
        .fav-btn:hover { transform: scale(1.08); }
        .fav-btn.saved .material-symbols-outlined { font-variation-settings: 'FILL' 1; color: #ef4444; }
        .fav-btn:not(.saved) .material-symbols-outlined { color: #94a3b8; }
        .qm-modal-overlay {
            display: none;
            position: fixed; inset: 0;
            background: rgba(15,23,42,0.55);
            align-items: center; justify-content: center;
            z-index: 10001; padding: 1rem;
        }
        .qm-modal-overlay.open { display: flex; }
        .qm-modal-box {
            background: #fff; border-radius: 1.5rem;
            box-shadow: 0 32px 64px -24px rgba(15,23,42,0.4);
            width: 100%; max-height: 90vh; overflow: hidden;
            display: flex; flex-direction: column;
        }
    </style>
</head>
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
    <div class="relative flex min-h-screen w-full flex-col">
        <jsp:include page="/WEB-INF/jsp/components/header.jsp" />

        <div class="flex flex-1 overflow-hidden">
            <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

            <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
                <div class="portal-page portal-page--detail">
                    <a href="${pageContext.request.contextPath}/vacancies" class="flex items-center gap-2 text-slate-500 hover:text-primary mb-6 transition-colors font-medium w-fit">
                        <span class="material-symbols-outlined">arrow_back</span>
                        ${language == 'zh' ? '返回岗位列表' : 'Back to Vacancies'}
                    </a>

                    <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                        <jsp:param name="containerClass" value="mb-6" />
                    </jsp:include>

                    <c:choose>
                        <c:when test="${pageState == 'loadError'}">
                            <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                                <jsp:param name="variant" value="error" />
                                <jsp:param name="icon" value="work_alert" />
                                <jsp:param name="title" value="${language == 'zh' ? '岗位暂不可用' : 'Vacancy unavailable'}" />
                                <jsp:param name="message" value="${language == 'zh' ? '当前无法加载该岗位，请刷新页面或返回列表后重试。' : 'Unable to load this vacancy right now. Please refresh the page or return to the list and try again.'}" />
                                <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                <jsp:param name="actionLabel" value="${language == 'zh' ? '返回岗位列表' : 'Back to Vacancies'}" />
                            </jsp:include>
                        </c:when>
                        <%-- vacancy 为空：无效或已删除的 vacancyId；与 loadError 区分（后者为加载过程异常）。 --%>
                        <c:when test="${empty vacancy}">
                            <jsp:include page="/WEB-INF/jsp/components/state_card.jsp">
                                <jsp:param name="variant" value="notFound" />
                                <jsp:param name="icon" value="search_off" />
                                <jsp:param name="title" value="${language == 'zh' ? '未找到岗位' : 'Vacancy not found'}" />
                                <jsp:param name="message" value="${language == 'zh' ? '你要查看的岗位不存在或已被移除。' : 'The vacancy you are looking for does not exist or has been removed.'}" />
                                <jsp:param name="actionHref" value="${pageContext.request.contextPath}/vacancies" />
                                <jsp:param name="actionLabel" value="${language == 'zh' ? '浏览岗位' : 'Browse Vacancies'}" />
                            </jsp:include>
                        </c:when>
                        <c:otherwise>
                            <div class="portal-panel overflow-hidden">
                                <div class="p-8 border-b border-slate-100">
                                    <div class="flex flex-col md:flex-row justify-between items-start gap-4">
                                        <div>
                                            <span class="px-3 py-1 bg-primary/10 text-primary text-xs font-bold uppercase rounded-md tracking-wider mb-3 inline-block">
                                                <c:out value="${vacancy.courseCode}"/>
                                            </span>
                                            <h2 class="text-3xl font-black text-slate-900 tracking-tight"><c:out value="${vacancy.title}"/></h2>
                                            <p class="text-lg text-slate-500 mt-1"><c:out value="${vacancy.department}"/></p>
                                            <c:if test="${not empty vacancy.labels}">
                                                <div class="flex flex-wrap gap-2 mt-3">
                                                    <c:forEach items="${vacancy.labels}" var="lb">
                                                        <span class="text-[11px] font-bold px-2.5 py-1 rounded-full bg-slate-100 text-slate-600 border border-slate-200"><c:out value="${lb}"/></span>
                                                    </c:forEach>
                                                </div>
                                            </c:if>
                                        </div>
                                        <%-- MO 仅当 isOwner 显示编辑；TA 等在 else 显示申请（与角色权限展示约定一致）。 --%>
                                        <c:choose>
                                            <c:when test="${userRole == 'MO'}">
                                                <c:choose>
                                                    <c:when test="${vacancy.owner}">
                                                        <a href="${pageContext.request.contextPath}/vacancy/edit?vacancyId=${vacancy.vacancyId}&amp;returnTo=detail"
                                                           class="portal-btn portal-btn-secondary inline-flex items-center gap-2">
                                                            <span class="material-symbols-outlined text-sm">edit</span>
                                                            ${language == 'zh' ? '编辑岗位' : 'Edit Vacancy'}
                                                        </a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="inline-flex items-center gap-2 rounded-full bg-slate-100 px-4 py-2 text-xs font-bold uppercase tracking-wider text-slate-600">
                                                            <span class="material-symbols-outlined text-base">visibility</span>
                                                            ${language == 'zh' ? '仅查看' : 'View Only'}
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:when test="${userRole == 'TA'}">
                                                <div class="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
                                                    <button type="button"
                                                            class="fav-btn ${vacancy.saved ? 'saved' : ''} inline-flex items-center justify-center w-12 h-12 rounded-2xl border border-slate-200 bg-white shadow-sm"
                                                            data-vacancy-id="${vacancy.vacancyId}"
                                                            title="${vacancy.saved ? (language == 'zh' ? '取消收藏' : 'Remove from saved') : (language == 'zh' ? '收藏岗位' : 'Save vacancy')}"
                                                            onclick="toggleVacancyFavorite(this)">
                                                        <span class="material-symbols-outlined text-2xl">favorite</span>
                                                    </button>
                                                    <button onclick="openApplyModal()" class="portal-btn portal-btn-primary flex-1 sm:flex-none">
                                                        ${language == 'zh' ? '立即申请' : 'Apply Now'}
                                                    </button>
                                                </div>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="inline-flex items-center gap-2 rounded-full bg-slate-100 px-4 py-2 text-xs font-bold uppercase tracking-wider text-slate-600">
                                                    <span class="material-symbols-outlined text-base">admin_panel_settings</span>
                                                    ${language == 'zh' ? '管理员查看' : 'Admin View'}
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <div class="grid grid-cols-2 md:grid-cols-4 gap-6 mt-8">
                                        <div class="space-y-1">
                                            <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">${language == 'zh' ? '工时' : 'Hours'}</p>
                                            <p class="text-sm font-bold text-slate-700"><c:out value="${vacancy.hoursPerWeek}"/> ${language == 'zh' ? '小时/周' : 'hrs/week'}</p>
                                        </div>
                                        <div class="space-y-1">
                                            <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">${language == 'zh' ? '时薪' : 'Pay Rate'}</p>
                                            <p class="text-sm font-bold text-slate-700">$<c:out value="${vacancy.hourlyRate}"/>/hr</p>
                                        </div>
                                        <div class="space-y-1">
                                            <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">${language == 'zh' ? '截止日期' : 'Deadline'}</p>
                                            <p class="text-sm font-bold text-red-500"><c:out value="${vacancy.deadline}"/></p>
                                        </div>
                                        <div class="space-y-1">
                                            <p class="text-xs font-bold text-slate-400 uppercase tracking-wider">${language == 'zh' ? '课程负责人' : 'Module Owner'}</p>
                                            <p class="text-sm font-bold text-slate-700"><c:out value="${vacancy.moduleOwner}"/></p>
                                        </div>
                                    </div>
                                </div>

                                <div class="p-8 space-y-8">
                                    <%-- description：单段文本；与契约 Vacancy.description 对应。 --%>
                                    <section>
                                        <h3 class="text-xl font-bold text-slate-900 mb-4">${language == 'zh' ? '岗位描述' : 'Description'}</h3>
                                        <p class="text-slate-600 leading-relaxed">
                                            <c:out value="${vacancy.description}"/>
                                        </p>
                                    </section>

                                    <%-- requirements：后端通常为 List<String>，逐条列出；与契约中 Vacancy 结构一致。 --%>
                                    <section>
                                        <h3 class="text-xl font-bold text-slate-900 mb-4">${language == 'zh' ? '岗位要求' : 'Requirements'}</h3>
                                        <c:choose>
                                            <c:when test="${empty vacancy.requirements}">
                                                <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                    <jsp:param name="icon" value="fact_check" />
                                                    <jsp:param name="title" value="${language == 'zh' ? '要求信息待补充' : 'Requirements pending'}" />
                                                    <jsp:param name="message" value="${language == 'zh' ? '该岗位暂未提供结构化要求说明，请稍后再查看或联系课程负责人。' : 'Structured requirement details are not available for this vacancy yet. Please check back later or contact the module owner.'}" />
                                                    <jsp:param name="containerClass" value="px-0 py-2" />
                                                </jsp:include>
                                            </c:when>
                                            <c:otherwise>
                                                <ul class="space-y-3">
                                                    <c:forEach items="${vacancy.requirements}" var="requirement">
                                                        <li class="flex gap-3 text-slate-600">
                                                            <span class="material-symbols-outlined text-primary text-lg">check_circle</span>
                                                            <span class="text-sm"><c:out value="${requirement}"/></span>
                                                        </li>
                                                    </c:forEach>
                                                </ul>
                                            </c:otherwise>
                                        </c:choose>
                                    </section>
                                    <%-- responsibilities 在契约中同为 List<String>；若产品需要展示，可在此复用与 requirements 相同的列表模式。 --%>
                                </div>
                            </div>

                            <%-- 申请弹窗依赖后端注入的 resumeList（契约）：无简历时 inline_state 提示 noResumeAvailable 语义，并禁用提交。 --%>
                            <!-- Apply Modal -->
                            <div id="applyModal" style="display:none" class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
                                <div class="portal-modal-card w-full max-w-lg p-8 shadow-2xl" style="max-height:90vh;overflow-y:auto">
                                    <div class="flex items-center justify-between mb-2">
                                        <h3 class="text-2xl font-black text-slate-900">${language == 'zh' ? '提交申请' : 'Submit application'}</h3>
                                        <button type="button" onclick="closeApplyModal()"
                                                class="text-slate-400 hover:text-slate-600 transition-colors">
                                            <span class="material-symbols-outlined text-2xl">close</span>
                                        </button>
                                    </div>
                                    <p class="text-slate-500 text-sm mb-3">${language == 'zh' ? '从已上传的简历/文件中选择，或上传新文件（MO 将看到本次提交的文件）。可选「AI 匹配排序」辅助选择。' : 'Pick a previously uploaded resume/file or upload a new one (the module organiser will see what you submit). Optional AI ranking is available separately.'}</p>

                                    <c:if test="${qwenConfigured && not empty resumeList}">
                                        <button type="button" onclick="openAiRankFromApplyModal()"
                                                class="mb-4 inline-flex items-center gap-1.5 text-xs font-bold text-violet-700 hover:text-violet-900 border border-violet-200 bg-violet-50 px-3 py-2 rounded-lg">
                                            <span class="material-symbols-outlined text-[16px]" style="font-variation-settings:'FILL' 1">auto_awesome</span>
                                            ${language == 'zh' ? 'AI 匹配排序（可选）' : 'AI rank resumes (optional)'}
                                        </button>
                                    </c:if>

                                    <!-- AI recommendation notice -->
                                    <div id="aiRankStatus" class="flex items-center gap-2 text-xs text-violet-600 mb-5" style="display:none!important">
                                        <span class="material-symbols-outlined text-[14px] animate-spin">autorenew</span>
                                        <span>${language == 'zh' ? 'AI 正在分析你的简历与该岗位的匹配度…' : 'AI is analysing your resumes for this role…'}</span>
                                    </div>
                                    <div id="aiRankDone" class="flex items-center gap-2 text-xs text-violet-600 mb-5" style="display:none!important">
                                        <span class="material-symbols-outlined text-[14px]" style="font-variation-settings:'FILL' 1">auto_awesome</span>
                                        <span>${language == 'zh' ? 'AI 推荐已生成，请查看下方分数' : 'AI recommendation ready — see scores below'}</span>
                                    </div>
                                    <div id="aiRankErrorMsg" class="text-xs text-red-700 mb-5 rounded-lg border border-red-200 bg-red-50 px-3 py-2" style="display:none"></div>

                                    <form action="${pageContext.request.contextPath}/application" method="POST" enctype="multipart/form-data">
                                        <input type="hidden" name="vacancyId" value="${vacancy.vacancyId}">

                                        <div class="mb-6 rounded-2xl border border-slate-200 bg-slate-50/80 p-4" id="resumeSourceSection">
                                            <label class="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-3">
                                                ${language == 'zh' ? '简历文件（本次申请）' : 'Resume file (this application)'}
                                            </label>
                                            <div class="flex gap-2 mb-4" role="tablist">
                                                <button type="button" id="resumeSourceSavedBtn" role="tab" aria-selected="true"
                                                        class="resume-source-tab flex-1 py-2 px-3 rounded-xl text-sm font-bold border transition-all border-violet-300 bg-white text-violet-800 shadow-sm">
                                                    ${language == 'zh' ? '从已上传中选择' : 'Choose uploaded'}
                                                </button>
                                                <button type="button" id="resumeSourceUploadBtn" role="tab" aria-selected="false"
                                                        class="resume-source-tab flex-1 py-2 px-3 rounded-xl text-sm font-bold border transition-all border-transparent bg-transparent text-slate-600 hover:bg-white/80">
                                                    ${language == 'zh' ? '上传新文件' : 'Upload new'}
                                                </button>
                                            </div>
                                            <div id="resumeSourceSavedPanel" role="tabpanel">
                                                <div id="resumeOptionsList" class="space-y-3 max-h-52 overflow-y-auto pr-1">
                                                    <c:choose>
                                                        <c:when test="${empty resumeList}">
                                                            <p class="text-sm text-slate-500">${language == 'zh' ? '暂无已上传的简历，请切换到「上传新文件」或前往简历页上传。' : 'No uploaded resumes yet. Switch to Upload new or go to Resumes.'}</p>
                                                            <a href="${pageContext.request.contextPath}/resumes" class="inline-flex items-center gap-1 text-sm font-semibold text-violet-700 hover:text-violet-900 mt-2">
                                                                <span class="material-symbols-outlined text-base">folder_open</span>
                                                                ${language == 'zh' ? '前往简历页' : 'Go to Resumes'}
                                                            </a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:forEach items="${resumeList}" var="resume" varStatus="status">
                                                                <label id="resumeLabel_${resume.resumeId}"
                                                                       class="resume-pick-option w-full rounded-2xl border border-slate-200 bg-white p-4 text-left transition-all flex items-center gap-3 cursor-pointer hover:border-violet-300">
                                                                    <input type="radio" name="resumeId" value="${resume.resumeId}"
                                                                           class="resume-id-radio w-5 h-5 text-primary border-slate-300 focus:ring-primary shrink-0"
                                                                           data-has-file="${resume.fileAvailable}"
                                                                           <c:if test="${status.first}">checked="checked"</c:if>>
                                                                    <span class="material-symbols-outlined text-2xl text-slate-400 shrink-0" style="font-variation-settings:'FILL' 0">
                                                                        <c:choose>
                                                                            <c:when test="${resume.fileAvailable}">description</c:when>
                                                                            <c:otherwise>person</c:otherwise>
                                                                        </c:choose>
                                                                    </span>
                                                                    <div class="flex-1 min-w-0">
                                                                        <p class="text-sm font-bold text-slate-900 truncate"><c:out value="${resume.resumeName}"/></p>
                                                                        <p class="text-xs text-slate-500 truncate mt-0.5">
                                                                            <c:choose>
                                                                                <c:when test="${resume.fileAvailable && not empty resume.originalFileName}">
                                                                                    <c:out value="${resume.originalFileName}"/>
                                                                                </c:when>
                                                                                <c:when test="${resume.fileAvailable}">
                                                                                    ${language == 'zh' ? '已上传文件' : 'Uploaded file'}
                                                                                </c:when>
                                                                                <c:otherwise>
                                                                                    ${language == 'zh' ? '仅文字档案（无附件）' : 'Profile only (no file)'}
                                                                                </c:otherwise>
                                                                            </c:choose>
                                                                        </p>
                                                                        <div id="scoreBadge_${resume.resumeId}" class="mt-1"></div>
                                                                    </div>
                                                                </label>
                                                            </c:forEach>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                            <div id="resumeSourceUploadPanel" role="tabpanel" class="hidden">
                                                <input type="file" id="applyResumeFileInput" name="resumeFile" accept=".pdf,.doc,.docx,.jpg,.jpeg,.png,.txt"
                                                       class="block w-full text-sm text-slate-600 file:mr-3 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-violet-50 file:text-violet-700 hover:file:bg-violet-100"/>
                                                <p class="text-xs text-slate-500 mt-2">${language == 'zh' ? '支持 PDF、Word、图片、TXT。' : 'PDF, Word, images, or TXT.'}</p>
                                            </div>
                                        </div>

                                        <div class="mb-4">
                                            <label class="block text-xs font-bold uppercase tracking-wider text-slate-500 mb-2">
                                                ${language == 'zh' ? '求职信 / 动机（可选）' : 'Cover letter (optional)'}
                                            </label>
                                            <textarea name="coverLetter" rows="3" maxlength="4000"
                                                      class="w-full rounded-xl border border-slate-200 px-3 py-2 text-sm text-slate-800 focus:ring-2 focus:ring-violet-200 focus:border-violet-400"
                                                      placeholder="${language == 'zh' ? '简要说明申请动机…' : 'Brief motivation for this role…'}"></textarea>
                                        </div>

                                        <div class="flex gap-3">
                                            <button type="button" onclick="closeApplyModal()" class="portal-btn portal-btn-secondary flex-1">
                                                ${language == 'zh' ? '取消' : 'Cancel'}
                                            </button>
                                            <button type="submit" id="applySubmitBtn" class="portal-btn portal-btn-primary flex-1">
                                                ${language == 'zh' ? '确认申请' : 'Confirm Apply'}
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            </div>

                            <c:if test="${userRole == 'TA'}">
                            <div id="aiResumeRankDisclaimerModal" class="qm-modal-overlay"
                                 onclick="if(event.target===this) closeAiResumeRankDisclaimer()">
                                <div class="qm-modal-box max-w-lg" style="max-height:90vh">
                                    <div class="px-7 pt-7 pb-5 overflow-y-auto">
                                        <div class="flex items-start gap-4 mb-4">
                                            <div class="w-12 h-12 rounded-2xl bg-violet-100 flex items-center justify-center shrink-0">
                                                <span class="material-symbols-outlined text-2xl text-violet-600" style="font-variation-settings:'FILL' 1">policy</span>
                                            </div>
                                            <div>
                                                <h2 class="text-xl font-bold text-slate-900">${language == 'zh' ? 'AI 简历匹配排序' : 'AI resume ranking'}</h2>
                                                <p class="text-sm text-slate-500 mt-0.5">${language == 'zh' ? '继续前请先阅读' : 'Please read before continuing'}</p>
                                            </div>
                                        </div>
                                        <div class="bg-amber-50 border border-amber-200 rounded-2xl p-4 mb-4 text-sm text-amber-900 leading-relaxed">
                                            <ul class="list-disc pl-4 space-y-1.5">
                                                <li>${language == 'zh' ? '将把本岗位描述与你名下各份简历的摘要发送至第三方模型（Qwen）以生成 0–100 的匹配分与推荐标签。' : 'Vacancy text and short resume summaries are sent to a third-party model (Qwen) for 0–100 match scores and a pick tag.'}</li>
                                                <li>${language == 'zh' ? '分数仅供参考，不构成录用或拒绝依据。' : 'Scores are indicative only and are not an admission decision.'}</li>
                                                <li>${language == 'zh' ? '法律与隐私说明以本弹窗及你点击的确认按钮为准；模型回复中不会替代该等同意。' : 'Legal/privacy terms are those shown here and confirmed by your button click; the model reply does not replace that consent.'}</li>
                                            </ul>
                                        </div>
                                        <p class="text-sm text-slate-500">${language == 'zh' ? '点击「同意并排序」即表示你已阅读并接受上述说明，此后才会发起 AI 请求。' : 'Click “I agree & rank” to accept the above and send the AI request.'}</p>
                                    </div>
                                    <div class="px-7 py-5 bg-slate-50 border-t border-slate-100 flex justify-end gap-3 shrink-0">
                                        <button type="button" onclick="closeAiResumeRankDisclaimer()" class="portal-btn portal-btn-secondary">${language == 'zh' ? '取消' : 'Cancel'}</button>
                                        <button type="button" onclick="agreeAiResumeRankDisclaimer()" class="portal-btn portal-btn-primary">
                                            <span class="material-symbols-outlined text-sm">auto_awesome</span>
                                            ${language == 'zh' ? '同意并排序' : 'I agree & rank'}
                                        </button>
                                    </div>
                                </div>
                            </div>
                            </c:if>

                            <script>
                                <%-- 与列表页传入的 vacancyId 一致，供 AI 排序等请求定位岗位 --%>
                                var JOB_ID = '${vacancy.vacancyId}';
                                var CTX    = '${pageContext.request.contextPath}';
                                var QWEN_OK = ${qwenConfigured ? 'true' : 'false'};
                                var RESUME_COUNT = ${fn:length(resumeList)};
                                var IS_TA = ${userRole == 'TA' ? 'true' : 'false'};

                                async function toggleVacancyFavorite(btn) {
                                    var vacancyId = btn.dataset.vacancyId;
                                    if (!vacancyId) return;
                                    btn.disabled = true;
                                    try {
                                        var params = new URLSearchParams();
                                        params.append('vacancyId', vacancyId);
                                        var res = await fetch(CTX + '/favorites', {
                                            method: 'POST',
                                            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                                            body: params.toString()
                                        });
                                        if (res.status === 401) {
                                            alert('${language == 'zh' ? '请先登录后再收藏岗位。' : 'Please log in to save vacancies.'}');
                                            return;
                                        }
                                        if (!res.ok) throw new Error('server');
                                        var data = await res.json();
                                        if (data.saved) {
                                            btn.classList.add('saved');
                                            btn.title = '${language == 'zh' ? '取消收藏' : 'Remove from saved'}';
                                        } else {
                                            btn.classList.remove('saved');
                                            btn.title = '${language == 'zh' ? '收藏岗位' : 'Save vacancy'}';
                                        }
                                    } catch (e) { console.error(e); }
                                    finally { btn.disabled = false; }
                                }

                                var RESUME_SOURCE_SAVED = 'saved';
                                var RESUME_SOURCE_UPLOAD = 'upload';

                                function setResumeSourceTabActive(btn, active) {
                                    if (!btn) return;
                                    btn.setAttribute('aria-selected', active ? 'true' : 'false');
                                    btn.classList.toggle('border-violet-300', active);
                                    btn.classList.toggle('bg-white', active);
                                    btn.classList.toggle('text-violet-800', active);
                                    btn.classList.toggle('shadow-sm', active);
                                    btn.classList.toggle('border-transparent', !active);
                                    btn.classList.toggle('bg-transparent', !active);
                                    btn.classList.toggle('text-slate-600', !active);
                                }

                                function switchResumeSource(mode) {
                                    var savedBtn = document.getElementById('resumeSourceSavedBtn');
                                    var uploadBtn = document.getElementById('resumeSourceUploadBtn');
                                    var savedPanel = document.getElementById('resumeSourceSavedPanel');
                                    var uploadPanel = document.getElementById('resumeSourceUploadPanel');
                                    var fileInput = document.getElementById('applyResumeFileInput');
                                    var isUpload = mode === RESUME_SOURCE_UPLOAD;

                                    if (savedPanel) savedPanel.classList.toggle('hidden', isUpload);
                                    if (uploadPanel) uploadPanel.classList.toggle('hidden', !isUpload);
                                    setResumeSourceTabActive(savedBtn, !isUpload);
                                    setResumeSourceTabActive(uploadBtn, isUpload);

                                    document.querySelectorAll('.resume-id-radio').forEach(function(radio) {
                                        if (isUpload) {
                                            radio.checked = false;
                                            radio.disabled = true;
                                        } else {
                                            radio.disabled = false;
                                        }
                                    });
                                    if (!isUpload) {
                                        var checked = document.querySelector('.resume-id-radio:checked');
                                        if (!checked) {
                                            var first = document.querySelector('.resume-id-radio');
                                            if (first) first.checked = true;
                                        }
                                    }
                                    if (fileInput) {
                                        if (isUpload) {
                                            fileInput.disabled = false;
                                            fileInput.setAttribute('name', 'resumeFile');
                                        } else {
                                            fileInput.value = '';
                                            fileInput.disabled = true;
                                            fileInput.removeAttribute('name');
                                        }
                                    }
                                }

                                function initResumeSourceTabs() {
                                    var savedBtn = document.getElementById('resumeSourceSavedBtn');
                                    var uploadBtn = document.getElementById('resumeSourceUploadBtn');
                                    if (savedBtn) {
                                        savedBtn.addEventListener('click', function() { switchResumeSource(RESUME_SOURCE_SAVED); });
                                    }
                                    if (uploadBtn) {
                                        uploadBtn.addEventListener('click', function() { switchResumeSource(RESUME_SOURCE_UPLOAD); });
                                    }
                                    if (RESUME_COUNT <= 0 && savedBtn) {
                                        savedBtn.disabled = true;
                                        savedBtn.classList.add('opacity-50', 'cursor-not-allowed');
                                    }
                                    switchResumeSource(RESUME_COUNT > 0 ? RESUME_SOURCE_SAVED : RESUME_SOURCE_UPLOAD);

                                    var applyForm = document.querySelector('#applyModal form');
                                    if (applyForm) {
                                        applyForm.addEventListener('submit', function(e) {
                                            var uploadPanel = document.getElementById('resumeSourceUploadPanel');
                                            var isUpload = uploadPanel && !uploadPanel.classList.contains('hidden');
                                            var hasFile = document.getElementById('applyResumeFileInput');
                                            hasFile = hasFile && hasFile.files && hasFile.files.length > 0;
                                            var hasResume = document.querySelector('.resume-id-radio:checked');
                                            if (!isUpload && !hasResume) {
                                                e.preventDefault();
                                                alert('${language == 'zh' ? '请选择一份已上传的简历。' : 'Please select an uploaded resume.'}');
                                                return;
                                            }
                                            if (isUpload && !hasFile) {
                                                e.preventDefault();
                                                alert('${language == 'zh' ? '请选择要上传的简历文件。' : 'Please choose a resume file to upload.'}');
                                            }
                                        });
                                    }
                                }

                                if (IS_TA) {
                                    document.addEventListener('DOMContentLoaded', initResumeSourceTabs);
                                }

                                function openApplyModal() {
                                    document.getElementById('applyModal').style.display = 'flex';
                                    switchResumeSource(RESUME_COUNT > 0 ? RESUME_SOURCE_SAVED : RESUME_SOURCE_UPLOAD);
                                }
                                function openAiRankFromApplyModal() {
                                    if (!QWEN_OK) {
                                        alert('${language == 'zh' ? '未配置 QWEN_API_KEY，无法使用 AI。' : 'QWEN_API_KEY is not configured.'}');
                                        return;
                                    }
                                    if (RESUME_COUNT <= 0) {
                                        alert('${language == 'zh' ? '请先上传简历。' : 'Upload a resume first.'}');
                                        return;
                                    }
                                    var disc = document.getElementById('aiResumeRankDisclaimerModal');
                                    if (disc) disc.classList.add('open');
                                }
                                function closeAiResumeRankDisclaimer() {
                                    var disc = document.getElementById('aiResumeRankDisclaimerModal');
                                    if (disc) disc.classList.remove('open');
                                }
                                function agreeAiResumeRankDisclaimer() {
                                    closeAiResumeRankDisclaimer();
                                    if (document.getElementById('applyModal').style.display !== 'flex') {
                                        document.getElementById('applyModal').style.display = 'flex';
                                    }
                                    fetchAIRankings();
                                }
                                function closeApplyModal() {
                                    document.getElementById('applyModal').style.display = 'none';
                                }

                                function fetchAIRankings() {
                                    if (!JOB_ID) return;

                                    var status = document.getElementById('aiRankStatus');
                                    var done   = document.getElementById('aiRankDone');
                                    var errBox = document.getElementById('aiRankErrorMsg');
                                    if (errBox) { errBox.style.display = 'none'; errBox.textContent = ''; }
                                    if (status) status.style.setProperty('display', 'flex', 'important');
                                    if (done)   done.style.setProperty('display', 'none', 'important');

                                    var params = new URLSearchParams();
                                    params.append('jobId', JOB_ID);

                                    fetch(CTX + '/ai-resume-rank', {
                                        method: 'POST',
                                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                                        body: params.toString()
                                    })
                                        .then(function(r) { return r.json().then(function(data) { return { okHttp: r.ok, data: data }; }); })
                                        .then(function(res) {
                                            if (status) status.style.setProperty('display', 'none', 'important');
                                            var data = res.data || {};
                                            if (!res.okHttp || !data.ok || !data.hasResumes) {
                                                var msg = data.error || '${language == 'zh' ? 'AI 排序失败（请确认已配置 QWEN_API_KEY 或稍后重试）' : 'AI ranking failed (check QWEN_API_KEY or try again)'}';
                                                if (errBox) {
                                                    errBox.textContent = msg;
                                                    errBox.style.display = 'block';
                                                }
                                                return;
                                            }

                                            data.rankings.forEach(function(r) {
                                                var badge = document.getElementById('scoreBadge_' + r.resumeId);
                                                var label = document.getElementById('resumeLabel_' + r.resumeId);
                                                if (!badge) return;

                                                if (r.score < 0) {
                                                    badge.innerHTML = '';
                                                    return;
                                                }

                                                var color, icon, text;
                                                if (r.score >= 85) {
                                                    color = 'text-emerald-700 bg-emerald-50 border-emerald-200';
                                                    icon  = '🌟'; text = r.score + '% ${language == 'zh' ? '匹配' : 'match'}';
                                                } else if (r.score >= 65) {
                                                    color = 'text-blue-700 bg-blue-50 border-blue-200';
                                                    icon  = '✓'; text = r.score + '% ${language == 'zh' ? '匹配' : 'match'}';
                                                } else {
                                                    color = 'text-slate-500 bg-slate-50 border-slate-200';
                                                    icon  = ''; text = r.score + '% ${language == 'zh' ? '匹配' : 'match'}';
                                                }

                                                var recTag = '';
                                                if (r.recommended) {
                                                    recTag = '<span class="ml-1 text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-violet-100 text-violet-700">${language == 'zh' ? 'AI 推荐' : 'AI Pick'}</span>';
                                                    if (label) label.classList.add('border-violet-300', 'bg-violet-50/40');
                                                }

                                                badge.innerHTML = '<span class="inline-flex items-center gap-1 text-[11px] font-semibold px-2 py-0.5 rounded-full border ' + color + '">'
                                                    + icon + ' ' + text + '</span>' + recTag;
                                            });

                                            if (done) done.style.setProperty('display', 'flex', 'important');
                                        })
                                        .catch(function(e) {
                                            if (status) status.style.setProperty('display', 'none', 'important');
                                            var errBox2 = document.getElementById('aiRankErrorMsg');
                                            if (errBox2) {
                                                errBox2.textContent = '${language == 'zh' ? '网络错误：' : 'Network error: '}' + e.message;
                                                errBox2.style.display = 'block';
                                            }
                                        });
                                }
                            </script>
                        </c:otherwise>
                    </c:choose>
                </div>
            </main>
        </div>
    </div>
</body>
</html>
