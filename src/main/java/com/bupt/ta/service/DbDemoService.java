package com.bupt.ta.service;

import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.Resume;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.UserRole;
import com.bupt.ta.persistence.TaDatabase;
import com.bupt.ta.repository.ApplicationRepository;
import com.bupt.ta.repository.JobRepository;
import com.bupt.ta.repository.ResumeRepository;
import com.bupt.ta.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class DbDemoService {
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public DbDemoService(UserRepository userRepository,
                         ResumeRepository resumeRepository,
                         JobRepository jobRepository,
                         ApplicationRepository applicationRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository must not be null");
        this.resumeRepository = Objects.requireNonNull(resumeRepository, "resumeRepository must not be null");
        this.jobRepository = Objects.requireNonNull(jobRepository, "jobRepository must not be null");
        this.applicationRepository = Objects.requireNonNull(applicationRepository, "applicationRepository must not be null");
    }

    public static DbDemoService from(TaDatabase database) {
        return new DbDemoService(
                database.users(),
                database.resumes(),
                database.jobs(),
                database.applications()
        );
    }

    public List<User> listUsers() {
        return userRepository.listAll();
    }

    public List<Resume> listResumes() {
        return resumeRepository.listAll();
    }

    public List<Job> listJobs() {
        return jobRepository.listAll();
    }

    public List<Application> listApplications() {
        return applicationRepository.listAll();
    }

    public Optional<User> findUser(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<Resume> findResume(UUID id) {
        return resumeRepository.findById(id);
    }

    public Optional<Job> findJob(UUID id) {
        return jobRepository.findById(id);
    }

    public Optional<Application> findApplication(UUID id) {
        return applicationRepository.findById(id);
    }

    public User saveUser(User user) {
        if (user.getId() != null) {
            requireExistingUser(user.getId(), "User not found");
        }
        requireNonBlank(user.getEmail(), "User email must not be blank");
        requireNonBlank(user.getPasswordHash(), "User passwordHash must not be blank");
        requireNonBlank(user.getFullName(), "User fullName must not be blank");
        requireNonNull(user.getRole(), "User role must not be null");

        user.setEmail(user.getEmail().trim());
        user.setPasswordHash(user.getPasswordHash().trim());
        user.setFullName(user.getFullName().trim());
        if (user.getPhone() != null) {
            user.setPhone(user.getPhone().trim());
        }
        return userRepository.save(user);
    }

    public boolean deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        if (!user.isActive()) {
            throw new IllegalArgumentException("User is already inactive");
        }
        return userRepository.setActive(id, false);
    }

    public Resume saveResume(Resume resume) {
        if (resume.getId() != null) {
            requireExistingResume(resume.getId(), "Resume not found");
        }
        requireNonNull(resume.getUserId(), "Resume userId must not be null");
        requireNonBlank(resume.getTitle(), "Resume title must not be blank");
        requireNonNull(resume.getDegreeLevel(), "Resume degreeLevel must not be null");
        requireExistingUser(resume.getUserId(), "Resume owner does not exist");

        resume.setTitle(resume.getTitle().trim());
        resume.setDepartment(trimToNull(resume.getDepartment()));
        resume.setBio(trimToNull(resume.getBio()));
        resume.setAvailabilityJson(trimToNull(resume.getAvailabilityJson()));
        return resumeRepository.save(resume);
    }

    public boolean deleteResume(UUID id) {
        requireExistingResume(id, "Resume not found");
        boolean hasApplications = applicationRepository.listByResumeId(id).stream().findAny().isPresent();
        if (hasApplications) {
            throw new IllegalArgumentException("Cannot delete a resume that is already referenced by applications");
        }
        return resumeRepository.delete(id);
    }

    public Job saveJob(Job job) {
        if (job.getId() != null) {
            requireExistingJob(job.getId(), "Job not found");
        }
        requireNonNull(job.getPostedBy(), "Job postedBy must not be null");
        requireNonBlank(job.getTitle(), "Job title must not be blank");
        requireNonNull(job.getType(), "Job type must not be null");
        requireNonNull(job.getStatus(), "Job status must not be null");
        requireNonNull(job.getDeadline(), "Job deadline must not be null");

        User poster = requireExistingUser(job.getPostedBy(), "Job poster does not exist");
        if (poster.getRole() != UserRole.MO && poster.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Job poster must be an MO or ADMIN user");
        }
        validateDateRange(job.getStartDate(), job.getEndDate(), "Job endDate must not be before startDate");

        job.setTitle(job.getTitle().trim());
        job.setModuleCode(trimToNull(job.getModuleCode()));
        job.setDescription(trimToNull(job.getDescription()));
        return jobRepository.save(job);
    }

    public boolean deleteJob(UUID id) {
        requireExistingJob(id, "Job not found");
        boolean hasApplications = applicationRepository.listByJobId(id).stream().findAny().isPresent();
        if (hasApplications) {
            throw new IllegalArgumentException("Cannot delete a job that is already referenced by applications");
        }
        return jobRepository.delete(id);
    }

    public Application saveApplication(Application application) {
        if (application.getId() != null) {
            applicationRepository.findById(application.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Application not found: " + application.getId()));
        }
        requireNonNull(application.getResumeId(), "Application resumeId must not be null");
        requireNonNull(application.getJobId(), "Application jobId must not be null");
        requireNonNull(application.getStatus(), "Application status must not be null");

        requireExistingResume(application.getResumeId(), "Application resume does not exist");
        requireExistingJob(application.getJobId(), "Application job does not exist");

        if (application.getReviewedBy() != null) {
            User reviewer = requireExistingUser(application.getReviewedBy(), "Application reviewer does not exist");
            if (reviewer.getRole() != UserRole.MO && reviewer.getRole() != UserRole.ADMIN) {
                throw new IllegalArgumentException("Application reviewer must be an MO or ADMIN user");
            }
        }

        application.setCoverLetter(trimToNull(application.getCoverLetter()));
        application.setMoNotes(trimToNull(application.getMoNotes()));
        return applicationRepository.save(application);
    }

    public boolean deleteApplication(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
        return applicationRepository.delete(application.getId());
    }

    private User requireExistingUser(UUID id, String message) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(message + ": " + id));
    }

    private Resume requireExistingResume(UUID id, String message) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(message + ": " + id));
    }

    private Job requireExistingJob(UUID id, String message) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(message + ": " + id));
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate, String message) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
