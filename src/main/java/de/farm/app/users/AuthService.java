package de.farm.app.users;

import org.springframework.stereotype.Service;

import de.farm.app.users.dto.AuthResponse;
import de.farm.app.users.dto.LoginRequest;
import de.farm.app.users.dto.RegisterRequest;

@Service
public interface AuthService {

    public AuthResponse register(RegisterRequest request);

    public AuthResponse login(LoginRequest request);

    public AuthResponse refresh(String refreshToken);
}
