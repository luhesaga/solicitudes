package co.com.crediya.solicitudes.api.mapper;

import co.com.crediya.solicitudes.api.dto.GenericResponseDTO;
import co.com.crediya.solicitudes.api.dto.RequestDTO;
import co.com.crediya.solicitudes.api.dto.RequestEnrichedDTO;
import co.com.crediya.solicitudes.api.dto.RequestResponseDTO;
import co.com.crediya.solicitudes.model.request.Request;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public Request toModel(RequestDTO dto) {
        return Request.builder()
                .amount(dto.getAmount())
                .term(dto.getTerm())
                .email(dto.getEmail())
                .loanTypeId(dto.getLoanTypeId())
                .build();
    }

    public RequestEnrichedDTO toEnrichDTO(Request model) {
        return RequestEnrichedDTO.builder()
                .amount(model.getAmount())
                .term(model.getTerm())
                .email(model.getEmail())
                .userName(model.getUserName())
                .salary(model.getSalary())
                .loanTypeName(model.getLoanTypeName())
                .interestRate(model.getInterestRate())
                .statusName(model.getStatusName())
                .approximateMonthlyAmount(model.getApproximateMonthlyAmount())
                .build();
    }

    public GenericResponseDTO<RequestResponseDTO> toSuccessResponse(Request request) {
        RequestResponseDTO requestData = RequestResponseDTO.builder()
                .amount(request.getAmount())
                .term(request.getTerm())
                .email(request.getEmail())
                .loanTypeId(request.getLoanTypeId())
                .build();

        return GenericResponseDTO.<RequestResponseDTO>builder()
                .code("201-001")
                .message("Operación exitosa.")
                .data(requestData)
                .build();
    }
}
