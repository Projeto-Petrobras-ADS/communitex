package br.senai.sc.communitex.model;

import br.senai.sc.communitex.enums.StatusPraca;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pracas")
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

    @OneToMany(mappedBy = "praca", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Adocao> adocoes = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "cadastrado_por_id")
    @JsonIgnore
    private PessoaFisica cadastradoPor;

    public Praca() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Double getMetragemM2() {
        return metragemM2;
    }

    public void setMetragemM2(Double metragemM2) {
        this.metragemM2 = metragemM2;
    }

    public StatusPraca getStatus() {
        return status;
    }

    public void setStatus(StatusPraca status) {
        this.status = status;
    }

    public List<Adocao> getAdocoes() {
        return adocoes;
    }

    public void setAdocoes(List<Adocao> adocoes) {
        this.adocoes = adocoes;
    }

    public PessoaFisica getCadastradoPor() {
        return cadastradoPor;
    }

    public void setCadastradoPor(PessoaFisica cadastradoPor) {
        this.cadastradoPor = cadastradoPor;
    }
}

