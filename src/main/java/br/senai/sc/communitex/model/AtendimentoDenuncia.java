package br.senai.sc.communitex.model;

import br.senai.sc.communitex.enums.AtendimentoDenunciaStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "atendimentos_denuncia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtendimentoDenuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "denuncia_id", nullable = false, unique = true)
    private Denuncia denuncia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AtendimentoDenunciaStatus status;

    @Column(name = "descricao_planejada", nullable = false, length = 2000)
    private String descricaoPlanejada;

    @Column(name = "descricao_reparo", length = 2000)
    private String descricaoReparo;

    @Column(name = "foto_depois_url", length = 500)
    private String fotoDepoisUrl;

    @Column(name = "motivo_contestacao", length = 2000)
    private String motivoContestacao;

    @Column(name = "data_aceite", nullable = false)
    private LocalDateTime dataAceite;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_conclusao_empresa")
    private LocalDateTime dataConclusaoEmpresa;

    @Column(name = "data_confirmacao_autor")
    private LocalDateTime dataConfirmacaoAutor;
}
