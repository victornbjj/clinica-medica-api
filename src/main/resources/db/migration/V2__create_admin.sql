-- Senha BCrypt: admin123
INSERT INTO tb_users (id, email, password, role)
SELECT '00000000-0000-0000-0000-000000000001', 'admin@clinic.com',
       '$2a$10$Y1ygWGI65mgym8xu8B66JuGB/r8edV1OER5jur4cvMDKthf2SVsSC', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM tb_users WHERE email = 'admin@clinic.com');
