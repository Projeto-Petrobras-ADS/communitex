# Implementação: Cadastro de Representante junto com Empresa

## Descrição da Funcionalidade

Esta implementação permite cadastrar um Representante (como um usuário do sistema) automaticamente ao criar uma nova Empresa. O Representante é criado como um usuário com a role `ROLE_EMPRESA` e é associado à Empresa através de um relacionamento One-to-One.

## Alterações Realizadas

### 1. **DTO de Entrada: EmpresaRequestDTO**

O DTO foi modificado para incluir os dados do Representante:

```java
public record EmpresaRequestDTO(
        @NotBlank
        String razaoSocial,
        @NotBlank
        String cnpj,
        String nomeFantasia,
        @NotBlank
        String email,
        String telefone,
        RepresentanteEmpresa representanteEmpresa,
        @NotBlank(message = "O nome do representante é obrigatório")
        String nomeRepresentante,
        @NotBlank(message = "O email do representante é obrigatório")
        @Email(message = "Email do representante inválido")
        String emailRepresentante,
        @NotBlank(message = "A senha do representante é obrigatória")
        String senhaRepresentante
)
```

**Campos adicionados:**
- `nomeRepresentante`: Nome do representante (será armazenado no objeto Usuario para referência futura)
- `emailRepresentante`: Email que será usado como username do usuário (deve ser único)
- `senhaRepresentante`: Senha do representante (será encodada com BCrypt)

### 2. **Entidade Empresa**

Foi adicionado um relacionamento One-to-One com a entidade `Usuario`:

```java
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "usuario_representante_id")
@JsonIgnore
private Usuario usuarioRepresentante;
```

**Características:**
- Cascade: `CascadeType.ALL` - garante que o Usuario seja salvo e deletado junto com a Empresa
- JoinColumn: `usuario_representante_id` - coluna na tabela `empresas` que referencia o Usuario
- JsonIgnore: Evita serialização cíclica

### 3. **Service Layer: EmpresaService**

O método `create()` foi modificado com as seguintes responsabilidades:

```java
@Transactional
public EmpresaResponseDTO create(EmpresaRequestDTO dto) {
    // 1. Validar se CNPJ já existe
    Optional<Empresa> existente = empresaRepository.findByCnpj(dto.cnpj());
    if (existente.isPresent()) {
        throw new BusinessExpection("Já existe uma empresa cadastrada com o CNPJ: " + dto.cnpj());
    }

    // 2. Validar se email do Representante já existe
    Optional<Usuario> usuarioExistente = usuarioService.findByUsername(dto.emailRepresentante());
    if (usuarioExistente.isPresent()) {
        throw new BusinessExpection("Já existe um usuário cadastrado com o email: " + dto.emailRepresentante());
    }

    // 3. Criar novo Usuario
    Usuario usuarioRepresentante = new Usuario();
    usuarioRepresentante.setUsername(dto.emailRepresentante());
    usuarioRepresentante.setPassword(passwordEncoder.encode(dto.senhaRepresentante()));
    usuarioRepresentante.setRole("ROLE_EMPRESA");
    Usuario usuarioSalvo = usuarioService.save(usuarioRepresentante);

    // 4. Criar nova Empresa e associar ao Usuario
    Empresa empresa = new Empresa();
    BeanUtils.copyProperties(dto, empresa, "usuarioRepresentante");
    empresa.setCnpj(dto.cnpj().replaceAll("\\D", ""));
    empresa.setTelefone(dto.telefone().replaceAll("\\D", ""));
    empresa.setUsuarioRepresentante(usuarioSalvo);

    // 5. Persistir Empresa (Usuario já foi salvo antes)
    return toResponseDTO(empresaRepository.save(empresa));
}
```

**Características:**
- **@Transactional**: Garante que a operação seja atômica - se algo falhar, tudo é revertido
- **Validação dupla**: Verifica se CNPJ e email do Representante já existem
- **Criação do Usuario**: Cria um novo Usuario com a role `ROLE_EMPRESA`
- **Encoding de senha**: Usa `PasswordEncoder` (BCrypt) para encodar a senha
- **Associação**: Associa o Usuario recém-criado à Empresa antes de salvar

### 4. **Dependências Injetadas**

O `EmpresaService` agora recebe:
- `EmpresaRepository`: Para operações com Empresa
- `UsuarioService`: Para operações com Usuario
- `PasswordEncoder`: Para encodar senhas (já configurado no projeto)

```java
public EmpresaService(EmpresaRepository empresaRepository, 
                     UsuarioService usuarioService, 
                     PasswordEncoder passwordEncoder) {
    this.empresaRepository = empresaRepository;
    this.usuarioService = usuarioService;
    this.passwordEncoder = passwordEncoder;
}
```

## Fluxo de Operação

```
1. Cliente envia POST /api/empresas com dados da Empresa e do Representante
                          ↓
2. Validação de entrada (anotações @Valid, @NotBlank, @Email)
                          ↓
3. EmpresaService.create() é chamado (com @Transactional)
                          ↓
4. Verifica se CNPJ já existe → se sim, lança exceção
                          ↓
5. Verifica se email do Representante já existe → se sim, lança exceção
                          ↓
6. Cria novo Usuario:
   - username = emailRepresentante
   - password = encode(senhaRepresentante)
   - role = "ROLE_EMPRESA"
   - Persiste no banco
                          ↓
7. Cria nova Empresa:
   - Copia dados do DTO
   - Remove espaços de CNPJ e telefone
   - Associa o Usuario recém-criado
   - Persiste no banco
                          ↓
8. Retorna EmpresaResponseDTO com os dados da Empresa
                          ↓
9. Status HTTP: 201 (CREATED)
```

## Exemplo de Requisição

### POST /api/empresas

```json
{
  "razaoSocial": "Empresa XYZ LTDA",
  "cnpj": "12345678000195",
  "nomeFantasia": "EmpresaXYZ",
  "email": "contato@empresa.com",
  "telefone": "4733333333",
  "nomeRepresentante": "João Silva",
  "emailRepresentante": "joao.silva@empresa.com",
  "senhaRepresentante": "SenhaSegura@123"
}
```

### Resposta (HTTP 201 Created)

```json
{
  "id": 1,
  "razaoSocial": "Empresa XYZ LTDA",
  "cnpj": "12345678000195",
  "nomeFantasia": "EmpresaXYZ",
  "email": "contato@empresa.com",
  "telefone": "4733333333",
  "representanteEmpresas": null,
  "adocaos": null
}
```

## Tratamento de Erros

### 1. Email do Representante já existe
```json
HTTP 400 Bad Request
{
  "message": "Já existe um usuário cadastrado com o email: joao.silva@empresa.com"
}
```

### 2. CNPJ já existe
```json
HTTP 400 Bad Request
{
  "message": "Já existe uma empresa cadastrada com o CNPJ: 12345678000195"
}
```

### 3. Validação de entrada falha (campo obrigatório faltando)
```json
HTTP 400 Bad Request
{
  "message": "O nome do representante é obrigatório"
}
```

## Transacionalidade

A anotação `@Transactional` no método `create()` garante que:

1. Se a criação do Usuario falhar, a Empresa não será criada
2. Se a criação da Empresa falhar, o Usuario será revertido
3. Ambas as operações são atômicas - tudo ou nada

## Segurança

1. **Senhas**: Encodadas com BCrypt (padrão de segurança)
2. **Email único**: Validado antes de criar o Usuario
3. **Role automática**: Sempre é atribuída `ROLE_EMPRESA`
4. **CNPJ único**: Validado antes de criar a Empresa

## Uso Posterior

Após criar a Empresa com seu Representante:

1. O Representante pode fazer login com:
   - Username: `emailRepresentante`
   - Password: `senhaRepresentante`

2. O Representante terá acesso com a role `ROLE_EMPRESA`

3. A Empresa terá um relacionamento para acessar seu Usuario:
   ```java
   empresa.getUsuarioRepresentante(); // Retorna o Usuario do Representante
   ```

## Próximas Melhorias Sugeridas

1. Adicionar um campo `nome` na entidade Usuario para armazenar `nomeRepresentante`
2. Enviar email de confirmação ao representante
3. Implementar um endpoint GET `/api/empresas/{id}/representante` para retornar dados do representante
4. Adicionar validação de força de senha
5. Implementar dois-fatores (2FA) para representantes

## Testes

Execute os testes para validar a implementação:

```bash
./mvnw test
```

Os testes devem cobrir:
- Criação bem-sucedida de Empresa com Representante
- Erro ao tentar criar Empresa com CNPJ duplicado
- Erro ao tentar criar Representante com email duplicado
- Validação de transacionalidade (rollback em caso de erro)

