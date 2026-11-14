package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in;

import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;

/**
 * Filtro de una consulta de clientes. Admite listar a todos o filtrar por
 * campos específicos; cada variante valida sus propios datos al construirse.
 *
 * <p>Los textos se conservan tal cual (sin normalizar mayúsculas) porque el
 * Sistema Dueño distingue mayúsculas y minúsculas.
 */
public sealed interface FiltroCliente {

    /**
     * Crea el filtro del listado a partir de los parámetros opcionales de nombre.
     *
     * @param nombres nombres del cliente (opcional)
     * @param primerApellido primer apellido (opcional)
     * @param segundoApellido segundo apellido (opcional)
     * @return el filtro indicado por nombre completo, o {@link Todos} si no se envió filtro
     * @throws SolicitudInvalidaException si los datos enviados son incoherentes
     */
    static FiltroCliente desde(String nombres, String primerApellido, String segundoApellido) {
        if (nombres == null && primerApellido == null && segundoApellido == null) {
            return new Todos();
        }
        return new PorNombre(nombres, primerApellido, segundoApellido);
    }

    private static void exigirTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new SolicitudInvalidaException(campo + " es obligatorio.");
        }
        validarSinDiagonales(valor, campo);
    }

    private static void validarSinDiagonales(String valor, String campo) {
        if (valor != null && (valor.contains("/") || valor.contains("\\"))) {
            throw new ReglaNegocioException(campo + " no puede contener / ni \\.");
        }
    }

    /**
     * Sin filtro: todos los clientes.
     */
    record Todos() implements FiltroCliente {
    }

    /**
     * Clientes filtrados por nombre completo.
     *
     * @param nombres nombres del cliente
     * @param primerApellido primer apellido del cliente
     * @param segundoApellido segundo apellido del cliente (opcional)
     */
    record PorNombre(String nombres, String primerApellido, String segundoApellido)
            implements FiltroCliente {

        /**
         * Valida nombres y apellidos.
         */
        public PorNombre {
            exigirTexto(nombres, "El parametro nombres");
            exigirTexto(primerApellido, "El parametro primerApellido");
            validarSinDiagonales(segundoApellido, "El parametro segundoApellido");
        }
    }
}