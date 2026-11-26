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
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.exception.BeneficiarioNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.RecursoNoEncontradoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.ConflictoDatosException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    private PuertoPolizaFalso puertoPoliza;
    private BeneficiarioService service;

    @BeforeEach
    void configurar() {
        puertoBeneficiario = new PuertoBeneficiarioFalso();
        puertoPoliza = new PuertoPolizaFalso(puertoBeneficiario);
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

    @Test
    void debeEliminarBeneficiarioConOperacionCompuestaConservandoLosDemas() {
        puertoBeneficiario.almacen.add(new Beneficiario(
                "Mario",
                "Prueba",
                "Lopez",
                LocalDate.of(2000, 2, 2),
                CLAVE_EXISTENTE,
                60));
        puertoBeneficiario.almacen.add(new Beneficiario(
                "Ana",
                "Gomez",
                null,
                LocalDate.of(1998, 4, 15),
                CLAVE_EXISTENTE,
                40));

        service.eliminar(
                CLAVE_EXISTENTE, "Mario", "Prueba", "Lopez", LocalDate.of(2000, 2, 2));

        assertEquals(1, puertoPoliza.reinicios);
        List<Beneficiario> restantes = service.listarPorPoliza(CLAVE_EXISTENTE);
        assertEquals(1, restantes.size());
        assertEquals("Ana", restantes.get(0).getNombres());
        assertEquals(40, restantes.get(0).getPorcentaje());
    }

    @Test
    void debeLanzar404AlEliminarBeneficiarioInexistenteSinReiniciarPoliza() {
        assertThrows(
                BeneficiarioNoEncontradoException.class,
                () -> service.eliminar(
                        CLAVE_EXISTENTE,
                        "Nadie",
                        "Ninguno",
                        null,
                        LocalDate.of(1990, 1, 1)));
        assertEquals(0, puertoPoliza.reinicios);
    }

    @Test
    void debeActualizarSoloPorcentajeSinReiniciarPoliza() {
        puertoBeneficiario.almacen.add(new Beneficiario(
                "Mario",
                "Prueba",
                "Lopez",
                LocalDate.of(2000, 2, 2),
                CLAVE_EXISTENTE,
                60));
        puertoBeneficiario.almacen.add(new Beneficiario(
                "Ana",
                "Gomez",
                null,
                LocalDate.of(1998, 4, 15),
                CLAVE_EXISTENTE,
                40));

        Beneficiario cambios = new Beneficiario(
                "Ana",
                "Gomez",
                null,
                LocalDate.of(1998, 4, 15),
                CLAVE_EXISTENTE,
                30);

        Beneficiario actualizado = service.actualizar(
                CLAVE_EXISTENTE,
                "Ana",
                "Gomez",
                null,
                LocalDate.of(1998, 4, 15),
                cambios);

        assertEquals(0, puertoPoliza.reinicios);
        assertEquals(30, actualizado.getPorcentaje());
        assertEquals(2, service.listarPorPoliza(CLAVE_EXISTENTE).size());
    }

    @Test
    void debeActualizarLlaveCompuestaConOperacionCompuestaConservandoHermanos() {
        puertoBeneficiario.almacen.add(new Beneficiario(
                "Mario",
                "Prueba",
                "Lopez",
                LocalDate.of(2000, 2, 2),
                CLAVE_EXISTENTE,
                60));
        puertoBeneficiario.almacen.add(new Beneficiario(
                "Ana",
                "Gomez",
                null,
                LocalDate.of(1998, 4, 15),
                CLAVE_EXISTENTE,
                40));

        Beneficiario cambios = new Beneficiario(
                "Ana Maria",
                "Gomez",
                "Ruiz",
                LocalDate.of(1998, 4, 16),
                CLAVE_EXISTENTE,
                35);

        Beneficiario actualizado = service.actualizar(
                CLAVE_EXISTENTE,
                "Ana",
                "Gomez",
                null,
                LocalDate.of(1998, 4, 15),
                cambios);

        assertEquals(1, puertoPoliza.reinicios);
        assertEquals("Ana Maria", actualizado.getNombres());
        assertEquals(35, actualizado.getPorcentaje());
        List<Beneficiario> finales = service.listarPorPoliza(CLAVE_EXISTENTE);
        assertEquals(2, finales.size());
        assertTrue(finales.stream().anyMatch(b -> "Mario".equals(b.getNombres())));
    }

    @Test
    void debeRechazarEdicionSiExcede100ODuplicaOtroBeneficiarioSinReiniciarPoliza() {
        puertoBeneficiario.almacen.add(new Beneficiario(
                "Mario",
                "Prueba",
                "Lopez",
                LocalDate.of(2000, 2, 2),
                CLAVE_EXISTENTE,
                60));
        puertoBeneficiario.almacen.add(new Beneficiario(
                "Ana",
                "Gomez",
                null,
                LocalDate.of(1998, 4, 15),
                CLAVE_EXISTENTE,
                40));

        Beneficiario excedente = new Beneficiario(
                "Ana",
                "Gomez",
                null,
                LocalDate.of(1998, 4, 15),
                CLAVE_EXISTENTE,
                50);
        assertThrows(
                ReglaNegocioException.class,
                () -> service.actualizar(
                        CLAVE_EXISTENTE,
                        "Ana",
                        "Gomez",
                        null,
                        LocalDate.of(1998, 4, 15),
                        excedente));

        Beneficiario duplicado = new Beneficiario(
                "Mario",
                "Prueba",
                "Lopez",
                LocalDate.of(2000, 2, 2),
                CLAVE_EXISTENTE,
                30);
        assertThrows(
                ConflictoDatosException.class,
                () -> service.actualizar(
                        CLAVE_EXISTENTE,
                        "Ana",
                        "Gomez",
                        null,
                        LocalDate.of(1998, 4, 15),
                        duplicado));

        assertEquals(0, puertoPoliza.reinicios);
    }

    private static final class PuertoPolizaFalso implements PolizaServicioPort {

        private final PuertoBeneficiarioFalso puertoBeneficiario;

        private int reinicios;

        private PuertoPolizaFalso(PuertoBeneficiarioFalso puertoBeneficiario) {
            this.puertoBeneficiario = puertoBeneficiario;
        }

        @Override
        public boolean existePolizaPorClave(UUID clave) {
            return CLAVE_EXISTENTE.equals(clave);
        }

        @Override
        public void reiniciarPolizaConservandoClave(UUID clave) {
            reinicios++;
            puertoBeneficiario.almacen.removeIf(b -> Objects.equals(b.getClavePoliza(), clave));
        }
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
        public List<Beneficiario> listarTodos() {
            return List.copyOf(almacen);
        }

        @Override
        public List<Beneficiario> listarPorPoliza(UUID clavePoliza) {
            return almacen.stream()
                    .filter(b -> Objects.equals(b.getClavePoliza(), clavePoliza))
                    .toList();
        }

        @Override
        public Beneficiario guardar(Beneficiario beneficiario) {
            almacen.removeIf(b -> Objects.equals(b.getId(), beneficiario.getId()));
            almacen.add(beneficiario);
            return beneficiario;
        }
    }
}
