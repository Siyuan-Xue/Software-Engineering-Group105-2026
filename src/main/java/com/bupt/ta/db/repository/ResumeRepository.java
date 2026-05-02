package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.Resume;

import java.util.List;
import java.util.UUID;

public interface ResumeRepository extends CrudRepository<Resume> {
    List<Resume> listByUserId(UUID userId);

    Resume duplicate(UUID resumeId);
}
