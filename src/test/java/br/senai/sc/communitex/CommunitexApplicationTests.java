package br.senai.sc.communitex;

import br.senai.sc.communitex.config.DevDataSeeder;
import br.senai.sc.communitex.repository.AdocaoRepository;
import br.senai.sc.communitex.repository.AtendimentoDenunciaRepository;
import br.senai.sc.communitex.repository.DenunciaInteracaoRepository;
import br.senai.sc.communitex.repository.DenunciaRepository;
import br.senai.sc.communitex.repository.EmpresaRepository;
import br.senai.sc.communitex.repository.PessoaFisicaRepository;
import br.senai.sc.communitex.repository.PracaRepository;
import br.senai.sc.communitex.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CommunitexApplicationTests {

	@Autowired
	private DevDataSeeder seeder;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PessoaFisicaRepository pessoaFisicaRepository;

	@Autowired
	private EmpresaRepository empresaRepository;

	@Autowired
	private PracaRepository pracaRepository;

	@Autowired
	private AdocaoRepository adocaoRepository;

	@Autowired
	private DenunciaRepository denunciaRepository;

	@Autowired
	private DenunciaInteracaoRepository interacaoRepository;

	@Autowired
	private AtendimentoDenunciaRepository atendimentoRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void givenApplicationContext_whenStart_thenLoadsSuccessfully() {
	}

	@Test
	void givenDevSeeder_whenRunAgain_thenKeepsDataAndSharedPassword() throws Exception {
		List<Long> countsBefore = repositoryCounts();

		seeder.run();

		assertEquals(countsBefore, repositoryCounts());
		for (String username : List.of(
				"admin",
				"ana.souza@exemplo.com",
				"bruno.lima@exemplo.com",
				"contato@verdevida.com.br",
				"contato@urbanasolucoes.com.br")) {
			var user = usuarioRepository.findByUsername(username).orElseThrow();
			assertTrue(passwordEncoder.matches("password", user.getPassword()));
		}
	}

	@Test
	void givenDevDataSeeder_thenItIsRestrictedToDevProfile() {
		Profile profile = DevDataSeeder.class.getAnnotation(Profile.class);

		assertArrayEquals(new String[]{"dev"}, profile.value());
	}

	@Test
	void givenAnonymousVisitor_whenRequestPublicDashboard_thenAllowsAccess() throws Exception {
		mockMvc.perform(get("/api/dashboard/publico"))
				.andExpect(status().isOk());
	}

	private List<Long> repositoryCounts() {
		return List.of(
				usuarioRepository.count(),
				pessoaFisicaRepository.count(),
				empresaRepository.count(),
				pracaRepository.count(),
				adocaoRepository.count(),
				denunciaRepository.count(),
				interacaoRepository.count(),
				atendimentoRepository.count());
	}
}
