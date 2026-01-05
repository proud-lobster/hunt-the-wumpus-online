package com.proudlobster.wumpus.server.service;

import java.util.Properties;

import com.proudlobster.wumpus.core.Engine;
import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.service.LifecycleService;
import com.proudlobster.wumpus.core.service.SettingService;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService implements LifecycleService {

    public static final String ERR_FAILED_TO_EMAIL = "Failed to send email.";
    public static final String AUTH_ENABLED = "true";
    public static final String STARTTLS_ENABLED = "true";
    public static final String CONTENT_TYPE = "text/plain; charset=utf-8";
    public static final Boolean STRICT_ADDR_ENABLED = false;

    private SettingService settings;
    private Authenticator auth;
    private Properties props;
    private String from;

    @Override
    public void handleInitialized(final Engine eng) {
        settings = eng.service(SettingService.class);
    }

    @Override
    public void handleRunning() {
        final PasswordAuthentication pwAuth = new PasswordAuthentication(
                settings.getCritical("proudlobster.email.account"),
                settings.getCritical("proudlobster.email.password"));

        auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return pwAuth;
            }
        };

        props = new Properties();
        props.setProperty("mail.smtp.host", settings.getCritical("proudlobster.email.host"));
        props.setProperty("mail.smtp.port", settings.getCritical("proudlobster.email.port"));
        props.setProperty("mail.smtp.auth", AUTH_ENABLED);
        props.setProperty("mail.smtp.starttls.enable", STARTTLS_ENABLED);

        from = settings.getCritical("proudlobster.email.account");
    }

    public void sendEmail(final String to, final String subject, final String body) {
        try {
            final Session session = Session.getInstance(props, auth);
            final Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to, STRICT_ADDR_ENABLED));
            message.setSubject(subject);
            message.setContent(body, CONTENT_TYPE);
            Transport.send(message);
        } catch (final MessagingException e) {
            throw new OperatingError(ERR_FAILED_TO_EMAIL, e);
        }
    }

}