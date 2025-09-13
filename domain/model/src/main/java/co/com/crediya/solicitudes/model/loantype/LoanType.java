package co.com.crediya.solicitudes.model.loantype;
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
public class LoanType {
    private Long id;
    private String name;
    private BigDecimal minimumAmount;
    private BigDecimal maximunAmount;
    private Double interestRate;
    private Boolean automaticValidation;
}
