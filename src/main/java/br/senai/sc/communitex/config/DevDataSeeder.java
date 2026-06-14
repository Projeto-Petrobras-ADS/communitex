package br.senai.sc.communitex.config;

import br.senai.sc.communitex.enums.*;
import br.senai.sc.communitex.model.*;
import br.senai.sc.communitex.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    static final String SHARED_PASSWORD = "password";

    private final UsuarioRepository usuarioRepository;
    private final PessoaFisicaRepository pessoaFisicaRepository;
    private final EmpresaRepository empresaRepository;
    private final PracaRepository pracaRepository;
    private final AdocaoRepository adocaoRepository;
    private final DenunciaRepository denunciaRepository;
    private final DenunciaInteracaoRepository interacaoRepository;
    private final AtendimentoDenunciaRepository atendimentoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        String password = passwordEncoder.encode(SHARED_PASSWORD);
        getOrCreateUser("admin", "Administrador Communitex", "ROLE_ADMIN", password);
        Usuario anaUser = getOrCreateUser("ana.souza@exemplo.com", "Ana Souza", "ROLE_USER", password);
        Usuario brunoUser = getOrCreateUser("bruno.lima@exemplo.com", "Bruno Lima", "ROLE_USER", password);
        Usuario verdeUser = getOrCreateUser("contato@verdevida.com.br", "Marina Costa", "ROLE_EMPRESA", password);
        Usuario urbanaUser = getOrCreateUser("contato@urbanasolucoes.com.br", "Carlos Mendes", "ROLE_EMPRESA", password);

        PessoaFisica ana = getOrCreatePerson("11144477735", "Ana Souza", "ana.souza@exemplo.com", "48999990001",
                "88010000", "Rua das Flores", "120", "Centro", "Florianopolis", "SC", anaUser);
        PessoaFisica bruno = getOrCreatePerson("52998224725", "Bruno Lima", "bruno.lima@exemplo.com", "48999990002",
                "88015000", "Avenida Mauro Ramos", "850", "Centro", "Florianopolis", "SC", brunoUser);

        Empresa verde = getOrCreateCompany("11222333000181", "Verde Vida Sustentabilidade Ltda", "Verde Vida",
                "contato@verdevida.com.br", "4833331000", "88020000", "Rua Bocaiuva", "500", "Centro",
                "Florianopolis", "SC", verdeUser);
        Empresa urbana = getOrCreateCompany("45987654000110", "Urbana Solucoes Ambientais Ltda", "Urbana Solucoes",
                "contato@urbanasolucoes.com.br", "4833332000", "88034000", "Rua Deputado Antonio Edu Vieira", "900",
                "Pantanal", "Florianopolis", "SC", urbanaUser);

        Praca liberdade = getOrCreateSquare("Praca da Liberdade", "Rua Tenente Silveira", "Centro",
                -27.5954, -48.5480, 3200.0, StatusPraca.DISPONIVEL, ana);
        Praca esperanca = getOrCreateSquare("Praca da Esperanca", "Rua Lauro Linhares", "Trindade",
                -27.5885, -48.5205, 4800.0, StatusPraca.EM_PROCESSO, bruno);
        Praca natureza = getOrCreateSquare("Parque Natureza Viva", "Avenida Madre Benvenuta", "Santa Monica",
                -27.5908, -48.5095, 7500.0, StatusPraca.ADOTADA, ana);

        createAdoptionIfMissing(verde, esperanca, StatusAdocao.EM_ANALISE,
                "Revitalizacao do playground, paisagismo e manutencao mensal.", LocalDate.now().minusDays(20), null);
        createAdoptionIfMissing(urbana, natureza, StatusAdocao.APROVADA,
                "Manutencao das areas verdes e instalacao de lixeiras seletivas.",
                LocalDate.now().minusMonths(4), LocalDate.now().plusMonths(8));
        createAdoptionIfMissing(urbana, liberdade, StatusAdocao.REJEITADA,
                "Instalacao de mobiliario urbano sustentavel.", LocalDate.now().minusMonths(2), null);

        Denuncia luz = getOrCreateIssue("Poste sem iluminacao na praca", "Dois postes estao apagados ha varios dias.",
                -27.5953, -48.5481, IssueStatus.ABERTA, IssueType.ILUMINACAO, anaUser);
        Denuncia calcada = getOrCreateIssue("Calcada danificada perto da escola",
                "Trecho da calcada apresenta risco para pedestres.", -27.5887, -48.5207,
                IssueStatus.EM_ANDAMENTO, IssueType.CALCADA_DANIFICADA, brunoUser);
        Denuncia lixo = getOrCreateIssue("Descarte irregular de lixo", "Ha residuos acumulados ao lado das lixeiras.",
                -27.5907, -48.5093, IssueStatus.RESOLVIDA, IssueType.LIXO, anaUser);
        Denuncia vazamento = getOrCreateIssue("Vazamento proximo ao playground",
                "Agua escorrendo continuamente pela calcada.", -27.5890, -48.5210,
                IssueStatus.CONTESTADA, IssueType.VAZAMENTO, brunoUser);

        createInteractionIfMissing(luz, brunoUser, InteractionType.APOIO, null);
        createInteractionIfMissing(luz, brunoUser, InteractionType.COMENTARIO, "O problema tambem afeta a rua lateral.");
        createInteractionIfMissing(calcada, anaUser, InteractionType.CURTIDA, null);
        createInteractionIfMissing(lixo, brunoUser, InteractionType.COMENTARIO,
                "A limpeza foi realizada e o local esta organizado.");

        createServiceIfMissing(calcada, verde, AtendimentoDenunciaStatus.EM_ANDAMENTO,
                "Nivelar o trecho e substituir as placas danificadas.", null, null);
        createServiceIfMissing(lixo, urbana, AtendimentoDenunciaStatus.CONFIRMADO_PELO_AUTOR,
                "Remover residuos e instalar sinalizacao educativa.",
                "Residuos removidos e duas placas educativas instaladas.", null);
        createServiceIfMissing(vazamento, urbana, AtendimentoDenunciaStatus.CONTESTADO,
                "Localizar a origem e reparar a tubulacao.", "Tubulacao reparada e area limpa.",
                "O vazamento voltou a aparecer apos o reparo.");

        log.info("Seed de desenvolvimento concluido. Usuarios ficticios usam a senha '{}'.", SHARED_PASSWORD);
    }

    private Usuario getOrCreateUser(String username, String name, String role, String password) {
        return usuarioRepository.findByUsername(username).orElseGet(() -> usuarioRepository.save(Usuario.builder()
                .username(username).nome(name).role(role).password(password).build()));
    }

    private PessoaFisica getOrCreatePerson(String cpf, String name, String email, String phone, String cep,
                                           String street, String number, String district, String city, String state,
                                           Usuario user) {
        return pessoaFisicaRepository.findByCpf(cpf).orElseGet(() -> pessoaFisicaRepository.save(PessoaFisica.builder()
                .cpf(cpf).nome(name).email(email).telefone(phone).cep(cep).logradouro(street).numero(number)
                .bairro(district).cidade(city).estado(state).usuario(user).build()));
    }

    private Empresa getOrCreateCompany(String cnpj, String legalName, String tradeName, String email, String phone,
                                       String cep, String street, String number, String district, String city,
                                       String state, Usuario user) {
        return empresaRepository.buscarPorCnpj(cnpj).orElseGet(() -> empresaRepository.save(Empresa.builder()
                .cnpj(cnpj).razaoSocial(legalName).nomeFantasia(tradeName).email(email).telefone(phone).cep(cep)
                .logradouro(street).numero(number).bairro(district).cidade(city).estado(state)
                .usuarioRepresentante(user).build()));
    }

    private Praca getOrCreateSquare(String name, String street, String district, double latitude, double longitude,
                                    double area, StatusPraca status, PessoaFisica creator) {
        return pracaRepository.findByNomeAndCidade(name, "Florianopolis").orElseGet(() -> pracaRepository.save(
                Praca.builder().nome(name).cidade("Florianopolis").logradouro(street).bairro(district)
                        .latitude(latitude).longitude(longitude).metragemM2(area).status(status)
                        .descricao("Espaco publico ficticio criado para demonstracao.").cadastradoPor(creator).build()));
    }

    private void createAdoptionIfMissing(Empresa company, Praca square, StatusAdocao status, String description,
                                         LocalDate start, LocalDate end) {
        if (!adocaoRepository.existsByEmpresaIdAndPracaIdAndStatusIn(
                company.getId(), square.getId(), Arrays.asList(StatusAdocao.values()))) {
            adocaoRepository.save(Adocao.builder().empresa(company).praca(square).status(status)
                    .descricaoProjeto(description).dataInicio(start).dataFim(end).build());
        }
    }

    private Denuncia getOrCreateIssue(String title, String description, double latitude, double longitude,
                                      IssueStatus status, IssueType type, Usuario author) {
        return denunciaRepository.findByTituloAndAutorId(title, author.getId()).orElseGet(() -> denunciaRepository.save(
                Denuncia.builder().titulo(title).descricao(description).latitude(latitude).longitude(longitude)
                        .status(status).tipo(type).autor(author).build()));
    }

    private void createInteractionIfMissing(Denuncia issue, Usuario user, InteractionType type, String content) {
        if (interacaoRepository.findByIssueIdAndUsuarioIdAndTipo(issue.getId(), user.getId(), type).isEmpty()) {
            interacaoRepository.save(DenunciaInteracao.builder()
                    .issue(issue).usuario(user).tipo(type).conteudo(content).build());
        }
    }

    private void createServiceIfMissing(Denuncia issue, Empresa company, AtendimentoDenunciaStatus status,
                                        String planned, String repair, String dispute) {
        if (!atendimentoRepository.existsByDenunciaId(issue.getId())) {
            LocalDateTime now = LocalDateTime.now();
            atendimentoRepository.save(AtendimentoDenuncia.builder()
                    .denuncia(issue).empresa(company).status(status).descricaoPlanejada(planned).descricaoReparo(repair)
                    .motivoContestacao(dispute).dataAceite(now.minusDays(15)).dataInicio(now.minusDays(12))
                    .dataConclusaoEmpresa(repair == null ? null : now.minusDays(3))
                    .dataConfirmacaoAutor(status == AtendimentoDenunciaStatus.CONFIRMADO_PELO_AUTOR
                            ? now.minusDays(2) : null)
                    .build());
        }
    }
}
