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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ActualizarBeneficiarioUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ConsultaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.FiltroBeneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.RegistrarBeneficiarioUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.BeneficiarioId;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Controlador web Thymeleaf de Capa 4 para el modulo de beneficiarios: listado
 * paginado, consulta individual por llave compuesta, alta y modificacion.
 */
@Controller
@RequestMapping("/web/beneficiarios")
public class BeneficiarioViewController {

    private static final int TAMANO_POR_DEFECTO = 10;

    private static final String VISTA_LISTA = "beneficiarios/lista";

    private static final String VISTA_DETALLE = "beneficiarios/detalle";

    private static final String VISTA_FORMULARIO = "beneficiarios/formulario";

    private static final String VISTA_BUSCAR_EDITAR = "beneficiarios/buscar-editar";

    private static final String ATRIBUTO_ALERTA = "alerta";

    private static final String ATRIBUTO_FORMULARIO = "formulario";

    private static final String ATRIBUTO_MODO_EDICION = "modoEdicion";

    private final ConsultaBeneficiarioUseCase consultaBeneficiarioUseCase;

    private final RegistrarBeneficiarioUseCasePort registrarBeneficiarioUseCasePort;

    private final ActualizarBeneficiarioUseCasePort actualizarBeneficiarioUseCasePort;

    public BeneficiarioViewController(
            ConsultaBeneficiarioUseCase consultaBeneficiarioUseCase,
            RegistrarBeneficiarioUseCasePort registrarBeneficiarioUseCasePort,
            ActualizarBeneficiarioUseCasePort actualizarBeneficiarioUseCasePort) {
        this.consultaBeneficiarioUseCase = Objects.requireNonNull(
                consultaBeneficiarioUseCase,
                "El puerto ConsultaBeneficiarioUseCase es obligatorio.");
        this.registrarBeneficiarioUseCasePort = Objects.requireNonNull(
                registrarBeneficiarioUseCasePort,
                "El puerto RegistrarBeneficiarioUseCasePort es obligatorio.");
        this.actualizarBeneficiarioUseCasePort = Objects.requireNonNull(
                actualizarBeneficiarioUseCasePort,
                "El puerto ActualizarBeneficiarioUseCasePort es obligatorio.");
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String clavePoliza,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            Model model) {
        String clavePolizaNorm = normalizarOpcional(clavePoliza);
        String nombresNorm = normalizarOpcional(nombres);
        String primerApNorm = normalizarOpcional(primerApellido);
        String segundoApNorm = normalizarOpcional(segundoApellido);

        cargarFiltrosLista(model, clavePolizaNorm, nombresNorm, primerApNorm, segundoApNorm, pagina, tamano);
        try {
            UUID uuidPoliza = clavePolizaNorm != null ? parsearUuid(clavePolizaNorm) : null;
            FiltroBeneficiario filtro = FiltroBeneficiario.desde(
                    uuidPoliza, nombresNorm, primerApNorm, segundoApNorm);
            Pagina<Beneficiario> resultado = consultaBeneficiarioUseCase.consultar(
                    filtro, pagina, tamano);
            model.addAttribute("paginaBeneficiarios", resultado);
        } catch (RuntimeException ex) {
            model.addAttribute("paginaBeneficiarios", paginaVacia(tamano));
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
        }
        return VISTA_LISTA;
    }

    @GetMapping("/consultar")
    public String consultarPorId(
            @RequestParam(required = false) String clavePoliza,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String fechaNacimiento,
            Model model) {
        cargarParametrosBusqueda(model, clavePoliza, nombres, primerApellido, segundoApellido, fechaNacimiento);

        if (clavePoliza == null && nombres == null && primerApellido == null && fechaNacimiento == null) {
            return VISTA_DETALLE;
        }
        try {
            BeneficiarioId id = construirId(
                    clavePoliza, nombres, primerApellido, segundoApellido, fechaNacimiento);
            Beneficiario beneficiario = consultaBeneficiarioUseCase.consultarPorId(id);
            model.addAttribute("beneficiario", beneficiario);
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
        }
        return VISTA_DETALLE;
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        model.addAttribute(ATRIBUTO_FORMULARIO, new BeneficiarioFormulario());
        model.addAttribute(ATRIBUTO_MODO_EDICION, false);
        return VISTA_FORMULARIO;
    }

    @PostMapping("/nuevo")
    public String registrar(
            @ModelAttribute("formulario") BeneficiarioFormulario formulario,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Beneficiario nuevo = construirBeneficiario(formulario);
            Beneficiario creado = registrarBeneficiarioUseCasePort.registrarBeneficiario(nuevo);
            redirectAttributes.addFlashAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.exito(
                            AlertaWeb.tituloOperacionExitosa(),
                            "Beneficiario registrado correctamente."));
            agregarParametrosRedirect(redirectAttributes, creado);
            return "redirect:/web/beneficiarios/consultar";
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_FORMULARIO, formulario);
            model.addAttribute(ATRIBUTO_MODO_EDICION, false);
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            return VISTA_FORMULARIO;
        }
    }

    @GetMapping("/editar")
    public String buscarParaEditar(
            @RequestParam(required = false) String clavePoliza,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String fechaNacimiento,
            Model model) {
        cargarParametrosBusqueda(model, clavePoliza, nombres, primerApellido, segundoApellido, fechaNacimiento);

        if (clavePoliza == null && nombres == null && primerApellido == null && fechaNacimiento == null) {
            return VISTA_BUSCAR_EDITAR;
        }
        try {
            BeneficiarioId id = construirId(
                    clavePoliza, nombres, primerApellido, segundoApellido, fechaNacimiento);
            Beneficiario beneficiario = consultaBeneficiarioUseCase.consultarPorId(id);
            model.addAttribute(ATRIBUTO_FORMULARIO, BeneficiarioFormulario.desde(beneficiario));
            model.addAttribute(ATRIBUTO_MODO_EDICION, true);
            return VISTA_FORMULARIO;
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            return VISTA_BUSCAR_EDITAR;
        }
    }

    @PostMapping("/editar")
    public String actualizar(
            @ModelAttribute("formulario") BeneficiarioFormulario formulario,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Beneficiario actualizado = construirBeneficiario(formulario);
            Beneficiario guardado = actualizarBeneficiarioUseCasePort.actualizarBeneficiario(actualizado);
            redirectAttributes.addFlashAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.exito(
                            AlertaWeb.tituloOperacionExitosa(),
                            "Porcentaje del beneficiario actualizado correctamente."));
            agregarParametrosRedirect(redirectAttributes, guardado);
            return "redirect:/web/beneficiarios/consultar";
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_FORMULARIO, formulario);
            model.addAttribute(ATRIBUTO_MODO_EDICION, true);
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            return VISTA_FORMULARIO;
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String manejarParametroInvalido(
            MethodArgumentTypeMismatchException ex, Model model) {
        cargarFiltrosLista(model, null, null, null, null, 0, TAMANO_POR_DEFECTO);
        model.addAttribute("paginaBeneficiarios", paginaVacia(TAMANO_POR_DEFECTO));
        model.addAttribute(
                ATRIBUTO_ALERTA,
                AlertaWeb.validacion(
                        "Los parametros de paginacion o porcentaje deben ser numeros enteros validos."));
        return VISTA_LISTA;
    }

    private Beneficiario construirBeneficiario(BeneficiarioFormulario formulario) {
        BeneficiarioId id = construirId(
                formulario.getClavePoliza(),
                formulario.getNombres(),
                formulario.getPrimerApellido(),
                formulario.getSegundoApellido(),
                formulario.getFechaNacimiento());
        if (formulario.getPorcentaje() == null) {
            throw new ReglaNegocioException("El porcentaje es obligatorio.");
        }
        return new Beneficiario(id, formulario.getPorcentaje());
    }

    private BeneficiarioId construirId(
            String clavePolizaTexto,
            String nombresTexto,
            String primerApellidoTexto,
            String segundoApellidoTexto,
            String fechaNacimientoTexto) {
        UUID clavePoliza = parsearUuid(clavePolizaTexto);
        String nombres = limpiarObligatorio(nombresTexto);
        String primerApellido = limpiarObligatorio(primerApellidoTexto);
        String segundoApellido = normalizarOpcional(segundoApellidoTexto);
        LocalDate fechaNacimiento = parsearFecha(fechaNacimientoTexto);
        try {
            return new BeneficiarioId(
                    nombres, primerApellido, segundoApellido, fechaNacimiento, clavePoliza);
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException(ex.getMessage());
        }
    }

    private UUID parsearUuid(String uuidTexto) {
        if (uuidTexto == null || uuidTexto.trim().isEmpty()) {
            throw new ReglaNegocioException("La clave de la poliza (UUID) es obligatoria.");
        }
        try {
            return UUID.fromString(uuidTexto.trim());
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException("La clave de la poliza no tiene un formato UUID valido.");
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

    private void agregarParametrosRedirect(RedirectAttributes redirectAttributes, Beneficiario b) {
        redirectAttributes.addAttribute("clavePoliza", b.getClavePoliza().toString());
        redirectAttributes.addAttribute("nombres", b.getNombres());
        redirectAttributes.addAttribute("primerApellido", b.getPrimerApellido());
        if (b.getSegundoApellido() != null) {
            redirectAttributes.addAttribute("segundoApellido", b.getSegundoApellido());
        }
        redirectAttributes.addAttribute("fechaNacimiento", b.getFechaNacimiento().toString());
    }

    private void cargarParametrosBusqueda(
            Model model,
            String clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellido,
            String fechaNacimiento) {
        model.addAttribute("busquedaClavePoliza", clavePoliza != null ? clavePoliza : "");
        model.addAttribute("busquedaNombres", nombres != null ? nombres : "");
        model.addAttribute("busquedaPrimerApellido", primerApellido != null ? primerApellido : "");
        model.addAttribute("busquedaSegundoApellido", segundoApellido != null ? segundoApellido : "");
        model.addAttribute("busquedaFechaNacimiento", fechaNacimiento != null ? fechaNacimiento : "");
    }

    private void cargarFiltrosLista(
            Model model,
            String clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellido,
            int pagina,
            int tamano) {
        model.addAttribute("filtroClavePoliza", clavePoliza != null ? clavePoliza : "");
        model.addAttribute("filtroNombres", nombres != null ? nombres : "");
        model.addAttribute("filtroPrimerApellido", primerApellido != null ? primerApellido : "");
        model.addAttribute("filtroSegundoApellido", segundoApellido != null ? segundoApellido : "");
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("tamanoActual", tamano);
        boolean hayFiltro = clavePoliza != null
                || nombres != null
                || primerApellido != null
                || segundoApellido != null;
        model.addAttribute("hayFiltro", hayFiltro);
    }

    private Pagina<Beneficiario> paginaVacia(int tamano) {
        int tamanoSeguro = tamano >= 1 ? tamano : TAMANO_POR_DEFECTO;
        return new Pagina<>(List.of(), 0, tamanoSeguro, 0L, 0);
    }

    private String normalizarOpcional(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private String limpiarObligatorio(String valor) {
        return valor == null ? null : valor.trim();
    }
}