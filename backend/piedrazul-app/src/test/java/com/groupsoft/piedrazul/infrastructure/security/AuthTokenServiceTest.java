package com.groupsoft.piedrazul.infrastructure.security;

import com.groupsoft.piedrazul.user.application.dto.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthTokenServiceTest {

    private final AuthTokenService service = new AuthTokenService();

    @Test
    void issuesATokenThatCanBeResolved() {
        IssuedToken issued = service.issue(new AuthenticatedUser(
                4L, "admin", "Administrador", "ADMINISTRATOR", null, "admin@piedrazul.local", "3000000001"
        ));

        IssuedToken resolved = service.resolve(issued.token()).orElseThrow();

        assertEquals("ADMINISTRATOR", resolved.role());
        assertEquals(4L, resolved.userId());
    }

    @Test
    void rejectsAnUnknownToken() {
        assertTrue(service.resolve("no-existe").isEmpty());
        assertTrue(service.resolve(" ").isEmpty());
    }
}
