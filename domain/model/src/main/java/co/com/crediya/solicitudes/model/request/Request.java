package co.com.crediya.solicitudes.model.request;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Request {
    private Long id;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private Long statusId;
    private Long loanTypeId;
    private String loanTypeName;
    private Double interestRate;
    private String statusName;
    // Desde el microservicio 'autenticacion' (para el UseCase)
    private String userName;
    private BigDecimal salary;
    // Dato calculado (para el UseCase)
    private BigDecimal approximateMonthlyAmount;
}
