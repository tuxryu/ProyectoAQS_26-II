package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias para el modelo de dominio Beneficiario.
 */
class BeneficiarioTest {

    @Test
    void debeCrearBeneficiarioValido() {
        UUID clavePoliza = UUID.randomUUID();
        LocalDate fecha = LocalDate.of(1995, 5, 20);

        Beneficiario beneficiario = new Beneficiario(
                "Ana", "Lopez", "Perez", fecha, clavePoliza, 50);

        assertNotNull(beneficiario.getId());
        assertEquals("Ana", beneficiario.getNombres());
        assertEquals("Lopez", beneficiario.getPrimerApellido());
        assertEquals("Perez", beneficiario.getSegundoApellido());
        assertEquals(fecha, beneficiario.getFechaNacimiento());
        assertEquals(clavePoliza, beneficiario.getClavePoliza());
        assertEquals(50, beneficiario.getPorcentaje());
    }

    @Test
    void debeRechazarPorcentajeFueraDeRango() {
        UUID clavePoliza = UUID.randomUUID();
        LocalDate fecha = LocalDate.of(1995, 5, 20);

        assertThrows(IllegalArgumentException.class, () -> new Beneficiario(
                "Ana", "Lopez", null, fecha, clavePoliza, 0));

        assertThrows(IllegalArgumentException.class, () -> new Beneficiario(
                "Ana", "Lopez", null, fecha, clavePoliza, 101));
    }

    @Test
    void debeRechazarCamposObligatoriosNulosOVacios() {
        UUID clavePoliza = UUID.randomUUID();
        LocalDate fecha = LocalDate.of(1995, 5, 20);

        assertThrows(IllegalArgumentException.class, () -> new Beneficiario(
                "", "Lopez", null, fecha, clavePoliza, 50));

        assertThrows(IllegalArgumentException.class, () -> new Beneficiario(
                "Ana", " ", null, fecha, clavePoliza, 50));

        assertThrows(IllegalArgumentException.class, () -> new Beneficiario(
                "Ana", "Lopez", null, null, clavePoliza, 50));

        assertThrows(IllegalArgumentException.class, () -> new Beneficiario(
                "Ana", "Lopez", null, fecha, null, 50));
    }
}
