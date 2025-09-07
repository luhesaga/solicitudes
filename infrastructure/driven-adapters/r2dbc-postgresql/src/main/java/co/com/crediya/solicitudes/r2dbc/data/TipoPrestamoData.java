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
public class TipoPrestamoData {
    @Id
    @Column("id_tipo_prestamo")
    private Long idTipoPrestamo;

    @Column("nombre")
    private String nombre;

    @Column("monto_minimo")
    private BigDecimal montoMinimo;

    @Column("monto_maximo")
    private BigDecimal montoMaximo;

    @Column("tasa_interes")
    private Double tasaInteres;

    @Column("validacion_automatica")
    private Boolean validacionAutomatica;
}
