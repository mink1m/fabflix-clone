import org.jasypt.util.password.StrongPasswordEncryptor;

public class VerifyPassword {

    /*
     * After you update the passwords in customers table,
     *   you can use this program as an example to verify the password.
     *
     * Verify the password is simple:
     * success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
     *
     * Note that you need to use the same StrongPasswordEncryptor when encrypting the passwords
     *
     */

    public static boolean verifyCredentials(String password_plain, String password_encrypt) {

        boolean success = false;
        // use the same encryptor to compare the user input password with encrypted password stored in DB
        success = new StrongPasswordEncryptor().checkPassword(password_plain, password_encrypt);
        return success;
    }

}