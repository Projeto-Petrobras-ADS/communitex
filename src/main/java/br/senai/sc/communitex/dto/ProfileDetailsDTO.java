package br.senai.sc.communitex.dto;

public record ProfileDetailsDTO(
        TipoConta tipoConta,
        String nome,
        String documento,
        String emailAcesso,
        String razaoSocial,
        String telefone,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String estado,
        String nomeFantasia,
        String emailInstitucional,
        boolean completo
) {
}
