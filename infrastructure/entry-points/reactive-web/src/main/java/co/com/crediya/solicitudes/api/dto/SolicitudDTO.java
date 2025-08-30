package co.com.crediya.solicitudes.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para la creación de una nueva solicitud de préstamo.")
public class SolicitudDTO {
    @Schema(description = "Monto del préstamo solicitado.", example = "1500000.00")
    private BigDecimal monto;

    @Schema(description = "Plazo en meses para pagar el préstamo.", example = "12")
    private Integer plazo;

    @Schema(description = "Email del solicitante para notificaciones.", example = "solicitante@example.com")
    private String email;

    @Schema(description = "ID del tipo de préstamo que se está solicitando.", example = "1")
    private Long idTipoPrestamo;
}
