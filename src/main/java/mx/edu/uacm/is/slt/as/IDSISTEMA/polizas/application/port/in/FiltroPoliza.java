package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in;

import java.time.LocalDate;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;

/**
 * Filtro de una consulta de polizas. Cada consulta admite a lo mucho un filtro;
 * cada variante valida sus propios datos al construirse.
 *
 * <p>Los textos se conservan tal cual (sin normalizar mayusculas) porque el
 * Sistema Dueno distingue mayusculas y minusculas.
 */
public sealed interface FiltroPoliza {

    /**
     * Crea el filtro del listado general a partir de los parametros opcionales.
     *
     * @param curp CURP del titular (opcional)
     * @param tipo tipo de poliza (opcional)
     * @param fechaNacimientoBeneficiario fecha de nacimiento de un beneficiario (opcional)
     * @return el filtro indicado, o {@link Todas} si no se envio ninguno
     * @throws SolicitudInvalidaException si se envia mas de un filtro
     */
    static FiltroPoliza desde(String curp, Integer tipo, LocalDate fechaNacimientoBeneficiario) {
        int filtros = 0;
        if (curp != null) {
            filtros++;
        }
        if (tipo != null) {
            filtros++;
        }
        if (fechaNacimientoBeneficiario != null) {
            filtros++;
        }
        if (filtros > 1) {
            throw new SolicitudInvalidaException(
                    "Solo se permite un filtro por consulta: curp, tipo o "
                    + "fechaNacimientoBeneficiario.");
        }
        if (curp != null) {
            return new PorCurp(curp);
        }
        if (tipo != null) {
            return new PorTipo(tipo);
        }
        if (fechaNacimientoBeneficiario != null) {
            return new PorFechaNacimientoBeneficiario(fechaNacimientoBeneficiario);
        }
        return new Todas();
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
     * Sin filtro: todas las polizas.
     */
    record Todas() implements FiltroPoliza {
    }

    /**
     * Polizas de un cliente por su CURP.
     *
     * @param curp CURP del titular, tal cual
     */
    record PorCurp(String curp) implements FiltroPoliza {

        /**
         * Valida la CURP.
         *
         * @throws ReglaNegocioException si la CURP es solo numerica, porque el
         *         Sistema Dueno la confundiria con un tipo de poliza
         */
        public PorCurp {
            exigirTexto(curp, "La CURP");
            if (curp.trim().chars().allMatch(Character::isDigit)) {
                throw new ReglaNegocioException(
                        "La CURP no puede ser solo numerica: " + curp + ".");
            }
        }
    }

    /**
     * Polizas de un tipo.
     *
     * @param tipo tipo de poliza (0 = Auto, 1 = Vida, 2 = Medico)
     */
    record PorTipo(int tipo) implements FiltroPoliza {

        /**
         * Valida que el tipo este entre 0 y 2.
         */
        public PorTipo {
            if (tipo < 0 || tipo > 2) {
                throw new ReglaNegocioException(
                        "El tipo de poliza debe ser 0 (Auto), 1 (Vida) o 2 (Medico); "
                        + "se recibio " + tipo + ".");
            }
        }
    }

    /**
     * Polizas con un beneficiario nacido en la fecha indicada.
     *
     * @param fechaNacimiento fecha de nacimiento del beneficiario
     */
    record PorFechaNacimientoBeneficiario(LocalDate fechaNacimiento) implements FiltroPoliza {

        /**
         * Valida que la fecha no sea nula.
         */
        public PorFechaNacimientoBeneficiario {
            if (fechaNacimiento == null) {
                throw new SolicitudInvalidaException(
                        "La fecha de nacimiento del beneficiario es obligatoria.");
            }
        }
    }

    /**
     * Polizas cuyo titular tiene el nombre indicado.
     *
     * @param nombres nombres del cliente
     * @param primerApellido primer apellido del cliente
     * @param segundoApellido segundo apellido del cliente (opcional)
     */
    record PorCliente(String nombres, String primerApellido, String segundoApellido)
            implements FiltroPoliza {

        /**
         * Valida nombres y apellidos.
         */
        public PorCliente {
            exigirTexto(nombres, "El parametro nombres");
            exigirTexto(primerApellido, "El parametro primerApellido");
            validarSinDiagonales(segundoApellido, "El parametro segundoApellido");
        }
    }

    /**
     * Polizas que tienen un beneficiario con el nombre indicado.
     *
     * @param nombres nombres del beneficiario
     * @param primerApellido primer apellido del beneficiario
     * @param segundoApellido segundo apellido del beneficiario (opcional)
     */
    record PorBeneficiario(String nombres, String primerApellido, String segundoApellido)
            implements FiltroPoliza {

        /**
         * Valida nombres y apellidos.
         */
        public PorBeneficiario {
            exigirTexto(nombres, "El parametro nombres");
            exigirTexto(primerApellido, "El parametro primerApellido");
            validarSinDiagonales(segundoApellido, "El parametro segundoApellido");
        }
    }
}
