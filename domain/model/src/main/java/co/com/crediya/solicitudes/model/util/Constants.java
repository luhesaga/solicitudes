package co.com.crediya.solicitudes.model.util;
import java.util.regex.Pattern;

public class Constants {
    private Constants() {}

    public static final String INITIAL_REQUEST_STATUS = "RECIBIDA";
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    public static final String ERROR_AMOUNT_OR_RATE_INVALID = "El monto y el plazo deben ser valores positivos.";
    public static final String ERROR_LOAN_TYPE_DOES_NOT_EXIST = "El tipo de préstamo seleccionado no existe.";
    public static final String ERROR_OUT_OF_RANGE_AMOUNT = "El monto solicitado está fuera de los rangos permitidos para el tipo de préstamo.";
    public static final String ERROR_INVALID_EMAIL_FORMAT = "El email no puede ser nulo y debe tener un formato válido.";
    public static final String ERROR_UNAUTHENTICATED_EMAIL = "No tiene permiso para crear solicitudes para otros usuarios.";
    public static final String ERROR_USER_NOT_FOUND = "No se encontró usuario en el servicio de autenticación para el email: {}";
    public static final String ERROR_REQUEST = "Error enriqueciendo solicitud para el email: {}";
    public static  final String ERROR_MESSAGE_REQUIRED_FIELDS = "Los campos nombres, apellidos, email y salario_base son obligatorios.";
}
