package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
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
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.AltaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.BajaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ConsultaBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in.ModificacionBeneficiarioUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;

/**
 * Controlador web Thymeleaf de Capa 4 para el modulo de beneficiarios: listado
 * por poliza con suma actual y porcentaje disponible hasta 100%, alta
 * vinculada, busqueda individual, modificacion y baja individual mediante
 * operaciones compuestas. Invoca unicamente puertos de entrada.
 */
@Controller
@RequestMapping("/web/beneficiarios")
public class BeneficiarioViewController {

    private static final int PORCENTAJE_MAXIMO = 100;

    private static final String VISTA_LISTA = "beneficiarios/lista";

    private static final String VISTA_DETALLE = "beneficiarios/detalle";

    private static final String VISTA_FORMULARIO = "beneficiarios/formulario";

    private static final String ATRIBUTO_ALERTA = "alerta";

    private static final String ATRIBUTO_FORMULARIO = "formulario";

    private static final String ATRIBUTO_MODO_EDICION = "modoEdicion";

    private static final String ATRIBUTO_PORCENTAJE_MAXIMO_EDICION = "porcentajeMaximoEdicion";

    private static final String ATRIBUTO_CLAVE_POLIZA = "clavePolizaBuscada";

    private static final String ATRIBUTO_BENEFICIARIOS = "beneficiarios";

    private static final String ATRIBUTO_POLIZA_CONSULTADA = "polizaConsultada";

    private static final String ATRIBUTO_SUMA_PORCENTAJE = "sumaPorcentaje";

    private static final String ATRIBUTO_PORCENTAJE_DISPONIBLE = "porcentajeDisponible";

    private static final String ATRIBUTO_CUPO_COMPLETO = "cupoCompleto";

    private final AltaBeneficiarioUseCase altaBeneficiarioUseCase;

    private final ConsultaBeneficiarioUseCase consultaBeneficiarioUseCase;

    private final ModificacionBeneficiarioUseCase modificacionBeneficiarioUseCase;

    private final BajaBeneficiarioUseCase bajaBeneficiarioUseCase;

    /**
     * Constructor compatible con pruebas que inyectan solo alta y consulta.
     *
     * @param altaBeneficiarioUseCase puerto de alta de beneficiario
     * @param consultaBeneficiarioUseCase puerto de consulta de beneficiarios
     */
    public BeneficiarioViewController(
            AltaBeneficiarioUseCase altaBeneficiarioUseCase,
            ConsultaBeneficiarioUseCase consultaBeneficiarioUseCase) {
        this(
                altaBeneficiarioUseCase,
                consultaBeneficiarioUseCase,
                Optional.empty(),
                Optional.empty());
    }

    /**
     * Crea el controlador de vistas de beneficiarios inyectando unicamente los
     * puertos de entrada del modulo.
     *
     * @param altaBeneficiarioUseCase puerto de alta de beneficiario
     * @param consultaBeneficiarioUseCase puerto de consulta de beneficiarios
     * @param modificacionBeneficiarioUseCase puerto de modificacion compuesta
     * @param bajaBeneficiarioUseCase puerto de baja compuesta
     */
    @Autowired
    public BeneficiarioViewController(
            AltaBeneficiarioUseCase altaBeneficiarioUseCase,
            ConsultaBeneficiarioUseCase consultaBeneficiarioUseCase,
            Optional<ModificacionBeneficiarioUseCase> modificacionBeneficiarioUseCase,
            Optional<BajaBeneficiarioUseCase> bajaBeneficiarioUseCase) {
        this.altaBeneficiarioUseCase = Objects.requireNonNull(
                altaBeneficiarioUseCase,
                "El puerto AltaBeneficiarioUseCase es obligatorio.");
        this.consultaBeneficiarioUseCase = Objects.requireNonNull(
                consultaBeneficiarioUseCase,
                "El puerto ConsultaBeneficiarioUseCase es obligatorio.");
        this.modificacionBeneficiarioUseCase = Objects.requireNonNull(
                modificacionBeneficiarioUseCase,
                "El Optional de ModificacionBeneficiarioUseCase no puede ser nulo.")
                .orElse(null);
        this.bajaBeneficiarioUseCase = Objects.requireNonNull(
                bajaBeneficiarioUseCase,
                "El Optional de BajaBeneficiarioUseCase no puede ser nulo.")
                .orElse(null);
    }

    /**
     * Muestra la vista de beneficiarios por poliza con tabla de beneficiarios,
     * suma actual de porcentajes y porcentaje disponible hasta 100%.
     *
     * @param clavePoliza clave UUID de la poliza a consultar (opcional)
     * @param model modelo para la vista Thymeleaf
     * @return plantilla del listado de beneficiarios por poliza
     */
    @GetMapping
    public String listarPorPoliza(
            @RequestParam(required = false) String clavePoliza,
            Model model) {
        inicializarEstadoLista(model, clavePoliza);
        if (clavePoliza == null) {
            return VISTA_LISTA;
        }
        String claveLimpia = clavePoliza.trim();
        if (claveLimpia.isEmpty()) {
            model.addAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.validacion(
                            "Ingrese una clave de poliza (UUID) para consultar beneficiarios."));
            return VISTA_LISTA;
        }
        try {
            UUID uuidPoliza = parsearUuid(claveLimpia);
            List<Beneficiario> beneficiarios
                    = consultaBeneficiarioUseCase.listarPorPoliza(uuidPoliza);
            cargarResumenPoliza(model, uuidPoliza.toString(), beneficiarios);
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
        }
        return VISTA_LISTA;
    }

    /**
     * Consulta un beneficiario individual por su llave compuesta (clave de
     * poliza, nombres, apellidos y fecha de nacimiento).
     *
     * @param clavePoliza clave UUID de la poliza
     * @param nombres nombres del beneficiario
     * @param primerApellido primer apellido del beneficiario
     * @param segundoApellido segundo apellido del beneficiario (opcional)
     * @param fechaNacimiento fecha de nacimiento en formato AAAA-MM-DD
     * @param model modelo para la vista
     * @return plantilla de consulta individual del beneficiario
     */
    @GetMapping("/consultar")
    public String consultarIndividual(
            @RequestParam(required = false) String clavePoliza,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String fechaNacimiento,
            Model model) {
        cargarParametrosBusqueda(
                model,
                clavePoliza,
                nombres,
                primerApellido,
                segundoApellido,
                fechaNacimiento);
        cargarLimitesFechaFormulario(model);
        if (sinParametrosBusqueda(clavePoliza, nombres, primerApellido, fechaNacimiento)) {
            return VISTA_DETALLE;
        }
        try {
            UUID uuidPoliza = parsearUuid(clavePoliza);
            LocalDate fecha = parsearFecha(fechaNacimiento);
            Beneficiario beneficiario = consultaBeneficiarioUseCase.consultarPorDatos(
                    uuidPoliza,
                    limpiarObligatorio(nombres),
                    limpiarObligatorio(primerApellido),
                    normalizarOpcional(segundoApellido),
                    fecha);
            model.addAttribute("beneficiario", beneficiario);
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
        }
        return VISTA_DETALLE;
    }

    /**
     * Muestra el formulario de alta vinculada de beneficiario. Si se recibe
     * una clave de poliza valida, consulta y muestra la suma actual y el
     * porcentaje disponible hasta 100%.
     *
     * @param clavePoliza clave UUID opcional de la poliza a vincular
     * @param model modelo para la vista
     * @return plantilla del formulario de alta
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(
            @RequestParam(required = false) String clavePoliza,
            Model model) {
        String claveNorm = normalizarOpcional(clavePoliza);
        BeneficiarioFormulario formulario = claveNorm != null
                ? BeneficiarioFormulario.paraPoliza(claveNorm)
                : new BeneficiarioFormulario();
        model.addAttribute(ATRIBUTO_FORMULARIO, formulario);
        model.addAttribute(ATRIBUTO_MODO_EDICION, false);
        cargarLimitesFechaFormulario(model);
        if (claveNorm != null) {
            cargarResumenOpcionalParaFormulario(claveNorm, model);
        }
        return VISTA_FORMULARIO;
    }

    /**
     * Procesa el alta de un nuevo beneficiario vinculado a una poliza.
     *
     * @param formulario datos capturados en el formulario HTML
     * @param model modelo para re-renderizar en caso de error
     * @param redirectAttributes atributos flash para la redireccion exitosa
     * @return redireccion al listado de la poliza o el formulario con alerta
     */
    @PostMapping("/nuevo")
    public String registrar(
            @ModelAttribute("formulario") BeneficiarioFormulario formulario,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Beneficiario nuevo = construirBeneficiario(formulario);
            Beneficiario creado = altaBeneficiarioUseCase.registrar(nuevo);
            redirectAttributes.addFlashAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.exito(
                            AlertaWeb.tituloOperacionExitosa(),
                            "Beneficiario registrado correctamente con "
                                    + creado.getPorcentaje() + "% asignado."));
            redirectAttributes.addAttribute("clavePoliza", creado.getClavePoliza().toString());
            return "redirect:/web/beneficiarios";
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_FORMULARIO, formulario);
            model.addAttribute(ATRIBUTO_MODO_EDICION, false);
            cargarLimitesFechaFormulario(model);
            String claveNorm = normalizarOpcional(formulario.getClavePoliza());
            if (claveNorm != null) {
                cargarResumenSilenciosoParaFormulario(claveNorm, model);
            }
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            return VISTA_FORMULARIO;
        }
    }

    /**
     * Muestra el formulario de edicion precargado con los datos del
     * beneficiario identificado por su llave compuesta.
     *
     * @param clavePoliza clave UUID de la poliza
     * @param nombres nombres actuales del beneficiario
     * @param primerApellido primer apellido actual del beneficiario
     * @param segundoApellido segundo apellido actual (opcional)
     * @param fechaNacimiento fecha de nacimiento actual (AAAA-MM-DD)
     * @param model modelo para la vista
     * @param redirectAttributes atributos flash si ocurre error al cargar
     * @return plantilla del formulario en modo edicion o redireccion al listado
     */
    @GetMapping("/editar")
    public String mostrarFormularioEdicion(
            @RequestParam String clavePoliza,
            @RequestParam String nombres,
            @RequestParam String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam String fechaNacimiento,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            UUID uuidPoliza = parsearUuid(clavePoliza);
            LocalDate fecha = parsearFecha(fechaNacimiento);
            Beneficiario existente = consultaBeneficiarioUseCase.consultarPorDatos(
                    uuidPoliza,
                    limpiarObligatorio(nombres),
                    limpiarObligatorio(primerApellido),
                    normalizarOpcional(segundoApellido),
                    fecha);
            model.addAttribute(ATRIBUTO_FORMULARIO, BeneficiarioFormulario.desde(existente));
            model.addAttribute(ATRIBUTO_MODO_EDICION, true);
            cargarLimitesFechaFormulario(model);
            cargarResumenParaEdicion(uuidPoliza.toString(), existente.getPorcentaje(), model);
            return VISTA_FORMULARIO;
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            if (clavePoliza != null && !clavePoliza.trim().isEmpty()) {
                redirectAttributes.addAttribute("clavePoliza", clavePoliza.trim());
            }
            return "redirect:/web/beneficiarios";
        }
    }

    /**
     * Procesa la modificacion de un beneficiario mediante operacion compuesta.
     *
     * @param formulario datos originales y modificados del formulario HTML
     * @param model modelo para re-renderizar en caso de error
     * @param redirectAttributes atributos flash para la redireccion exitosa
     * @return redireccion al listado de la poliza o el formulario con alerta
     */
    @PostMapping("/editar")
    public String actualizar(
            @ModelAttribute("formulario") BeneficiarioFormulario formulario,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            UUID clavePoliza = parsearUuid(formulario.getClavePoliza());
            LocalDate fechaOriginal = parsearFecha(formulario.getFechaNacimientoOriginal());
            Beneficiario actualizado = construirBeneficiario(formulario);
            Beneficiario guardado = modificacionBeneficiarioUseCase.actualizar(
                    clavePoliza,
                    limpiarObligatorio(formulario.getNombresOriginales()),
                    limpiarObligatorio(formulario.getPrimerApellidoOriginal()),
                    normalizarOpcional(formulario.getSegundoApellidoOriginal()),
                    fechaOriginal,
                    actualizado);
            redirectAttributes.addFlashAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.exito(
                            AlertaWeb.tituloOperacionExitosa(),
                            "Beneficiario actualizado correctamente con "
                                    + guardado.getPorcentaje() + "% asignado."));
            redirectAttributes.addAttribute("clavePoliza", guardado.getClavePoliza().toString());
            return "redirect:/web/beneficiarios";
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_FORMULARIO, formulario);
            model.addAttribute(ATRIBUTO_MODO_EDICION, true);
            cargarLimitesFechaFormulario(model);
            String claveNorm = normalizarOpcional(formulario.getClavePoliza());
            int porcentajeOriginal = parsearPorcentajeSeguro(formulario.getPorcentajeOriginal());
            if (claveNorm != null) {
                cargarResumenParaEdicion(claveNorm, porcentajeOriginal, model);
            }
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            return VISTA_FORMULARIO;
        }
    }

    /**
     * Procesa la baja individual de un beneficiario mediante operacion
     * compuesta conservando la poliza y al resto de beneficiarios.
     *
     * @param clavePoliza clave UUID de la poliza
     * @param nombres nombres del beneficiario a eliminar
     * @param primerApellido primer apellido del beneficiario
     * @param segundoApellido segundo apellido (opcional)
     * @param fechaNacimiento fecha de nacimiento (AAAA-MM-DD)
     * @param origen vista de origen opcional ({@code poliza} para regresar al detalle)
     * @param redirectAttributes atributos flash con resultado
     * @return redireccion al listado de beneficiarios o al detalle de la poliza
     */
    @PostMapping("/eliminar")
    public String eliminar(
            @RequestParam String clavePoliza,
            @RequestParam String nombres,
            @RequestParam String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam String fechaNacimiento,
            @RequestParam(required = false) String origen,
            RedirectAttributes redirectAttributes) {
        String claveLimpia = clavePoliza != null ? clavePoliza.trim() : "";
        try {
            UUID uuidPoliza = parsearUuid(claveLimpia);
            LocalDate fecha = parsearFecha(fechaNacimiento);
            bajaBeneficiarioUseCase.eliminar(
                    uuidPoliza,
                    limpiarObligatorio(nombres),
                    limpiarObligatorio(primerApellido),
                    normalizarOpcional(segundoApellido),
                    fecha);
            redirectAttributes.addFlashAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.exito(
                            AlertaWeb.tituloOperacionExitosa(),
                            "Beneficiario eliminado correctamente. "
                                    + "La poliza y los demas beneficiarios se conservaron."));
            if ("poliza".equals(origen)) {
                return "redirect:/web/polizas/" + uuidPoliza;
            }
            redirectAttributes.addAttribute("clavePoliza", uuidPoliza.toString());
            return "redirect:/web/beneficiarios";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            if ("poliza".equals(origen) && !claveLimpia.isEmpty()) {
                return "redirect:/web/polizas/" + claveLimpia;
            }
            if (!claveLimpia.isEmpty()) {
                redirectAttributes.addAttribute("clavePoliza", claveLimpia);
            }
            return "redirect:/web/beneficiarios";
        }
    }

    /**
     * Maneja errores de conversion de parametros para responder con vista HTML
     * y alerta 422 en lugar de JSON.
     *
     * @param ex excepcion de tipo de parametro
     * @param model modelo para la vista
     * @return plantilla de listado con alerta 422
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String manejarParametroInvalido(
            MethodArgumentTypeMismatchException ex, Model model) {
        inicializarEstadoLista(model, null);
        model.addAttribute(
                ATRIBUTO_ALERTA,
                AlertaWeb.validacion("Uno o mas parametros enviados no tienen formato valido."));
        return VISTA_LISTA;
    }

    private void inicializarEstadoLista(Model model, String clavePoliza) {
        model.addAttribute(ATRIBUTO_CLAVE_POLIZA, clavePoliza != null ? clavePoliza : "");
        model.addAttribute(ATRIBUTO_BENEFICIARIOS, List.of());
        model.addAttribute(ATRIBUTO_POLIZA_CONSULTADA, false);
        model.addAttribute(ATRIBUTO_SUMA_PORCENTAJE, 0);
        model.addAttribute(ATRIBUTO_PORCENTAJE_DISPONIBLE, PORCENTAJE_MAXIMO);
        model.addAttribute(ATRIBUTO_CUPO_COMPLETO, false);
    }

    private void cargarResumenPoliza(
            Model model, String clavePoliza, List<Beneficiario> beneficiarios) {
        int suma = calcularSumaPorcentajes(beneficiarios);
        int disponible = Math.max(0, PORCENTAJE_MAXIMO - suma);
        model.addAttribute(ATRIBUTO_CLAVE_POLIZA, clavePoliza);
        model.addAttribute(ATRIBUTO_BENEFICIARIOS, beneficiarios);
        model.addAttribute(ATRIBUTO_POLIZA_CONSULTADA, true);
        model.addAttribute(ATRIBUTO_SUMA_PORCENTAJE, suma);
        model.addAttribute(ATRIBUTO_PORCENTAJE_DISPONIBLE, disponible);
        model.addAttribute(ATRIBUTO_CUPO_COMPLETO, suma >= PORCENTAJE_MAXIMO);
    }

    private void cargarResumenOpcionalParaFormulario(String clavePoliza, Model model) {
        try {
            UUID uuidPoliza = parsearUuid(clavePoliza);
            List<Beneficiario> actuales
                    = consultaBeneficiarioUseCase.listarPorPoliza(uuidPoliza);
            cargarResumenPoliza(model, uuidPoliza.toString(), actuales);
            int suma = calcularSumaPorcentajes(actuales);
            if (suma >= PORCENTAJE_MAXIMO) {
                model.addAttribute(
                        ATRIBUTO_ALERTA,
                        AlertaWeb.avisoCupoCompletoBeneficiarios(
                                uuidPoliza.toString(), suma));
            }
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_POLIZA_CONSULTADA, false);
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
        }
    }

    private void cargarResumenSilenciosoParaFormulario(String clavePoliza, Model model) {
        try {
            UUID uuidPoliza = parsearUuid(clavePoliza);
            List<Beneficiario> actuales
                    = consultaBeneficiarioUseCase.listarPorPoliza(uuidPoliza);
            cargarResumenPoliza(model, uuidPoliza.toString(), actuales);
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_POLIZA_CONSULTADA, false);
        }
    }

    private void cargarResumenParaEdicion(
            String clavePoliza, int porcentajeOriginal, Model model) {
        try {
            UUID uuidPoliza = parsearUuid(clavePoliza);
            List<Beneficiario> actuales
                    = consultaBeneficiarioUseCase.listarPorPoliza(uuidPoliza);
            cargarResumenPoliza(model, uuidPoliza.toString(), actuales);
            int suma = calcularSumaPorcentajes(actuales);
            int sumaOtros = Math.max(0, suma - porcentajeOriginal);
            int maximoEdicion = Math.max(1, Math.min(PORCENTAJE_MAXIMO,
                    PORCENTAJE_MAXIMO - sumaOtros));
            model.addAttribute(ATRIBUTO_PORCENTAJE_MAXIMO_EDICION, maximoEdicion);
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_POLIZA_CONSULTADA, false);
            model.addAttribute(ATRIBUTO_PORCENTAJE_MAXIMO_EDICION, PORCENTAJE_MAXIMO);
        }
    }

    private int parsearPorcentajeSeguro(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void cargarLimitesFechaFormulario(Model model) {
        model.addAttribute(
                "fechaMinimaBeneficiario", Beneficiario.fechaMinimaRegistro().toString());
        model.addAttribute(
                "fechaMaximaBeneficiario", Beneficiario.fechaMaximaRegistro().toString());
    }

    private int calcularSumaPorcentajes(List<Beneficiario> beneficiarios) {
        return beneficiarios.stream()
                .mapToInt(Beneficiario::getPorcentaje)
                .sum();
    }

    private Beneficiario construirBeneficiario(BeneficiarioFormulario formulario) {
        UUID clavePoliza = parsearUuid(formulario.getClavePoliza());
        LocalDate fechaNacimiento = parsearFecha(formulario.getFechaNacimiento());
        Beneficiario.validarFechaNacimientoParaRegistro(fechaNacimiento);
        int porcentaje = parsearPorcentaje(formulario.getPorcentaje());
        String nombres = limpiarObligatorio(formulario.getNombres());
        String primerApellido = limpiarObligatorio(formulario.getPrimerApellido());
        String segundoApellido = normalizarOpcional(formulario.getSegundoApellido());
        return new Beneficiario(
                nombres,
                primerApellido,
                segundoApellido,
                fechaNacimiento,
                clavePoliza,
                porcentaje);
    }

    private UUID parsearUuid(String uuidTexto) {
        if (uuidTexto == null || uuidTexto.trim().isEmpty()) {
            throw new ReglaNegocioException("La clave de la poliza (UUID) es obligatoria.");
        }
        try {
            return UUID.fromString(uuidTexto.trim());
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException(
                    "La clave de la poliza no tiene un formato UUID valido.");
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

    private int parsearPorcentaje(String porcentajeTexto) {
        if (porcentajeTexto == null || porcentajeTexto.trim().isEmpty()) {
            throw new ReglaNegocioException("El porcentaje es obligatorio (entero entre 1 y 100).");
        }
        try {
            return Integer.parseInt(porcentajeTexto.trim());
        } catch (NumberFormatException ex) {
            throw new ReglaNegocioException(
                    "El porcentaje debe ser un numero entero entre 1 y 100.");
        }
    }

    private boolean sinParametrosBusqueda(
            String clavePoliza,
            String nombres,
            String primerApellido,
            String fechaNacimiento) {
        return clavePoliza == null
                && nombres == null
                && primerApellido == null
                && fechaNacimiento == null;
    }

    private void cargarParametrosBusqueda(
            Model model,
            String clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellido,
            String fechaNacimiento) {
        model.addAttribute("busquedaClavePoliza", valorSeguro(clavePoliza));
        model.addAttribute("busquedaNombres", valorSeguro(nombres));
        model.addAttribute("busquedaPrimerApellido", valorSeguro(primerApellido));
        model.addAttribute("busquedaSegundoApellido", valorSeguro(segundoApellido));
        model.addAttribute("busquedaFechaNacimiento", valorSeguro(fechaNacimiento));
    }

    private String valorSeguro(String valor) {
        return valor != null ? valor : "";
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
