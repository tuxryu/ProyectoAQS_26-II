package mx.uacm.aqs.polizas.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;
import mx.uacm.aqs.polizas.domain.model.Poliza;
import mx.uacm.aqs.polizas.domain.model.TipoPoliza;
import org.junit.jupiter.api.Test;

class PolizaTest {

    @Test
    void debeCrearPolizaValida() {
        assertDoesNotThrow(() ->
                new Poliza(UUID.randomUUID(), TipoPoliza.AUTO, new BigDecimal("12000.00"), "ABCD010203HDFRRS09"));
    }

    @Test
    void debeRechazarMontoNoPositivo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Poliza(UUID.randomUUID(), TipoPoliza.VIDA, BigDecimal.ZERO, "ABCD010203HDFRRS09"));
    }

    @Test
    void debeRechazarClienteNoAsociado() {
        assertThrows(IllegalArgumentException.class, () ->
                new Poliza(UUID.randomUUID(), TipoPoliza.MEDICO, new BigDecimal("1000"), ""));
    }
}
