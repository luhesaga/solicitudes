package co.com.crediya.solicitudes.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenericResponseDTO<T> {
    private String code;
    private String message;
    private T data;
}
