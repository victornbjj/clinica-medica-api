CREATE TABLE tb_users (
                          id UUID NOT NULL,
                          email VARCHAR(255) NOT NULL,
                          password VARCHAR(255) NOT NULL,
                          role VARCHAR(50) NOT NULL,
                          endereco_id UUID,
                          PRIMARY KEY (id),
                          CONSTRAINT uk_users_email UNIQUE (email)
);



INSERT INTO tb_users (id, email, password, role)
VALUES (RANDOM_UUID(), 'admin@clinic.com', '$2a$10$Y1ygWGI65mgym8xu8B66JuGB/r8edV1OER5jur4cvMDKthf2SVsSC', 'ADMIN');