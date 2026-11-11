package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.out.rest;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.FiltroPoliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para {@link PolizaRestAdapter}.
 */
class PolizaRestAdapterTest {

    private static final UUID CLAVE_EXISTENTE
            = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID CLAVE_INEXISTENTE
            = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @Test
    void debeRetornarTrueCuandoPolizaExisteEnMemoria() {
        PolizaRestAdapter adapter = new PolizaRestAdapter(new ConsultaPolizaFalsa());

        assertTrue(adapter.existePolizaPorClave(CLAVE_EXISTENTE));
    }

    @Test
    void debeRetornarFalseCuandoPolizaNoExisteOClaveEsNula() {
        PolizaRestAdapter adapter = new PolizaRestAdapter(new ConsultaPolizaFalsa());

        assertFalse(adapter.existePolizaPorClave(CLAVE_INEXISTENTE));
        assertFalse(adapter.existePolizaPorClave(null));
    }

    @Test
    void debeRechazarCasoDeUsoNuloEnConstructor() {
        assertThrows(
                NullPointerException.class,
                () -> new PolizaRestAdapter(null));
    }

    private static final class ConsultaPolizaFalsa implements ConsultaPolizaUseCase {

        @Override
        public Poliza consultarPorClave(UUID clave) {
            if (CLAVE_EXISTENTE.equals(clave)) {
                return new Poliza(
                        CLAVE_EXISTENTE,
                        (byte) 1,
                        150000.0f,
                        "Poliza de vida",
                        "BROE550101HDFABC00");
            }
            throw new PolizaNoEncontradaException(clave);
        }

        @Override
        public Pagina<Poliza> consultar(FiltroPoliza filtro, int pagina, int tamano) {
            throw new UnsupportedOperationException("No requerido en esta prueba.");
        }
    }
}
