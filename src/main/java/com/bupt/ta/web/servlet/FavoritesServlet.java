package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.db.repository.UserRepository;
import com.bupt.ta.domain.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JSON API for toggling saved/favorite vacancies.
 * POST /favorites?vacancyId={uuid}  →  {"saved": true/false, "count": N}
 */
@WebServlet("/favorites")
public class FavoritesServlet extends HttpServlet {

    private UserRepository userRepository;

    @Override
    public void init() throws ServletException {
        TaDatabase database = DatabaseProvider.get(getServletContext());
        this.userRepository = database.users();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        User currentUser = resolveCurrentUser(req.getSession(false));
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Not authenticated\"}");
            return;
        }

        UUID vacancyId = parseUUID(req.getParameter("vacancyId"));
        if (vacancyId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Missing vacancyId\"}");
            return;
        }

        try {
            User freshUser = userRepository.findById(currentUser.getId()).orElse(null);
            if (freshUser == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"error\":\"User not found\"}");
                return;
            }

            Set<UUID> saved = new HashSet<>(freshUser.getSavedJobIds());
            boolean nowSaved;
            if (saved.contains(vacancyId)) {
                saved.remove(vacancyId);
                nowSaved = false;
            } else {
                saved.add(vacancyId);
                nowSaved = true;
            }
            freshUser.setSavedJobIds(saved);
            User updated = userRepository.save(freshUser);

            // Keep the session in sync
            HttpSession session = req.getSession(false);
            if (session != null) {
                Object sessionUser = session.getAttribute("currentUser");
                if (sessionUser instanceof User su) {
                    su.setSavedJobIds(updated.getSavedJobIds());
                }
            }

            int count = updated.getSavedJobIds().size();
            resp.getWriter().write("{\"saved\":" + nowSaved + ",\"count\":" + count + "}");
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Server error\"}");
        }
    }

    /** GET /favorites - returns the current user's saved vacancy IDs as JSON array */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        User currentUser = resolveCurrentUser(req.getSession(false));
        if (currentUser == null) {
            resp.getWriter().write("[]");
            return;
        }

        User freshUser = userRepository.findById(currentUser.getId()).orElse(currentUser);
        Set<UUID> saved = freshUser.getSavedJobIds();

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (UUID id : saved) {
            if (!first) sb.append(",");
            sb.append("\"").append(id).append("\"");
            first = false;
        }
        sb.append("]");
        resp.getWriter().write(sb.toString());
    }

    private User resolveCurrentUser(HttpSession session) {
        if (session == null) return null;
        Object value = session.getAttribute("currentUser");
        return value instanceof User u ? u : null;
    }

    private UUID parseUUID(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
