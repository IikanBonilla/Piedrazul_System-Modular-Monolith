package com.groupsoft.piedrazul.infrastructure.web;

import com.groupsoft.piedrazul.infrastructure.security.AuthTokenService;
import com.groupsoft.piedrazul.infrastructure.security.IssuedToken;
import com.groupsoft.piedrazul.infrastructure.service.RegisterAccountService;
import com.groupsoft.piedrazul.user.application.dto.AuthenticatedUser;
import com.groupsoft.piedrazul.user.application.dto.LoginRequest;
import com.groupsoft.piedrazul.user.application.dto.LoginResponse;
import com.groupsoft.piedrazul.user.application.dto.RegisterUserRequest;
import com.groupsoft.piedrazul.user.application.dto.RegisterUserResponse;
import com.groupsoft.piedrazul.user.application.usecase.LoginUseCase;
import com.groupsoft.piedrazul.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Inicio de sesión y registro")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterAccountService registerAccountService;
    private final AuthTokenService authTokenService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión con usuario y contraseña")
    public LoginResponse login(@RequestBody LoginRequest request) {
        AuthenticatedUser user = loginUseCase.execute(request);
        IssuedToken issued = authTokenService.issue(user);
        return new LoginResponse(
                issued.token(),
                issued.expiresAt(),
                user.id(),
                user.username(),
                user.fullName(),
                user.role(),
                user.doctorId(),
                user.email(),
                user.phone()
        );
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar paciente o médico")
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
