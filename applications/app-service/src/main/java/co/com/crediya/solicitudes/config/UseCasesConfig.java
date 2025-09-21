package co.com.crediya.solicitudes.config;

import co.com.crediya.solicitudes.model.log.gateways.LoggerGateway;
import co.com.crediya.solicitudes.model.notification.gateways.NotificationGateway;
import co.com.crediya.solicitudes.model.request.gateways.RequestRepository;
import co.com.crediya.solicitudes.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.solicitudes.model.status.gateways.StatusRepository;
import co.com.crediya.solicitudes.model.user.gateways.UserGateway;
import co.com.crediya.solicitudes.usecase.list.ManualListRequestUseCase;
import co.com.crediya.solicitudes.usecase.request.RequestUseCase;
import co.com.crediya.solicitudes.usecase.request.RequestValidator;
import co.com.crediya.solicitudes.usecase.updatestatus.UpdateRequestStatusUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {
    @Bean
    @ConditionalOnBean({RequestRepository.class, LoanTypeRepository.class})
    public RequestValidator requestValidator(RequestRepository requestRepository,
                                             LoanTypeRepository loanTypeRepository) {
        return new RequestValidator(requestRepository, loanTypeRepository);
    }

    @Bean
    @ConditionalOnBean({RequestRepository.class, RequestValidator.class})
    public RequestUseCase requestUseCase(RequestRepository requestRepository,
                                         RequestValidator requestValidator) {
        return new RequestUseCase(requestRepository, requestValidator);
    }

    @Bean
    public ManualListRequestUseCase manualListRequestUseCase(
            RequestRepository requestRepository,
            UserGateway userGateway,
            LoggerGateway loggerGateway) {
        return new ManualListRequestUseCase(requestRepository, userGateway, loggerGateway);
    }

    @Bean
    public UpdateRequestStatusUseCase updateRequestStatusUseCase(
            RequestRepository requestRepository,
            StatusRepository statusRepository,
            NotificationGateway notificationGateway) {
        return new UpdateRequestStatusUseCase(requestRepository, statusRepository, notificationGateway);
    }
}

