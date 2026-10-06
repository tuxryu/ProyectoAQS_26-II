package mx.uacm.aqs.beneficiarios.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import mx.uacm.aqs.beneficiarios.domain.model.Beneficiario;
import org.junit.jupiter.api.Test;

class BeneficiarioTest {

    @Test
    void debeConstruirLlaveCompuesta() {
        Beneficiario beneficiario = new Beneficiario("POL-001", "ABCD010203HDFRRS09", 50);

        assertEquals("POL-001:ABCD010203HDFRRS09", beneficiario.llaveCompuesta());
    }

    @Test
    void debeRechazarPorcentajeFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () ->
                new Beneficiario("POL-001", "ABCD010203HDFRRS09", 0));
        assertThrows(IllegalArgumentException.class, () ->
                new Beneficiario("POL-001", "ABCD010203HDFRRS09", 101));
    }
}
