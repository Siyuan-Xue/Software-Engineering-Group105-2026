package com.bupt.ta.repository;

import com.bupt.ta.model.Resume;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository {
    Optional<Resume> findById(UUID id);

    List<Resume> listAll();

    List<Resume> listByUserId(UUID userId);

    Resume save(Resume resume);

    boolean delete(UUID id);
}
