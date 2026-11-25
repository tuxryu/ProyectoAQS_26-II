package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CancelarPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultarCatalogoClientesUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.CrearPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ModificacionPolizaUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;

/**
 * Controlador web Thymeleaf de Capa 4 para las operaciones de escritura del
 * modulo de polizas: alta, modificacion y cancelacion. Invoca unicamente
 * puertos de entrada del modulo de polizas y el puerto de salida
 * {@link consultarCatalogoClientesUseCase} para poblar el selector de CURP.
 */
@Controller
@RequestMapping("/web/polizas")
public class PolizaEscrituraViewController {

    private final ConsultarCatalogoClientesUseCase consultarCatalogoClientesUseCase;

    private static final String VISTA_FORMULARIO = "polizas/formulario";

    private static final String VISTA_BUSCAR_EDITAR = "polizas/buscar-editar";

    private static final String ATRIBUTO_ALERTA = "alerta";

    private static final String ATRIBUTO_FORMULARIO = "formulario";

    private static final String ATRIBUTO_MODO_EDICION = "modoEdicion";

    private static final String ATRIBUTO_CLIENTES = "clientes";

    private final CrearPolizaUseCasePort crearPolizaUseCase;

    private final ModificacionPolizaUseCasePort modificacionPolizaUseCase;

    private final CancelarPolizaUseCasePort cancelarPolizaUseCase;

    private final ConsultaPolizaUseCase consultaPolizaUseCase;

     /**
     * Crea el controlador de escritura de polizas inyectando los puertos de
     * entrada del modulo y el puerto de salida del catalogo de clientes.
     *
     * @param crearPolizaUseCase puerto de alta de poliza
     * @param modificacionPolizaUseCase puerto de modificacion de poliza
     * @param cancelarPolizaUseCase puerto de cancelacion de poliza
     * @param consultaPolizaUseCase puerto de consulta (para precargar edicion).

     */
    public PolizaEscrituraViewController(
            CrearPolizaUseCasePort crearPolizaUseCase,
            ModificacionPolizaUseCasePort modificacionPolizaUseCase,
            CancelarPolizaUseCasePort cancelarPolizaUseCase,
            ConsultaPolizaUseCase consultaPolizaUseCase,
            ConsultarCatalogoClientesUseCase consultarCatalogoClientesUseCase) {
        this.crearPolizaUseCase = Objects.requireNonNull(
                crearPolizaUseCase, "El puerto CrearPolizaUseCasePort es obligatorio.");
        this.modificacionPolizaUseCase = Objects.requireNonNull(
                modificacionPolizaUseCase,
                "El puerto ModificacionPolizaUseCasePort es obligatorio.");
        this.cancelarPolizaUseCase = Objects.requireNonNull(
                cancelarPolizaUseCase, "El puerto CancelarPolizaUseCasePort es obligatorio.");
        this.consultaPolizaUseCase = Objects.requireNonNull(
                consultaPolizaUseCase, "El puerto ConsultaPolizaUseCase es obligatorio.");
        this.consultarCatalogoClientesUseCase = Objects.requireNonNull(
                consultarCatalogoClientesUseCase,
                "El puerto ConsultarCatalogoClientesUseCase es obligatorio.");
    }

    /**
     * Muestra el formulario vacio para dar de alta una nueva poliza.
     *
     * @param model modelo para la vista
     * @return plantilla del formulario de poliza
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        model.addAttribute(ATRIBUTO_FORMULARIO, new PolizaFormulario());
        model.addAttribute(ATRIBUTO_MODO_EDICION, false);
        cargarClientesParaSelector(model);
        return VISTA_FORMULARIO;
    }

    /**
     * Procesa el alta de una nueva poliza invocando el puerto de creacion.
     *
     * @param formulario datos capturados en el formulario HTML
     * @param model modelo para re-renderizar en caso de error
     * @param redirectAttributes atributos flash para la redireccion exitosa
     * @return redireccion al detalle de la poliza creada o el formulario con alerta
     */
    @PostMapping("/nuevo")
    public String registrar(
            @ModelAttribute("formulario") PolizaFormulario formulario,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Poliza nueva = construirPoliza(null, formulario);
            Poliza creada = crearPolizaUseCase.crearPoliza(nueva);
            redirectAttributes.addFlashAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.exito(
                            AlertaWeb.tituloOperacionExitosa(),
                            AlertaWeb.mensajeAltaPolizaExitosa(creada.getClave())));
            redirectAttributes.addAttribute("clave", creada.getClave());
            return "redirect:/web/polizas/consultar";
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_FORMULARIO, formulario);
            model.addAttribute(ATRIBUTO_MODO_EDICION, false);
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            cargarClientesParaSelector(model);
            return VISTA_FORMULARIO;
        }
    }

    /**
     * Punto de entrada desde el menu principal para modificar una poliza. Si se
     * proporciona la clave, carga directamente el formulario de edicion; en
     * caso contrario muestra el buscador por clave.
     *
     * @param clave clave UUID opcional de la poliza a modificar
     * @param model modelo para la vista
     * @return plantilla de busqueda para edicion o formulario precargado
     */
    @GetMapping("/editar")
    public String buscarParaEditar(
            @RequestParam(required = false) String clave,
            Model model) {
        model.addAttribute("claveBuscada", clave != null ? clave : "");
        if (clave == null || clave.trim().isEmpty()) {
            return VISTA_BUSCAR_EDITAR;
        }
        return cargarFormularioEdicion(clave.trim(), model);
    }

    /**
     * Muestra el formulario de modificacion precargado con los datos de la
     * poliza identificada por su clave UUID.
     *
     * @param clave clave UUID de la poliza a modificar
     * @param model modelo para la vista
     * @return plantilla de formulario en modo edicion o vista de busqueda con alerta
     */
    @GetMapping("/{clave}/editar")
    public String mostrarFormularioEdicion(
            @PathVariable String clave, Model model) {
        model.addAttribute("claveBuscada", clave);
        return cargarFormularioEdicion(clave, model);
    }

    /**
     * Procesa la modificacion de una poliza existente invocando el puerto de
     * modificacion.
     *
     * @param clave clave UUID de la poliza en la ruta (no editable)
     * @param formulario datos modificados
     * @param model modelo para re-renderizar en caso de error
     * @param redirectAttributes atributos flash para la redireccion exitosa
     * @return redireccion al detalle actualizado o el formulario con alerta
     */
    @PostMapping("/{clave}/editar")
    public String actualizar(
            @PathVariable String clave,
            @ModelAttribute("formulario") PolizaFormulario formulario,
            Model model,
            RedirectAttributes redirectAttributes) {
        formulario.setClave(clave);
        try {
            UUID uuid = parsearClave(clave);
            Poliza actualizada = construirPoliza(uuid, formulario);
            Poliza guardada = modificacionPolizaUseCase.modificarPoliza(uuid, actualizada);
            redirectAttributes.addFlashAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.exito(
                            AlertaWeb.tituloOperacionExitosa(),
                            AlertaWeb.mensajeActualizacionPolizaExitosa(guardada.getClave())));
            redirectAttributes.addAttribute("clave", guardada.getClave());
            return "redirect:/web/polizas/consultar";
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_FORMULARIO, formulario);
            model.addAttribute(ATRIBUTO_MODO_EDICION, true);
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            cargarClientesParaSelector(model);
            return VISTA_FORMULARIO;
        }
    }

    /**
     * Cancela (elimina) una poliza existente. La confirmacion de cascada se
     * gestiona en la plantilla mediante un dialogo JavaScript que advierte
     * sobre los beneficiarios asociados.
     *
     * @param clave clave UUID de la poliza a cancelar
     * @param redirectAttributes atributos flash para la redireccion
     * @return redireccion al listado con alerta de exito o error
     */
    @PostMapping("/{clave}/cancelar")
    public String cancelar(
            @PathVariable String clave,
            RedirectAttributes redirectAttributes) {
        try {
            UUID uuid = parsearClave(clave);
            cancelarPolizaUseCase.cancelarPoliza(uuid);
            redirectAttributes.addFlashAttribute(
                    "alerta",
                    AlertaWeb.exito(
                            AlertaWeb.tituloOperacionExitosa(),
                            AlertaWeb.mensajeCancelacionPolizaExitosa(clave)));
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "alerta", AlertaWeb.desdeExcepcion(ex));
        }
        // Lo mandamos al buscador de detalle para que vea que ya no existe
        return "redirect:/web/polizas/consultar";
    }

    private void cargarClientesParaSelector(Model model) {
        try {
            // Se llama al PUERTO DE ENTRADA
            List<?> clientes = consultarCatalogoClientesUseCase.obtenerCatalogo();
            model.addAttribute("clientes", clientes);
        } catch (RuntimeException ex) {
            model.addAttribute("clientes", List.of());
            model.addAttribute("alerta", AlertaWeb.desdeExcepcion(ex));
        }
    }

    private String cargarFormularioEdicion(String clave, Model model) {
        try {
            UUID uuid = parsearClave(clave);
            Poliza poliza = consultaPolizaUseCase.consultarPorClave(uuid);
            model.addAttribute(ATRIBUTO_FORMULARIO, PolizaFormulario.desde(poliza));
            model.addAttribute(ATRIBUTO_MODO_EDICION, true);
            cargarClientesParaSelector(model);
            return VISTA_FORMULARIO;
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            return VISTA_BUSCAR_EDITAR;
        }
    }

    private Poliza construirPoliza(UUID clave, PolizaFormulario formulario) {
        byte tipo = parsearTipo(formulario.getTipo());
        float monto = parsearMonto(formulario.getMonto());
        String descripcion = formulario.getDescripcion() != null
                ? formulario.getDescripcion().trim() : null;
        String curp = formulario.getCurpCliente() != null
                ? formulario.getCurpCliente().trim() : null;
        return new Poliza(clave, tipo, monto, descripcion, curp);
    }

    private byte parsearTipo(String tipoTexto) {
        if (tipoTexto == null || tipoTexto.trim().isEmpty()) {
            throw new ReglaNegocioException("El tipo de poliza es obligatorio.");
        }
        try {
            int valor = Integer.parseInt(tipoTexto.trim());
            if (valor < 0 || valor > 2) {
                throw new ReglaNegocioException(
                        "El tipo de poliza debe estar entre 0 (Auto), 1 (Vida) y 2 (Medico).");
            }
            return (byte) valor;
        } catch (NumberFormatException ex) {
            throw new ReglaNegocioException(
                    "El tipo de poliza debe ser un numero entero entre 0 y 2.");
        }
    }

    private float parsearMonto(String montoTexto) {
        if (montoTexto == null || montoTexto.trim().isEmpty()) {
            throw new ReglaNegocioException("El monto de la poliza es obligatorio.");
        }
        try {
            float valor = Float.parseFloat(montoTexto.trim());
            if (Float.isNaN(valor) || Float.isInfinite(valor) || valor <= 0.0f) {
                throw new ReglaNegocioException(
                        "El monto debe ser un numero mayor que cero.");
            }
            return valor;
        } catch (NumberFormatException ex) {
            throw new ReglaNegocioException(
                    "El monto debe ser un numero valido mayor que cero.");
        }
    }

    private UUID parsearClave(String clave) {
        try {
            return UUID.fromString(clave);
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException(
                    "La clave de la poliza debe ser un UUID valido.");
        }
    }
}
