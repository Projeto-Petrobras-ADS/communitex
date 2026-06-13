package br.senai.sc.communitex.dto;

import br.senai.sc.communitex.enums.IssueStatus;
import jakarta.validation.constraints.NotNull;

public record IssueStatusUpdateRequest(
        @NotNull(message = "O status é obrigatório")
        IssueStatus status
) {
}