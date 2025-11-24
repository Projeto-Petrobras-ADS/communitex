package br.senai.sc.communitex.model;

import br.senai.sc.communitex.enums.StatusAdocao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "adocoes")
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

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnore
    private Empresa empresa;


    @ManyToOne
    @JoinColumn(name = "praca_id", nullable = false)
    @JsonIgnore
    private Praca praca;



    public Adocao() {
    }

    public Adocao(Long id, LocalDate dataInicio, LocalDate dataFim, String descricaoProjeto, StatusAdocao status) {
        this.id = id;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.descricaoProjeto = descricaoProjeto;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public String getDescricaoProjeto() {
        return descricaoProjeto;
    }

    public void setDescricaoProjeto(String descricaoProjeto) {
        this.descricaoProjeto = descricaoProjeto;
    }

    public StatusAdocao getStatus() {
        return status;
    }

    public void setStatus(StatusAdocao status) {
        this.status = status;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }


    public Praca getPraca() {
        return praca;
    }

    public void setPraca(Praca praca) {
        this.praca = praca;
    }
}
