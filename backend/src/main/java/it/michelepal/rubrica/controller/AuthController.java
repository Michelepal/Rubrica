package it.michelepal.rubrica.controller;

import it.michelepal.rubrica.dto.LoginRequest;
import it.michelepal.rubrica.dto.LoginResponse;
import it.michelepal.rubrica.security.JwtService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        return new LoginResponse(jwtService.generateToken(userDetails), "Bearer", userDetails.getUsername());
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("message", "Logout completato.");
    }

    @GetMapping("/me")
    public Map<String, String> me(Principal principal) {
        return Map.of("username", principal.getName());
    }
}

