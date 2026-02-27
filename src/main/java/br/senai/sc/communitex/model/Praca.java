package br.senai.sc.communitex.model;

import br.senai.sc.communitex.enums.StatusPraca;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pracas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Praca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    private String logradouro;

    private String bairro;

    @NotBlank
    private String cidade;

    private Double latitude;

    private Double longitude;

    @Size(max = 1000)
    private String descricao;

    private String fotoUrl;

    @Column(name = "metragem_m2", nullable = false)
    private Double metragemM2;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatusPraca status;

    @Builder.Default
    @OneToMany(mappedBy = "praca", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Adocao> adocoes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cadastrado_por_id")
    @JsonIgnore
    private PessoaFisica cadastradoPor;
}
