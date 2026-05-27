CREATE TABLE IF NOT EXISTS tb_enderecos (
    id UUID PRIMARY KEY,
    rua VARCHAR(255) NOT NULL,
    numero VARCHAR(50),
    cidade VARCHAR(255) NOT NULL,
    estado VARCHAR(2) NOT NULL,
    cep VARCHAR(20) NOT NULL,
    complemento VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tb_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    endereco_id UUID,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_endereco FOREIGN KEY (endereco_id) REFERENCES tb_enderecos (id)
);

CREATE TABLE IF NOT EXISTS tb_medicos (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    crm VARCHAR(50) NOT NULL,
    especialidade VARCHAR(255) NOT NULL,
    id_user UUID NOT NULL,
    CONSTRAINT uk_medicos_crm UNIQUE (crm),
    CONSTRAINT uk_medicos_user UNIQUE (id_user),
    CONSTRAINT fk_medicos_user FOREIGN KEY (id_user) REFERENCES tb_users (id)
);

CREATE TABLE IF NOT EXISTS tb_pacientes (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    id_user UUID NOT NULL,
    CONSTRAINT uk_pacientes_user UNIQUE (id_user),
    CONSTRAINT fk_pacientes_user FOREIGN KEY (id_user) REFERENCES tb_users (id)
);

CREATE TABLE IF NOT EXISTS tb_consultas (
    id UUID PRIMARY KEY,
    data_hora TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    id_medico UUID NOT NULL,
    paciente_medico UUID NOT NULL,
    CONSTRAINT fk_consultas_medico FOREIGN KEY (id_medico) REFERENCES tb_medicos (id),
    CONSTRAINT fk_consultas_paciente FOREIGN KEY (paciente_medico) REFERENCES tb_pacientes (id)
);
