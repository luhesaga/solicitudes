package co.com.crediya.solicitudes.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UpdateStatusDTO {

    @Schema(description = "El nuevo estado para la solicitud. Debe ser 'APROBADA' o 'RECHAZADA'.",
            example = "APROBADA")
    private String newStatus;
}
