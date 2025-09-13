package co.com.crediya.solicitudes.r2dbc;

import co.com.crediya.solicitudes.r2dbc.data.LoanTypeData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface LoanTypeDataRepository extends R2dbcRepository<LoanTypeData, Long> {
}
