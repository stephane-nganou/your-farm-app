package de.farm.app.users.dto;

public record LoginRequest(
    String email,
    String password) {

}
