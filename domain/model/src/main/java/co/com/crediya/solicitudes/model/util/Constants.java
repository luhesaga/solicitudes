package co.com.crediya.solicitudes.model.util;
import java.util.regex.Pattern;

public class Constants {
    private Constants() {}

    // Estado inicial de toda nueva solicitud
    public static final String ESTADO_INICIAL_SOLICITUD = "RECIBIDA";
    // Expresión regular para una validación básica de email.
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    // Mensajes de Error de Negocio
    public static final String ERROR_MONTO_O_PLAZO_INVALIDO = "El monto y el plazo deben ser valores positivos.";
    public static final String ERROR_TIPO_PRESTAMO_NO_EXISTE = "El tipo de préstamo seleccionado no existe.";
    public static final String ERROR_MONTO_FUERA_DE_RANGO = "El monto solicitado está fuera de los rangos permitidos para el tipo de préstamo.";
    public static final String ERROR_FORMATO_EMAIL_INVALIDO = "El email no puede ser nulo y debe tener un formato válido.";
    public static final String ERROR_EMAIL_NO_AUTENTICADO = "No tiene permiso para crear solicitudes para otros usuarios.";
}
