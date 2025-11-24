package me.soilmonitoring.iam.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Argon2UtilityTest {

    private Argon2Utility utility;

    @BeforeEach
    void setUp() {
        utility = new Argon2Utility();
    }

    @Test
    void hash_ShouldReturnHashedPassword() {
        char[] password = "testPassword".toCharArray();
        String hashedPassword = utility.hash(password);

        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(new String(password), hashedPassword, "Hashed password should not match raw password");
    }

    @Test
    void check_ShouldReturnTrueForCorrectPassword() {
        char[] password = "testPassword".toCharArray();
        String hashedPassword = utility.hash(password);

        assertTrue(utility.check(hashedPassword, "testPassword".toCharArray()),
                "Password verification should succeed for correct password");
    }

    @Test
    void check_ShouldReturnFalseForIncorrectPassword() {
        char[] password = "testPassword".toCharArray();
        String hashedPassword = utility.hash(password);

        assertFalse(utility.check(hashedPassword, "wrongPassword".toCharArray()),
                "Password verification should fail for incorrect password");
    }

    @Test
    void generate_ShouldReturnHashedPassword() {
        char[] password = "anotherPassword".toCharArray();
        String hashedPassword = utility.generate(password);

        assertNotNull(hashedPassword, "Generated hash should not be null");
        assertNotEquals(new String(password), hashedPassword, "Generated hash should not match raw password");
    }

    @Test
    void verify_ShouldReturnTrueForCorrectPassword() {
        char[] password = "verifyPassword".toCharArray();
        String hashedPassword = utility.generate(password);

        assertTrue(utility.verify(password, hashedPassword),
                "Verification should succeed for correct password");
    }

    @Test
    void verify_ShouldReturnFalseForIncorrectPassword() {
        char[] password = "verifyPassword".toCharArray();
        String hashedPassword = utility.generate(password);

        assertFalse(utility.verify("wrongPassword".toCharArray(), hashedPassword),
                "Verification should fail for incorrect password");
    }

    @Test
    void multipleHashes_ShouldProduceDifferentHashes() {
        char[] password = "samePassword".toCharArray();
        String hash1 = utility.hash(password.clone());
        String hash2 = utility.hash(password.clone());

        assertNotEquals(hash1, hash2, "Two hashes of the same password should be different due to random salt");
    }
}
