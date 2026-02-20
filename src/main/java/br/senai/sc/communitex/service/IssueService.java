package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.IssueDetailResponseDTO;
import br.senai.sc.communitex.dto.IssueInteractionRequestDTO;
import br.senai.sc.communitex.dto.IssueInteractionResponseDTO;
import br.senai.sc.communitex.dto.IssueRequestDTO;
import br.senai.sc.communitex.dto.IssueResponseDTO;

import java.util.List;

public interface IssueService {

    IssueResponseDTO create(IssueRequestDTO dto);

    IssueResponseDTO findById(Long id);

    IssueDetailResponseDTO findByIdWithDetails(Long id);

    List<IssueResponseDTO> findAll();

    List<IssueResponseDTO> findByProximity(Double latitude, Double longitude, Double radiusMeters);

    IssueResponseDTO updateStatus(Long id, String status);

    IssueInteractionResponseDTO addInteraction(Long issueId, IssueInteractionRequestDTO dto);

    void removeInteraction(Long issueId, Long interactionId);

    List<IssueInteractionResponseDTO> findInteractionsByIssueId(Long issueId);
}
