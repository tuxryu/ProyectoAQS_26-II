package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.exception.ClienteNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.exception.CurpInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para el modelo de dominio Cliente y sus excepciones.
 */
class ClienteTest {

    @Test
    void debeCrearClienteValido() {
        LocalDate fecha = LocalDate.of(1990, 1, 1);
        Cliente cliente = new Cliente(
                "BROE550101HDFABC00",
                "Emmett",
                "Brown",
                "Lathrop",
                "Av. Universidad 123",
                fecha);

        assertEquals("BROE550101HDFABC00", cliente.getCurp());
        assertEquals("Emmett", cliente.getNombres());
        assertEquals("Brown", cliente.getPrimerApellido());
        assertEquals("Lathrop", cliente.getSegundoApellido());
        assertEquals("Av. Universidad 123", cliente.getDireccion());
        assertEquals(fecha, cliente.getFechaNacimiento());
    }

    @Test
    void debeRechazarCurpInvalidaConCurpInvalidaException() {
        LocalDate fecha = LocalDate.of(1990, 1, 1);

        assertThrows(CurpInvalidaException.class, () -> new Cliente(
                "", "Emmett", "Brown", null, "Dir", fecha));

        assertThrows(CurpInvalidaException.class, () -> new Cliente(
                "CORTA123", "Emmett", "Brown", null, "Dir", fecha));

        assertThrows(CurpInvalidaException.class, () -> new Cliente(
                "BROE550101/DFABC00", "Emmett", "Brown", null, "Dir", fecha));
    }

    @Test
    void debeRechazarCamposObligatoriosInvalidos() {
        LocalDate fecha = LocalDate.of(1990, 1, 1);

        assertThrows(IllegalArgumentException.class, () -> new Cliente(
                "BROE550101HDFABC00", " ", "Brown", null, "Dir", fecha));

        assertThrows(IllegalArgumentException.class, () -> new Cliente(
                "BROE550101HDFABC00", "Emmett", "Brown/Jr", null, "Dir", fecha));

        assertThrows(IllegalArgumentException.class, () -> new Cliente(
                "BROE550101HDFABC00", "Emmett", "Brown", null, "Dir", null));
    }

    @Test
    void debeConstruirExcepcionesDeDominioDeCliente() {
        ClienteNoEncontradoException noEncontrado
                = new ClienteNoEncontradoException("BROE550101HDFABC00");
        assertInstanceOf(RecursoNoEncontradoException.class, noEncontrado);
        assertTrue(noEncontrado.getMessage().contains("BROE550101HDFABC00"));

        IllegalArgumentException causa = new IllegalArgumentException("causa");
        CurpInvalidaException curpInvalida
                = new CurpInvalidaException("CURP con formato invalido", causa);
        assertEquals("CURP con formato invalido", curpInvalida.getMessage());
        assertSame(causa, curpInvalida.getCause());
    }
}
