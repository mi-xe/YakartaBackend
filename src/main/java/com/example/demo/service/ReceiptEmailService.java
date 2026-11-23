package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;

/** Sends the PDF copy supplied by the signed-in customer to that customer's verified Google email. */
@Service
public class ReceiptEmailService {
    private static final Logger log = LoggerFactory.getLogger(ReceiptEmailService.class);

    public record SendResult(String recipient, String sender, String messageId) {}
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String host;
    private final String username;
    private final String password;
    private final String configuredFrom;

    public ReceiptEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.host:}") String host,
            @Value("${spring.mail.username:}") String username,
            @Value("${spring.mail.password:}") String password,
            @Value("${app.mail.from:}") String configuredFrom) {
        this.mailSenderProvider = mailSenderProvider;
        this.host = host == null ? "" : host.trim();
        this.username = username == null ? "" : username.trim();
        this.password = password == null ? "" : password.trim();
        this.configuredFrom = configuredFrom == null ? "" : configuredFrom.trim();
    }

    public SendResult sendReceipt(String recipient, String customerName, Long orderId,
                                  LocalDate orderDate, byte[] pdf) throws MessagingException, MailException {
        if (host.isBlank() || username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("El envío de boletas no está configurado. Completa MAIL_HOST, MAIL_USERNAME y MAIL_PASSWORD en el .env del backend.");
        }
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalStateException("La cuenta Google no tiene un correo verificado disponible.");
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new IllegalStateException(
                    "No está disponible JavaMailSender. Verifica que pom.xml incluya " +
                    "spring-boot-starter-mail y que MAIL_HOST esté definido en el .env del backend."
            );
        }

        String from = configuredFrom.isBlank() ? username : configuredFrom;
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        try {
            helper.setFrom(from, "Yakarta Gaming");
        } catch (UnsupportedEncodingException ex) {
            throw new MessagingException("No se pudo configurar el remitente de la boleta.", ex);
        }
        helper.setReplyTo(username);
        helper.setTo(recipient);
        helper.setSubject("Tu boleta de compra Yakarta #" + orderId);
        helper.setValidateAddresses(true);
        message.setHeader("X-Yakarta-Order", String.valueOf(orderId));
        String safeName = escapeHtml(customerName == null || customerName.isBlank() ? "cliente" : customerName);
        String safeDate = escapeHtml(orderDate == null ? LocalDate.now().toString() : orderDate.toString());
        String html = """
                <!doctype html><html lang="es"><body style="margin:0;background:#f2f2f2;font-family:Arial,sans-serif;color:#222">
                  <div style="max-width:620px;margin:28px auto;background:#fff;border-radius:12px;padding:28px">
                    <h2 style="text-align:center;margin:0 0 18px;color:#161616">Tu boleta Yakarta está lista</h2>
                    <p>Hola %s,</p>
                    <p>Adjuntamos una copia en PDF de tu compra <strong>#%d</strong>, realizada el <strong>%s</strong>.</p>
                    <p>El archivo adjunto contiene el detalle de los juegos, cantidades y total pagado.</p>
                    <p style="margin-top:28px;color:#666;text-align:center">Gracias por comprar en Yakarta.</p>
                  </div>
                </body></html>
                """.formatted(safeName, orderId, safeDate);
        helper.setText("Hola " + (customerName == null ? "cliente" : customerName)
                + ", adjuntamos tu boleta Yakarta #" + orderId + ".", html);
        helper.addAttachment("boleta-yakarta-" + orderId + ".pdf",
                new ByteArrayResource(pdf), "application/pdf");
        // A successful return means the SMTP server accepted the message for processing.
        // It does not prove final delivery to Inbox: Gmail may route it to Spam/Promotions or bounce it later.
        message.saveChanges();
        mailSender.send(message);
        String messageId = message.getMessageID();
        log.info("SMTP aceptó la boleta. pedido={}, destinatario={}, remitente={}, messageId={}",
                orderId, maskEmail(recipient), maskEmail(from), messageId);
        if (!from.equalsIgnoreCase(username)) {
            log.warn("MAIL_FROM ({}) es distinto de MAIL_USERNAME ({}). En Gmail, MAIL_FROM debe ser la cuenta autenticada o un alias de envío verificado.",
                    maskEmail(from), maskEmail(username));
        }
        return new SendResult(recipient, from, messageId);
    }

    private static String maskEmail(String email) {
        if (email == null || email.isBlank()) return "(vacío)";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + (at >= 0 ? email.substring(at) : "");
        return email.substring(0, 2) + "***" + email.substring(at);
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
