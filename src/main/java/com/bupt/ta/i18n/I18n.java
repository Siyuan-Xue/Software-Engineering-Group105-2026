package com.bupt.ta.i18n;

import com.bupt.ta.domain.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

public final class I18n {
    public static final String EN = "en";
    public static final String ZH = "zh";
    public static final String DEFAULT_LANGUAGE = EN;
    public static final String SESSION_LANGUAGE_ATTR = "language";
    public static final String LIGHT = "light";
    public static final String DARK = "dark";
    public static final String DEFAULT_APPEARANCE = LIGHT;
    public static final String SESSION_APPEARANCE_ATTR = "appearance";

    private static final Map<String, String> EN_MESSAGES = Map.ofEntries(
            Map.entry("common.studentUser", "Student User"),
            Map.entry("common.taApplicant", "TA Applicant"),
            Map.entry("common.dashboard", "Dashboard"),
            Map.entry("common.applications", "Applications"),
            Map.entry("common.resumes", "Resumes"),
            Map.entry("common.vacancies", "Vacancies"),
            Map.entry("common.messages", "Messages"),
            Map.entry("common.settings", "Settings"),
            Map.entry("common.workloads", "Workloads"),
            Map.entry("common.notifications", "Notifications"),
            Map.entry("common.language", "Language"),
            Map.entry("common.appearance", "Appearance"),
            Map.entry("common.light", "Light"),
            Map.entry("common.dark", "Dark"),
            Map.entry("common.english", "English"),
            Map.entry("common.chinese", "Chinese"),
            Map.entry("common.on", "On"),
            Map.entry("common.off", "Off"),
            Map.entry("common.email", "Email"),
            Map.entry("common.phone", "Phone"),
            Map.entry("common.studentId", "Student ID"),
            Map.entry("common.department", "Department"),
            Map.entry("common.bio", "Bio"),
            Map.entry("common.fullName", "Full Name"),
            Map.entry("common.currentPassword", "Current Password"),
            Map.entry("common.newPassword", "New Password"),
            Map.entry("common.confirmNewPassword", "Confirm New Password"),
            Map.entry("common.notProvided", "Not provided"),
            Map.entry("common.departmentNotSet", "Department not set"),
            Map.entry("common.activeAccount", "Active account"),
            Map.entry("common.save", "Save"),
            Map.entry("common.logout", "Logout"),
            Map.entry("common.searchVacancies", "Search vacancies..."),
            Map.entry("common.profileStrength", "Profile Strength"),

            Map.entry("settings.pageTitle", "Settings - QM HIRE"),
            Map.entry("settings.title", "Settings"),
            Map.entry("settings.copy", "Manage your profile, security settings, and communication preferences."),
            Map.entry("settings.editProfileTitle", "Edit Profile"),
            Map.entry("settings.editProfileCopy", "All changes are saved permanently to your account."),
            Map.entry("settings.phonePlaceholder", "+44 7700 900000"),
            Map.entry("settings.departmentPlaceholder", "e.g. Computer Science"),
            Map.entry("settings.studentIdPlaceholder", "e.g. 220012345"),
            Map.entry("settings.bioPlaceholder", "Tell us a little about yourself, your background, and teaching interests..."),
            Map.entry("settings.enableNotifications", "Enable notifications"),
            Map.entry("settings.notificationsCopy", "Recruiter replies, application updates, and reminders."),
            Map.entry("settings.saveProfile", "Save Profile"),
            Map.entry("settings.preferencesTitle", "Preferences"),
            Map.entry("settings.preferencesCopy", "Choose how the system should be displayed."),
            Map.entry("settings.languageCopy", "Choose your preferred interface language."),
            Map.entry("settings.appearanceCopy", "Choose your preferred color theme."),
            Map.entry("settings.savePreferences", "Save Preferences"),
            Map.entry("settings.notificationsCopyShort", "Updates and reminders"),
            Map.entry("settings.changePasswordTitle", "Change Password"),
            Map.entry("settings.changePasswordCopy", "Minimum 8 characters required."),
            Map.entry("settings.passwordMismatch", "Passwords do not match."),
            Map.entry("settings.updatePassword", "Update Password"),
            Map.entry("settings.signOutTitle", "Sign Out"),
            Map.entry("settings.signOutCopy", "End your current session securely."),
            Map.entry("settings.footer", "QM HIRE v1.2 · © 2026 Queen Mary University"),

            Map.entry("login.pageTitle", "Login - QM HIRE"),
            Map.entry("login.welcomeBack", "Welcome back"),
            Map.entry("login.copy", "Sign in to access your TA portal"),
            Map.entry("login.emailLabel", "University Email"),
            Map.entry("login.emailPlaceholder", "student@university.edu"),
            Map.entry("login.passwordLabel", "Password"),
            Map.entry("login.forgot", "Forgot?"),
            Map.entry("login.signIn", "SIGN IN"),
            Map.entry("login.noAccount", "Don't have an account?"),
            Map.entry("login.requestAccess", "Request access"),

            Map.entry("index.pageTitle", "QM HIRE - University TA Portal"),
            Map.entry("index.badge", "Phase 01 / Introduction"),
            Map.entry("index.heroLine1", "Your Impact"),
            Map.entry("index.heroLine2", "Starts Here."),
            Map.entry("index.copy", "Join our academic excellence. The University TA Recruitment System is your gateway to shaping the next generation of scholars."),
            Map.entry("index.login", "LOGIN TO PORTAL"),

            Map.entry("auth.loginRequired", "Please log in to access this page"),
            Map.entry("auth.emailPasswordRequired", "Email and password are required."),
            Map.entry("auth.invalidCredentials", "Invalid email or password. Please try again."),
            Map.entry("auth.loggedOut", "You have signed out successfully."),

            Map.entry("msg.profileUpdated", "Profile updated successfully."),
            Map.entry("msg.profileSaveFailed", "Failed to save profile."),
            Map.entry("msg.passwordChanged", "Password changed successfully."),
            Map.entry("msg.passwordChangeFailed", "Failed to change password."),
            Map.entry("msg.preferencesUpdated", "Preferences updated successfully."),
            Map.entry("msg.preferencesUpdateFailed", "Failed to update preferences."),
            Map.entry("msg.fullNameRequired", "Full name is required."),
            Map.entry("msg.currentPasswordRequired", "Current password is required."),
            Map.entry("msg.newPasswordRequired", "New password is required."),
            Map.entry("msg.confirmPasswordRequired", "Please confirm your new password."),
            Map.entry("msg.currentPasswordIncorrect", "Current password is incorrect."),
            Map.entry("msg.passwordMismatch", "New passwords do not match."),
            Map.entry("msg.passwordTooShort", "New password must be at least 8 characters."),
            Map.entry("msg.languageRequired", "Please choose a language."),
            Map.entry("msg.appearanceRequired", "Please choose an appearance."),
            Map.entry("msg.vacancyLoadFailed", "Failed to load vacancies. Please try again."),
            Map.entry("msg.vacancyTitleRequired", "Title is required."),
            Map.entry("msg.vacancyIdRequired", "Vacancy ID is required."),
            Map.entry("msg.vacancyDeadlineInvalid", "Invalid or missing deadline."),
            Map.entry("msg.vacancyCreated", "Vacancy created successfully."),
            Map.entry("msg.vacancyUpdated", "Vacancy updated successfully."),
            Map.entry("msg.appDataMissing", "Missing required application data."),
            Map.entry("msg.appSubmitted", "Application submitted successfully!"),
            Map.entry("msg.appInvalidIds", "Invalid vacancy or resume ID."),
            Map.entry("msg.appSubmitFailed", "Failed to submit application. Please try again later."),
            Map.entry("msg.resumeNoFile", "No file selected."),
            Map.entry("msg.resumeUnsupportedType", "Unsupported file type. Allowed: PDF, DOC, DOCX, JPG, PNG, TXT."),
            Map.entry("msg.resumeUploadFailedPrefix", "Upload failed: "),
            Map.entry("msg.resumeUploadNetworkFailed", "Upload failed (network error). Please try again."),
            Map.entry("msg.resumeUnknownActionPrefix", "Unknown action: "),
            Map.entry("msg.resumeIdRequired", "resumeId is required."),
            Map.entry("msg.resumeUntitled", "Untitled"),
            Map.entry("msg.resumeNotFound", "Resume not found."),
            Map.entry("msg.aiKeyMissing", "Qwen API key is not configured on this server. Please ask your administrator to add QWEN_API_KEY to Tomcat's setenv.bat."),
            Map.entry("msg.vacancyDetailLoadFailed", "Failed to load vacancy details. Please try again."),
            Map.entry("msg.applicationsLoadFailed", "Failed to load applications. Please try again.")
    );

    private static final Map<String, String> ZH_MESSAGES = Map.ofEntries(
            Map.entry("common.studentUser", "学生用户"),
            Map.entry("common.taApplicant", "助教申请者"),
            Map.entry("common.dashboard", "仪表盘"),
            Map.entry("common.applications", "申请记录"),
            Map.entry("common.resumes", "简历"),
            Map.entry("common.vacancies", "岗位"),
            Map.entry("common.messages", "消息"),
            Map.entry("common.settings", "设置"),
            Map.entry("common.workloads", "工作量"),
            Map.entry("common.notifications", "通知"),
            Map.entry("common.language", "语言"),
            Map.entry("common.appearance", "外观"),
            Map.entry("common.light", "浅色"),
            Map.entry("common.dark", "深色"),
            Map.entry("common.english", "英文"),
            Map.entry("common.chinese", "中文"),
            Map.entry("common.on", "开启"),
            Map.entry("common.off", "关闭"),
            Map.entry("common.email", "邮箱"),
            Map.entry("common.phone", "电话"),
            Map.entry("common.studentId", "学号"),
            Map.entry("common.department", "院系"),
            Map.entry("common.bio", "个人简介"),
            Map.entry("common.fullName", "姓名"),
            Map.entry("common.currentPassword", "当前密码"),
            Map.entry("common.newPassword", "新密码"),
            Map.entry("common.confirmNewPassword", "确认新密码"),
            Map.entry("common.notProvided", "未提供"),
            Map.entry("common.departmentNotSet", "未设置院系"),
            Map.entry("common.activeAccount", "账号正常"),
            Map.entry("common.save", "保存"),
            Map.entry("common.logout", "退出登录"),
            Map.entry("common.searchVacancies", "搜索岗位..."),
            Map.entry("common.profileStrength", "资料完整度"),

            Map.entry("settings.pageTitle", "设置 - QM HIRE"),
            Map.entry("settings.title", "设置"),
            Map.entry("settings.copy", "管理你的个人资料、安全设置和沟通偏好。"),
            Map.entry("settings.editProfileTitle", "编辑资料"),
            Map.entry("settings.editProfileCopy", "所有修改都会永久保存到你的账号。"),
            Map.entry("settings.phonePlaceholder", "+86 138 0000 0000"),
            Map.entry("settings.departmentPlaceholder", "例如：计算机学院"),
            Map.entry("settings.studentIdPlaceholder", "例如：220012345"),
            Map.entry("settings.bioPlaceholder", "简单介绍一下你的背景、经历和教学兴趣..."),
            Map.entry("settings.enableNotifications", "开启通知"),
            Map.entry("settings.notificationsCopy", "招聘方回复、申请更新和提醒通知。"),
            Map.entry("settings.saveProfile", "保存资料"),
            Map.entry("settings.preferencesTitle", "偏好设置"),
            Map.entry("settings.preferencesCopy", "选择系统界面的显示方式。"),
            Map.entry("settings.languageCopy", "选择你偏好的界面语言。"),
            Map.entry("settings.appearanceCopy", "选择你偏好的主题颜色。"),
            Map.entry("settings.savePreferences", "保存偏好"),
            Map.entry("settings.notificationsCopyShort", "更新与提醒"),
            Map.entry("settings.changePasswordTitle", "修改密码"),
            Map.entry("settings.changePasswordCopy", "密码至少需要 8 位。"),
            Map.entry("settings.passwordMismatch", "两次输入的密码不一致。"),
            Map.entry("settings.updatePassword", "更新密码"),
            Map.entry("settings.signOutTitle", "退出登录"),
            Map.entry("settings.signOutCopy", "安全结束当前会话。"),
            Map.entry("settings.footer", "QM HIRE v1.2 · © 2026 英国伦敦玛丽女王大学"),

            Map.entry("login.pageTitle", "登录 - QM HIRE"),
            Map.entry("login.welcomeBack", "欢迎回来"),
            Map.entry("login.copy", "登录以进入你的助教招聘系统"),
            Map.entry("login.emailLabel", "学校邮箱"),
            Map.entry("login.emailPlaceholder", "student@university.edu"),
            Map.entry("login.passwordLabel", "密码"),
            Map.entry("login.forgot", "忘记密码？"),
            Map.entry("login.signIn", "登 录"),
            Map.entry("login.noAccount", "还没有账号？"),
            Map.entry("login.requestAccess", "申请访问"),

            Map.entry("index.pageTitle", "QM HIRE - 大学助教门户"),
            Map.entry("index.badge", "阶段 01 / 介绍"),
            Map.entry("index.heroLine1", "你的影响力"),
            Map.entry("index.heroLine2", "从这里开始。"),
            Map.entry("index.copy", "加入我们的学术卓越之旅。大学助教招聘系统将帮助你参与培养下一代学者。"),
            Map.entry("index.login", "进入系统"),

            Map.entry("auth.loginRequired", "请先登录后再访问此页面"),
            Map.entry("auth.emailPasswordRequired", "邮箱和密码不能为空。"),
            Map.entry("auth.invalidCredentials", "邮箱或密码错误，请重试。"),
            Map.entry("auth.loggedOut", "你已成功退出登录。"),

            Map.entry("msg.profileUpdated", "个人资料已更新。"),
            Map.entry("msg.profileSaveFailed", "保存个人资料失败。"),
            Map.entry("msg.passwordChanged", "密码修改成功。"),
            Map.entry("msg.passwordChangeFailed", "修改密码失败。"),
            Map.entry("msg.preferencesUpdated", "偏好设置已更新。"),
            Map.entry("msg.preferencesUpdateFailed", "更新偏好设置失败。"),
            Map.entry("msg.fullNameRequired", "姓名不能为空。"),
            Map.entry("msg.currentPasswordRequired", "当前密码不能为空。"),
            Map.entry("msg.newPasswordRequired", "新密码不能为空。"),
            Map.entry("msg.confirmPasswordRequired", "请确认你的新密码。"),
            Map.entry("msg.currentPasswordIncorrect", "当前密码不正确。"),
            Map.entry("msg.passwordMismatch", "两次输入的新密码不一致。"),
            Map.entry("msg.passwordTooShort", "新密码至少需要 8 位。"),
            Map.entry("msg.languageRequired", "请选择一种语言。"),
            Map.entry("msg.appearanceRequired", "请选择一种外观模式。"),
            Map.entry("msg.vacancyLoadFailed", "岗位列表加载失败，请重试。"),
            Map.entry("msg.vacancyTitleRequired", "标题不能为空。"),
            Map.entry("msg.vacancyIdRequired", "缺少岗位编号。"),
            Map.entry("msg.vacancyDeadlineInvalid", "截止时间无效或缺失。"),
            Map.entry("msg.vacancyCreated", "岗位创建成功。"),
            Map.entry("msg.vacancyUpdated", "岗位更新成功。"),
            Map.entry("msg.appDataMissing", "缺少必要的申请信息。"),
            Map.entry("msg.appSubmitted", "申请提交成功！"),
            Map.entry("msg.appInvalidIds", "岗位或简历编号无效。"),
            Map.entry("msg.appSubmitFailed", "提交申请失败，请稍后再试。"),
            Map.entry("msg.resumeNoFile", "未选择文件。"),
            Map.entry("msg.resumeUnsupportedType", "不支持的文件类型。允许：PDF、DOC、DOCX、JPG、PNG、TXT。"),
            Map.entry("msg.resumeUploadFailedPrefix", "上传失败："),
            Map.entry("msg.resumeUploadNetworkFailed", "上传失败（网络错误），请重试。"),
            Map.entry("msg.resumeUnknownActionPrefix", "未知操作："),
            Map.entry("msg.resumeIdRequired", "缺少简历编号。"),
            Map.entry("msg.resumeUntitled", "未命名"),
            Map.entry("msg.resumeNotFound", "未找到简历。"),
            Map.entry("msg.aiKeyMissing", "服务器未配置 QWEN_API_KEY，请联系管理员在 Tomcat 的 setenv.bat 中添加。"),
            Map.entry("msg.vacancyDetailLoadFailed", "岗位详情加载失败，请重试。"),
            Map.entry("msg.applicationsLoadFailed", "申请列表加载失败，请重试。")
    );

    private I18n() {
    }

    public static String normalizeLanguage(String language) {
        return ZH.equalsIgnoreCase(language) ? ZH : EN;
    }

    public static String normalizeAppearance(String appearance) {
        return DARK.equalsIgnoreCase(appearance) ? DARK : LIGHT;
    }

    public static boolean isChinese(String language) {
        return ZH.equals(normalizeLanguage(language));
    }

    public static String langTag(String language) {
        return isChinese(language) ? "zh-CN" : "en";
    }

    public static Map<String, String> messagesFor(String language) {
        return isChinese(language) ? ZH_MESSAGES : EN_MESSAGES;
    }

    public static String message(String language, String key) {
        return messagesFor(language).getOrDefault(key, EN_MESSAGES.getOrDefault(key, key));
    }

    public static String resolveLanguage(HttpServletRequest req) {
        if (req == null) return DEFAULT_LANGUAGE;

        Object requestLanguage = req.getAttribute("language");
        if (requestLanguage instanceof String value) {
            return normalizeLanguage(value);
        }

        HttpSession session = req.getSession(false);
        if (session != null) {
            Object currentUser = session.getAttribute("currentUser");
            if (currentUser instanceof User user && user.getPreferredLanguage() != null) {
                return normalizeLanguage(user.getPreferredLanguage());
            }
            Object sessionLanguage = session.getAttribute(SESSION_LANGUAGE_ATTR);
            if (sessionLanguage instanceof String value) {
                return normalizeLanguage(value);
            }
        }

        return DEFAULT_LANGUAGE;
    }

    public static String resolveAppearance(HttpServletRequest req) {
        if (req == null) return DEFAULT_APPEARANCE;

        Object requestAppearance = req.getAttribute("appearance");
        if (requestAppearance instanceof String value) {
            return normalizeAppearance(value);
        }

        HttpSession session = req.getSession(false);
        if (session != null) {
            Object currentUser = session.getAttribute("currentUser");
            if (currentUser instanceof User user && user.getPreferredAppearance() != null) {
                return normalizeAppearance(user.getPreferredAppearance());
            }
            Object sessionAppearance = session.getAttribute(SESSION_APPEARANCE_ATTR);
            if (sessionAppearance instanceof String value) {
                return normalizeAppearance(value);
            }
        }

        return DEFAULT_APPEARANCE;
    }

    public static String message(HttpServletRequest req, String key) {
        return message(resolveLanguage(req), key);
    }
}
