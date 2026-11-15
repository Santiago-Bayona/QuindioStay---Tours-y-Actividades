package co.edu.uniquindio.QuindioStay_TyA.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que el frontend de React (Vite, puerto 5173) llame a la API desde el navegador.
 * <p>
 * Sin esta configuración el navegador bloquea las peticiones porque el frontend y el
 * backend corren en puertos distintos. Si se usa el proxy de Vite no hace falta, pero
 * tampoco estorba.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
