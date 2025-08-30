package co.com.crediya.solicitudes.api;

import co.com.crediya.solicitudes.api.dto.ErrorDTO;
import co.com.crediya.solicitudes.api.dto.SolicitudDTO;
import co.com.crediya.solicitudes.model.exception.BusinessValidationException;
import co.com.crediya.solicitudes.model.solicitud.Solicitud;
import co.com.crediya.solicitudes.usecase.solicitud.SolicitudUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Gestión de Solicitudes", description = "Operaciones para crear y consultar solicitudes de préstamo.")
public class ApiRest {
    private final SolicitudUseCase solicitudUseCase;

    // Mapeador simple para convertir el DTO al Modelo de Dominio
    private Solicitud toModel(SolicitudDTO dto) {
        return Solicitud.builder()
                .monto(dto.getMonto())
                .plazo(dto.getPlazo())
                .email(dto.getEmail())
                .idTipoPrestamo(dto.getIdTipoPrestamo())
                .build();
    }

    @PostMapping(path = "/solicitudes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nueva solicitud de préstamo",
            description = "Recibe los datos de una nueva solicitud, la valida y la registra con un estado inicial de 'RECIBIDA'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente."),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (ej. monto fuera de rango, tipo de préstamo no existe).",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    public Mono<Solicitud> crearSolicitud(@RequestBody SolicitudDTO solicitudDTO) {
        return Mono.just(solicitudDTO)
                .map(this::toModel)
                .flatMap(solicitudUseCase::crearSolicitud)
                .onErrorMap(BusinessValidationException.class, e ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())
                );
    }
}
