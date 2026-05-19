package com.bupt.ta.web.servlet;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.i18n.I18n;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.service.ApplicationService;
import com.bupt.ta.util.ResumeFileUpload;
import com.bupt.ta.web.util.RedirectUrls;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Handles TA application submission with an existing resume or uploaded file.
 */
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 15,
        maxRequestSize = 1024 * 1024 * 20
)
@WebServlet("/application")
public class ApplicationSubmitServlet extends HttpServlet {
    private ApplicationService applicationService;
    private Path uploadDir;

    @Override
    public void init() throws ServletException {
        TaDatabase database = DatabaseProvider.get(getServletContext());
        this.applicationService = new ApplicationService(database);
        this.uploadDir = AppConfig.resolveDataDirectory().resolve("resumes").resolve("uploads");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session == null ? null : (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "auth.loginRequired"), StandardCharsets.UTF_8));
            return;
        }
        if (currentUser.getRole() != UserRole.TA) {
            resp.sendRedirect(req.getContextPath() + "/vacancies?errorMessage="
                    + URLEncoder.encode(I18n.message(req, "msg.appTaOnly"), StandardCharsets.UTF_8));
            return;
        }

        String vacancyIdStr = req.getParameter("vacancyId");
        String resumeIdStr = req.getParameter("resumeId");
        if (vacancyIdStr == null || vacancyIdStr.isBlank()) {
            redirectToVacancy(req, resp, vacancyIdStr,
                    "errorMessage", I18n.message(req, "msg.appDataMissing"));
            return;
        }

        UUID resumeId = null;
        if (resumeIdStr != null && !resumeIdStr.isBlank()) {
            try {
                resumeId = UUID.fromString(resumeIdStr.trim());
            } catch (IllegalArgumentException ex) {
                redirectToVacancy(req, resp, vacancyIdStr,
                        "errorMessage", I18n.message(req, "msg.appInvalidIds"));
                return;
            }
        }

        ResumeFileUpload.SavedResumeFile uploadedFile = null;
        try {
            Part filePart = req.getPart("resumeFile");
            if (filePart != null && filePart.getSize() > 0) {
                String originalName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                if (!ResumeFileUpload.isAllowedFileName(originalName)) {
                    redirectToVacancy(req, resp, vacancyIdStr,
                            "errorMessage", I18n.message(req, "msg.resumeUnsupportedType"));
                    return;
                }
                uploadedFile = ResumeFileUpload.save(
                        uploadDir,
                        currentUser.getId(),
                        originalName,
                        filePart.getContentType(),
                        filePart.getInputStream());
            }
        } catch (Exception ex) {
            redirectToVacancy(req, resp, vacancyIdStr,
                    "errorMessage", I18n.message(req, "msg.resumeUploadFailedPrefix") + ex.getMessage());
            return;
        }

        if (resumeId == null && uploadedFile == null) {
            redirectToVacancy(req, resp, vacancyIdStr,
                    "errorMessage", I18n.message(req, "msg.appResumeRequired"));
            return;
        }

        try {
            applicationService.submit(
                    currentUser.getId(),
                    resumeId,
                    UUID.fromString(vacancyIdStr.trim()),
                    req.getParameter("coverLetter"),
                    uploadedFile);
            resp.sendRedirect(RedirectUrls.withQueryParam(
                    req.getContextPath() + "/applications",
                    "successMessage",
                    I18n.message(req, "msg.appSubmitted")));
        } catch (IllegalArgumentException ex) {
            redirectToVacancy(req, resp, vacancyIdStr,
                    "errorMessage", I18n.message(req, "msg.appInvalidIds"));
        } catch (RuntimeException ex) {
            redirectToVacancy(req, resp, vacancyIdStr,
                    "errorMessage",
                    ex.getMessage() == null ? I18n.message(req, "msg.appSubmitFailed") : ex.getMessage());
        }
    }

    private void redirectToVacancy(HttpServletRequest req, HttpServletResponse resp, String vacancyIdStr,
                                  String paramName, String message) throws IOException {
        String base = req.getContextPath() + "/vacancy";
        if (vacancyIdStr != null && !vacancyIdStr.isBlank()) {
            base = RedirectUrls.withQueryParam(base, "vacancyId", vacancyIdStr.trim());
        }
        resp.sendRedirect(RedirectUrls.withQueryParam(base, paramName, message));
    }
}
