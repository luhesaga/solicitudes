package co.com.crediya.solicitudes.api;

import co.com.crediya.solicitudes.api.dto.*;
import co.com.crediya.solicitudes.api.mapper.RequestMapper;
import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.model.request.UpdateRequestStatus;
import co.com.crediya.solicitudes.usecase.list.ManualListRequestUseCase;
import co.com.crediya.solicitudes.usecase.request.RequestUseCase;
import co.com.crediya.solicitudes.usecase.updatestatus.UpdateRequestStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.security.Principal;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Gestion de Solicitudes", description = "Operaciones para crear y consultar solicitudes de préstamo.")
public class RequestApiRest {
    private final RequestUseCase requestUseCase;
    private final ManualListRequestUseCase manualListRequestUseCase;
    private final RequestMapper requestMapper;
    private final UpdateRequestStatusUseCase updateRequestStatusUseCase;


    @PostMapping(path = "/solicitudes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nueva solicitud de préstamo",
            description = "Recibe los datos de una nueva solicitud, la valida y la registra con un estado inicial de 'RECIBIDA'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Request creada exitosamente.",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (ej. monto fuera de rango, tipo de préstamo no existe).",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PreAuthorize("hasRole('CLIENTE')")
    public Mono<GenericResponseDTO<RequestResponseDTO>> createRequest(
            @RequestBody RequestDTO requestDTO,
            @Parameter(hidden = true) Mono<Principal> principalMono) {

        return principalMono
                .map(Principal::getName)
                .flatMap(authenticatedEmail -> {
                    Request request = requestMapper.toModel(requestDTO);
                            return requestUseCase.createRequest(request, authenticatedEmail);
                })
                .map(requestMapper::toSuccessResponse);
    }

    @GetMapping(path = "/solicitudes")
    @Operation(summary = "Listar solicitudes para revisión manual")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASESOR')")
    public Flux<RequestEnrichedDTO> manualRequestList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return manualListRequestUseCase.list(page, size)
                .map(requestMapper::toEnrichDTO);
    }

    @PutMapping(path = "/solicitudes/{requestId}")
    @Operation(summary = "Aprobar o Rechazar una solicitud")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitud actualizada exitosamente.",
                    content = @Content(schema = @Schema(implementation = GenericResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "El estado proporcionado no es válido."),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene permisos para esta acción."),
            @ApiResponse(responseCode = "404", description = "La solicitud no fue encontrada.")
    })
    @PreAuthorize("hasAnyRole('ASESOR')")
    public Mono<GenericResponseDTO<RequestResponseDTO>> updateRequestStatus(
            @PathVariable("requestId") Long requestId,
            @RequestBody UpdateStatusDTO updateStatusDTO) {

        UpdateRequestStatus command = UpdateRequestStatus.builder()
                .requestId(requestId)
                .status(updateStatusDTO.getNewStatus())
                .build();

        return updateRequestStatusUseCase.execute(command)
                .map(requestMapper::toSuccessResponse);
    }
}
