package xyz.zlatanov.frakkintoasters.backend.user;

import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * The login codes by SMTP, once {@code spring.mail.host} is set: Boot then makes the {@link JavaMailSender}.
 */
@Component
@ConditionalOnProperty("spring.mail.host")
public class SmtpLoginMail implements LoginMail {

    private final JavaMailSender sender;
    private final String         from;

    public SmtpLoginMail(JavaMailSender sender, @Value("${frakkin.mail-from}") String from) {
        this.sender = sender;
        this.from = from;
    }

    @Override
    public void send(String email, String code) {
        sender.send(message(email, code));
    }

    SimpleMailMessage message(String email, String code) {
        val message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Frakkin Toasters login code: " + code);
        message.setText("""
                Your login code for Frakkin Toasters is

                    %s

                It works for %d minutes. If you didn't ask for it, ignore this email: nobody logs in without the code.
                """.formatted(code, EmailLogin.CODE_VALID.toMinutes()));
        return message;
    }
}
