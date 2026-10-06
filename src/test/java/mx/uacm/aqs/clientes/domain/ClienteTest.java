package mx.uacm.aqs.clientes.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import mx.uacm.aqs.clientes.domain.model.Cliente;
import org.junit.jupiter.api.Test;

class ClienteTest {

    @Test
    void debeCrearClienteConCurpValida() {
        assertDoesNotThrow(() ->
                new Cliente("ABCD010203HDFRRS09", "Ana", "Lopez", "Diaz"));
    }

    @Test
    void debeRechazarCurpInvalida() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cliente("CURP-CORTA", "Ana", "Lopez", "Diaz"));
    }

    @Test
    void debeRechazarCamposObligatoriosVacios() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cliente("ABCD010203HDFRRS09", " ", "Lopez", "Diaz"));
    }
}
