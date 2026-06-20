package br.senai.sc.communitex.service;

import br.senai.sc.communitex.dto.RegistrationResponse;
import br.senai.sc.communitex.dto.TipoConta;
import br.senai.sc.communitex.dto.UnifiedRegisterRequest;
import br.senai.sc.communitex.exception.RegistrationValidationException;
import br.senai.sc.communitex.model.Empresa;
import br.senai.sc.communitex.model.PessoaFisica;
import br.senai.sc.communitex.model.Usuario;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    private final UsuarioService usuarioService;
    private final PessoaFisicaRepository pessoaFisicaRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public RegistrationResponse register(UnifiedRegisterRequest request) {
        var errors = validate(request);
        if (!errors.isEmpty()) {
            throw new RegistrationValidationException(errors);
        }

        var email = normalizeEmail(request.email());
        validateDuplicates(request, email);

        var displayName = request.tipoConta() == TipoConta.PESSOA_FISICA
                ? request.nome().trim()
                : request.nomeRepresentante().trim();
        var role = request.tipoConta() == TipoConta.PESSOA_FISICA ? "ROLE_USER" : "ROLE_EMPRESA";
        var usuario = Usuario.builder()
                .username(email)
                .password(passwordEncoder.encode(request.senha()))
                .role(role)
                .nome(displayName)
                .build();
        usuarioService.save(usuario);

        Long perfilId = request.tipoConta() == TipoConta.PESSOA_FISICA
                ? createPessoaFisica(request, email, usuario)
                : createEmpresa(request, email, usuario);

        var accessToken = jwtService.generateToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);
        usuario.setRefreshToken(refreshToken);
        usuarioService.save(usuario);

        return new RegistrationResponse(
                accessToken, refreshToken, request.tipoConta(), perfilId, displayName, email);
    }

    private Long createPessoaFisica(UnifiedRegisterRequest request, String email, Usuario usuario) {
        var pessoa = PessoaFisica.builder()
                .nome(request.nome().trim())
                .cpf(digits(request.cpf()))
                .email(email)
                .usuario(usuario)
                .build();
        return pessoaFisicaRepository.save(pessoa).getId();
    }

    private Long createEmpresa(UnifiedRegisterRequest request, String email, Usuario usuario) {
        var empresa = Empresa.builder()
                .razaoSocial(request.razaoSocial().trim())
                .cnpj(digits(request.cnpj()))
                .email(email)
                .usuarioRepresentante(usuario)
                .build();
        return empresaRepository.save(empresa).getId();
    }

    private Map<String, String> validate(UnifiedRegisterRequest request) {
        var errors = new LinkedHashMap<String, String>();
        if (request == null || request.tipoConta() == null) {
            errors.put("tipoConta", "Escolha entre morador e empresa");
        }
        if (request == null) {
            return errors;
        }

        if (isBlank(request.email()) || !EMAIL_PATTERN.matcher(request.email().trim()).matches()) {
            errors.put("email", "Informe um e-mail válido");
        }
        if (isBlank(request.senha()) || !STRONG_PASSWORD.matcher(request.senha()).matches()) {
            errors.put("senha", "Use 8 ou mais caracteres com maiúscula, minúscula, número e símbolo");
        }
        if (request.senha() == null || !request.senha().equals(request.confirmacaoSenha())) {
            errors.put("confirmacaoSenha", "As senhas devem ser iguais");
        }
        if (!Boolean.TRUE.equals(request.aceitouTermos())) {
            errors.put("aceitouTermos", "Você deve aceitar os termos de uso");
        }

        if (request.tipoConta() == TipoConta.PESSOA_FISICA) {
            if (isBlank(request.nome())) {
                errors.put("nome", "Informe seu nome completo");
            }
            if (!isValidCpf(digits(request.cpf()))) {
                errors.put("cpf", "Informe um CPF válido");
            }
        } else if (request.tipoConta() == TipoConta.EMPRESA) {
            if (isBlank(request.razaoSocial())) {
                errors.put("razaoSocial", "Informe a razão social");
            }
            if (!isValidCnpj(digits(request.cnpj()))) {
                errors.put("cnpj", "Informe um CNPJ válido");
            }
            if (isBlank(request.nomeRepresentante())) {
                errors.put("nomeRepresentante", "Informe o nome do representante");
            }
        }
        return errors;
    }

    private void validateDuplicates(UnifiedRegisterRequest request, String email) {
        var errors = new LinkedHashMap<String, String>();
        if (usuarioService.findByUsername(email).isPresent()
                || pessoaFisicaRepository.findByEmail(email).isPresent()) {
            errors.put("email", "Este e-mail já possui cadastro");
        }
        if (request.tipoConta() == TipoConta.PESSOA_FISICA
                && pessoaFisicaRepository.findByCpf(digits(request.cpf())).isPresent()) {
            errors.put("cpf", "Este CPF já possui cadastro");
        }
        if (request.tipoConta() == TipoConta.EMPRESA
                && empresaRepository.buscarPorCnpj(digits(request.cnpj())).isPresent()) {
            errors.put("cnpj", "Este CNPJ já possui cadastro");
        }
        if (!errors.isEmpty()) {
            throw new RegistrationValidationException(errors);
        }
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String digits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    public static boolean isValidCpf(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return checkDigit(cpf, 9, 10) == cpf.charAt(9) - '0'
                && checkDigit(cpf, 10, 11) == cpf.charAt(10) - '0';
    }

    public static boolean isValidCnpj(String cnpj) {
        if (cnpj.length() != 14 || cnpj.chars().distinct().count() == 1) {
            return false;
        }
        int[] firstWeights = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] secondWeights = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return cnpjDigit(cnpj, firstWeights) == cnpj.charAt(12) - '0'
                && cnpjDigit(cnpj, secondWeights) == cnpj.charAt(13) - '0';
    }

    private static int checkDigit(String value, int length, int weight) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (value.charAt(i) - '0') * (weight - i);
        }
        int result = 11 - (sum % 11);
        return result >= 10 ? 0 : result;
    }

    private static int cnpjDigit(String value, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += (value.charAt(i) - '0') * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
