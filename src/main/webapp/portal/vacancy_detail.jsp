<%--
  单个 Vacancy 详情页：必须由 query 参数 vacancyId 定位资源；详情对象来自 request attribute `vacancy`。
  无 vacancy 时展示 notFound 状态页而非抛错，便于用户理解与返回列表（也是 TA/MO 差异化最明显的页面之一）。
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                                        </div>
                                        <%-- MO 仅当 isOwner 显示编辑；TA 等在 else 显示申请（与角色权限展示约定一致）。 --%>
                                        <c:choose>
                                            <c:when test="${userRole == 'MO'}">
                                                <c:choose>
                                                    <c:when test="${vacancy.owner}">
                                                        <button class="portal-btn portal-btn-secondary">
                                                            <span class="material-symbols-outlined text-sm">edit</span>
                                                            ${language == 'zh' ? '编辑岗位' : 'Edit Vacancy'}
                                                        </button>
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
                                                <button onclick="openApplyModal()" class="portal-btn portal-btn-primary">
                                                    ${language == 'zh' ? '立即申请' : 'Apply Now'}
                                                </button>
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
                                <div class="portal-modal-card w-full max-w-md p-8 shadow-2xl" style="max-height:90vh;overflow-y:auto">
                                    <div class="flex items-center justify-between mb-2">
                                        <h3 class="text-2xl font-black text-slate-900">${language == 'zh' ? '选择简历' : 'Select Resume'}</h3>
                                        <button type="button" onclick="closeApplyModal()"
                                                class="text-slate-400 hover:text-slate-600 transition-colors">
                                            <span class="material-symbols-outlined text-2xl">close</span>
                                        </button>
                                    </div>
                                    <p class="text-slate-500 text-sm mb-1">${language == 'zh' ? '选择你想用于本次申请的简历。' : 'Choose which resume you want to submit for this application.'}</p>

                                    <!-- AI recommendation notice -->
                                    <div id="aiRankStatus" class="flex items-center gap-2 text-xs text-violet-600 mb-5" style="display:none!important">
                                        <span class="material-symbols-outlined text-[14px] animate-spin">autorenew</span>
                                        <span>${language == 'zh' ? 'AI 正在分析你的简历与该岗位的匹配度…' : 'AI is analysing your resumes for this role…'}</span>
                                    </div>
                                    <div id="aiRankDone" class="flex items-center gap-2 text-xs text-violet-600 mb-5" style="display:none!important">
                                        <span class="material-symbols-outlined text-[14px]" style="font-variation-settings:'FILL' 1">auto_awesome</span>
                                        <span>${language == 'zh' ? 'AI 推荐已生成，请查看下方分数' : 'AI recommendation ready — see scores below'}</span>
                                    </div>

                                    <form action="${pageContext.request.contextPath}/application" method="POST">
                                        <input type="hidden" name="vacancyId" value="${vacancy.vacancyId}">
                                        <div id="resumeOptionsList" class="space-y-3 mb-8">
                                            <c:choose>
                                                <c:when test="${empty resumeList}">
                                                    <jsp:include page="/WEB-INF/jsp/components/inline_state.jsp">
                                                        <jsp:param name="icon" value="description" />
                                                        <jsp:param name="title" value="${language == 'zh' ? '没有可用简历' : 'No resumes available'}" />
                                                        <jsp:param name="message" value="${language == 'zh' ? '请先上传简历，再提交该申请。' : 'Upload a resume first so you can submit this application.'}" />
                                                        <jsp:param name="actionHref" value="${pageContext.request.contextPath}/resumes" />
                                                        <jsp:param name="actionLabel" value="${language == 'zh' ? '前往简历页面' : 'Go to Resumes'}" />
                                                        <jsp:param name="containerClass" value="px-0 py-2" />
                                                    </jsp:include>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach items="${resumeList}" var="resume" varStatus="status">
                                                        <label id="resumeLabel_${resume.resumeId}"
                                                               class="w-full rounded-2xl border border-slate-200 bg-slate-50/70 p-4 text-left transition-all flex items-center gap-4 cursor-pointer hover:border-violet-300 hover:bg-white">
                                                            <input type="radio" name="resumeId" value="${resume.resumeId}"
                                                                   class="w-5 h-5 text-primary border-slate-300 focus:ring-primary shrink-0"
                                                                   <c:if test="${status.first}">required="required"</c:if>>
                                                            <div class="flex-1 min-w-0">
                                                                <p class="text-sm font-bold text-slate-900 truncate"><c:out value="${resume.resumeName}"/></p>
                                                                <!-- AI score badge injected here by JS -->
                                                                <div id="scoreBadge_${resume.resumeId}" class="mt-1"></div>
                                                            </div>
                                                        </label>
                                                    </c:forEach>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

                                        <div class="flex gap-3">
                                            <button type="button" onclick="closeApplyModal()" class="portal-btn portal-btn-secondary flex-1">
                                                ${language == 'zh' ? '取消' : 'Cancel'}
                                            </button>
                                            <button type="submit" class="portal-btn portal-btn-primary flex-1" <c:if test="${empty resumeList}">disabled="disabled"</c:if>>
                                                ${language == 'zh' ? '确认申请' : 'Confirm Apply'}
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            </div>

                            <script>
                                <%-- 与列表页传入的 vacancyId 一致，供 AI 排序等请求定位岗位 --%>
                                var JOB_ID = '${vacancy.vacancyId}';
                                var CTX    = '${pageContext.request.contextPath}';

                                function openApplyModal() {
                                    document.getElementById('applyModal').style.display = 'flex';
                                    fetchAIRankings();
                                }
                                function closeApplyModal() {
                                    document.getElementById('applyModal').style.display = 'none';
                                }

                                function fetchAIRankings() {
                                    if (!JOB_ID) return;

                                    var status = document.getElementById('aiRankStatus');
                                    var done   = document.getElementById('aiRankDone');
                                    if (status) status.style.setProperty('display', 'flex', 'important');
                                    if (done)   done.style.setProperty('display', 'none', 'important');

                                    var params = new URLSearchParams();
                                    params.append('jobId', JOB_ID);

                                    fetch(CTX + '/ai-resume-rank', {
                                        method: 'POST',
                                        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                                        body: params.toString()
                                    })
                                        .then(function(r) { return r.json(); })
                                        .then(function(data) {
                                            if (status) status.style.setProperty('display', 'none', 'important');
                                            if (!data.ok || !data.hasResumes) return;

                                            var hasRecommended = data.rankings.some(function(r) { return r.recommended; });
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
                                        .catch(function() {
                                            if (status) status.style.setProperty('display', 'none', 'important');
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
