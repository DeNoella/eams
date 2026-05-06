package com.eams.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${eams.mail.from-address}")
    private String fromAddress;

    @Value("${eams.mail.from-name}")
    private String fromName;

    @Async
    public void sendMagicLinkEmail(String recipientName, String recipientEmail,
            String magicLinkUrl) {
        try {
            Context context = new Context();
            context.setVariable("name", recipientName);
            context.setVariable("magicLinkUrl", magicLinkUrl);
            context.setVariable("expiryMinutes", 15);
            String html = templateEngine.process("email/magic-link", context);
            sendHtmlEmail(recipientEmail, "Your E-AMS Login Link", html);
            log.info("Magic link email sent to: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send magic link email to {}: {}", recipientEmail, e.getMessage());
        }
    }

    @Async
    public void sendExpiryAlert(String recipientEmail, String recipientName,
            String resourceType, String resourceName, long daysRemaining) {
        try {
            Context context = new Context();
            context.setVariable("name", recipientName);
            context.setVariable("resourceType", resourceType);
            context.setVariable("resourceName", resourceName);
            context.setVariable("daysRemaining", daysRemaining);
            String html = templateEngine.process("email/expiry-alert", context);
            String subject = String.format("[E-AMS Alert] %s '%s' expires in %d days",
                resourceType, resourceName, daysRemaining);
            sendHtmlEmail(recipientEmail, subject, html);
        } catch (Exception e) {
            log.error("Failed to send expiry alert email: {}", e.getMessage());
        }
    }

    @Async
    public void sendOverdueAlert(String recipientEmail, String recipientName,
            String alertType, String details) {
        try {
            Context context = new Context();
            context.setVariable("name", recipientName);
            context.setVariable("alertType", alertType);
            context.setVariable("details", details);
            String html = templateEngine.process("email/overdue-alert", context);
            sendHtmlEmail(recipientEmail, "[E-AMS OVERDUE] " + alertType, html);
        } catch (Exception e) {
            log.error("Failed to send overdue alert: {}", e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }
}