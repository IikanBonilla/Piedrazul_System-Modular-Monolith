package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.infrastructure.security.CurrentSessionService;
import com.groupsoft.piedrazul.infrastructure.service.RegisterAccountService;
import com.groupsoft.piedrazul.user.application.dto.RegisterUserRequest;
import com.groupsoft.piedrazul.user.application.dto.RegisterUserResponse;
import com.groupsoft.piedrazul.user.application.dto.SessionProfile;
import com.groupsoft.piedrazul.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registro local y perfil de la sesión Keycloak")
public class AuthController {

    private final RegisterAccountService registerAccountService;
    private final CurrentSessionService currentSessionService;

    @GetMapping("/me")
    @Operation(summary = "Ficha clínica del usuario autenticado con el JWT de Keycloak")
    public SessionProfile me(@AuthenticationPrincipal Jwt jwt) {
        return currentSessionService.profile(jwt);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar paciente o médico y crear su usuario en Keycloak")
    public RegisterUserResponse register(@RequestBody RegisterUserRequest request) {
        User user = registerAccountService.register(request);
        return new RegisterUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                "Cuenta creada. Ya puedes iniciar sesión."
        );
    }
}
