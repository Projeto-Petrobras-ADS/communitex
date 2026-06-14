package br.senai.sc.communitex.controller;


import br.senai.sc.communitex.dto.AuthRequest;
import br.senai.sc.communitex.dto.AuthResponse;
import br.senai.sc.communitex.dto.RefreshRequest;
import br.senai.sc.communitex.dto.RegisterRequest;
import br.senai.sc.communitex.exception.ResourceNotFoundException;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.service.JwtService;
import br.senai.sc.communitex.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gerenciamento de usuários")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService,
                          UsuarioService usuarioService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Autenticar usuário")
    @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @PostMapping("/login")
    public AuthResponse createAuthenticationToken(@Valid @RequestBody AuthRequest authRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username());
        final String accessToken = jwtService.generateToken(userDetails);
        final String refreshToken = jwtService.generateRefreshToken(userDetails);

        Usuario usuario = usuarioService.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado após autenticação"));

        usuario.setRefreshToken(refreshToken);
        usuarioService.save(usuario);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Operation(summary = "Registrar novo usuário")
    @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Nome de usuário já existe ou dados inválidos")
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (usuarioService.findByUsername(registerRequest.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Erro: Nome de usuário já está em uso!");
        }
        Usuario novoUsuario = new Usuario();
        novoUsuario.setUsername(registerRequest.username());

        novoUsuario.setPassword(passwordEncoder.encode(registerRequest.password()));
        novoUsuario.setRole("ROLE_USER");
        usuarioService.save(novoUsuario);

        return ResponseEntity.ok("Usuário registrado com sucesso!");
    }

    @Operation(summary = "Renovar token de acesso")
    @ApiResponse(responseCode = "200", description = "Token renovado com sucesso")
    @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    @PostMapping("/refresh")
    public AuthResponse refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
        String requestRefreshToken = refreshRequest.refreshToken();
        String username;

        try {
            username = jwtService.extractUsername(requestRefreshToken);
        } catch (Exception ex) {
            throw new AuthenticationServiceException("Refresh token invalido ou expirado");
        }

        Usuario usuario = usuarioService.findByUsername(username)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token invalido ou expirado"));

        if (jwtService.isTokenValid(requestRefreshToken, usuario) &&
                usuario.getRefreshToken() != null &&
                usuario.getRefreshToken().equals(requestRefreshToken)) {

            String newAccessToken = jwtService.generateToken(usuario);

            return new AuthResponse(newAccessToken, requestRefreshToken);
        }

        throw new AuthenticationServiceException("Refresh token invalido ou expirado");
    }
}
