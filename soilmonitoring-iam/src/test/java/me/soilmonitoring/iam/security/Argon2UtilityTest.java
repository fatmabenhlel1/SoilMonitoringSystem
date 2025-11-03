package me.soilmonitoring.iam.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Argon2UtilityTest {

    @Test
    void hash() {
        char[] password = "testPassword".toCharArray();
        String hashedPassword = Argon2Utility.hash(password);

        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(new String(password), hashedPassword, "Hashed password should not match the raw password");
    }

    @Test
    void check() {
        char[] password = "testPassword".toCharArray();
        String hashedPassword = Argon2Utility.hash(password);

        assertTrue(Argon2Utility.check(hashedPassword, "testPassword".toCharArray()), "Password verification should succeed");
        assertFalse(Argon2Utility.check(hashedPassword, "wrongPassword".toCharArray()), "Password verification should fail for incorrect password");
    }

    @Test
    void generate() {
        char[] password = "testPassword".toCharArray();
        Argon2Utility utility = new Argon2Utility();
        String hashedPassword = utility.generate(password);

        assertNotNull(hashedPassword, "Generated password hash should not be null");
        assertNotEquals(new String(password), hashedPassword, "Generated hash should not match the raw password");
    }

    @Test
    void verify() {
        char[] password = "testPassword".toCharArray();
        Argon2Utility utility = new Argon2Utility();
        String hashedPassword = utility.generate(password);

        assertTrue(utility.verify(password, hashedPassword), "Password verification should succeed");
        assertFalse(utility.verify("wrongPassword".toCharArray(), hashedPassword), "Password verification should fail for incorrect password");
    }
}