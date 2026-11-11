package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.FiltroPoliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.PolizaRemotaPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.service.PolizaService;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.FuncionalidadNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de PolizaService con un puerto falso en memoria (sin Spring):
 * verifica lectura por clave, paginacion ordenada por clave, aplicacion de un
 * filtro por llamada y rechazo cuando se envia mas de un filtro o datos invalidos.
 */
class PolizaServiceTest {

    private static final UUID CLAVE_A
            = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID CLAVE_B
            = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID CLAVE_C
            = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID CLAVE_INEXISTENTE
            = UUID.fromString("99999999-9999-9999-9999-999999999999");

    private PuertoPolizaFalso puertoFalso;
    private PolizaService service;

    @BeforeEach
    void configurar() {
        puertoFalso = new PuertoPolizaFalso();
        service = new PolizaService(puertoFalso);
    }

    @Test
    void debeConsultarPolizaPorClaveCuandoExiste() {
        Poliza resultado = service.consultarPorClave(CLAVE_B);

        assertEquals(CLAVE_B, resultado.getClave());
        assertEquals("broe550101hdfabc00", resultado.getCurpCliente());
        assertEquals("buscarPorClave", puertoFalso.ultimaOperacion);
    }

    @Test
    void debeLanzarPolizaNoEncontradaExceptionCuandoLaClaveNoExiste() {
        assertThrows(
                PolizaNoEncontradaException.class,
                () -> service.consultarPorClave(CLAVE_INEXISTENTE));
    }

    @Test
    void debePaginarOrdenandoPorClaveEnPrimeraUltimaYPaginaFueraDeRango() {
        Pagina<Poliza> primera = service.consultar(new FiltroPoliza.Todas(), 0, 2);

        assertEquals(2, primera.contenido().size());
        assertEquals(CLAVE_A, primera.contenido().get(0).getClave());
        assertEquals(CLAVE_B, primera.contenido().get(1).getClave());
        assertEquals(0, primera.pagina());
        assertEquals(2, primera.tamano());
        assertEquals(3L, primera.totalElementos());
        assertEquals(2, primera.totalPaginas());

        Pagina<Poliza> ultima = service.consultar(new FiltroPoliza.Todas(), 1, 2);

        assertEquals(1, ultima.contenido().size());
        assertEquals(CLAVE_C, ultima.contenido().get(0).getClave());
        assertEquals(1, ultima.pagina());
        assertEquals(2, ultima.totalPaginas());

        Pagina<Poliza> fueraDeRango = service.consultar(new FiltroPoliza.Todas(), 5, 2);

        assertTrue(fueraDeRango.contenido().isEmpty());
        assertEquals(5, fueraDeRango.pagina());
        assertEquals(3L, fueraDeRango.totalElementos());
        assertEquals(2, fueraDeRango.totalPaginas());
    }

    @Test
    void debeInvocarUnSoloMetodoDelPuertoSegunElFiltro() {
        service.consultar(FiltroPoliza.desde("broe550101hdfabc00", null, null), 0, 10);
        assertEquals("buscarPorCurp:broe550101hdfabc00", puertoFalso.ultimaOperacion);

        service.consultar(FiltroPoliza.desde(null, 2, null), 0, 10);
        assertEquals("buscarPorTipo:2", puertoFalso.ultimaOperacion);

        service.consultar(new FiltroPoliza.PorCliente("Emmett", "Brown", null), 0, 10);
        assertEquals("buscarPorCliente:Emmett:Brown:null", puertoFalso.ultimaOperacion);

        service.consultar(new FiltroPoliza.PorBeneficiario("Clara", "Clayton", "Smith"), 0, 10);
        assertEquals("buscarPorBeneficiario:Clara:Clayton:Smith", puertoFalso.ultimaOperacion);
    }

    @Test
    void debeRechazarMasDeUnFiltroEnLaMismaConsulta() {
        assertThrows(
                SolicitudInvalidaException.class,
                () -> FiltroPoliza.desde("broe550101hdfabc00", 1, null));
        assertThrows(
                SolicitudInvalidaException.class,
                () -> FiltroPoliza.desde(
                        "broe550101hdfabc00", null, LocalDate.of(1990, 1, 1)));
        assertThrows(
                SolicitudInvalidaException.class,
                () -> FiltroPoliza.desde(null, 0, LocalDate.of(1990, 1, 1)));
    }

    @Test
    void debeRechazarTipoFueraDeRangoYDiagonalesConReglaNegocioException() {
        assertThrows(
                ReglaNegocioException.class,
                () -> FiltroPoliza.desde(null, 7, null));
        assertThrows(
                ReglaNegocioException.class,
                () -> FiltroPoliza.desde("BROE550101/DFABC00", null, null));
        assertThrows(
                ReglaNegocioException.class,
                () -> FiltroPoliza.desde("1", null, null));
        assertThrows(
                ReglaNegocioException.class,
                () -> new FiltroPoliza.PorCliente("Juan\\", "Perez", null));
    }

    @Test
    void debeRechazarPaginacionInvalida() {
        assertThrows(
                SolicitudInvalidaException.class,
                () -> service.consultar(new FiltroPoliza.Todas(), -1, 10));
        assertThrows(
                SolicitudInvalidaException.class,
                () -> service.consultar(new FiltroPoliza.Todas(), 0, 0));
        assertThrows(
                SolicitudInvalidaException.class,
                () -> service.consultar(new FiltroPoliza.Todas(), 0, 101));
    }

    @Test
    void debeLanzarFuncionalidadNoDisponibleParaFiltroPorFechaNacimientoBeneficiario() {
        FiltroPoliza filtro = FiltroPoliza.desde(null, null, LocalDate.of(1990, 1, 1));
        assertThrows(
                FuncionalidadNoDisponibleException.class,
                () -> service.consultar(filtro, 0, 10));
    }

    private static final class PuertoPolizaFalso implements PolizaRemotaPort {

        private String ultimaOperacion;

        private final List<Poliza> datos = List.of(
                new Poliza(CLAVE_C, (byte) 2, 500000.0f, "Medico", "broe550101hdfabc00"),
                new Poliza(CLAVE_A, (byte) 0, 1000000.0f, "Auto", "broe550101hdfabc00"),
                new Poliza(CLAVE_B, (byte) 1, 1500000.0f, "Vida", "broe550101hdfabc00"));

        @Override
        public Optional<Poliza> buscarPorClave(UUID clave) {
            ultimaOperacion = "buscarPorClave";
            return datos.stream()
                    .filter(poliza -> poliza.getClave().equals(clave))
                    .findFirst();
        }

        @Override
        public List<Poliza> listarTodas() {
            ultimaOperacion = "listarTodas";
            return datos;
        }

        @Override
        public List<Poliza> buscarPorCurp(String curp) {
            ultimaOperacion = "buscarPorCurp:" + curp;
            return datos.stream()
                    .filter(poliza -> poliza.getCurpCliente().equals(curp))
                    .toList();
        }

        @Override
        public List<Poliza> buscarPorTipo(int tipo) {
            ultimaOperacion = "buscarPorTipo:" + tipo;
            return datos.stream()
                    .filter(poliza -> poliza.getTipo() == (byte) tipo)
                    .toList();
        }

        @Override
        public List<Poliza> buscarPorCliente(
                String nombres, String primerApellido, String segundoApellido) {
            ultimaOperacion = "buscarPorCliente:"
                    + nombres + ":" + primerApellido + ":" + segundoApellido;
            return datos;
        }

        @Override
        public List<Poliza> buscarPorBeneficiario(
                String nombres, String primerApellido, String segundoApellido) {
            ultimaOperacion = "buscarPorBeneficiario:"
                    + nombres + ":" + primerApellido + ":" + segundoApellido;
            return datos;
        }
    }
}
