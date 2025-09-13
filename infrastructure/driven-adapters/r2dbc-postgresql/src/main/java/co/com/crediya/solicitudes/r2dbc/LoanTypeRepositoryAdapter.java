package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.model.loantype.LoanType;
import co.com.crediya.solicitudes.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.solicitudes.r2dbc.data.LoanTypeData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class LoanTypeRepositoryAdapter implements LoanTypeRepository {
    private final LoanTypeDataRepository loanTypeDataRepository;

    private LoanType toLoanTypeModel(LoanTypeData data) {
        return LoanType.builder()
                .id(data.getId())
                .name(data.getName())
                .minimumAmount(data.getMinimumAmount())
                .maximunAmount(data.getMaximumAmount())
                .interestRate(data.getInterestRate())
                .automaticValidation(data.getAutomaticValidation())
                .build();
    }

    @Override
    public Mono<LoanType> findById(Long id) {
        return loanTypeDataRepository.findById(id)
                .map(this::toLoanTypeModel);
    }
}
