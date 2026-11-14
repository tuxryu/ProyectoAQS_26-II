package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.MontoInvalidoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para el modelo de dominio Poliza y MontoInvalidoException.
 */
class PolizaTest {

    @Test
    void debeCrearPolizaValida() {
        UUID clave = UUID.randomUUID();
        Poliza poliza = new Poliza(
                clave, (byte) 1, 150000.0f, "Seguro de vida", "BROE550101HDFABC00");

        assertEquals(clave, poliza.getClave());
        assertEquals((byte) 1, poliza.getTipo());
        assertEquals(150000.0f, poliza.getMonto());
        assertEquals("Seguro de vida", poliza.getDescripcion());
        assertEquals("BROE550101HDFABC00", poliza.getCurpCliente());
    }

    @Test
    void debePermitirDescripcionNula() {
        UUID clave = UUID.randomUUID();
        Poliza poliza = new Poliza(
                clave, (byte) 0, 50000.0f, null, "broe550101hdfabc00");

        assertNull(poliza.getDescripcion());
    }

    @Test
    void debeRechazarMontoMenorOIgualACeroConMontoInvalidoException() {
        UUID clave = UUID.randomUUID();

        assertThrows(MontoInvalidoException.class, () -> new Poliza(
                clave, (byte) 0, 0.0f, "Auto", "BROE550101HDFABC00"));

        assertThrows(MontoInvalidoException.class, () -> new Poliza(
                clave, (byte) 0, -500.0f, "Auto", "BROE550101HDFABC00"));
    }

    @Test
    void debeRechazarTipoFueraDeRangoYDiagonales() {
        UUID clave = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> new Poliza(
                clave, (byte) 7, 1000.0f, "Auto", "BROE550101HDFABC00"));

        assertThrows(IllegalArgumentException.class, () -> new Poliza(
                clave, (byte) 0, 1000.0f, "Auto/Moto", "BROE550101HDFABC00"));

        assertThrows(IllegalArgumentException.class, () -> new Poliza(
                clave, (byte) 0, 1000.0f, "Auto", " "));
    }

    @Test
    void debeConstruirMontoInvalidoException() {
        MontoInvalidoException porMonto = new MontoInvalidoException(-500.0f);
        assertTrue(porMonto.getMessage().contains("-500.0"));

        MontoInvalidoException porMensaje
                = new MontoInvalidoException("Monto fuera de rango.");
        assertEquals("Monto fuera de rango.", porMensaje.getMessage());

        IllegalArgumentException causa = new IllegalArgumentException("causa");
        MontoInvalidoException conCausa
                = new MontoInvalidoException("Error de monto", causa);
        assertEquals("Error de monto", conCausa.getMessage());
        assertSame(causa, conCausa.getCause());
    }
}
