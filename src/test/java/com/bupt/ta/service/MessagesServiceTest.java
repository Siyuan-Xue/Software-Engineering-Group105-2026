package com.bupt.ta.service;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.Application;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.Resume;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.JobStatus;
import com.bupt.ta.model.enums.UserRole;
import com.bupt.ta.persistence.TaDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MessagesServiceTest {
    @TempDir
    Path tempDir;

    private MessagesService messagesService;
    private User taUser;
    private User moUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        TaDatabase database = TaDatabase.open(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));
        messagesService = new MessagesService(database);

        taUser = new User();
        taUser.setEmail("ta@example.com");
        taUser.setFullName("Taylor Applicant");
        taUser.setRole(UserRole.TA);
        taUser = database.users().save(taUser);

        moUser = new User();
        moUser.setEmail("mo@example.com");
        moUser.setFullName("Morgan Organiser");
        moUser.setRole(UserRole.MO);
        moUser = database.users().save(moUser);

        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setFullName("Avery Admin");
        adminUser.setRole(UserRole.ADMIN);
        adminUser = database.users().save(adminUser);

        Job job = new Job();
        job.setPostedBy(moUser.getId());
        job.setTitle("Algorithms TA");
        job.setModuleCode("CS101");
        job.setStatus(JobStatus.OPEN);
        job.setHourlyRate(BigDecimal.valueOf(28));
        job.setDeadline(Instant.now().plus(7, ChronoUnit.DAYS));
        job = database.jobs().save(job);

        Resume resume = new Resume();
        resume.setUserId(taUser.getId());
        resume.setTitle("Taylor Resume");
        resume = database.resumes().save(resume);

        Application application = new Application();
        application.setResumeId(resume.getId());
        application.setJobId(job.getId());
        application.setMoNotes("Please share your availability for the first two teaching weeks.");
        database.applications().save(application);
    }

    @Test
    void loadInboxBuildsSharedConversationForTaAndMo() {
        MessagesService.InboxData taInbox = messagesService.loadInbox(taUser, null);

        assertEquals("noActiveConversation", taInbox.pageState());
        assertEquals(1, taInbox.conversations().size());
        assertEquals("Morgan Organiser", taInbox.conversations().get(0).getContactName());
        assertEquals("Module Organiser", taInbox.conversations().get(0).getContactRole());

        String conversationId = taInbox.conversations().get(0).getConversationId();
        MessagesService.InboxData moInbox = messagesService.loadInbox(moUser, conversationId);

        assertEquals("normal", moInbox.pageState());
        assertNotNull(moInbox.activeConversation());
        assertEquals("Taylor Applicant", moInbox.activeConversation().getContactName());
        assertEquals("Teaching Assistant Applicant", moInbox.activeConversation().getContactRole());
        assertFalse(moInbox.activeConversation().getMessages().isEmpty());
    }

    @Test
    void appendMessageMakesReplyVisibleToTheOtherParticipant() {
        String conversationId = messagesService.loadInbox(taUser, null).conversations().get(0).getConversationId();

        messagesService.appendMessage(taUser, conversationId, "I am available on Monday and Wednesday afternoons.");
        MessagesService.InboxData moInbox = messagesService.loadInbox(moUser, conversationId);

        assertEquals("I am available on Monday and Wednesday afternoons.",
                moInbox.activeConversation().getMessages().get(moInbox.activeConversation().getMessages().size() - 1).getContent());
        assertFalse(moInbox.activeConversation().getMessages().get(moInbox.activeConversation().getMessages().size() - 1).getIsMine());

        messagesService.appendMessage(moUser, conversationId, "Thanks. We will confirm the interview slot shortly.");
        MessagesService.InboxData taInbox = messagesService.loadInbox(taUser, conversationId);

        assertEquals("Thanks. We will confirm the interview slot shortly.",
                taInbox.activeConversation().getMessages().get(taInbox.activeConversation().getMessages().size() - 1).getContent());
        assertFalse(taInbox.activeConversation().getMessages().get(taInbox.activeConversation().getMessages().size() - 1).getIsMine());
    }

    @Test
    void loadInboxReturnsEmptyStateWhenUserHasNoAccessibleConversations() {
        MessagesService.InboxData adminInbox = messagesService.loadInbox(adminUser, null);

        assertEquals("empty", adminInbox.pageState());
        assertEquals(0, adminInbox.conversations().size());
    }

    @Test
    void loadInboxReturnsNoActiveConversationForUnknownThread() {
        MessagesService.InboxData inbox = messagesService.loadInbox(taUser, "missing-conversation");

        assertEquals("noActiveConversation", inbox.pageState());
        assertNotNull(inbox.derivedErrorMessage());
    }
}
