package co.edu.uniquindio.QuindioStay_TyA.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de seguridad de contraseñas.
 * <p>
 * Las contraseñas se guardan cifradas con BCrypt (los datos de prueba de
 * {@code 02_datos.sql} ya vienen en ese formato). Solo se usa
 * {@code spring-security-crypto}, no el framework completo de seguridad.
 */
@Configuration
public class SeguridadConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
