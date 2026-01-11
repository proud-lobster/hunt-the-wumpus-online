package com.proudlobster.wumpus.server.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.proudlobster.wumpus.core.Engine;
import com.proudlobster.wumpus.core.entity.CoreComponent;
import com.proudlobster.wumpus.core.entity.Entity;
import com.proudlobster.wumpus.core.error.CriticalError;
import com.proudlobster.wumpus.core.error.OperatingError;
import com.proudlobster.wumpus.core.service.EntityService;
import com.proudlobster.wumpus.core.service.LifecycleService;
import com.proudlobster.wumpus.core.utility.Security;
import com.proudlobster.wumpus.server.ServerComponent;
import com.proudlobster.wumpus.server.io.ClientMessage;
import com.proudlobster.wumpus.server.io.Directive;

public class AccountService implements LifecycleService {

    private static final Integer LOCK_PERIOD_HOURS = 24;
    private static final Long LOCK_PERIOD_MILLIS = LOCK_PERIOD_HOURS * 3600000L;
    private static final Integer CODE_TIME_LIMIT_MINS = 10;
    private static final Integer CODE_TIME_LIMIT_MILLIS = CODE_TIME_LIMIT_MINS * 60000;
    private static final String NEW_ACCOUNT = "A new account has been created for the email address provided.  The player name for this account is %s.\n\nA temporary passcode has been sent to your email address.  By entering that passcode below you assert that you are at least 13 years of age and agree that as a live service game the terms of service for the game are subject to change.";
    private static final String LOGIN_LOCKED = "Your account is currently locked.  Contact admin@proudlobster.com for support.";
    private static final String LOGIN_SUCCESS = "You are now logged in as %s.";
    private static final String RECONNECT = "You have been reconnected to the server.";
    private static final String CODE_INVALID = "The passcode provided is not correct.  Please try again.  Repeated failures may result in your account becoming locked";
    private static final String CODE_FAIL_LOCK = "You have exceeded the number of allowable attempts to provide a passcode.  Your account has been locked for 24 hours.";
    private static final String CODE_EXPIRED = "This passcode has expired.  A new passcode has been sent to your email address.";
    private static final String LOGOUT_SUCCESS = "You have been logged out.";
    public static final String CODE_PROMPT = "No client token was found for your account.  A temporary passcode has been sent to your email address.  Enter it below to log in.";
    public static final String CODE_EMAIL_SUBJ = "Temporary Passcode for Hunt the Wumpus Online";
    public static final String CODE_EMAIL_BODY = "Your temporary passcode is %s.  It will expire in approximately "
            + CODE_TIME_LIMIT_MINS + " minutes.";
    public static final String ACCT_EMAIL_SUBJ = "New Account for Hunt the Wumpus Online";
    public static final String ACCT_EMAIL_BODY = """
            Thank you for creating this new account!
            Your player name is %s.
            You will receive a separate email shortly with a temporary passcode for logging in to your account.

            If you did not create this account, please reply to this email and let us know.
                    """;

    private static List<String> loadNameFile(final String file) {
        final ClassLoader cl = AccountService.class.getClassLoader();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(cl.getResourceAsStream("namefiles/" + file), StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        } catch (IOException e) {
            throw new CriticalError("Failed to load name file: " + file, e);
        }
    }

    private static final Random RAND = new Random();
    private EntityService entities;
    private EmailService emailService;
    private List<String> adjectives;
    private List<String> animals;

    private String generatePlayerName() {
        return adjectives.get(RAND.nextInt(adjectives.size())) +
                animals.get(RAND.nextInt(animals.size())) +
                RAND.nextInt(10) + RAND.nextInt(10) + RAND.nextInt(10);
    }

    @Override
    public void handleInitialized(final Engine eng) {
        entities = eng.service(EntityService.class);
        emailService = eng.service(EmailService.class);

        adjectives = loadNameFile("adjectives.txt");
        animals = loadNameFile("animals.txt");
    }

    public Optional<? extends Entity> accountByEmail(final String email) {
        return entities.lookup(ServerComponent.EMAIL_ADDRESS, email)
                .filter(ServerComponent.ACCOUNT)
                .findFirst();
    }

    public Entity playerBySession(final Long sessionId) {
        return entities.byIdentifier(sessionId)
                .reference(ServerComponent.ACCOUNT_REF)
                .reference(CoreComponent.PLAYER_REF);
    }

    public Optional<Entity> sessionByPlayer(final Long playerId) {
        return Optional.ofNullable(entities.byIdentifier(playerId))
                .map(e -> e.reference(ServerComponent.ACCOUNT_REF))
                .map(e -> e.reference(ServerComponent.SESSION_REF));
    }

    public ClientMessage processLogin(final Entity account, final Long sessionId, final String token) {

        final String salt = account.stringValue(ServerComponent.SALT);

        final boolean isLocked = account.is(ServerComponent.LOCK_UNTIL_TIMESTAMP)
                && account.longValue(ServerComponent.LOCK_UNTIL_TIMESTAMP) > System.currentTimeMillis();
        final boolean isSuccessful = account.is(ServerComponent.CLIENT_TOKEN) &&
                Security.hash(token, salt).equals(account.stringValue(ServerComponent.CLIENT_TOKEN));

        // Locked
        if (isLocked) {
            return Directive.FAILURE.create(sessionId, LOGIN_LOCKED);
        }
        // Failed login
        else if (!isSuccessful) {
            final String tempcode = Security.tempCode();
            final String hashtempcode = Security.hash(tempcode, salt);

            emailService.sendEmail(account.stringValue(ServerComponent.EMAIL_ADDRESS), CODE_EMAIL_SUBJ,
                    String.format(CODE_EMAIL_BODY, tempcode));

            account.addComponent(ServerComponent.TEMP_CODE, hashtempcode)
                    .addComponent(ServerComponent.TEMP_CODE_TIMESTAMP, System.currentTimeMillis())
                    .addComponent(ServerComponent.TEMP_CODE_ATTEMPTS, 0L);

            return Directive.PRINT.create(sessionId, CODE_PROMPT);
        }
        // Successful login
        else {
            final boolean reconnect = account.is(ServerComponent.DISCONNECT_TIMESTAMP);
            final String playerName = account
                    .reference(CoreComponent.PLAYER_REF)
                    .stringValue(CoreComponent.NAME);

            account.addComponent(ServerComponent.SESSION_REF, sessionId)
                    .removeComponent(ServerComponent.LOCK_UNTIL_TIMESTAMP)
                    .removeComponent(ServerComponent.DISCONNECT_TIMESTAMP);
            entities.byIdentifier(sessionId)
                    .addComponent(ServerComponent.ACCOUNT_REF, account.identifier());

            return Directive.SUCCESS.create(sessionId,
                    reconnect ? RECONNECT : String.format(LOGIN_SUCCESS, playerName));
        }
    }

    public ClientMessage processLogout(final Long sessionId) {
        final Entity session = entities.byIdentifier(sessionId);

        if (session.is(ServerComponent.ACCOUNT_REF)) {
            session.reference(ServerComponent.ACCOUNT_REF)
                    .removeComponent(ServerComponent.SESSION_REF)
                    .removeComponent(ServerComponent.CLIENT_TOKEN);
        }
        session.removeComponent(ServerComponent.ACCOUNT_REF);

        return Directive.LOGOUT.create(sessionId, LOGOUT_SUCCESS);
    }

    public ClientMessage processTokenRequest(final String email, final Long sessionId, final String code) {
        final Entity account = accountByEmail(email).orElseThrow(() -> new OperatingError("Account not found"));
        final String salt = account.stringValue(ServerComponent.SALT);
        final Long now = System.currentTimeMillis();

        final boolean isExpired = now > (account.longValue(ServerComponent.TEMP_CODE_TIMESTAMP)
                + CODE_TIME_LIMIT_MILLIS);

        final boolean isSuccessful = Security.hash(code, salt)
                .equals(account.stringValue(ServerComponent.TEMP_CODE));

        if (isExpired) {
            final String tempcode = Security.tempCode();
            final String hashtempcode = Security.hash(tempcode, salt);
            account.addComponent(ServerComponent.TEMP_CODE, hashtempcode)
                    .addComponent(ServerComponent.TEMP_CODE_TIMESTAMP, System.currentTimeMillis())
                    .addComponent(ServerComponent.TEMP_CODE_ATTEMPTS, 0L);
            emailService.sendEmail(email, CODE_EMAIL_SUBJ, String.format(CODE_EMAIL_BODY, tempcode));
            return Directive.PRINT.create(sessionId, CODE_EXPIRED);
        } else if (!isSuccessful) {
            final Long failedAttempts = account.longValue(ServerComponent.TEMP_CODE_ATTEMPTS);
            if (failedAttempts >= 2) {
                account.addComponent(ServerComponent.LOCK_UNTIL_TIMESTAMP, now + LOCK_PERIOD_MILLIS);
                return Directive.FAILURE.create(sessionId, CODE_FAIL_LOCK);
            } else {
                account.addComponent(ServerComponent.TEMP_CODE_ATTEMPTS, failedAttempts + 1);
                return Directive.PRINT.create(sessionId, CODE_INVALID);
            }
        } else {
            final String token = Security.token();
            final String hashtoken = Security.hash(token, salt);
            account.addComponent(ServerComponent.CLIENT_TOKEN, hashtoken)
                    .removeComponent(ServerComponent.TEMP_CODE);
            return Directive.TOKEN.create(sessionId, token);
        }

    }

    public ClientMessage createNewAccount(final String email, final Long sessionId) {
        final String salt = Security.salt();
        final String tempcode = Security.tempCode();
        final String hashtempcode = Security.hash(tempcode, salt);
        final String playerName = generatePlayerName();

        final Entity newPlayer = entities.create()
                .addComponent(CoreComponent.PLAYER)
                .addComponent(CoreComponent.PERSISTENT)
                .addComponent(CoreComponent.NAME, playerName)
                .addComponent(CoreComponent.CONTAINER)
                .addComponent(CoreComponent.HUD, playerName + "@Lobby");

        final Entity newAccount = entities.create()
                .addComponent(ServerComponent.ACCOUNT)
                .addComponent(ServerComponent.SESSION_REF, sessionId)
                .addComponent(CoreComponent.PLAYER_REF, newPlayer.identifier())
                .addComponent(ServerComponent.EMAIL_ADDRESS, email)
                .addComponent(ServerComponent.SALT, salt)
                .addComponent(ServerComponent.TEMP_CODE, hashtempcode)
                .addComponent(ServerComponent.TEMP_CODE_TIMESTAMP, System.currentTimeMillis())
                .addComponent(ServerComponent.TEMP_CODE_ATTEMPTS, 0L);

        newPlayer.addComponent(ServerComponent.ACCOUNT_REF, newAccount.identifier());

        newPlayer.persist();
        newAccount.persist();

        emailService.sendEmail(email, ACCT_EMAIL_SUBJ, String.format(ACCT_EMAIL_BODY, playerName));
        emailService.sendEmail(email, CODE_EMAIL_SUBJ, String.format(CODE_EMAIL_BODY, tempcode));

        return Directive.PRINT.create(sessionId, String.format(NEW_ACCOUNT, playerName));
    }

    public ClientMessage handleLoginMessage(final ClientMessage m) {
        return accountByEmail(m.payloadPart(0))
                .map(e -> processLogin(e, m.sessionId(), m.payloadPart(1)))
                .orElseGet(() -> createNewAccount(m.payloadPart(0), m.sessionId()));
    }
}
