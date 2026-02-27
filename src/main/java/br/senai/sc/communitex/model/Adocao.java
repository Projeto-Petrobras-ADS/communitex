package br.senai.sc.communitex.model;

import br.senai.sc.communitex.enums.StatusAdocao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "adocoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adocao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "descricao_projeto")
    private String descricaoProjeto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAdocao status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "praca_id", nullable = false)
    @JsonIgnore
    private Praca praca;
}
