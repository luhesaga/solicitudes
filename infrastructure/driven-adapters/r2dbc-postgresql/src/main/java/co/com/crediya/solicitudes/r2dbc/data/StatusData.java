package co.com.crediya.solicitudes.r2dbc.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@Table("estados")
public class StatusData {
    @Id
    @Column("id_estado")
    private Long id;

    @Column("nombre")
    private String name;

    @Column("descripcion")
    private String description;
}
