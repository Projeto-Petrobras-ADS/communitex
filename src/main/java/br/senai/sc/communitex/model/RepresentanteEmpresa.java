package br.senai.sc.communitex.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "representantes_empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepresentanteEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O campo Nome é obrigatório!")
    private String nome;

    @NotNull(message = "O campo Ativo é obrigatório!")
    private Boolean ativo;

    @NotNull(message = "O campo e-mail é obrigatório!")
    @Email(message = "E-mail inválido")
    private String email;

    @JsonBackReference
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", unique = true)
    private Empresa empresa;
}
