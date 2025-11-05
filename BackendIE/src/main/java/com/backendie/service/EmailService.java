package com.backendie.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String emailFrom;
    private final String frontendUrl;
    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender,
                        @Value("${email.from}") String emailFrom,
                        @Value("${frontend.url}") String frontendUrl) {
        this.mailSender = mailSender;
        this.emailFrom = emailFrom;
        this.frontendUrl = frontendUrl;
    }

    public void sendVerificationEmail(String to, String token) {
        log.info("Preparando correo de verificación para: {}", to);
        String subject = "Completa tu registro en ComplianceAI";
        String link = frontendUrl + "/auth/verify?token=" + token;
        String body = "<html><body>"
                + "<h2>Completa tu registro en ComplianceAI</h2>"
                + "<p>Haz clic en el siguiente enlace para verificar tu correo. El enlace expira en 30 minutos.</p>"
                + "<p><a href='" + link + "' style='background:#1976d2;color:white;padding:10px 20px;text-decoration:none;border-radius:5px;'>Verificar correo</a></p>"
                + "<p>Si no lo solicitaste, ignora este correo.</p>"
                + "</body></html>";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            // send may throw MailException (e.g. TLS/SSL handshake problems)
            mailSender.send(message);
            log.info("Correo de verificación enviado a: {}", to);
        } catch (MessagingException e) {
            // problems creating the message
            log.error("Error creando el mensaje de verificación para {}: {}", to, e.toString());
            log.debug("MessagingException: ", e);
            // swallow: in development we prefer not to fail the whole request
        } catch (MailException e) {
            // problems sending the message (connection, TLS, auth...)
            log.error("No se pudo enviar correo a {}: {}", to, e.getMessage());
            log.debug("MailException: ", e);
            // swallow to avoid 500; the user remains created as pending
        }
    }
}
