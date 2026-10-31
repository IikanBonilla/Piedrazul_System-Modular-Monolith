package com.groupsoft.piedrazul.user.application.port;

/**
 * Alta de la identidad de acceso. La contraseña y el rol de autorización
 * viven en el proveedor (Keycloak), no en el monolito.
 */
public interface IdentityProvisioner {

    void provision(String username, String email, String fullName, String rawPassword, String role);

    void rollback(String username);
}
