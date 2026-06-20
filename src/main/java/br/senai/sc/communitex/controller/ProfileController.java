package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.AuthResponse;
import br.senai.sc.communitex.dto.PasswordChangeRequest;
import br.senai.sc.communitex.dto.ProfileDetailsDTO;
import br.senai.sc.communitex.dto.ProfileUpdateRequest;
import br.senai.sc.communitex.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("communitexProfileController")
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileDetailsDTO> getCurrentProfile() {
        return ResponseEntity.ok(profileService.getCurrentProfile());
    }

    @PutMapping
    public ResponseEntity<ProfileDetailsDTO> updateCurrentProfile(@RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateCurrentProfile(request));
    }

    @PutMapping("/password")
    public ResponseEntity<AuthResponse> changePassword(@RequestBody PasswordChangeRequest request) {
        return ResponseEntity.ok(profileService.changePassword(request));
    }
}
