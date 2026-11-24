package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ConsultaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.FiltroPoliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Controlador web Thymeleaf de Capa 4 para las consultas GET de polizas:
 * listado paginado con 6 modos de filtro (todas, CURP, tipo, fecha de
 * nacimiento de beneficiario, nombre de cliente y nombre de beneficiario) y
 * detalle de poliza con sus beneficiarios actuales. Invoca unicamente puertos
 * de entrada ({@link ConsultaPolizaUseCase} y
 * {@link ConsultaBeneficiarioUseCase}).
 */
@Controller
@RequestMapping("/web/polizas")
public class PolizaViewController {

    private static final int TAMANO_POR_DEFECTO = 10;

    private static final int PORCENTAJE_MAXIMO = 100;

    private static final String FILTRO_TODAS = "todas";

    private static final String FILTRO_CURP = "curp";

    private static final String FILTRO_TIPO = "tipo";

    private static final String FILTRO_FECHA_BENEFICIARIO = "fechaBeneficiario";

    private static final String FILTRO_CLIENTE = "cliente";

    private static final String FILTRO_BENEFICIARIO = "beneficiario";

    private static final String VISTA_LISTA = "polizas/lista";

    private static final String VISTA_DETALLE = "polizas/detalle";

    private static final String ATRIBUTO_ALERTA = "alerta";

    private static final String ATRIBUTO_PAGINA = "paginaPolizas";

    private final ConsultaPolizaUseCase consultaPolizaUseCase;

    private final ConsultaBeneficiarioUseCase consultaBeneficiarioUseCase;

    /**
     * Crea el controlador de vistas de lectura de polizas inyectando
     * unicamente puertos de entrada.
     *
     * @param consultaPolizaUseCase puerto de consulta de polizas
     * @param consultaBeneficiarioUseCase puerto de consulta de beneficiarios
     */
    public PolizaViewController(
            ConsultaPolizaUseCase consultaPolizaUseCase,
            ConsultaBeneficiarioUseCase consultaBeneficiarioUseCase) {
        this.consultaPolizaUseCase = Objects.requireNonNull(
                consultaPolizaUseCase,
                "El puerto ConsultaPolizaUseCase es obligatorio.");
        this.consultaBeneficiarioUseCase = Objects.requireNonNull(
                consultaBeneficiarioUseCase,
                "El puerto ConsultaBeneficiarioUseCase es obligatorio.");
    }

    /**
     * Muestra el listado paginado de polizas aplicando el filtro seleccionado
     * (todas, CURP, tipo, fecha de nacimiento de beneficiario, nombre de
     * cliente o nombre de beneficiario).
     *
     * @param tipoFiltro criterio de filtro seleccionado
     * @param curp CURP del titular (sin alterar mayusculas/minusculas)
     * @param tipo tipo de poliza 0..2
     * @param fechaBeneficiario fecha AAAA-MM-DD de nacimiento de beneficiario
     * @param nombres nombres del cliente o beneficiario
     * @param primerApellido primer apellido del cliente o beneficiario
     * @param segundoApellido segundo apellido opcional
     * @param pagina numero de pagina (base 0)
     * @param tamano cantidad de elementos por pagina
     * @param model modelo para la vista Thymeleaf
     * @return plantilla del listado paginado de polizas
     */
    @GetMapping
    public String listar(
            @RequestParam(defaultValue = FILTRO_TODAS) String tipoFiltro,
            @RequestParam(required = false) String curp,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String fechaBeneficiario,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            Model model) {
        String filtroNormalizado = normalizarTipoFiltro(tipoFiltro);
        cargarEstadoFiltros(
                model,
                filtroNormalizado,
                curp,
                tipo,
                fechaBeneficiario,
                nombres,
                primerApellido,
                segundoApellido,
                pagina,
                tamano);
        try {
            FiltroPoliza filtro = construirFiltro(
                    filtroNormalizado,
                    curp,
                    tipo,
                    fechaBeneficiario,
                    nombres,
                    primerApellido,
                    segundoApellido);
            Pagina<Poliza> resultado = consultaPolizaUseCase.consultar(
                    filtro, pagina, tamano);
            model.addAttribute(ATRIBUTO_PAGINA, resultado);
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_PAGINA, paginaVacia(tamano));
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
        }
        return VISTA_LISTA;
    }

    /**
     * Consulta una poliza por su clave UUID recibida como parametro de consulta
     * y muestra sus datos junto con sus beneficiarios actuales.
     *
     * @param clave clave UUID opcional de la poliza
     * @param model modelo para la vista
     * @return plantilla de detalle de poliza
     */
    @GetMapping("/consultar")
    public String consultarPorParametro(
            @RequestParam(required = false) String clave,
            Model model) {
        model.addAttribute("claveBuscada", valorSeguro(clave));
        inicializarBeneficiariosDetalle(model);
        if (clave == null || clave.trim().isEmpty()) {
            return VISTA_DETALLE;
        }
        cargarDetalleConBeneficiarios(clave.trim(), model);
        return VISTA_DETALLE;
    }

    /**
     * Muestra el detalle de una poliza y sus beneficiarios actuales a partir
     * de su clave UUID en la ruta.
     *
     * @param clave clave UUID de la poliza
     * @param model modelo para la vista
     * @return plantilla de detalle de poliza
     */
    @GetMapping("/{clave}")
    public String verDetalle(@PathVariable String clave, Model model) {
        model.addAttribute("claveBuscada", valorSeguro(clave));
        inicializarBeneficiariosDetalle(model);
        cargarDetalleConBeneficiarios(clave.trim(), model);
        return VISTA_DETALLE;
    }

    /**
     * Maneja errores de tipo en parametros de consulta (por ejemplo pagina no
     * numerica) respondiendo con HTML amigable en lugar de JSON.
     *
     * @param ex excepcion de conversion de parametro
     * @param model modelo para la vista
     * @return plantilla del listado con alerta 422
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String manejarParametroInvalido(
            MethodArgumentTypeMismatchException ex, Model model) {
        cargarEstadoFiltros(
                model, FILTRO_TODAS, null, null, null, null, null, null, 0, TAMANO_POR_DEFECTO);
        model.addAttribute(ATRIBUTO_PAGINA, paginaVacia(TAMANO_POR_DEFECTO));
        model.addAttribute(
                ATRIBUTO_ALERTA,
                AlertaWeb.validacion(
                        "Los parametros de paginacion deben ser numeros enteros validos."));
        return VISTA_LISTA;
    }

    private void cargarDetalleConBeneficiarios(String claveTexto, Model model) {
        try {
            UUID uuid = parsearUuid(claveTexto);
            Poliza poliza = consultaPolizaUseCase.consultarPorClave(uuid);
            model.addAttribute("poliza", poliza);
            cargarBeneficiariosDePoliza(uuid, model);
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
        }
    }

    private void cargarBeneficiariosDePoliza(UUID clavePoliza, Model model) {
        try {
            List<Beneficiario> beneficiarios
                    = consultaBeneficiarioUseCase.listarPorPoliza(clavePoliza);
            int suma = beneficiarios.stream()
                    .mapToInt(Beneficiario::getPorcentaje)
                    .sum();
            int disponible = Math.max(0, PORCENTAJE_MAXIMO - suma);
            model.addAttribute("beneficiarios", beneficiarios);
            model.addAttribute("sumaPorcentaje", suma);
            model.addAttribute("porcentajeDisponible", disponible);
            model.addAttribute("beneficiariosCargados", true);
        } catch (RuntimeException ex) {
            model.addAttribute("beneficiarios", List.of());
            model.addAttribute("beneficiariosCargados", false);
            model.addAttribute("alertaBeneficiarios", AlertaWeb.desdeExcepcion(ex));
        }
    }

    private void inicializarBeneficiariosDetalle(Model model) {
        model.addAttribute("beneficiarios", List.of());
        model.addAttribute("sumaPorcentaje", 0);
        model.addAttribute("porcentajeDisponible", PORCENTAJE_MAXIMO);
        model.addAttribute("beneficiariosCargados", false);
    }

    private FiltroPoliza construirFiltro(
            String tipoFiltro,
            String curp,
            String tipo,
            String fechaBeneficiario,
            String nombres,
            String primerApellido,
            String segundoApellido) {
        if (FILTRO_TODAS.equals(tipoFiltro)) {
            return new FiltroPoliza.Todas();
        }
        if (FILTRO_CURP.equals(tipoFiltro)) {
            return new FiltroPoliza.PorCurp(limpiarTexto(curp));
        }
        if (FILTRO_TIPO.equals(tipoFiltro)) {
            return new FiltroPoliza.PorTipo(parsearTipo(tipo));
        }
        if (FILTRO_FECHA_BENEFICIARIO.equals(tipoFiltro)) {
            return new FiltroPoliza.PorFechaNacimientoBeneficiario(
                    parsearFecha(fechaBeneficiario));
        }
        if (FILTRO_CLIENTE.equals(tipoFiltro)) {
            return new FiltroPoliza.PorCliente(
                    limpiarTexto(nombres),
                    limpiarTexto(primerApellido),
                    normalizarOpcional(segundoApellido));
        }
        if (FILTRO_BENEFICIARIO.equals(tipoFiltro)) {
            return new FiltroPoliza.PorBeneficiario(
                    limpiarTexto(nombres),
                    limpiarTexto(primerApellido),
                    normalizarOpcional(segundoApellido));
        }
        throw new ReglaNegocioException(
                "El tipo de filtro seleccionado no es valido.");
    }

    private String normalizarTipoFiltro(String tipoFiltro) {
        if (tipoFiltro == null || tipoFiltro.trim().isEmpty()) {
            return FILTRO_TODAS;
        }
        return tipoFiltro.trim();
    }

    private UUID parsearUuid(String uuidTexto) {
        try {
            return UUID.fromString(uuidTexto);
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException(
                    "La clave de la poliza debe tener un formato UUID valido.");
        }
    }

    private int parsearTipo(String tipoTexto) {
        if (tipoTexto == null || tipoTexto.trim().isEmpty()) {
            throw new ReglaNegocioException(
                    "Seleccione un tipo de poliza: 0 (Auto), 1 (Vida) o 2 (Medico).");
        }
        try {
            return Integer.parseInt(tipoTexto.trim());
        } catch (NumberFormatException ex) {
            throw new ReglaNegocioException(
                    "El tipo de poliza debe ser 0 (Auto), 1 (Vida) o 2 (Medico).");
        }
    }

    private LocalDate parsearFecha(String fechaTexto) {
        if (fechaTexto == null || fechaTexto.trim().isEmpty()) {
            throw new ReglaNegocioException(AlertaWeb.mensajeFechaInvalida());
        }
        try {
            return LocalDate.parse(fechaTexto.trim());
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException(AlertaWeb.mensajeFechaInvalida());
        }
    }

    private void cargarEstadoFiltros(
            Model model,
            String tipoFiltro,
            String curp,
            String tipo,
            String fechaBeneficiario,
            String nombres,
            String primerApellido,
            String segundoApellido,
            int pagina,
            int tamano) {
        model.addAttribute("tipoFiltro", tipoFiltro);
        model.addAttribute("filtroCurp", valorSeguro(curp));
        model.addAttribute("filtroTipo", valorSeguro(tipo));
        model.addAttribute("filtroFechaBeneficiario", valorSeguro(fechaBeneficiario));
        model.addAttribute("filtroNombres", valorSeguro(nombres));
        model.addAttribute("filtroPrimerApellido", valorSeguro(primerApellido));
        model.addAttribute("filtroSegundoApellido", valorSeguro(segundoApellido));
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("tamanoActual", tamano);
        model.addAttribute("hayFiltroActivo", !FILTRO_TODAS.equals(tipoFiltro));
    }

    private Pagina<Poliza> paginaVacia(int tamano) {
        int tamanoSeguro = tamano >= 1 ? tamano : TAMANO_POR_DEFECTO;
        return new Pagina<>(List.of(), 0, tamanoSeguro, 0L, 0);
    }

    private String valorSeguro(String valor) {
        return valor != null ? valor : "";
    }

    private String limpiarTexto(String valor) {
        return valor == null ? null : valor.trim();
    }

    private String normalizarOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
