package co.com.crediya.solicitudes.r2dbc.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Table("solicitudes")
public class RequestData {
    @Id
    @Column("id_solicitud")
    private Long id;

    @Column("monto")
    private BigDecimal amount;

    @Column("plazo")
    private Integer term;

    @Column("email")
    private String email;

    @Column("fecha_creacion")
    private LocalDateTime creationDate;

    @Column("id_estado")
    private Long statusId;

    @Column("id_tipo_prestamo")
    private Long loanTypeId;

    @ReadOnlyProperty
    @Column("nombreTipoPrestamo")
    private String loanTypeName;

    @ReadOnlyProperty
    @Column("tasaInteres")
    private Double interestRate;

    @ReadOnlyProperty
    @Column("nombreEstado")
    private String statusName;
}
