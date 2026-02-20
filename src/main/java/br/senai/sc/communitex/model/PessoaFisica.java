package br.senai.sc.communitex.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

@Entity
@Table(name = "pessoas_fisicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PessoaFisica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo Nome é obrigatório!")
    private String nome;

    @NotBlank(message = "O campo CPF é obrigatório!")
    @Pattern(regexp = "\\d{11}", message = "CPF inválido! Use o formato 000.000.000-00")
    @Column(unique = true)
    private String cpf;

    @NotBlank(message = "O campo E-mail é obrigatório!")
    @Email(message = "Email inválido")
    @Column(unique = true)
    private String email;

    @Pattern(regexp = "\\d{10,11}", message = "Telefone inválido! Use o formato (99) 99999-9999")
    private String telefone;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;
}
