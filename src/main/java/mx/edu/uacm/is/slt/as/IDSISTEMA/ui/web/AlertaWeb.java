package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import java.time.format.DateTimeParseException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;

/**
 * Modelo de alerta amigable para mostrar mensajes de exito, advertencia o
 * error en las vistas Thymeleaf sin filtrar trazas ni detalles internos.
 *
 * @param tipo clase visual de la alerta ({@code exito}, {@code aviso}, {@code error})
 * @param codigo codigo HTTP asociado (p. ej. 404, 409, 422, 502, 503) o {@code null}
 * @param titulo encabezado breve de la alerta
 * @param mensaje descripcion amigable para el usuario
 */
public record AlertaWeb(String tipo, Integer codigo, String titulo, String mensaje) {

    private static final char A_ACENTO = (char) 0xE1;

    private static final char O_ACENTO = (char) 0xF3;

    private static final char U_ACENTO = (char) 0xFA;

    private static final char N_TILDE = (char) 0xF1;

    private static final int HTTP_404 = 404;

    private static final int HTTP_409 = 409;

    private static final int HTTP_422 = 422;

    private static final int HTTP_502 = 502;

    private static final int HTTP_503 = 503;

    private static final int HTTP_500 = 500;

    private static final String TIPO_EXITO = "exito";

    private static final String TIPO_AVISO = "aviso";

    private static final String TIPO_ERROR = "error";

    private static final String MENSAJE_404
            = "No se encontr" + O_ACENTO + " el recurso solicitado.";

    private static final String MENSAJE_409
            = "Ya existe un registro con los mismos datos de identificaci" + O_ACENTO + "n.";

    private static final String MENSAJE_422
            = "Los datos capturados no cumplen las reglas de validaci" + O_ACENTO + "n.";

    private static final String MENSAJE_502
            = "El Sistema Due" + N_TILDE + "o rechaz" + O_ACENTO
            + " la operaci" + O_ACENTO
            + "n o no confirm" + O_ACENTO + " los cambios. Verifique los datos e intente de nuevo.";

    private static final String MENSAJE_503
            = "El Sistema Due" + N_TILDE + "o no est" + A_ACENTO
            + " disponible temporalmente. Por favor, intente m" + A_ACENTO + "s tarde.";

    private static final String MENSAJE_500
            = "Ocurri" + O_ACENTO + " un error inesperado al procesar la solicitud.";

    /**
     * Crea una alerta de operacion exitosa.
     *
     * @param titulo titulo breve
     * @param mensaje detalle del resultado
     * @return alerta de tipo exito
     */
    public static AlertaWeb exito(String titulo, String mensaje) {
        return new AlertaWeb(TIPO_EXITO, null, titulo, mensaje);
    }

    /**
     * Crea una alerta de validacion (422) con un mensaje controlado.
     *
     * @param mensaje motivo de validacion
     * @return alerta con codigo 422
     */
    public static AlertaWeb validacion(String mensaje) {
        return new AlertaWeb(
                TIPO_ERROR,
                HTTP_422,
                "Datos inv" + A_ACENTO + "lidos",
                sanitizar(mensaje, MENSAJE_422));
    }

    /**
     * Traduce una excepcion lanzada por un caso de uso en una alerta amigable
     * correspondiente a los codigos 404, 409, 422, 502 o 503.
     *
     * @param ex excepcion capturada en el controlador de vistas
     * @return alerta lista para renderizar en pantalla
     */
    public static AlertaWeb desdeExcepcion(RuntimeException ex) {
        if (ex == null) {
            return errorInterno();
        }
        if (ex instanceof RecursoNoEncontradoException) {
            return new AlertaWeb(
                    TIPO_AVISO,
                    HTTP_404,
                    "Recurso no encontrado",
                    sanitizar(ex.getMessage(), MENSAJE_404));
        }
        if (ex instanceof ReglaNegocioException
                || ex instanceof SolicitudInvalidaException
                || ex instanceof IllegalArgumentException
                || ex instanceof DateTimeParseException) {
            return validacion(ex.getMessage());
        }
        return desdeNombreExcepcion(ex);
    }

    private static AlertaWeb desdeNombreExcepcion(RuntimeException ex) {
        String nombre = ex.getClass().getSimpleName();
        if ("ConflictoDatosException".equals(nombre)) {
            return new AlertaWeb(
                    TIPO_AVISO,
                    HTTP_409,
                    "Registro duplicado",
                    sanitizar(ex.getMessage(), MENSAJE_409));
        }
        if ("SistemaDuenoRechazoException".equals(nombre)) {
            return new AlertaWeb(
                    TIPO_ERROR,
                    HTTP_502,
                    "Operaci" + O_ACENTO + "n rechazada por el Sistema Due" + N_TILDE + "o",
                    MENSAJE_502);
        }
        if ("SistemaDuenoNoDisponibleException".equals(nombre)) {
            return new AlertaWeb(
                    TIPO_ERROR,
                    HTTP_503,
                    "Servicio temporalmente no disponible",
                    MENSAJE_503);
        }
        return errorInterno();
    }

    private static AlertaWeb errorInterno() {
        return new AlertaWeb(
                TIPO_ERROR,
                HTTP_500,
                "Error inesperado",
                MENSAJE_500);
    }

    private static String sanitizar(String mensaje, String respaldo) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return respaldo;
        }
        if (contieneInformacionInterna(mensaje)) {
            return respaldo;
        }
        return mensaje;
    }

    private static boolean contieneInformacionInterna(String texto) {
        return texto.contains("\n")
                || texto.contains("\r")
                || texto.contains("\tat ")
                || texto.contains("Exception")
                || texto.contains("mx.edu.uacm")
                || texto.contains("org.springframework")
                || texto.contains("java.lang")
                || texto.contains("nachintoch.mx")
                || texto.contains("http://")
                || texto.contains("https://")
                || texto.contains("C:\\")
                || texto.contains("/home/");
    }

    /**
     * Devuelve un mensaje con acentos correctos para fechas invalidas.
     *
     * @return texto amigable de error de fecha
     */
    public static String mensajeFechaInvalida() {
        return "La fecha de nacimiento es obligatoria y debe tener el formato AAAA-MM-DD.";
    }

    /**
     * Devuelve un mensaje amigable cuando la CURP en alta no esta en mayusculas.
     *
     * @return texto amigable de validacion de mayusculas
     */
    public static String mensajeCurpMayusculas() {
        return "Para el alta de cliente, la CURP debe capturarse completamente en may"
                + U_ACENTO + "sculas (18 caracteres).";
    }

    /**
     * Devuelve un mensaje amigable de cliente registrado con exito.
     *
     * @param curp CURP del cliente creado
     * @return mensaje de confirmacion
     */
    public static String mensajeAltaExitosa(String curp) {
        return "El cliente con CURP " + curp
                + " se registr" + O_ACENTO + " correctamente y fue verificado en el Sistema Due"
                + N_TILDE + "o.";
    }

    /**
     * Devuelve un mensaje amigable de cliente actualizado con exito.
     *
     * @param curp CURP del cliente actualizado
     * @return mensaje de confirmacion
     */
    public static String mensajeActualizacionExitosa(String curp) {
        return "Los datos del cliente con CURP " + curp
                + " se actualizaron correctamente y fueron verificados en el Sistema Due"
                + N_TILDE + "o.";
    }

    /**
     * Devuelve un titulo con acento para operaciones exitosas.
     *
     * @return titulo de exito
     */
    public static String tituloOperacionExitosa() {
        return "Operaci" + O_ACENTO + "n exitosa";
    }

    /**
     * Devuelve un mensaje cuando falta la CURP en una busqueda.
     *
     * @return texto amigable de validacion
     */
    public static String mensajeCurpObligatoria() {
        return "Ingrese una CURP v" + A_ACENTO + "lida para realizar la consulta.";
    }

    /**
     * Devuelve un mensaje cuando la CURP incluye diagonales prohibidas.
     *
     * @return texto amigable de validacion
     */
    public static String mensajeCurpSinDiagonales() {
        return "La CURP no puede contener los caracteres / ni \\.";
    }

    /**
     * Devuelve un mensaje cuando la CURP excede los 18 caracteres.
     *
     * @return texto amigable de validacion
     */
    public static String mensajeCurpLongitud() {
        return "La CURP no puede exceder los 18 caracteres.";
    }
}
