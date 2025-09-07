package co.com.crediya.solicitudes.api.handler;

import co.com.crediya.solicitudes.api.dto.ErrorDTO;
import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Logger para registrar los errores internos
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja las excepciones de negocio
     * Estas son las excepciones que lanzamos a propósito desde el UseCase.
     */
    @ExceptionHandler(BusinessValidationException.class)
    public Mono<ResponseEntity<ErrorDTO>> handleBusinessValidationException(BusinessValidationException ex) { // Cambiado el tipo de excepción
        logger.warn("Excepción de negocio: {}", ex.getMessage());
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorDTO(ex.getMessage()))
        );
    }

    /**
     * Maneja excepciones cuando un recurso no se encuentra.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorDTO>> handleResponseStatusException(ResponseStatusException ex) {
        logger.warn("Excepción de estado de respuesta: {}", ex.getMessage());
        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorDTO(ex.getMessage()))
        );
    }


    /**
     * Para el Manejo de cualquier otra excepción no controlada.
     * (Ej: error de conexión a la BD, NullPointerException, etc.).
     * Le devuelve al cliente un mensaje genérico para no exponer detalles internos.
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorDTO>> handleGenericException(Exception ex) {
        logger.error("Error inesperado en la aplicación", ex);

        return Mono.just(
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorDTO("Ha ocurrido un error inesperado. Por favor, contacte al soporte."))
        );
    }
}
