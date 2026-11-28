package xyz.zlatanov.frakkintoasters.backend.user;

/**
 * Sends a login code to an email address. Without one, the email login is off.
 */
public interface LoginMail {

    void send(String email, String code);
}
