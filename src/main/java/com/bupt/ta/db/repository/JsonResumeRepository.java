package com.bupt.ta.db.repository;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.db.store.JsonTableStore;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class JsonResumeRepository extends BaseJsonRepository<Resume> implements ResumeRepository {
    public JsonResumeRepository(JsonTableStore<Resume> store) {
        super(store);
    }

    @Override
    public List<Resume> findAll() {
        return super.findAll().stream()
                .sorted(Comparator.comparing(Resume::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Override
    public List<Resume> listByUserId(UUID userId) {
        return findAll().stream()
                .filter(resume -> userId.equals(resume.getUserId()))
                .toList();
    }

    @Override
    public Resume duplicate(UUID resumeId) {
        Resume original = findById(resumeId)
                .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + resumeId));
        Resume duplicate = new Resume();
        duplicate.setUserId(original.getUserId());
        duplicate.setTitle(uniqueTitle(original.getUserId(), original.getTitle() + " Copy", null));
        duplicate.setDepartment(original.getDepartment());
        duplicate.setDegreeLevel(original.getDegreeLevel());
        duplicate.setGpa(original.getGpa());
        duplicate.setBio(original.getBio());
        duplicate.setMaxWeeklyHours(original.getMaxWeeklyHours());
        duplicate.setAvailabilitySlots(original.getAvailabilitySlots());
        duplicate.setUploadedFilePath(original.getUploadedFilePath());
        duplicate.setOriginalFileName(original.getOriginalFileName());
        duplicate.setLabels(original.getLabels());
        return save(duplicate);
    }

    @Override
    public Resume save(Resume entity) {
        if (entity.getUserId() == null) {
            throw new ConstraintViolationException("Resume userId must not be null");
        }
        if (entity.getTitle() == null || entity.getTitle().isBlank()) {
            throw new ConstraintViolationException("Resume title must not be blank");
        }
        String normalizedTitle = entity.getTitle().trim();
        boolean duplicateTitle = findAll().stream()
                .filter(resume -> entity.getUserId().equals(resume.getUserId()))
                .filter(resume -> entity.getId() == null || !entity.getId().equals(resume.getId()))
                .anyMatch(resume -> normalizedTitle.equalsIgnoreCase(resume.getTitle()));
        if (duplicateTitle) {
            throw new ConstraintViolationException("Resume title must be unique for this TA");
        }
        entity.setTitle(normalizedTitle);
        return super.save(entity);
    }

    private String uniqueTitle(UUID userId, String baseTitle, UUID excludingId) {
        String base = baseTitle == null || baseTitle.isBlank() ? "Resume Copy" : baseTitle.trim();
        String candidate = base;
        int suffix = 2;
        while (titleExists(userId, candidate, excludingId)) {
            candidate = base + " " + suffix++;
        }
        return candidate;
    }

    private boolean titleExists(UUID userId, String title, UUID excludingId) {
        return findAll().stream()
                .filter(resume -> userId.equals(resume.getUserId()))
                .filter(resume -> excludingId == null || !excludingId.equals(resume.getId()))
                .anyMatch(resume -> title.equalsIgnoreCase(resume.getTitle()));
    }
}
