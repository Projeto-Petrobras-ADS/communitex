package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.AuthResponse;
import br.senai.sc.communitex.dto.PasswordChangeRequest;
import br.senai.sc.communitex.dto.ProfileDetailsDTO;
import br.senai.sc.communitex.dto.ProfileUpdateRequest;
import br.senai.sc.communitex.dto.TipoConta;
import br.senai.sc.communitex.exception.RegistrationValidationException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import br.senai.sc.communitex.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    private final PessoaFisicaRepository pessoaFisicaRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public ProfileDetailsDTO getCurrentProfile() {
        var username = AuthenticatedUser.username();
        return pessoaFisicaRepository.findByUsuarioUsername(username)
                .map(this::toDto)
                .orElseGet(() -> empresaRepository.buscarPorUsuarioRepresentanteUsername(username)
                        .map(this::toDto)
                        .orElseThrow(() -> new RegistrationValidationException(
                                java.util.Map.of("perfil", "Perfil não encontrado"))));
    }

    @Transactional
    public ProfileDetailsDTO updateCurrentProfile(ProfileUpdateRequest request) {
        validate(request);
        var username = AuthenticatedUser.username();
        var pessoa = pessoaFisicaRepository.findByUsuarioUsername(username);
        if (pessoa.isPresent()) {
            apply(pessoa.get(), request);
            return toDto(pessoaFisicaRepository.save(pessoa.get()));
        }

        var empresa = empresaRepository.buscarPorUsuarioRepresentanteUsername(username)
                .orElseThrow(() -> new RegistrationValidationException(
                        java.util.Map.of("perfil", "Perfil não encontrado")));
        apply(empresa, request);
        empresa.setNomeFantasia(blankToNull(request.nomeFantasia()));
        if (!isBlank(request.emailInstitucional())) {
            empresa.setEmail(request.emailInstitucional().trim().toLowerCase(Locale.ROOT));
        }
        return toDto(empresaRepository.save(empresa));
    }

    @Transactional
    public AuthResponse changePassword(PasswordChangeRequest request) {
        var errors = new LinkedHashMap<String, String>();
        var usuario = usuarioService.findByUsername(AuthenticatedUser.username())
                .orElseThrow(() -> new RegistrationValidationException(
                        java.util.Map.of("senhaAtual", "Usuário não encontrado")));

        if (request == null || isBlank(request.senhaAtual())
                || !passwordEncoder.matches(request.senhaAtual(), usuario.getPassword())) {
            errors.put("senhaAtual", "A senha atual está incorreta");
        }
        if (request == null || isBlank(request.novaSenha())
                || !STRONG_PASSWORD.matcher(request.novaSenha()).matches()) {
            errors.put("novaSenha", "Use 8 ou mais caracteres com maiúscula, minúscula, número e símbolo");
        }
        if (request == null || request.novaSenha() == null
                || !request.novaSenha().equals(request.confirmacaoSenha())) {
            errors.put("confirmacaoSenha", "As senhas devem ser iguais");
        }
        if (request != null && request.senhaAtual() != null
                && request.senhaAtual().equals(request.novaSenha())) {
            errors.put("novaSenha", "A nova senha deve ser diferente da senha atual");
        }
        if (!errors.isEmpty()) {
            throw new RegistrationValidationException(errors);
        }

        usuario.setPassword(passwordEncoder.encode(request.novaSenha()));
        var accessToken = jwtService.generateToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);
        usuario.setRefreshToken(refreshToken);
        usuarioService.save(usuario);
        return new AuthResponse(accessToken, refreshToken);
    }

    private void validate(ProfileUpdateRequest request) {
        var errors = new LinkedHashMap<String, String>();
        var phone = digits(request.telefone());
        var cep = digits(request.cep());
        if (!phone.isEmpty() && phone.length() != 10 && phone.length() != 11) {
            errors.put("telefone", "Informe um telefone com DDD");
        }
        if (!cep.isEmpty() && cep.length() != 8) {
            errors.put("cep", "Informe um CEP com 8 dígitos");
        }
        if (!isBlank(request.estado()) && !request.estado().trim().matches("[A-Za-z]{2}")) {
            errors.put("estado", "Use a sigla do estado com 2 letras");
        }
        if (!isBlank(request.emailInstitucional())
                && !EMAIL_PATTERN.matcher(request.emailInstitucional().trim()).matches()) {
            errors.put("emailInstitucional", "Informe um e-mail válido");
        }
        if (!errors.isEmpty()) {
            throw new RegistrationValidationException(errors);
        }
    }

    private void apply(PessoaFisica pessoa, ProfileUpdateRequest request) {
        pessoa.setTelefone(emptyToNull(digits(request.telefone())));
        pessoa.setCep(emptyToNull(digits(request.cep())));
        pessoa.setLogradouro(blankToNull(request.logradouro()));
        pessoa.setNumero(blankToNull(request.numero()));
        pessoa.setComplemento(blankToNull(request.complemento()));
        pessoa.setBairro(blankToNull(request.bairro()));
        pessoa.setCidade(blankToNull(request.cidade()));
        pessoa.setEstado(upperOrNull(request.estado()));
    }

    private void apply(Empresa empresa, ProfileUpdateRequest request) {
        empresa.setTelefone(emptyToNull(digits(request.telefone())));
        empresa.setCep(emptyToNull(digits(request.cep())));
        empresa.setLogradouro(blankToNull(request.logradouro()));
        empresa.setNumero(blankToNull(request.numero()));
        empresa.setComplemento(blankToNull(request.complemento()));
        empresa.setBairro(blankToNull(request.bairro()));
        empresa.setCidade(blankToNull(request.cidade()));
        empresa.setEstado(upperOrNull(request.estado()));
    }

    private ProfileDetailsDTO toDto(PessoaFisica pessoa) {
        return new ProfileDetailsDTO(TipoConta.PESSOA_FISICA, pessoa.getNome(), pessoa.getCpf(),
                pessoa.getEmail(), null, pessoa.getTelefone(), pessoa.getCep(),
                pessoa.getLogradouro(), pessoa.getNumero(), pessoa.getComplemento(), pessoa.getBairro(),
                pessoa.getCidade(), pessoa.getEstado(), null, null,
                isComplete(pessoa.getTelefone(), pessoa.getCep(), pessoa.getLogradouro(), pessoa.getNumero(),
                        pessoa.getBairro(), pessoa.getCidade(), pessoa.getEstado()));
    }

    private ProfileDetailsDTO toDto(Empresa empresa) {
        var usuario = empresa.getUsuarioRepresentante();
        return new ProfileDetailsDTO(TipoConta.EMPRESA,
                usuario == null ? null : usuario.getNome(), empresa.getCnpj(),
                usuario == null ? null : usuario.getUsername(), empresa.getRazaoSocial(), empresa.getTelefone(), empresa.getCep(),
                empresa.getLogradouro(), empresa.getNumero(), empresa.getComplemento(), empresa.getBairro(),
                empresa.getCidade(), empresa.getEstado(), empresa.getNomeFantasia(), empresa.getEmail(),
                isComplete(empresa.getTelefone(), empresa.getCep(), empresa.getLogradouro(), empresa.getNumero(),
                        empresa.getBairro(), empresa.getCidade(), empresa.getEstado()));
    }

    private boolean isComplete(String... requiredFields) {
        for (String field : requiredFields) {
            if (isBlank(field)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String digits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private static String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    private static String upperOrNull(String value) {
        return isBlank(value) ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
