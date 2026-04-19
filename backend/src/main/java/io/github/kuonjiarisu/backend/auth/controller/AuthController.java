package io.github.kuonjiarisu.backend.auth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.kuonjiarisu.backend.auth.model.AuthUserView;
import io.github.kuonjiarisu.backend.auth.model.CaptchaResponse;
import io.github.kuonjiarisu.backend.auth.model.LoginRequest;
import io.github.kuonjiarisu.backend.auth.model.LoginResponse;
import io.github.kuonjiarisu.backend.auth.model.LogoutResponse;
import io.github.kuonjiarisu.backend.auth.security.AuthenticatedUser;
import io.github.kuonjiarisu.backend.auth.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/captcha")
    public CaptchaResponse createCaptcha() {
        return authService.createCaptcha();
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public LogoutResponse logout() {
        return authService.logout();
    }

    @GetMapping("/me")
    public AuthUserView me(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return authenticatedUser.toView();
    }
}
