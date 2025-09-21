package co.com.crediya.solicitudes.r2dbc.mapper;

import co.com.crediya.solicitudes.model.request.Request;
import co.com.crediya.solicitudes.r2dbc.data.RequestData;

public class RequestMapper {

    private RequestMapper() {}

    public static Request toRequestModel(RequestData data) {
        return Request.builder()
                .id(data.getId())
                .amount(data.getAmount())
                .term(data.getTerm())
                .email(data.getEmail())
                .creationDate(data.getCreationDate())
                .statusId(data.getStatusId())
                .loanTypeId(data.getLoanTypeId())
                .loanTypeName(data.getLoanTypeName())
                .interestRate(data.getInterestRate())
                .statusName(data.getStatusName())
                .build();
    }

    public static RequestData toRequestData(Request model) {
        RequestData data = new RequestData();
        data.setId(model.getId());
        data.setAmount(model.getAmount());
        data.setTerm(model.getTerm());
        data.setEmail(model.getEmail());
        data.setCreationDate(model.getCreationDate());
        data.setStatusId(model.getStatusId());
        data.setLoanTypeId(model.getLoanTypeId());
        return data;
    }
}
