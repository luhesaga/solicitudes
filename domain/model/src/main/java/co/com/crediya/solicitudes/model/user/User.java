package co.com.crediya.solicitudes.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class User {
    // Definimos solo los campos que nos interesan del microservicio de autenticación
    private String name;
    private String lastname;
    private BigDecimal salary;
}
