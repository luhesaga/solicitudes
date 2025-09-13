package co.com.crediya.solicitudes.api.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiError {

    INVALID_DATA("400-001", "Datos de entrada inválidos"),
    ACCESS_DENIED("403-001", "El usuario no tiene los permisos necesarios para realizar esta acción."),
    UNEXPECTED_ERROR("500-001", "Ha ocurrido un error inesperado. Por favor, contacte al soporte.");

    private final String code;
    private final String message;
}
