package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.exception.BeneficiarioNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.exception.PorcentajeInvalidoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.BeneficiarioId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void debeRechazarPorcentajeFueraDeRangoConExcepcionDeDominio() {
        UUID clavePoliza = UUID.randomUUID();
        LocalDate fecha = LocalDate.of(1995, 5, 20);

        assertThrows(PorcentajeInvalidoException.class, () -> new Beneficiario(
                "Ana", "Lopez", null, fecha, clavePoliza, 0));

        assertThrows(PorcentajeInvalidoException.class, () -> new Beneficiario(
                "Ana", "Lopez", null, fecha, clavePoliza, 101));
    }

    @Test
    void debeConstruirExcepcionesDeDominioDeBeneficiario() {
        UUID clavePoliza = UUID.randomUUID();
        LocalDate fecha = LocalDate.of(1995, 5, 20);
        BeneficiarioId id = new BeneficiarioId("Ana", "Lopez", "Perez", fecha, clavePoliza);

        BeneficiarioNoEncontradoException noEncontradoPorId
                = new BeneficiarioNoEncontradoException(id);
        assertTrue(noEncontradoPorId.getMessage().contains("Ana Lopez"));
        assertTrue(noEncontradoPorId.getMessage().contains(clavePoliza.toString()));

        BeneficiarioNoEncontradoException noEncontradoPorMensaje
                = new BeneficiarioNoEncontradoException("Beneficiario inexistente.");
        assertEquals("Beneficiario inexistente.", noEncontradoPorMensaje.getMessage());

        PorcentajeInvalidoException porcentajeInvalido
                = new PorcentajeInvalidoException("La suma de porcentajes excede el 100%.");
        assertEquals("La suma de porcentajes excede el 100%.", porcentajeInvalido.getMessage());
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
