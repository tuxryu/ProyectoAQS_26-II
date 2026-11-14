package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out.BeneficiarioSistemaDuenoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out.PolizaServicioPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.service.BeneficiarioService;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias de {@link BeneficiarioService} con puertos falsos en
 * memoria (sin Spring).
 */
class BeneficiarioServiceTest {

    private static final UUID CLAVE_EXISTENTE
            = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID CLAVE_INEXISTENTE
            = UUID.fromString("99999999-9999-9999-9999-999999999999");

    private PuertoBeneficiarioFalso puertoBeneficiario;
    private PolizaServicioPort puertoPoliza;
    private BeneficiarioService service;

    @BeforeEach
    void configurar() {
        puertoBeneficiario = new PuertoBeneficiarioFalso();
        puertoPoliza = clave -> CLAVE_EXISTENTE.equals(clave);
        service = new BeneficiarioService(puertoBeneficiario, puertoPoliza);
    }

    @Test
    void debeRegistrarBeneficiarioCuandoPolizaExisteYSumaNoRebasa100() {
        puertoBeneficiario.almacen.add(new Beneficiario(
                "Mario",
                "Prueba",
                "Lopez",
                LocalDate.of(2000, 2, 2),
                CLAVE_EXISTENTE,
                60));

        Beneficiario nuevo = new Beneficiario(
                "Ana",
                "Gomez",
                null,
                LocalDate.of(1998, 4, 15),
                CLAVE_EXISTENTE,
                40);

        Beneficiario registrado = service.registrar(nuevo);

        assertEquals("Ana", registrado.getNombres());
        assertEquals(40, registrado.getPorcentaje());
        assertEquals(2, service.listarPorPoliza(CLAVE_EXISTENTE).size());
    }

    @Test
    void debeLanzar404CuandoPolizaNoExisteAlRegistrarOListar() {
        Beneficiario nuevo = new Beneficiario(
                "Ana",
                "Gomez",
                null,
                LocalDate.of(1998, 4, 15),
                CLAVE_INEXISTENTE,
                40);

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.registrar(nuevo));
        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.listarPorPoliza(CLAVE_INEXISTENTE));
    }

    @Test
    void debeLanzar409CuandoBeneficiarioYaExisteEnLaPoliza() {
        Beneficiario existente = new Beneficiario(
                "Mario",
                "Prueba",
                "Lopez",
                LocalDate.of(2000, 2, 2),
                CLAVE_EXISTENTE,
                60);
        puertoBeneficiario.almacen.add(existente);

        assertThrows(
                ConflictoDatosException.class,
                () -> service.registrar(existente));
    }

    @Test
    void debeLanzar422CuandoSumaDePorcentajesRebasaEl100PorCiento() {
        puertoBeneficiario.almacen.add(new Beneficiario(
                "Mario",
                "Prueba",
                "Lopez",
                LocalDate.of(2000, 2, 2),
                CLAVE_EXISTENTE,
                70));

        Beneficiario excedente = new Beneficiario(
                "Ana",
                "Gomez",
                null,
                LocalDate.of(1998, 4, 15),
                CLAVE_EXISTENTE,
                35);

        assertThrows(
                ReglaNegocioException.class,
                () -> service.registrar(excedente));
    }

    @Test
    void debeConsultarBeneficiarioPorDatosYLanzar404SiNoExiste() {
        Beneficiario existente = new Beneficiario(
                "Mario",
                "Prueba",
                "Lopez",
                LocalDate.of(2000, 2, 2),
                CLAVE_EXISTENTE,
                60);
        puertoBeneficiario.almacen.add(existente);

        Beneficiario encontrado = service.consultarPorDatos(
                CLAVE_EXISTENTE,
                "Mario",
                "Prueba",
                "Lopez",
                LocalDate.of(2000, 2, 2));
        assertEquals(60, encontrado.getPorcentaje());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.consultarPorDatos(
                        CLAVE_EXISTENTE,
                        "Inexistente",
                        "Prueba",
                        null,
                        LocalDate.of(2000, 2, 2)));
    }

    private static final class PuertoBeneficiarioFalso
            implements BeneficiarioSistemaDuenoPort {

        private final List<Beneficiario> almacen = new ArrayList<>();

        @Override
        public Optional<Beneficiario> buscarPorDatos(
                LocalDate fechaNacimiento,
                UUID clavePoliza,
                String nombres,
                String primerApellido,
                String segundoApellido) {
            return almacen.stream()
                    .filter(b -> Objects.equals(b.getFechaNacimiento(), fechaNacimiento)
                            && Objects.equals(b.getClavePoliza(), clavePoliza)
                            && Objects.equals(b.getNombres(), nombres)
                            && Objects.equals(b.getPrimerApellido(), primerApellido)
                            && Objects.equals(b.getSegundoApellido(), segundoApellido))
                    .findFirst();
        }

        @Override
        public List<Beneficiario> listarPorPoliza(UUID clavePoliza) {
            return almacen.stream()
                    .filter(b -> Objects.equals(b.getClavePoliza(), clavePoliza))
                    .toList();
        }

        @Override
        public Beneficiario guardar(Beneficiario beneficiario) {
            almacen.add(beneficiario);
            return beneficiario;
        }
    }
}
