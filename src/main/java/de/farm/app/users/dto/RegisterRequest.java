package de.farm.app.users.dto;

public record RegisterRequest(
    String email,
    String password,
    String fullName,
    String phone
) {

}
