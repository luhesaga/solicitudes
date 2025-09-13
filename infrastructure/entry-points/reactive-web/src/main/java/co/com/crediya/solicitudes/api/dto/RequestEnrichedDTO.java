package co.com.crediya.solicitudes.api.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class RequestEnrichedDTO {
    // Datos de la solicitud
    private BigDecimal amount;
    private Integer term;
    private String email;

    // Datos enriquecidos del usuario
    private String userName;
    private BigDecimal salary;

    // Datos enriquecidos del préstamo
    private String loanTypeName;
    private Double interestRate;

    // Datos enriquecidos del estado
    private String statusName;

    // Dato calculado
    private BigDecimal approximateMonthlyAmount;
}
