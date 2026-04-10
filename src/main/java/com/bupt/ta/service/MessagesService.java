package com.bupt.ta.service;

import com.bupt.ta.dto.ConversationView;
import com.bupt.ta.dto.MessageView;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.Resume;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.ApplicationStatus;
import com.bupt.ta.model.enums.UserRole;
import com.bupt.ta.persistence.TaDatabase;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessagesService {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneId.systemDefault());

    // Temporary in-memory message store until a dedicated message repository is introduced.
    private static final Map<String, List<StoredMessage>> TRANSIENT_MESSAGES = new ConcurrentHashMap<>();

    private final TaDatabase database;

    public MessagesService(TaDatabase database) {
        this.database = Objects.requireNonNull(database, "database must not be null");
    }

    public InboxData loadInbox(User currentUser, String requestedConversationId) {
        List<ConversationBundle> bundles = new ArrayList<>(switch (currentUser.getRole()) {
            case TA -> buildTaConversations(currentUser);
            case MO -> buildMoConversations(currentUser);
            default -> List.of();
        });

        bundles.sort(Comparator.comparing(ConversationBundle::lastActivity).reversed());
        List<ConversationView> conversations = bundles.stream()
                .map(ConversationBundle::conversation)
                .toList();

        if (conversations.isEmpty()) {
            return new InboxData(conversations, null, "empty", null);
        }

        if (requestedConversationId == null || requestedConversationId.isBlank()) {
            return new InboxData(conversations, null, "noActiveConversation", null);
        }

        for (ConversationView conversation : conversations) {
            if (requestedConversationId.equals(conversation.getConversationId())) {
                return new InboxData(conversations, conversation, "normal", null);
            }
        }

        return new InboxData(
                conversations,
                null,
                "noActiveConversation",
                "We couldn't open that conversation. Choose another thread from the list."
        );
    }

    public void appendMessage(User currentUser, String conversationId, String messageContent) {
        String normalizedConversationId = require(conversationId, "Conversation ID is required.");
        String normalizedContent = require(messageContent, "Message content is required.");

        boolean hasAccess = switch (currentUser.getRole()) {
            case TA -> buildTaConversations(currentUser).stream()
                    .anyMatch(bundle -> normalizedConversationId.equals(bundle.conversation().getConversationId()));
            case MO -> buildMoConversations(currentUser).stream()
                    .anyMatch(bundle -> normalizedConversationId.equals(bundle.conversation().getConversationId()));
            default -> false;
        };

        if (!hasAccess) {
            throw new IllegalArgumentException("Conversation not found.");
        }

        TRANSIENT_MESSAGES.computeIfAbsent(normalizedConversationId, key -> new CopyOnWriteArrayList<>())
                .add(new StoredMessage(
                        UUID.randomUUID().toString(),
                        currentUser.getId(),
                        normalizedContent,
                        Instant.now()
                ));
    }

    private List<ConversationBundle> buildTaConversations(User currentUser) {
        Map<UUID, Job> jobsById = indexById(database.jobs().listAll());
        Map<UUID, Resume> resumesById = indexById(database.resumes().listAll());
        Map<UUID, User> usersById = indexById(database.users().listAll());
        List<ConversationBundle> conversations = new ArrayList<>();

        for (Resume resume : database.resumes().listByUserId(currentUser.getId())) {
            for (Application application : database.applications().listByResumeId(resume.getId())) {
                conversations.add(buildConversation(application, currentUser, jobsById, resumesById, usersById));
            }
        }

        return conversations;
    }

    private List<ConversationBundle> buildMoConversations(User currentUser) {
        Map<UUID, Job> jobsById = indexById(database.jobs().listAll());
        Map<UUID, Resume> resumesById = indexById(database.resumes().listAll());
        Map<UUID, User> usersById = indexById(database.users().listAll());
        List<ConversationBundle> conversations = new ArrayList<>();

        for (Job job : database.jobs().listByPoster(currentUser.getId())) {
            for (Application application : database.applications().listByJobId(job.getId())) {
                conversations.add(buildConversation(application, currentUser, jobsById, resumesById, usersById));
            }
        }

        return conversations;
    }

    private ConversationBundle buildConversation(Application application,
                                                 User currentUser,
                                                 Map<UUID, Job> jobsById,
                                                 Map<UUID, Resume> resumesById,
                                                 Map<UUID, User> usersById) {
        Job job = jobsById.get(application.getJobId());
        Resume resume = resumesById.get(application.getResumeId());
        User organiser = job == null ? null : usersById.get(job.getPostedBy());
        User applicant = resume == null ? null : usersById.get(resume.getUserId());

        String conversationId = application.getId().toString();
        boolean taView = currentUser.getRole() == UserRole.TA;
        User contactUser = taView ? organiser : applicant;

        String contactName = safeContactName(contactUser, taView ? "Department Contact" : "Applicant");
        String contactRole = taView ? "Module Organiser" : "Teaching Assistant Applicant";
        UUID organiserId = organiser == null ? null : organiser.getId();

        List<TimelineMessage> timeline = buildTimeline(application, currentUser, job, organiserId, conversationId);
        TimelineMessage latest = timeline.get(timeline.size() - 1);

        ConversationView conversation = new ConversationView();
        conversation.setConversationId(conversationId);
        conversation.setContactName(contactName);
        conversation.setContactRole(contactRole);
        conversation.setContactAvatar(null);
        conversation.setLastMessage(latest.content());
        conversation.setLastMessageTime(formatInstant(latest.timestamp()));
        conversation.setUnreadCount(0);
        conversation.setMessages(toViewMessages(timeline));

        return new ConversationBundle(conversation, latest.timestamp());
    }

    private List<TimelineMessage> buildTimeline(Application application,
                                                User currentUser,
                                                Job job,
                                                UUID organiserId,
                                                String conversationId) {
        List<TimelineMessage> timeline = new ArrayList<>();
        Instant submittedAt = firstNonNull(application.getCreatedAt(), application.getUpdatedAt(), Instant.now());
        String vacancyLabel = job != null && notBlank(job.getTitle()) ? job.getTitle() : "this vacancy";

        timeline.add(new TimelineMessage(
                "system-" + conversationId + "-submitted",
                "Application submitted for " + vacancyLabel + ".",
                submittedAt,
                false,
                true
        ));

        if (application.getStatus() != null && application.getStatus() != ApplicationStatus.PENDING) {
            Instant statusAt = firstNonNull(application.getReviewedAt(), application.getUpdatedAt(), submittedAt);
            timeline.add(new TimelineMessage(
                    "system-" + conversationId + "-status",
                    "Application status updated to " + humanizeStatus(application.getStatus()) + ".",
                    statusAt,
                    false,
                    true
            ));
        }

        if (notBlank(application.getMoNotes())) {
            Instant noteAt = firstNonNull(application.getReviewedAt(), application.getUpdatedAt(), submittedAt);
            boolean noteIsMine = organiserId != null && organiserId.equals(currentUser.getId());
            timeline.add(new TimelineMessage(
                    "note-" + conversationId,
                    application.getMoNotes().trim(),
                    noteAt,
                    noteIsMine,
                    false
            ));
        }

        for (StoredMessage storedMessage : TRANSIENT_MESSAGES.getOrDefault(conversationId, List.of())) {
            timeline.add(new TimelineMessage(
                    storedMessage.messageId(),
                    storedMessage.content(),
                    storedMessage.createdAt(),
                    currentUser.getId() != null && currentUser.getId().equals(storedMessage.senderId()),
                    false
            ));
        }

        timeline.sort(Comparator.comparing(TimelineMessage::timestamp));
        return timeline;
    }

    private List<MessageView> toViewMessages(List<TimelineMessage> timeline) {
        List<MessageView> messages = new ArrayList<>(timeline.size());
        for (TimelineMessage item : timeline) {
            MessageView view = new MessageView();
            view.setMessageId(item.messageId());
            view.setContent(item.content());
            view.setTimestamp(formatInstant(item.timestamp()));
            view.setMine(item.isMine());
            view.setSystemMessage(item.isSystemMessage());
            messages.add(view);
        }
        return messages;
    }

    private <T extends com.bupt.ta.model.BaseEntity> Map<UUID, T> indexById(Collection<T> items) {
        Map<UUID, T> indexed = new LinkedHashMap<>();
        for (T item : items) {
            if (item.getId() != null) {
                indexed.put(item.getId(), item);
            }
        }
        return indexed;
    }

    private String safeContactName(User user, String fallback) {
        if (user == null || !notBlank(user.getFullName())) {
            return fallback;
        }
        return user.getFullName().trim();
    }

    private String humanizeStatus(ApplicationStatus status) {
        String normalized = status.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private String formatInstant(Instant instant) {
        return TIMESTAMP_FORMATTER.format(instant);
    }

    private Instant firstNonNull(Instant first, Instant second, Instant fallback) {
        return first != null ? first : (second != null ? second : fallback);
    }

    private String require(String value, String message) {
        String normalized = normalize(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    public record InboxData(List<ConversationView> conversations,
                            ConversationView activeConversation,
                            String pageState,
                            String derivedErrorMessage) {
    }

    private record ConversationBundle(ConversationView conversation, Instant lastActivity) {
    }

    private record TimelineMessage(String messageId,
                                   String content,
                                   Instant timestamp,
                                   boolean isMine,
                                   boolean isSystemMessage) {
    }

    private record StoredMessage(String messageId,
                                 UUID senderId,
                                 String content,
                                 Instant createdAt) {
    }
}
