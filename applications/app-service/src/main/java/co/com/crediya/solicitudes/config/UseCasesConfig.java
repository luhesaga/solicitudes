package co.com.crediya.solicitudes.config;

import co.com.crediya.solicitudes.model.solicitud.gateways.SolicitudRepository;
import co.com.crediya.solicitudes.model.tipoprestamo.gateways.TipoPrestamoRepository;
import co.com.crediya.solicitudes.usecase.solicitud.SolicitudUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
public class UseCasesConfig {
    @Bean
    @ConditionalOnBean({SolicitudRepository.class, TipoPrestamoRepository.class})
    public SolicitudUseCase solicitudUseCase(SolicitudRepository solicitudRepository,
                                             TipoPrestamoRepository tipoPrestamoRepository) {
        return new SolicitudUseCase(solicitudRepository, tipoPrestamoRepository);
    }
}

