package de.farm.app.config;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtServiceImpl implements JwtService {
    
    private final Key key;
    private final String issuer;
    private final long accessTtlMillis;
    private final long refreshTtlMillis;

    public JwtServiceImpl(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.issuer}") String issuer,
        @Value("${jwt.accessTokenTtlMinutes}") long accessTtlMillis,
        @Value("${jwt.refreshTokenTtlDays}") long refreshTtlMillis
    ){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.issuer = issuer;
        this.accessTtlMillis = accessTtlMillis * 60_000;
        this.refreshTtlMillis = refreshTtlMillis * 24L * 60 * 60 * 1000;
    }

    @Override
    public String createAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();

        return Jwts.builder()
            .setIssuer(issuer)
            .setSubject(subject)
            .addClaims(claims)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusMillis(accessTtlMillis)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    @Override
    public String createRefreshToken(String subject) {
        Instant now = Instant.now();

        return Jwts.builder()
            .setIssuer(issuer)
            .setSubject(subject)
            .claim("typ", "refresh")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusMillis(refreshTtlMillis)))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    @Override
    public Jws<Claims> parse(String token) {
        
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .requireIssuer(issuer)
            .build()
            .parseClaimsJws(token);
    }

}
