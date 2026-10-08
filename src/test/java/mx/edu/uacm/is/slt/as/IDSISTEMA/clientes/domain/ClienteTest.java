package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain;

import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    void crearClienteValidoConTodosLosCampos() {
        Cliente cliente = new Cliente(
                "ABCDEF123456789012",
                "Luis Enrique",
                "Vargas",
                "Loza",
                "Av. Siempre Viva 123",
                LocalDate.of(2000, 5, 15)
        );

        assertNotNull(cliente);
        assertEquals("ABCDEF123456789012", cliente.getCurp());
        assertEquals("Loza", cliente.getSegundoApellido());
    }

    @Test
    void crearClienteValidoSinSegundoApellido() {
        Cliente cliente = new Cliente(
                "ABCDEF123456789012",
                "Luis",
                "Vargas",
                null,
                "Av. Siempre Viva 123",
                LocalDate.of(2000, 5, 15)
        );

        assertNotNull(cliente);
        assertNull(cliente.getSegundoApellido());
    }

    @Test
    void errorCuandoCurpNoTiene18Caracteres() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("CURP_INCORRECTO", "Luis", "Vargas", null, "Dirección", LocalDate.now());
        });

        assertEquals("El CURP debe tener exactamente 18 caracteres.", exception.getMessage());
    }

    @Test
    void errorCuandoNombreEsNulo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("ABCDEF123456789012", null, "Vargas", null, "Dirección", LocalDate.now());
        });

        assertEquals("El nombre no puede ser nulo ni vacío.", exception.getMessage());
    }

    @Test
    void errorCuandoFechaNacimientoEsNula() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Cliente("ABCDEF123456789012", "Luis", "Vargas", null, "Dirección", null);
        });

        assertEquals("La fecha de nacimiento no puede ser nula.", exception.getMessage());
    }
    
}

