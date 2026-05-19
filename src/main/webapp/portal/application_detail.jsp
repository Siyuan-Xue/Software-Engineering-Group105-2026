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

                        <c:if test="${canTaRespond}">
                            <section class="rounded-2xl border border-amber-200 bg-amber-50 p-5">
                                <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                                    <div>
                                        <h3 class="text-sm font-black text-amber-950">${language == 'zh' ? '你收到了录用 offer' : 'You received an offer'}</h3>
                                        <p class="mt-1 text-sm text-amber-900">${language == 'zh' ? '接受后系统会生成你的工作量记录；拒绝则不会生成工作量。' : 'Accepting creates your workload record; declining leaves workload unchanged.'}</p>
                                    </div>
                                    <div class="flex gap-2">
                                        <form action="${pageContext.request.contextPath}/application/decision" method="POST">
                                            <input type="hidden" name="applicationId" value="${applicationId}"/>
                                            <input type="hidden" name="action" value="accept"/>
                                            <input type="hidden" name="returnTo" value="detail"/>
                                            <button type="submit" class="portal-btn portal-btn-primary text-sm">${language == 'zh' ? '接受 offer' : 'Accept offer'}</button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/application/decision" method="POST" onsubmit="return confirm('${language == 'zh' ? '确定拒绝该录用？' : 'Decline this offer?'}');">
                                            <input type="hidden" name="applicationId" value="${applicationId}"/>
                                            <input type="hidden" name="action" value="decline"/>
                                            <input type="hidden" name="returnTo" value="detail"/>
                                            <button type="submit" class="portal-btn portal-btn-secondary text-sm text-red-700">${language == 'zh' ? '拒绝' : 'Decline'}</button>
                                        </form>
                                    </div>
                                </div>
                            </section>
                        </c:if>

                        <section class="border-t border-slate-100 pt-6">
                            <c:choose>
                                <c:when test="${canMoManage}">
                                    <div class="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                                        <div>
                                            <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500">${language == 'zh' ? '匹配分析' : 'Match analysis'}</h3>
                                            <p class="mt-1 text-xs text-slate-500">${language == 'zh' ? '仅 MO 可见。显示规则分、AI/fallback 分、最终分和建议；AI 不可用时使用规则分兜底。' : 'MO-only view. Shows rule score, AI/fallback score, final score, and recommendation; rule scoring is used when AI is unavailable.'}</p>
                                        </div>
                                        <c:choose>
                                            <c:when test="${canRunMatchAnalysis}">
                                                <form action="${pageContext.request.contextPath}/match-analysis" method="POST">
                                                    <input type="hidden" name="applicationId" value="${applicationId}"/>
                                                    <button type="submit" class="portal-btn portal-btn-secondary text-sm">
                                                        <span class="material-symbols-outlined text-sm">analytics</span>
                                                        ${language == 'zh' ? '刷新分析' : 'Refresh analysis'}
                                                    </button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="rounded-lg bg-slate-100 px-3 py-2 text-xs font-bold text-slate-500">
                                                    ${language == 'zh' ? '开始审核后可刷新分析' : 'Start review to refresh analysis'}
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <c:if test="${not empty skillCoverage}">
                                        <div class="mb-4 rounded-xl border ${skillCoverage.lowCoverageWarning ? 'border-amber-200 bg-amber-50 text-amber-900' : 'border-emerald-100 bg-emerald-50 text-emerald-900'} p-4 text-sm">
                                            <div class="font-bold">
                                                ${language == 'zh' ? '规则技能覆盖' : 'Rule-based skill coverage'}:
                                                <c:out value="${skillCoverage.requiredMatched}"/>/<c:out value="${skillCoverage.requiredTotal}"/>
                                                ${language == 'zh' ? '必需' : 'required'}
                                                (<c:out value="${skillCoverage.requiredCoveragePct}"/>%),
                                                <c:out value="${skillCoverage.overallCoveragePct}"/>% ${language == 'zh' ? '总体' : 'overall'}.
                                            </div>
                                            <c:if test="${not empty skillCoverage.missingRequiredSkills}">
                                                <div class="mt-2 flex flex-wrap gap-2">
                                                    <c:forEach items="${skillCoverage.missingRequiredSkills}" var="missingSkill">
                                                        <span class="rounded-full bg-white/70 px-3 py-1 text-xs font-bold text-red-700"><c:out value="${missingSkill}"/></span>
                                                    </c:forEach>
                                                </div>
                                            </c:if>
                                        </div>
                                    </c:if>
                                    <c:choose>
                                        <c:when test="${not empty matchScore}">
                                            <div class="grid gap-3 sm:grid-cols-3">
                                                <div class="rounded-xl border border-slate-100 bg-slate-50 p-4">
                                                    <p class="text-[11px] font-bold uppercase tracking-wider text-slate-400">${language == 'zh' ? '规则分' : 'Rule score'}</p>
                                                    <p class="mt-1 text-2xl font-black text-slate-900"><c:out value="${matchScore.ruleScore}"/></p>
                                                </div>
                                                <div class="rounded-xl border border-slate-100 bg-slate-50 p-4">
                                                    <p class="text-[11px] font-bold uppercase tracking-wider text-slate-400">${language == 'zh' ? 'AI/Fallback 分' : 'AI/Fallback score'}</p>
                                                    <p class="mt-1 text-2xl font-black text-slate-900"><c:out value="${matchScore.aiScore}"/></p>
                                                </div>
                                                <div class="rounded-xl border border-slate-100 bg-slate-50 p-4">
                                                    <p class="text-[11px] font-bold uppercase tracking-wider text-slate-400">${language == 'zh' ? '最终分' : 'Final score'}</p>
                                                    <p class="mt-1 text-2xl font-black ${matchScore.aiRecommend ? 'text-green-700' : 'text-amber-700'}"><c:out value="${matchScore.finalScore}"/></p>
                                                </div>
                                            </div>
                                            <div class="mt-4 grid gap-3 sm:grid-cols-4">
                                                <div class="text-sm text-slate-600">${language == 'zh' ? '技能覆盖' : 'Skill coverage'}: <strong><c:out value="${matchScore.skillCoveragePct}"/>%</strong></div>
                                                <div class="text-sm text-slate-600">${language == 'zh' ? '缺失必需技能' : 'Missing required'}: <strong><c:out value="${matchScore.missingRequiredCount}"/></strong></div>
                                                <div class="text-sm text-slate-600">${language == 'zh' ? '剩余工时' : 'Remaining hours'}: <strong><c:out value="${matchScore.workloadRemainingHours}"/></strong></div>
                                                <div class="text-sm text-slate-600">
                                                    ${language == 'zh' ? '建议' : 'Recommendation'}:
                                                    <strong>
                                                        <c:choose>
                                                            <c:when test="${matchScore.aiRecommend}">
                                                                ${language == 'zh' ? '推荐' : 'Recommend'}
                                                            </c:when>
                                                            <c:otherwise>
                                                                ${language == 'zh' ? '谨慎' : 'Caution'}
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </strong>
                                                </div>
                                            </div>
                                            <p class="mt-4 rounded-xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900"><c:out value="${matchScore.aiExplanation}"/></p>
                                            <c:if test="${not empty matchScore.missingSkillSuggestions}">
                                                <div class="mt-4">
                                                    <p class="mb-2 text-xs font-bold uppercase tracking-wider text-slate-500">${language == 'zh' ? '技能缺口' : 'Skill gaps'}</p>
                                                    <div class="flex flex-wrap gap-2">
                                                        <c:forEach items="${matchScore.missingSkillSuggestions}" var="missingSkill">
                                                            <span class="rounded-full bg-red-50 px-3 py-1 text-xs font-bold text-red-700"><c:out value="${missingSkill}"/></span>
                                                        </c:forEach>
                                                    </div>
                                                </div>
                                            </c:if>
                                            <p class="mt-3 text-xs text-slate-400">${language == 'zh' ? '更新时间' : 'Updated'}: <c:out value="${matchComputedAt}"/></p>
                                        </c:when>
                                        <c:otherwise>
                                            <p class="rounded-xl border border-slate-100 bg-slate-50 p-4 text-sm text-slate-500">${language == 'zh' ? '尚未生成持久化分析。将申请标记为审核中后，可刷新并写入 match_scores.json。' : 'No persisted analysis yet. Start review, then refresh to save results to match_scores.json.'}</p>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <div class="mb-4">
                                        <h3 class="text-xs font-bold uppercase tracking-wider text-slate-500">${language == 'zh' ? '技能反馈' : 'Skill feedback'}</h3>
                                        <p class="mt-1 text-xs text-slate-500">${language == 'zh' ? '仅显示可行动的技能缺口；评分与录用建议仅供 MO 审核使用。' : 'Only actionable skill gaps are shown here; scores and hiring recommendations are reserved for MO review.'}</p>
                                    </div>
                                    <c:choose>
                                        <c:when test="${taFeedbackAvailable}">
                                            <c:choose>
                                                <c:when test="${not empty taSkillGapSuggestions}">
                                                    <div class="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
                                                        <p class="font-bold">${language == 'zh' ? '建议补充或提升这些技能：' : 'Consider improving or evidencing these skills:'}</p>
                                                        <div class="mt-3 flex flex-wrap gap-2">
                                                            <c:forEach items="${taSkillGapSuggestions}" var="missingSkill">
                                                                <span class="rounded-full bg-white/80 px-3 py-1 text-xs font-bold text-amber-800"><c:out value="${missingSkill}"/></span>
                                                            </c:forEach>
                                                        </div>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <p class="rounded-xl border border-emerald-100 bg-emerald-50 p-4 text-sm font-semibold text-emerald-800">${language == 'zh' ? '当前未发现必需技能缺口。' : 'No required skill gaps were identified.'}</p>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <p class="rounded-xl border border-slate-100 bg-slate-50 p-4 text-sm text-slate-500">${language == 'zh' ? '暂时没有反馈。MO 运行匹配分析后，这里会显示可行动的技能缺口。' : 'No feedback is available yet. Skill gaps will appear here after the MO refreshes the match analysis.'}</p>
                                        </c:otherwise>
                                    </c:choose>
                                </c:otherwise>
                            </c:choose>
                        </section>

                        <c:if test="${canMoManage && applicationStatus != 'Withdrawn' && applicationStatus != 'Accepted' && applicationStatus != 'Rejected' && applicationStatus != 'Declined'}">
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
                                        <form action="${pageContext.request.contextPath}/application/decision" method="POST" onsubmit="return confirm('${language == 'zh' ? '向该申请人发送 offer？TA 接受后才会生成工作量。' : 'Send an offer? Workload is created only after the TA accepts.'}');">
                                            <input type="hidden" name="applicationId" value="${applicationId}"/>
                                            <input type="hidden" name="action" value="offer"/>
                                            <input type="hidden" name="returnTo" value="detail"/>
                                            <button type="submit" class="portal-btn portal-btn-primary text-sm">${language == 'zh' ? '发 offer' : 'Send offer'}</button>
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
