package me.soilmonitoring.iam.security;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.security.enterprise.identitystore.PasswordHash;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public class Argon2Utility implements PasswordHash {

    private static final int DEFAULT_SALT_LENGTH = 16;
    private static final int DEFAULT_HASH_LENGTH = 32;
    private static final int DEFAULT_ITERATIONS = 2;
    private static final int DEFAULT_MEMORY = 65536;
    private static final int DEFAULT_THREADS = 1;

    private final int saltLength;
    private final int hashLength;
    private final int iterations;
    private final int memory;
    private final int threads;

    public Argon2Utility() {
        Config config = ConfigProvider.getConfig();

        this.saltLength = config.getOptionalValue("argon2.saltLength", Integer.class)
                .orElse(DEFAULT_SALT_LENGTH);

        this.hashLength = config.getOptionalValue("argon2.hashLength", Integer.class)
                .orElse(DEFAULT_HASH_LENGTH);

        this.iterations = config.getOptionalValue("argon2.iterations", Integer.class)
                .orElse(DEFAULT_ITERATIONS);

        this.memory = config.getOptionalValue("argon2.memory", Integer.class)
                .orElse(DEFAULT_MEMORY);

        this.threads = config.getOptionalValue("argon2.threads", Integer.class)
                .orElse(DEFAULT_THREADS);
    }

    public String hash(char[] clientHash) {
        Argon2 argon2 = Argon2Factory.create(
                Argon2Factory.Argon2Types.ARGON2id,
                saltLength,
                hashLength
        );

        try {
            return argon2.hash(iterations, memory, threads, clientHash);
        } finally {
            argon2.wipeArray(clientHash);
        }
    }

    public boolean check(String serverHash, char[] clientHash) {
        Argon2 argon2 = Argon2Factory.create(
                Argon2Factory.Argon2Types.ARGON2id,
                saltLength,
                hashLength
        );

        try {
            return argon2.verify(serverHash, clientHash);
        } finally {
            argon2.wipeArray(clientHash);
        }
    }

    @Override
    public String generate(char[] password) {
        return hash(password.clone());
    }

    @Override
    public boolean verify(char[] password, String hashedPassword) {
        return check(hashedPassword, password.clone());
    }

}
