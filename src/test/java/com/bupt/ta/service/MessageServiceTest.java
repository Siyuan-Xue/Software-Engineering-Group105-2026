package com.bupt.ta.service;

import com.bupt.ta.db.core.JsonMapperFactory;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.db.facade.FileTaDatabase;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.NotificationType;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.dto.ConversationDTO;
import com.bupt.ta.dto.MessageDTO;
import com.bupt.ta.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MessageServiceTest {
    @TempDir
    Path tempDir;

    private TaDatabase db;
    private MessageService messageService;
    private User userA;
    private User userB;
    private User userC;

    @BeforeEach
    void setUp() {
        db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, JsonMapperFactory.create()));
        messageService = new MessageService(db);

        // Create test users
        userA = new User();
        userA.setEmail("usera@test.com");
        userA.setPasswordHash(PasswordUtil.hashPassword("password"));
        userA.setRole(UserRole.TA);
        userA.setFullName("Alice TA");
        userA = db.users().save(userA);

        userB = new User();
        userB.setEmail("userb@test.com");
        userB.setPasswordHash(PasswordUtil.hashPassword("password"));
        userB.setRole(UserRole.MO);
        userB.setFullName("Bob MO");
        userB = db.users().save(userB);

        userC = new User();
        userC.setEmail("userc@test.com");
        userC.setPasswordHash(PasswordUtil.hashPassword("password"));
        userC.setRole(UserRole.ADMIN);
        userC.setFullName("Charlie Admin");
        userC = db.users().save(userC);
    }

    @Test
    void testEmptyConversations() {
        List<ConversationDTO> conversations = messageService.listConversations(userA.getId(), false);
        assertTrue(conversations.isEmpty());
    }

    @Test
    void testSendAndReceiveMessage() {
        String convId = messageService.getOrCreateConversationId(userA.getId(), userB.getId());
        assertNotNull(convId);
        assertTrue(convId.startsWith("conv-"));

        // Send a message from A to B
        messageService.sendMessage(userA.getId(), convId, "Hello Bob!");

        // Check B's conversations
        List<ConversationDTO> bConversations = messageService.listConversations(userB.getId(), false);
        assertEquals(1, bConversations.size());
        assertEquals("Alice TA", bConversations.get(0).getContactName());
        assertEquals(1, bConversations.get(0).getUnreadCount());

        // Check A's conversations (A sent it, so A should see it too)
        List<ConversationDTO> aConversations = messageService.listConversations(userA.getId(), false);
        assertEquals(1, aConversations.size());
        assertEquals("Bob MO", aConversations.get(0).getContactName());
        assertEquals(0, aConversations.get(0).getUnreadCount()); // A is sender, not unread for A
    }

    @Test
    void testGetConversationWithMessages() {
        String convId = messageService.getOrCreateConversationId(userA.getId(), userB.getId());

        messageService.sendMessage(userA.getId(), convId, "Hello Bob!");
        messageService.sendMessage(userB.getId(), convId, "Hi Alice!");
        messageService.sendMessage(userA.getId(), convId, "How are you?");

        // Get conversation from A's perspective
        ConversationDTO conv = messageService.getConversation(userA.getId(), convId, false);
        assertNotNull(conv);
        assertEquals(3, conv.getMessages().size());

        // First message should be from A (isMine=true from A's perspective)
        assertTrue(conv.getMessages().get(0).getIsMine());
        assertEquals("Hello Bob!", conv.getMessages().get(0).getContent());

        // Second message should be from B (isMine=false from A's perspective)
        assertFalse(conv.getMessages().get(1).getIsMine());
        assertEquals("Hi Alice!", conv.getMessages().get(1).getContent());

        // Third message should be from A
        assertTrue(conv.getMessages().get(2).getIsMine());
        assertEquals("How are you?", conv.getMessages().get(2).getContent());
    }

    @Test
    void testConversationIdIsDeterministic() {
        String id1 = MessageService.buildConversationId(userA.getId(), userB.getId());
        String id2 = MessageService.buildConversationId(userB.getId(), userA.getId());
        assertEquals(id1, id2);
    }

    @Test
    void testMultipleConversations() {
        String convAB = messageService.getOrCreateConversationId(userA.getId(), userB.getId());
        String convAC = messageService.getOrCreateConversationId(userA.getId(), userC.getId());

        messageService.sendMessage(userA.getId(), convAB, "Hello Bob!");
        messageService.sendMessage(userA.getId(), convAC, "Hello Charlie!");
        messageService.sendMessage(userC.getId(), convAC, "Hi Alice!");

        List<ConversationDTO> aConversations = messageService.listConversations(userA.getId(), false);
        assertEquals(2, aConversations.size());
    }

    @Test
    void testMarkReadOnOpen() {
        String convId = messageService.getOrCreateConversationId(userA.getId(), userB.getId());

        messageService.sendMessage(userA.getId(), convId, "Hello Bob!");
        messageService.sendMessage(userA.getId(), convId, "Are you there?");

        // B has 2 unread
        List<ConversationDTO> bConvsBefore = messageService.listConversations(userB.getId(), false);
        assertEquals(2, bConvsBefore.get(0).getUnreadCount());

        // B opens the conversation (getConversation marks read)
        messageService.getConversation(userB.getId(), convId, false);

        // B should now have 0 unread
        List<ConversationDTO> bConvsAfter = messageService.listConversations(userB.getId(), false);
        assertEquals(0, bConvsAfter.get(0).getUnreadCount());
    }

    @Test
    void testSendMessageWithInvalidConversationId() {
        assertThrows(IllegalArgumentException.class, () ->
                messageService.sendMessage(userA.getId(), "invalid-id", "Hello"));
    }

    @Test
    void testSendMessageWithEmptyContent() {
        String convId = messageService.getOrCreateConversationId(userA.getId(), userB.getId());
        assertThrows(IllegalArgumentException.class, () ->
                messageService.sendMessage(userA.getId(), convId, ""));
    }

    @Test
    void testSendMessageWithNullContent() {
        String convId = messageService.getOrCreateConversationId(userA.getId(), userB.getId());
        assertThrows(IllegalArgumentException.class, () ->
                messageService.sendMessage(userA.getId(), convId, null));
    }

    @Test
    void testGetConversationInvalidId() {
        ConversationDTO conv = messageService.getConversation(userA.getId(), "invalid", false);
        assertNull(conv);
    }

    @Test
    void testGetConversationNotParticipant() {
        String convId = messageService.getOrCreateConversationId(userA.getId(), userB.getId());
        // userC is not part of this conversation
        ConversationDTO conv = messageService.getConversation(userC.getId(), convId, false);
        assertNull(conv);
    }

    @Test
    void testChineseLocale() {
        String convId = messageService.getOrCreateConversationId(userA.getId(), userB.getId());
        messageService.sendMessage(userA.getId(), convId, "你好！");

        List<ConversationDTO> conversations = messageService.listConversations(userB.getId(), true);
        assertEquals(1, conversations.size());
        assertEquals("助教", conversations.get(0).getContactRole());
    }

    @Test
    void testMessageNotificationsAreFilteredFromRegularNotifications() {
        // Ensure MESSAGE type notifications don't interfere with regular notification flow
        String convId = messageService.getOrCreateConversationId(userA.getId(), userB.getId());
        messageService.sendMessage(userA.getId(), convId, "Test message");

        // The notification should be of type MESSAGE
        long messageCount = db.notifications().findAll().stream()
                .filter(n -> n.getNotifType() == NotificationType.MESSAGE)
                .count();
        assertEquals(1, messageCount);
    }

    @Test
    void testConversationsSortedByMostRecent() throws InterruptedException {
        String convAB = messageService.getOrCreateConversationId(userA.getId(), userB.getId());
        String convAC = messageService.getOrCreateConversationId(userA.getId(), userC.getId());

        messageService.sendMessage(userA.getId(), convAC, "Hello Charlie - first");
        // Small delay to ensure different timestamps
        Thread.sleep(50);
        messageService.sendMessage(userA.getId(), convAB, "Hello Bob - second");

        List<ConversationDTO> conversations = messageService.listConversations(userA.getId(), false);
        assertEquals(2, conversations.size());
        // Most recent conversation (with Bob) should be first
        assertEquals("Bob MO", conversations.get(0).getContactName());
        assertEquals("Charlie Admin", conversations.get(1).getContactName());
    }
}