package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.junit.jupiter.api.Assertions.*;

class SmtpLoginMailTest {

    @Test
    void theMailNamesTheCodeAndHowLongItWorks() {
        val message = new SmtpLoginMail(new JavaMailSenderImpl(), "Frakkin Toasters <login@example.com>")
                .message("kara@example.com", "042317");

        assertEquals("Frakkin Toasters <login@example.com>", message.getFrom());
        assertArrayEquals(new String[]{"kara@example.com"}, message.getTo());
        assertEquals("Frakkin Toasters login code: 042317", message.getSubject());
        assertTrue(message.getText().contains("    042317\n"), message.getText());
        assertTrue(message.getText().contains("It works for 10 minutes."), message.getText());
    }
}
