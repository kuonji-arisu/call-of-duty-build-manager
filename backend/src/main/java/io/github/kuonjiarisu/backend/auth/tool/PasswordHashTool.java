package io.github.kuonjiarisu.backend.auth.tool;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PasswordHashTool {

    private PasswordHashTool() {
    }

    public static void main(String[] args) {
        if (args.length != 1 || args[0] == null || args[0].isBlank()) {
            System.err.println("Usage: ./gradlew passwordHash --args \"your-password\"");
            System.exit(1);
        }

        System.out.println(new BCryptPasswordEncoder().encode(args[0]));
    }
}
