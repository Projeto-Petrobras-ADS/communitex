package br.senai.sc.communitex.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo Razão Social é obrigatório!")
    private String razaoSocial;

    @NotBlank(message = "O campo CNPJ é obrigatório!")
    @Pattern(regexp = "\\d{14}", message = "CNPJ inválido! Use o formato 00.000.000/0000-00")
    private String cnpj;

    private String nomeFantasia;

    @NotBlank(message = "O campo E-mail é obrigatório!")
    @Email(message = "Email inválido")
    private String email;

    @Pattern(regexp = "\\d{10,11}", message = "Telefone inválido! Use o formato (99) 99999-9999")
    private String telefone;

    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<RepresentanteEmpresa> representantes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Adocao> adocaos = new ArrayList<>();

    @OneToOne(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private RepresentanteEmpresa representanteEmpresas;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_representante_id")
    @JsonIgnore
    private Usuario usuarioRepresentante;
}
