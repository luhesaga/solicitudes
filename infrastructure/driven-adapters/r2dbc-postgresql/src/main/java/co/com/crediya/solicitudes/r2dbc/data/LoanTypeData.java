package co.com.crediya.solicitudes.r2dbc.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Table("tipos_prestamo")
public class LoanTypeData {
    @Id
    @Column("id_tipo_prestamo")
    private Long id;

    @Column("nombre")
    private String name;

    @Column("monto_minimo")
    private BigDecimal minimumAmount;

    @Column("monto_maximo")
    private BigDecimal maximumAmount;

    @Column("tasa_interes")
    private Double interestRate;

    @Column("validacion_automatica")
    private Boolean automaticValidation;
}
