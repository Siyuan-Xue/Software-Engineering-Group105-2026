package com.bupt.ta.i18n;

import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.support.ServletHarness;
import com.bupt.ta.support.TestData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class I18nTest {

    @Test
    void normalizationAndMessageLookupShouldUseEnglishFallbacks() {
        assertEquals("zh", I18n.normalizeLanguage("ZH"));
        assertEquals("en", I18n.normalizeLanguage("fr"));
        assertEquals("dark", I18n.normalizeAppearance("DARK"));
        assertEquals("light", I18n.normalizeAppearance("blue"));
        assertTrue(I18n.isChinese("zh"));
        assertFalse(I18n.isChinese("en"));
        assertEquals("zh-CN", I18n.langTag("zh"));
        assertEquals("en", I18n.langTag("en"));
        assertEquals("missing.key", I18n.message("zh", "missing.key"));
    }

    @Test
    void resolveLanguageAndAppearanceShouldPreferRequestThenCurrentUserThenSession() {
        ServletHarness requestValue = new ServletHarness()
                .requestAttribute("language", "zh")
                .requestAttribute("appearance", "dark");
        assertEquals("zh", I18n.resolveLanguage(requestValue.request()));
        assertEquals("dark", I18n.resolveAppearance(requestValue.request()));

        User user = TestData.user(TestData.uniqueEmail("i18n"), UserRole.TA, "I18n User");
        user.setPreferredLanguage("zh");
        user.setPreferredAppearance("dark");
        ServletHarness currentUserValue = new ServletHarness().currentUser(user);
        currentUserValue.session().setAttribute(I18n.SESSION_LANGUAGE_ATTR, "en");
        currentUserValue.session().setAttribute(I18n.SESSION_APPEARANCE_ATTR, "light");
        assertEquals("zh", I18n.resolveLanguage(currentUserValue.request()));
        assertEquals("dark", I18n.resolveAppearance(currentUserValue.request()));

        ServletHarness sessionValue = new ServletHarness();
        sessionValue.session().setAttribute(I18n.SESSION_LANGUAGE_ATTR, "zh");
        sessionValue.session().setAttribute(I18n.SESSION_APPEARANCE_ATTR, "dark");
        assertEquals("zh", I18n.resolveLanguage(sessionValue.request()));
        assertEquals("dark", I18n.resolveAppearance(sessionValue.request()));
    }
}
