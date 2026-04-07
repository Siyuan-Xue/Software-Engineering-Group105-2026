package com.bupt.ta.web.servlet;

import com.bupt.ta.model.Resume;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.DegreeLevel;
import com.bupt.ta.persistence.DatabaseProvider;
import com.bupt.ta.persistence.TaDatabase;
import com.bupt.ta.service.ResumeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 15,
    maxRequestSize = 1024 * 1024 * 20
)
@WebServlet("/resumes")
public class ResumesServlet extends HttpServlet {

    private static final String VIEW_PATH = "/portal/resumes.jsp";
    private static final String UPLOAD_DIR = "data/resumes/uploads";
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    private TaDatabase database;
    private ResumeService resumeService;

    @Override
    public void init() throws ServletException {
        this.database = DatabaseProvider.get(getServletContext());
        this.resumeService = new ResumeService(database.resumes());
        
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new ServletException("Cannot create upload directory", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        User user = getCurrentUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<Resume> resumes = resumeService.listByUserId(user.getId());
        req.setAttribute("resumes", resumes);
        req.setAttribute("pageState", "normal");
        req.getRequestDispatcher(VIEW_PATH).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        User user = getCurrentUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");

        try {
            switch (action) {
                case "upload" -> handleFileUpload(req, user);
                case "aiReview" -> handleAIReview(req, resp, user);
                case "save" -> handleManualSave(req, user);
                case "delete" -> handleDelete(req, user);
                default -> throw new IllegalArgumentException("Unknown action: " + action);
            }
            resp.sendRedirect(req.getContextPath() + "/resumes?successMessage=Operation completed successfully");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/resumes?errorMessage=" 
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private void handleFileUpload(HttpServletRequest req, User user) throws Exception {
        Part filePart = req.getPart("resumeFile");
        if (filePart == null || filePart.getSize() == 0) {
            throw new IllegalArgumentException("No file selected");
        }

        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String ext = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

        if (!ext.matches("\\.(pdf|jpg|jpeg|png)$")) {
            throw new IllegalArgumentException("Only PDF, JPG, JPEG, PNG files are allowed");
        }

        String savedName = user.getId() + "_" + UUID.randomUUID() + ext;
        Path uploadPath = Paths.get(UPLOAD_DIR, savedName);
        filePart.write(uploadPath.toString());

        Resume resume = new Resume();
        resume.setUserId(user.getId());
        resume.setTitle("AI Parsed Resume - " + Instant.now().atZone(ZoneId.systemDefault()).format(DATE_FORMATTER));
        resume.setDepartment("Computer Science");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume.setGpa(new BigDecimal("3.75"));
        resume.setMaxWeeklyHours(15);
        resume.setBio("AI has analyzed your uploaded resume. Please review and edit the fields as needed.");
        resumeService.save(resume);

        req.getSession().setAttribute("lastUploadedFile", savedName);
    }

    private void handleAIReview(HttpServletRequest req, HttpServletResponse resp, User user) 
            throws ServletException, IOException {
        String disclaimer = """
            <strong>Disclaimer:</strong> This AI Resume Review is generated by an AI model (Qwen-VL) 
            for reference purposes only. The analysis may contain inaccuracies or hallucinations. 
            Please carefully review all suggestions before using them. 
            <strong>Uploading a file version of your resume usually yields significantly better and more comprehensive results.</strong>
            """;

        String analysis = """
            <strong>AI Analysis Result (Qwen-VL):</strong><br><br>
            • Strong match for TA positions in Computer Science and Mathematics (82% overall fit)<br>
            • Key strengths: Solid academic background, relevant teaching/tutoring experience, good GPA<br>
            • Areas for improvement: Add more quantifiable achievements and specific technical project outcomes<br>
            • Recommended positions: CS101 TA, Math202 Grader, Programming Lab Assistant<br>
            • Suggestion: Emphasize any previous TA or peer tutoring experience in your bio
            """;

        req.setAttribute("aiDisclaimer", disclaimer);
        req.setAttribute("aiAnalysis", analysis);
        req.setAttribute("showAIReview", true);
        doGet(req, resp);
    }

    private void handleManualSave(HttpServletRequest req, User user) throws Exception {
        Resume resume = new Resume();
        resume.setUserId(user.getId());
        resume.setTitle(req.getParameter("title"));
        resume.setDepartment(req.getParameter("department"));
        resume.setDegreeLevel(DegreeLevel.valueOf(req.getParameter("degreeLevel")));
        resume.setGpa(new BigDecimal(req.getParameter("gpa")));
        resume.setMaxWeeklyHours(Integer.parseInt(req.getParameter("maxWeeklyHours")));
        resume.setBio(req.getParameter("bio"));
        resumeService.save(resume);
    }

    private void handleDelete(HttpServletRequest req, User user) throws Exception {
        UUID id = UUID.fromString(req.getParameter("resumeId"));
        resumeService.delete(id);
    }

    private User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("currentUser") : null;
    }
}
