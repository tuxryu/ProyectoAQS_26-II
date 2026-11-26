package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in;

import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;

/**
 * Filtro de una consulta de clientes. Admite listar a todos o filtrar por
 * campos especificos; cada variante valida sus propios datos al construirse.
 *
 * <p>Los textos se conservan tal cual (sin normalizar mayusculas) porque el
 * Sistema Dueno distingue mayusculas y minusculas.
 */
public sealed interface FiltroCliente {

    /** Longitud maxima permitida para nombres y apellidos. */
    int LONGITUD_MAXIMA_NOMBRE = 128;

    /**
     * Crea el filtro del listado a partir de los parametros opcionales de nombre.
     *
     * @param nombres nombres del cliente (opcional)
     * @param primerApellido primer apellido (opcional)
     * @param segundoApellido segundo apellido (opcional)
     * @return el filtro indicado por nombre completo, o {@link Todos} si no se envio filtro
     * @throws SolicitudInvalidaException si los datos enviados son incoherentes
     */
    static FiltroCliente desde(String nombres, String primerApellido, String segundoApellido) {
        String nombresNorm = normalizarOpcional(nombres);
        String primerNorm = normalizarOpcional(primerApellido);
        String segundoNorm = normalizarOpcional(segundoApellido);
        if (nombresNorm == null && primerNorm == null && segundoNorm == null) {
            return new Todos();
        }
        return new PorNombre(nombresNorm, primerNorm, segundoNorm);
    }

    private static String normalizarOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private static void validarLongitudYCaracteres(String valor, String campo) {
        if (valor != null) {
            if (valor.length() > LONGITUD_MAXIMA_NOMBRE) {
                throw new ReglaNegocioException(
                        campo + " excede los " + LONGITUD_MAXIMA_NOMBRE + " caracteres.");
            }
            if (valor.contains("/") || valor.contains("\\")) {
                throw new ReglaNegocioException(campo + " no puede contener / ni \\.");
            }
        }
    }

    /**
     * Sin filtro: todos los clientes.
     */
    record Todos() implements FiltroCliente {
    }

    /**
     * Clientes filtrados por nombre o apellidos (requiere al menos un criterio).
     *
     * @param nombres nombres del cliente (opcional)
     * @param primerApellido primer apellido del cliente (opcional)
     * @param segundoApellido segundo apellido del cliente (opcional)
     */
    record PorNombre(String nombres, String primerApellido, String segundoApellido)
            implements FiltroCliente {

        /**
         * Valida que exista al menos un criterio y que ningun campo exceda la
         * longitud maxima ni incluya diagonales.
         */
        public PorNombre {
            boolean sinNombres = nombres == null || nombres.trim().isEmpty();
            boolean sinPrimer = primerApellido == null || primerApellido.trim().isEmpty();
            boolean sinSegundo = segundoApellido == null || segundoApellido.trim().isEmpty();
            if (sinNombres && sinPrimer && sinSegundo) {
                throw new SolicitudInvalidaException(
                        "Debe indicar al menos un nombre o apellido para filtrar.");
            }
            validarLongitudYCaracteres(nombres, "El parametro nombres");
            validarLongitudYCaracteres(primerApellido, "El parametro primerApellido");
            validarLongitudYCaracteres(segundoApellido, "El parametro segundoApellido");
        }
    }
}
