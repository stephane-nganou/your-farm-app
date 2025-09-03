package de.farm.app.users;

import java.util.Map;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.farm.app.config.JwtService;
import de.farm.app.users.dto.AuthResponse;
import de.farm.app.users.dto.LoginRequest;
import de.farm.app.users.dto.RegisterRequest;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    // ToDo: Add dto validations
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        
        repo.findByEmailIgnoreCase(request.email())
            .ifPresent(user -> {
                throw new IllegalArgumentException("Email already registered");
            });

        var user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(encoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRoles(Set.of(Role.CUSTOMER));
        repo.save(user);
        return tokensFor(user);

    }

    @Override
    public AuthResponse login(LoginRequest request) {

        var user = repo.findByEmailIgnoreCase(request.email().toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!encoder.matches(request.password(), user.getPasswordHash())) 
            throw new IllegalArgumentException("Invalid credentials");
        return tokensFor(user);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        
        Claims claims = jwt.parse(refreshToken).getBody();
        if(!"refresh".equals(claims.get("typ")))
            throw new IllegalArgumentException("Invalid refresh token");
        var userId = java.util.UUID.fromString(claims.getSubject());
        var user = repo.findById(userId).orElseThrow(); // will throw an exception, that will be handled

        return tokensFor(user);
    }

    private AuthResponse tokensFor(User user) {
        var access = jwt.createAccessToken(user.getId().toString(), Map.of("roles", user.getRoles()));
        var refresh = jwt.createRefreshToken(user.getId().toString());
        return new AuthResponse(access, refresh);
  }

}
