# Clinic API

API REST para gerenciamento de consultórios médicos: cadastro de usuários, médicos, pacientes e agendamento de consultas, com autenticação JWT e controle de acesso por perfis (**ADMIN**, **MEDICO**, **CLIENTE**).

---

## Sumário

- [Funcionalidades](#funcionalidades)
- [Stack tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Configuração e execução](#configuração-e-execução)
- [Autenticação](#autenticação)
- [Perfis e permissões](#perfis-e-permissões)
- [Endpoints](#endpoints)
- [DTOs de resposta](#dtos-de-resposta)
- [Tratamento de erros](#tratamento-de-erros)
- [Banco de dados](#banco-de-dados)
- [Integração ViaCEP](#integração-viacep)
- [Documentação interativa (Swagger)](#documentação-interativa-swagger)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Testes](#testes)
- [Roadmap sugerido](#roadmap-sugerido)

---

## Funcionalidades

- **Autenticação JWT** com login e registro público (cliente) ou administrativo (médico/admin).
- **CRUD** de médicos, pacientes, usuários e consultas.
- **Regras de negócio** por perfil (visibilidade e alteração de dados no service layer).
- **Validação de CEP** via API pública [ViaCEP](https://viacep.com.br/).
- **DTOs de resposta** para não expor entidades JPA (`password`, `idUser`, relacionamentos internos).
- **Tratamento global de exceções** com códigos HTTP consistentes.
- **Migração Flyway** com usuário administrador inicial.
- **Console H2** para inspeção do banco em desenvolvimento.

---

## Stack tecnológica

| Tecnologia | Versão / uso |
|------------|----------------|
| Java | 21 |
| Spring Boot | 3.4.5 |
| Spring Security | JWT + `@PreAuthorize` |
| Spring Data JPA | Persistência |
| Spring Cloud OpenFeign | Cliente ViaCEP |
| H2 Database | Banco em memória (dev) |
| Flyway | Migrações SQL |
| JJWT | Geração/validação de token |
| Lombok | Redução de boilerplate |
| SpringDoc OpenAPI | Swagger UI |
| Maven | Build e dependências |

---

## Arquitetura

Camadas em pacote único `com.clinic_api`:

```mermaid
flowchart TB
    subgraph client [Cliente HTTP]
        REQ[Request + Bearer JWT]
    end

    subgraph api [API]
        CTRL[Controllers]
        SEC[SecurityConfig + JwtAuthenticationFilter]
        EXC[GlobalExceptionHandler]
    end

    subgraph app [Aplicação]
        SVC[Services / Impl]
        DTO[DTOs Request/Response]
    end

    subgraph infra [Infraestrutura]
        REPO[JPA Repositories]
        FEIGN[ViaCepClient]
        DB[(H2)]
    end

    REQ --> SEC --> CTRL
    CTRL --> SVC
    SVC --> DTO
    SVC --> REPO
    SVC --> FEIGN
    REPO --> DB
    FEIGN --> ViaCEP[viacep.com.br]
    SVC -.-> EXC
    CTRL -.-> EXC
```

**Fluxo de autenticação:**

```mermaid
sequenceDiagram
    participant C as Cliente
    participant A as /auth/login
    participant J as JwtUtil
    participant F as JwtAuthenticationFilter
    participant API as Endpoints protegidos

    C->>A: email + password
    A->>J: generateToken(email, role)
    J-->>C: JWT
    C->>F: Authorization: Bearer {token}
    F->>F: valida e preenche SecurityContext
    F->>API: request autenticado
```

---

## Pré-requisitos

- **JDK 21** instalado e no `PATH`
- **Maven 3.9+** (ou use o wrapper `./mvnw` incluído no projeto)
- Variáveis de ambiente para JWT (obrigatórias)

---

## Configuração e execução

### 1. Variáveis de ambiente

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `JWT_SECRET` | Chave secreta para assinar o JWT (mín. 32 caracteres recomendado) | `minha-chave-super-secreta-com-32-chars` |
| `JWT_EXPIRATION` | Tempo de expiração do token em **milissegundos** | `86400000` (24 h) |

**Windows (PowerShell):**

```powershell
$env:JWT_SECRET="sua-chave-secreta-aqui-minimo-32-caracteres"
$env:JWT_EXPIRATION="86400000"
```

**Linux / macOS:**

```bash
export JWT_SECRET="sua-chave-secreta-aqui-minimo-32-caracteres"
export JWT_EXPIRATION=86400000
```

### 2. Executar a aplicação

```bash
# Com Maven instalado
mvn spring-boot:run

# Ou com wrapper
./mvnw spring-boot:run        # Linux/macOS
.\mvnw.cmd spring-boot:run    # Windows
```

A API sobe em **`http://localhost:8080`**.

### 3. Compilar sem rodar

```bash
mvn clean package -DskipTests
java -jar target/clinic-api-0.0.1-SNAPSHOT.jar
```

### 4. Usuário administrador inicial (Flyway)

Após o primeiro start, existe um usuário seed criado em `src/main/resources/db/migration/V1__create_admin.sql`:

| Campo | Valor |
|-------|--------|
| E-mail | `admin@clinic.com` |
| Role | `ADMIN` |
| Senha | Hash BCrypt definido na migration (defina/ajuste conforme seu ambiente de desenvolvimento) |

> **Dica:** Para testes rápidos, faça login com esse usuário após alinhar a senha com o hash da migration, ou cadastre novos usuários via `/auth/register` e `/auth/admin/register`.

---

## Autenticação

Endpoints públicos (sem token):

- `POST /auth/login`
- `POST /auth/register`

Demais rotas exigem header:

```http
Authorization: Bearer {seu-jwt}
```

### Login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "admin@clinic.com",
  "password": "sua-senha"
}
```

**Resposta (200):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Registro público (CLIENTE)

Cria usuário com role `CLIENTE`, endereço (ViaCEP) e registro de paciente vinculado.

```http
POST /auth/register
Content-Type: application/json

{
  "email": "cliente@email.com",
  "password": "senha123",
  "nome": "João Silva",
  "cep": "01310100",
  "numero": "100"
}
```

### Registro administrativo (ADMIN autenticado)

```http
POST /auth/admin/register
Authorization: Bearer {token-admin}
Content-Type: application/json

{
  "email": "medico@clinic.com",
  "password": "senha123",
  "nome": "Dr. Ana",
  "cep": "01310100",
  "numero": "200",
  "role": "MEDICO",
  "crm": "12345-SP",
  "especialidade": "Cardiologia"
}
```

Roles aceitas em `role`: `ADMIN`, `MEDICO`, `CLIENTE` (com criação automática de `Medico` ou `Paciente` quando aplicável).

---

## Perfis e permissões

| Recurso | ADMIN | MEDICO | CLIENTE |
|---------|:-----:|:------:|:-------:|
| Listar todos os usuários | ✅ | ❌ | ❌ |
| Ver/editar próprio usuário | ✅ | ✅ (próprio) | ✅ (próprio) |
| CRUD médicos (criar/excluir) | ✅ | ❌ / próprio update | ❌ |
| Listar médicos | ✅ (dados completos) | ✅ (próprio com crm/email) | ✅ (dados públicos) |
| CRUD pacientes (criar) | ✅ | ✅ | ❌ |
| Ver pacientes | ✅ | ✅ | ✅ (próprio) |
| Consultas | ✅ (todas) | ✅ (do médico) | ✅ (próprias) |
| Agendar consulta | ✅ | ✅ (como médico) | ✅ (como paciente) |
| Cancelar consulta | ✅ | ✅ (próprias) | ✅ (próprias) |

Regras detalhadas (conflito de horário, identidade do agendamento, etc.) estão implementadas na camada **service**.

---

## Endpoints

Base URL: `http://localhost:8080`

### Auth — `/auth`

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| `POST` | `/auth/login` | Não | Login e JWT |
| `POST` | `/auth/register` | Não | Cadastro de cliente |
| `POST` | `/auth/admin/register` | ADMIN | Cadastro administrativo |

### Usuários — `/users`

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| `GET` | `/users/me` | Autenticado | Usuário logado |
| `GET` | `/users` | ADMIN | Lista usuários |
| `GET` | `/users/{id}` | Autenticado | Detalhe (regras no service) |
| `PUT` | `/users/{id}` | Autenticado | Atualização |
| `DELETE` | `/users/{id}` | ADMIN | Remove usuário |

### Médicos — `/medicos`

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| `GET` | `/medicos` | Autenticado | Lista (DTO por role) |
| `GET` | `/medicos/{id}` | Autenticado | Detalhe |
| `POST` | `/medicos` | ADMIN | Cria médico |
| `PUT` | `/medicos/{id}` | Autenticado | Atualiza |
| `DELETE` | `/medicos/{id}` | ADMIN | Remove |

### Pacientes — `/pacientes`

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| `GET` | `/pacientes` | Autenticado | Lista |
| `GET` | `/pacientes/{id}` | Autenticado | Detalhe |
| `POST` | `/pacientes` | ADMIN, MEDICO | Cria |
| `PUT` | `/pacientes/{id}` | Autenticado | Atualiza |
| `DELETE` | `/pacientes/{id}` | ADMIN | Remove |

### Consultas — `/consultas`

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| `GET` | `/consultas` | Autenticado | Lista (filtrada por role) |
| `GET` | `/consultas/medico/{medicoId}` | Autenticado | Por médico |
| `GET` | `/consultas/paciente/{pacienteId}` | Autenticado | Por paciente |
| `GET` | `/consultas/{id}` | Autenticado | Detalhe |
| `POST` | `/consultas` | Autenticado | Agenda |
| `PUT` | `/consultas/{id}` | Autenticado | Atualiza |
| `DELETE` | `/consultas/{id}` | Autenticado | Cancela (`status: CANCELADA`) |

**Status de consulta:** `AGENDADA`, `CANCELADA`, `CONCLUIDA`, `REAGENDADA`.

**Exemplo — criar consulta:**

```json
{
  "dataHora": "2026-06-15T14:30:00",
  "status": "AGENDADA",
  "medico": { "id": "uuid-do-medico" },
  "paciente": { "id": "uuid-do-paciente" }
}
```

---

## DTOs de resposta

As respostas de leitura (**GET**) não retornam entidades JPA:

| DTO | Campos principais |
|-----|-------------------|
| `UserResponse` | `id`, `email`, `role`, `endereco` |
| `MedicoResponse` | `id`, `nome`, `especialidade`, `crm?`, `email?` |
| `PacienteResponse` | `id`, `name`, `email` |
| `ConsultaResponse` | `id`, `dataHora`, `status`, `medicoId`, `medicoNome`, `pacienteId`, `pacienteNome` |
| `EnderecoResponse` | `id`, `logradouro`, `numero`, `localidade`, `uf`, `cep`, `complemento` |

Campos opcionais em `MedicoResponse` usam `@JsonInclude(NON_NULL)` conforme o perfil do usuário.

**POST/PUT** ainda aceitam entidades no body onde é necessário persistir relacionamentos (`Medico`, `Paciente`, `Consulta`, `User`).

---

## Tratamento de erros

Resposta padrão:

```json
{
  "timestamp": "2026-05-24T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Acesso negado"
}
```

| HTTP | Exceção / situação |
|------|---------------------|
| **403** | Acesso negado (custom ou Spring Security / `@PreAuthorize`) |
| **404** | `ResourceNotFoundException` |
| **409** | `BusinessException` (e-mail duplicado, CEP inválido, horário ocupado, etc.) |
| **500** | Erro não mapeado |

---

## Banco de dados

- **H2 em memória** (`jdbc:h2:mem:testdb`)
- **DDL:** `spring.jpa.hibernate.ddl-auto=update` (complementa entidades JPA)
- **Flyway:** `V1__create_admin.sql` (tabela e seed do admin)

**Console H2 (desenvolvimento):**

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Usuário: `sa`
- Senha: *(vazia)*

---

## Integração ViaCEP

O `CepService` consulta `https://viacep.com.br/ws/{cep}/json/` e:

- Normaliza o CEP (apenas dígitos, 8 caracteres)
- Rejeita CEP inexistente (`erro: true` na API)
- Mapeia para a entidade `Endereco` antes do cadastro

Erros de CEP retornam **409** com mensagem clara (ex.: `"CEP não encontrado"`), não 500.

---

## Documentação interativa (Swagger)

Com a aplicação rodando:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

No Swagger, use o botão **Authorize** e informe: `Bearer {seu-token}`.

---

## Estrutura do projeto

```
clinic-api/
├── src/main/java/com/clinic_api/
│   ├── ClinicApiApplication.java
│   ├── client/              # Feign (ViaCEP)
│   ├── config/              # SecurityConfig
│   ├── controller/          # REST controllers
│   ├── domain/              # Entidades JPA
│   ├── dto/                 # Request/Response DTOs
│   ├── enums/               # Role, StatusConsulta
│   ├── exception/           # Exceções + GlobalExceptionHandler
│   ├── repository/          # Spring Data JPA
│   │   └── security/        # JWT filter e util
│   └── service/             # Interfaces e implementações
├── src/main/resources/
│   ├── application.properties
│   └── db/migration/        # Flyway SQL
├── src/test/java/
├── pom.xml
├── mvnw / mvnw.cmd
└── README.md
```

---

## Testes

```bash
mvn test
```

Teste de contexto padrão em `ClinicApiApplicationTests`.

---

## Roadmap sugerido

- [ ] DTOs de **request** para POST/PUT (desacoplar totalmente das entidades)
- [ ] Banco PostgreSQL em produção
- [ ] Testes de integração com MockMvc / Testcontainers
- [ ] CI/CD (GitHub Actions)
- [ ] Perfil `application-prod` e secrets externos
- [ ] Paginação e filtros nas listagens
- [ ] Remoção de DTOs legados (`MedicoPublicResponse`, `MedicoAdminResponse`)

---

## Licença

Projeto de portfólio / estudo. Defina a licença conforme sua necessidade (ex.: MIT).

---

## Autor

Desenvolvido como API de consultório médico — **Clinic API**.
