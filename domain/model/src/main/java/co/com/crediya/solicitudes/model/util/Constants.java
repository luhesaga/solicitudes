package co.com.crediya.solicitudes.model.util;

public class Constants {
    private Constants() {}

    // Estado inicial de toda nueva solicitud
    public static final String ESTADO_INICIAL_SOLICITUD = "RECIBIDA";

    // Mensajes de Error de Negocio
    public static final String ERROR_MONTO_O_PLAZO_INVALIDO = "El monto y el plazo deben ser valores positivos.";
    public static final String ERROR_TIPO_PRESTAMO_NO_EXISTE = "El tipo de préstamo seleccionado no existe.";
    public static final String ERROR_MONTO_FUERA_DE_RANGO = "El monto solicitado está fuera de los rangos permitidos para el tipo de préstamo.";
    public static final String ERROR_ESTADO_INICIAL_NO_ENCONTRADO = "El estado inicial de configuración no se encuentra en el sistema.";
    public static final String ERROR_FORMATO_EMAIL_INVALIDO = "El email no puede ser nulo y debe tener un formato válido.";
}
