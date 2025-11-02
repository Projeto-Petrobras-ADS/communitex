package br.senai.sc.communitex.dto;

public class AuthResponse {
    public String accessToken;
    public String refreshToken;

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}