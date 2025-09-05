package de.farm.app.config;

import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;


public interface JwtService {

    public String createAccessToken(String subject, Map<String, Object> claims);

    public String createRefreshToken(String subject);

    public Jws<Claims> parse(String token);
}
