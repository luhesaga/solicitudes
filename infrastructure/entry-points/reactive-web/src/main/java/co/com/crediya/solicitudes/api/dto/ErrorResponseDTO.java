package co.com.crediya.solicitudes.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ErrorResponseDTO {
    private String code;
    private String message;
    private List<String> errors;
}
