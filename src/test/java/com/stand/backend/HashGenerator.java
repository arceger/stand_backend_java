package com.stand.backend;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "SuaSenha";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("HASH GERADO: " + encodedPassword);
    }
}

/*

-- criar user
INSERT INTO stand.admin_user (
    id,
    email,
    full_name,
    password_hash,
    created_at
)
VALUES (
    gen_random_uuid(),
    'teu.email@dominio.com',
    'Nome do Administrador',
    'HASH_GERADO_PELO_JAVA',
    NOW()
);

-- deletar user de testes
-- Elimina as sessões ativas associadas ao e-mail
DELETE FROM stand.admin_session
WHERE admin_user_id = (
    SELECT id FROM stand.admin_user WHERE email = 'admin@stand.local'
);


DELETE FROM stand.admin_user
WHERE email = 'admin@stand.local';
 */