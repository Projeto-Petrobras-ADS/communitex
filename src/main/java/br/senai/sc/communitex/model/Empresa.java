package br.senai.sc.communitex.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo Razão Social é obrigatório!")
    private String razaoSocial;

    @NotBlank(message = "O campo CNPJ é obrigatório!")
    @Pattern(regexp = "^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$",
            message = "CNPJ inválido! Use o formato 00.000.000/0000-00"    )
    private String cnpj;

    private String nomeFantasia;

    @NotBlank(message = "O campo E-mail é obrigatório!")
    @Email(message = "Email inválido")
    private String email;


    @Pattern(regexp = "^\\(\\d{2}\\)\\s?\\d{4,5}-\\d{4}$",
            message ="Telefone inválido! Use o formato (99) 99999-9999")
    private String telefone;


    @JsonManagedReference
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepresentanteEmpresa> representantes = new ArrayList<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Adocao> adocaos;

    public Empresa() {
    }

    public Empresa(Long id, String razaoSocial, String cnpj, String nomeFantasia, String email, String telefone) {
        this.id = id;
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.nomeFantasia = nomeFantasia;
        this.email = email;
        this.telefone = telefone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<RepresentanteEmpresa> getRepresentantes() {
        return representantes;
    }

    public void setRepresentantes(List<RepresentanteEmpresa> representantes) {
        this.representantes = representantes;
    }
    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public List<Adocao> getAdocaos() {
        return adocaos;
    }

    public void setAdocaos(List<Adocao> adocaos) {
        this.adocaos = adocaos;
    }

}


