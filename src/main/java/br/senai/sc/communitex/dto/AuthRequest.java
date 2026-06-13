package br.senai.sc.communitex.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "O username é obrigatório")
        String username,

        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}