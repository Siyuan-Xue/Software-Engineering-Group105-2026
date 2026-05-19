<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="${langTag}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${i18n['register.pageTitle']}</title>
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
<body data-theme="${appearance}" class="bg-background-light font-sans text-slate-900 min-h-screen flex items-center justify-center p-4">
    <div class="w-full max-w-lg bg-white rounded-3xl shadow-xl border border-slate-100 p-8 relative overflow-hidden">
        <div class="absolute top-0 right-0 w-32 h-32 bg-accent/10 rounded-bl-full -z-10"></div>

        <div class="flex items-center gap-3 mb-8">
            <div class="flex items-center justify-center w-10 h-10 rounded-xl bg-primary text-white shadow-md">
                <span class="material-symbols-outlined text-2xl">person_add</span>
            </div>
            <div class="flex items-center gap-1">
                <span class="font-black text-2xl tracking-tighter text-primary">QM</span>
                <span class="font-light text-2xl tracking-tight text-slate-500">HIRE</span>
            </div>
        </div>

        <h2 class="text-3xl font-black mb-2">${i18n['register.title']}</h2>
        <p class="text-slate-500 mb-6">${i18n['register.copy']}</p>

        <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
            <jsp:param name="containerClass" value="mb-6" />
        </jsp:include>

        <form action="${pageContext.request.contextPath}/register" method="POST" class="space-y-4">
            <div>
                <label class="block text-xs font-black text-slate-400 uppercase tracking-wider mb-2">${i18n['common.fullName']}</label>
                <input type="text" name="fullName" value="<c:out value='${fullName}'/>"
                       class="w-full bg-slate-50 border border-slate-200 rounded-xl py-3 px-4 focus:ring-2 focus:ring-accent/20 focus:border-accent outline-none transition-all"
                       placeholder="${i18n['common.fullName']}" autocomplete="name" required />
            </div>
            <input type="hidden" name="role" value="TA" />
            <p class="rounded-xl border border-blue-100 bg-blue-50 px-4 py-3 text-xs font-semibold text-blue-700">
                ${language == 'zh' ? '自助注册仅创建 TA / 学生申请者账号；MO 和 Admin 账号请由管理员在系统内创建。' : 'Self-registration creates TA / student applicant accounts only. MO and Admin accounts are created by an administrator inside the system.'}
            </p>
            <div>
                <label class="block text-xs font-black text-slate-400 uppercase tracking-wider mb-2">${i18n['login.emailLabel']}</label>
                <div class="relative">
                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">mail</span>
                    <input type="email" name="email" value="<c:out value='${email}'/>"
                           class="w-full bg-slate-50 border border-slate-200 rounded-xl py-3 pl-10 pr-4 focus:ring-2 focus:ring-accent/20 focus:border-accent outline-none transition-all"
                           placeholder="${i18n['login.emailPlaceholder']}" autocomplete="email" required />
                </div>
            </div>
            <div>
                <label class="block text-xs font-black text-slate-400 uppercase tracking-wider mb-2">${i18n['login.passwordLabel']}</label>
                <div class="relative">
                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">lock</span>
                    <input type="password" name="password" minlength="8"
                           class="ta-pw-min8 w-full bg-slate-50 border border-slate-200 rounded-xl py-3 pl-10 pr-4 focus:ring-2 focus:ring-accent/20 focus:border-accent outline-none transition-all"
                           placeholder="••••••••" autocomplete="new-password" required />
                </div>
                <p class="text-xs text-slate-400 mt-1">${i18n['settings.changePasswordCopy']}</p>
            </div>
            <div>
                <label class="block text-xs font-black text-slate-400 uppercase tracking-wider mb-2">${i18n['common.confirmNewPassword']}</label>
                <div class="relative">
                    <span class="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">lock_reset</span>
                    <input type="password" name="confirmPassword" minlength="8"
                           class="ta-pw-min8 w-full bg-slate-50 border border-slate-200 rounded-xl py-3 pl-10 pr-4 focus:ring-2 focus:ring-accent/20 focus:border-accent outline-none transition-all"
                           placeholder="••••••••" autocomplete="new-password" required />
                </div>
            </div>
            <div>
                <label class="block text-xs font-black text-slate-400 uppercase tracking-wider mb-2">${i18n['common.phone']}</label>
                <input type="text" name="phone" value="<c:out value='${phone}'/>"
                       class="w-full bg-slate-50 border border-slate-200 rounded-xl py-3 px-4 focus:ring-2 focus:ring-accent/20 focus:border-accent outline-none transition-all"
                       placeholder="${i18n['settings.phonePlaceholder']}" autocomplete="tel" />
            </div>
            <div>
                <label class="block text-xs font-black text-slate-400 uppercase tracking-wider mb-2">${i18n['common.department']}</label>
                <input type="text" name="department" value="<c:out value='${department}'/>"
                       class="w-full bg-slate-50 border border-slate-200 rounded-xl py-3 px-4 focus:ring-2 focus:ring-accent/20 focus:border-accent outline-none transition-all"
                       placeholder="${i18n['settings.departmentPlaceholder']}" />
            </div>
            <div>
                <label class="block text-xs font-black text-slate-400 uppercase tracking-wider mb-2">${i18n['common.studentId']}</label>
                <input type="text" name="studentId" value="<c:out value='${studentId}'/>"
                       class="w-full bg-slate-50 border border-slate-200 rounded-xl py-3 px-4 focus:ring-2 focus:ring-accent/20 focus:border-accent outline-none transition-all"
                       placeholder="${i18n['settings.studentIdPlaceholder']}" />
            </div>

            <button type="submit" class="w-full bg-primary text-white font-black py-4 rounded-xl shadow-lg shadow-primary/20 hover:scale-[1.02] active:scale-[0.98] transition-all flex items-center justify-center gap-2 mt-4">
                ${i18n['register.submit']}
                <span class="material-symbols-outlined text-sm">arrow_forward</span>
            </button>
        </form>

        <div class="mt-8 text-center">
            <a href="${pageContext.request.contextPath}/login" class="text-sm font-bold text-accent hover:underline">${i18n['register.backToLogin']}</a>
        </div>
    </div>
    <script src="${pageContext.request.contextPath}/js/password-minlength-en.js"></script>
</body>
</html>
