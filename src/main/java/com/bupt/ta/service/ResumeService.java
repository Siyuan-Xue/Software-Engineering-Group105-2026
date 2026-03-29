package com.bupt.ta.service;

import com.bupt.ta.model.Resume;
import com.bupt.ta.repository.ResumeRepository;

import java.util.List;
import java.util.UUID;

public class ResumeService {
    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public List<Resume> listByUserId(UUID userId) {
        return resumeRepository.listByUserId(userId);
    }

    public Resume save(Resume resume) {
        return resumeRepository.save(resume);
    }

    public boolean delete(UUID resumeId) {
        return resumeRepository.delete(resumeId);
    }
}
