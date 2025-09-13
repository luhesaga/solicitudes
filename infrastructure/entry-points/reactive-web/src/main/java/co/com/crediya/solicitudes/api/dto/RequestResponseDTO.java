package co.com.crediya.solicitudes.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RequestResponseDTO {
    private BigDecimal amount;
    private Integer term;
    private String email;
    private Long loanTypeId;
}
