package br.senai.sc.communitex.dto;

public record PessoaFisicaResponseDTO(
        Long id, String nome, String cpf, String email, String telefone,
        String cep, String logradouro, String numero, String complemento,
        String bairro, String cidade, String estado
) {
    public PessoaFisicaResponseDTO(Long id, String nome, String cpf, String email, String telefone) {
        this(id, nome, cpf, email, telefone, null, null, null, null, null, null, null);
    }
}
