package br.senai.sc.communitex.controller;

import br.senai.sc.communitex.dto.AuthRequest;
import br.senai.sc.communitex.dto.RefreshRequest;
import br.senai.sc.communitex.dto.RegisterRequest;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.service.JwtService;
import br.senai.sc.communitex.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void givenValidCredentials_whenLogin_thenReturnsTokens() throws Exception {
        var request = new AuthRequest();
        request.username = "murilo@communitex.com";
        request.password = "senha123";

        var user = new Usuario();
        user.setId(1L);
        user.setUsername("murilo@communitex.com");
        user.setPassword("hash");
        user.setRole("ROLE_USER");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.username, request.password));
        when(userDetailsService.loadUserByUsername(request.username)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(usuarioService.findByUsername(request.username)).thenReturn(Optional.of(user));
        when(usuarioService.save(user)).thenReturn(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void givenInvalidCredentials_whenLogin_thenReturnsInternalServerError() throws Exception {
        var request = new AuthRequest();
        request.username = "murilo@communitex.com";
        request.password = "errada";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void givenExistingUsername_whenRegister_thenReturnsBadRequest() throws Exception {
        var request = new RegisterRequest();
        request.setUsername("murilo@communitex.com");
        request.setPassword("senha123");

        when(usuarioService.findByUsername("murilo@communitex.com")).thenReturn(Optional.of(new Usuario()));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erro: Nome de usuário já está em uso!"));
    }

    @Test
    void givenNewUsername_whenRegister_thenReturnsOk() throws Exception {
        var request = new RegisterRequest();
        request.setUsername("novo@communitex.com");
        request.setPassword("senha123");
        request.setRole("ADMIN");

        when(usuarioService.findByUsername("novo@communitex.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("senha-hash");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuário registrado com sucesso!"));
    }

    @Test
    void givenValidRefreshToken_whenRefresh_thenReturnsNewAccessToken() throws Exception {
        var refreshRequest = new RefreshRequest();
        refreshRequest.refreshToken = "refresh-token";

        var user = new Usuario();
        user.setUsername("murilo@communitex.com");
        user.setPassword("hash");
        user.setRole("ROLE_USER");
        user.setRefreshToken("refresh-token");

        when(jwtService.extractUsername("refresh-token")).thenReturn("murilo@communitex.com");
        when(usuarioService.findByUsername("murilo@communitex.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("refresh-token", user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("novo-access-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("novo-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void givenInvalidRefreshToken_whenRefresh_thenReturnsInternalServerError() throws Exception {
        var refreshRequest = new RefreshRequest();
        refreshRequest.refreshToken = "refresh-invalido";

        var user = new Usuario();
        user.setUsername("murilo@communitex.com");
        user.setPassword("hash");
        user.setRole("ROLE_USER");
        user.setRefreshToken("refresh-cadastrado");

        when(jwtService.extractUsername("refresh-invalido")).thenReturn("murilo@communitex.com");
        when(usuarioService.findByUsername("murilo@communitex.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(eq("refresh-invalido"), eq(user))).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}


