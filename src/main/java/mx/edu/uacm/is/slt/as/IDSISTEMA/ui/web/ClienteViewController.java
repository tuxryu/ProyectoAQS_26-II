package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ActualizarClienteUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.ConsultaClienteUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.FiltroCliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.application.port.in.RegistrarClienteUseCasePort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model.Cliente;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.ReglaNegocioException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Controlador web Thymeleaf de Capa 4 para el modulo de clientes: listado
 * paginado, consulta individual por CURP, alta y modificacion. Invoca
 * unicamente puertos de entrada ({@code *ClienteUseCase*}).
 */
@Controller
@RequestMapping("/web/clientes")
public class ClienteViewController {

    private static final int LONGITUD_CURP = 18;

    private static final int TAMANO_POR_DEFECTO = 10;

    private static final String VISTA_LISTA = "clientes/lista";

    private static final String VISTA_DETALLE = "clientes/detalle";

    private static final String VISTA_FORMULARIO = "clientes/formulario";

    private static final String VISTA_BUSCAR_EDITAR = "clientes/buscar-editar";

    private static final String ATRIBUTO_ALERTA = "alerta";

    private static final String ATRIBUTO_FORMULARIO = "formulario";

    private static final String ATRIBUTO_MODO_EDICION = "modoEdicion";

    private final ConsultaClienteUseCase consultaClienteUseCase;

    private final RegistrarClienteUseCasePort registrarClienteUseCasePort;

    private final ActualizarClienteUseCasePort actualizarClienteUseCasePort;

    /**
     * Crea el controlador de vistas de clientes inyectando unicamente los
     * puertos de entrada del modulo.
     *
     * @param consultaClienteUseCase puerto de consultas y listado paginado
     * @param registrarClienteUseCasePort puerto de alta de cliente
     * @param actualizarClienteUseCasePort puerto de modificacion de cliente
     */
    public ClienteViewController(
            ConsultaClienteUseCase consultaClienteUseCase,
            RegistrarClienteUseCasePort registrarClienteUseCasePort,
            ActualizarClienteUseCasePort actualizarClienteUseCasePort) {
        this.consultaClienteUseCase = Objects.requireNonNull(
                consultaClienteUseCase,
                "El puerto ConsultaClienteUseCase es obligatorio.");
        this.registrarClienteUseCasePort = Objects.requireNonNull(
                registrarClienteUseCasePort,
                "El puerto RegistrarClienteUseCasePort es obligatorio.");
        this.actualizarClienteUseCasePort = Objects.requireNonNull(
                actualizarClienteUseCasePort,
                "El puerto ActualizarClienteUseCasePort es obligatorio.");
    }

    /**
     * Muestra el listado paginado de clientes (consumiendo GET /clientes a
     * traves del caso de uso) con soporte opcional de filtro por nombre y
     * acceso directo a la busqueda por CURP.
     *
     * @param nombres filtro opcional de nombres
     * @param primerApellido filtro opcional de primer apellido
     * @param segundoApellido filtro opcional de segundo apellido
     * @param pagina numero de pagina (base 0)
     * @param tamano cantidad de registros por pagina
     * @param model modelo para la vista Thymeleaf
     * @return plantilla del listado de clientes
     */
    @GetMapping
    public String listar(
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            Model model) {
        String nombresNorm = normalizarOpcional(nombres);
        String primerApNorm = normalizarOpcional(primerApellido);
        String segundoApNorm = normalizarOpcional(segundoApellido);

        cargarFiltrosLista(model, nombresNorm, primerApNorm, segundoApNorm, pagina, tamano);
        try {
            FiltroCliente filtro = FiltroCliente.desde(
                    nombresNorm, primerApNorm, segundoApNorm);
            Pagina<Cliente> resultado = consultaClienteUseCase.consultar(
                    filtro, pagina, tamano);
            model.addAttribute("paginaClientes", resultado);
        } catch (RuntimeException ex) {
            model.addAttribute("paginaClientes", paginaVacia(tamano));
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
        }
        return VISTA_LISTA;
    }

    /**
     * Consulta un cliente individual por su CURP respetando mayusculas y
     * minusculas exactas.
     *
     * @param curp CURP a consultar
     * @param model modelo para la vista
     * @return plantilla de detalle del cliente
     */
    @GetMapping("/consultar")
    public String consultarPorCurp(
            @RequestParam(required = false) String curp,
            Model model) {
        model.addAttribute("curpBuscada", curp != null ? curp : "");
        if (curp == null) {
            return VISTA_DETALLE;
        }
        String curpLimpia = curp.trim();
        if (curpLimpia.isEmpty()) {
            model.addAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.validacion(AlertaWeb.mensajeCurpObligatoria()));
            return VISTA_DETALLE;
        }
        try {
            validarCurpConsulta(curpLimpia);
            Cliente cliente = consultaClienteUseCase.consultarPorCurp(curpLimpia);
            model.addAttribute("cliente", cliente);
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
        }
        return VISTA_DETALLE;
    }

    /**
     * Muestra el formulario vacio para dar de alta un nuevo cliente.
     *
     * @param model modelo para la vista
     * @return plantilla del formulario de cliente
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioAlta(Model model) {
        model.addAttribute(ATRIBUTO_FORMULARIO, new ClienteFormulario());
        model.addAttribute(ATRIBUTO_MODO_EDICION, false);
        return VISTA_FORMULARIO;
    }

    /**
     * Procesa el alta de un nuevo cliente invocando el puerto de registro.
     *
     * @param formulario datos capturados en el formulario HTML
     * @param model modelo para re-renderizar en caso de error
     * @param redirectAttributes atributos flash para la redireccion exitosa
     * @return redireccion al detalle del cliente creado o el formulario con alerta
     */
    @PostMapping("/nuevo")
    public String registrar(
            @ModelAttribute("formulario") ClienteFormulario formulario,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Cliente nuevo = construirClienteParaAlta(formulario);
            Cliente creado = registrarClienteUseCasePort.registrarCliente(nuevo);
            redirectAttributes.addFlashAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.exito(
                            AlertaWeb.tituloOperacionExitosa(),
                            AlertaWeb.mensajeAltaExitosa(creado.getCurp())));
            redirectAttributes.addAttribute("curp", creado.getCurp());
            return "redirect:/web/clientes/consultar";
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_FORMULARIO, formulario);
            model.addAttribute(ATRIBUTO_MODO_EDICION, false);
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            return VISTA_FORMULARIO;
        }
    }

    /**
     * Punto de entrada desde el menu principal para modificar un cliente. Si se
     * proporciona la CURP, carga directamente el formulario de edicion; en caso
     * contrario muestra el buscador por CURP.
     *
     * @param curp CURP opcional del cliente a modificar
     * @param model modelo para la vista
     * @return plantilla de busqueda para edicion o formulario precargado
     */
    @GetMapping("/editar")
    public String buscarParaEditar(
            @RequestParam(required = false) String curp,
            Model model) {
        model.addAttribute("curpBuscada", curp != null ? curp : "");
        if (curp == null) {
            return VISTA_BUSCAR_EDITAR;
        }
        String curpLimpia = curp.trim();
        if (curpLimpia.isEmpty()) {
            model.addAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.validacion(AlertaWeb.mensajeCurpObligatoria()));
            return VISTA_BUSCAR_EDITAR;
        }
        return cargarFormularioEdicion(curpLimpia, model);
    }

    /**
     * Muestra el formulario de modificacion precargado con los datos del
     * cliente identificado por su CURP.
     *
     * @param curp CURP exacta del cliente a modificar
     * @param model modelo para la vista
     * @return plantilla de formulario en modo edicion o vista de busqueda con alerta
     */
    @GetMapping("/{curp}/editar")
    public String mostrarFormularioEdicion(@PathVariable String curp, Model model) {
        model.addAttribute("curpBuscada", curp);
        return cargarFormularioEdicion(curp, model);
    }

    /**
     * Procesa la modificacion de un cliente existente invocando el puerto de
     * actualizacion.
     *
     * @param curp CURP del cliente en la ruta (no editable)
     * @param formulario datos modificados
     * @param model modelo para re-renderizar en caso de error
     * @param redirectAttributes atributos flash para la redireccion exitosa
     * @return redireccion al detalle actualizado o el formulario con alerta
     */
    @PostMapping("/{curp}/editar")
    public String actualizar(
            @PathVariable String curp,
            @ModelAttribute("formulario") ClienteFormulario formulario,
            Model model,
            RedirectAttributes redirectAttributes) {
        formulario.setCurp(curp);
        try {
            Cliente actualizado = construirCliente(curp, formulario);
            Cliente guardado = actualizarClienteUseCasePort.actualizarCliente(actualizado);
            redirectAttributes.addFlashAttribute(
                    ATRIBUTO_ALERTA,
                    AlertaWeb.exito(
                            AlertaWeb.tituloOperacionExitosa(),
                            AlertaWeb.mensajeActualizacionExitosa(guardado.getCurp())));
            redirectAttributes.addAttribute("curp", guardado.getCurp());
            return "redirect:/web/clientes/consultar";
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_FORMULARIO, formulario);
            model.addAttribute(ATRIBUTO_MODO_EDICION, true);
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            return VISTA_FORMULARIO;
        }
    }

    /**
     * Maneja errores de tipo en parametros de consulta (por ejemplo pagina no
     * numerica) para mostrar una vista HTML amigable en lugar de JSON.
     *
     * @param ex excepcion de conversion de parametro
     * @param model modelo para la vista
     * @return plantilla de listado con alerta 422
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String manejarParametroInvalido(
            MethodArgumentTypeMismatchException ex, Model model) {
        cargarFiltrosLista(model, null, null, null, 0, TAMANO_POR_DEFECTO);
        model.addAttribute("paginaClientes", paginaVacia(TAMANO_POR_DEFECTO));
        model.addAttribute(
                ATRIBUTO_ALERTA,
                AlertaWeb.validacion(
                        "Los parametros de paginacion deben ser numeros enteros validos."));
        return VISTA_LISTA;
    }

    private String cargarFormularioEdicion(String curp, Model model) {
        try {
            validarCurpConsulta(curp);
            Cliente cliente = consultaClienteUseCase.consultarPorCurp(curp);
            model.addAttribute(ATRIBUTO_FORMULARIO, ClienteFormulario.desde(cliente));
            model.addAttribute(ATRIBUTO_MODO_EDICION, true);
            return VISTA_FORMULARIO;
        } catch (RuntimeException ex) {
            model.addAttribute(ATRIBUTO_ALERTA, AlertaWeb.desdeExcepcion(ex));
            return VISTA_BUSCAR_EDITAR;
        }
    }

    private Cliente construirClienteParaAlta(ClienteFormulario formulario) {
        String curp = formulario.getCurp() != null ? formulario.getCurp().trim() : "";
        if (!curp.isEmpty() && !curp.equals(curp.toUpperCase(Locale.ROOT))) {
            throw new ReglaNegocioException(AlertaWeb.mensajeCurpMayusculas());
        }
        return construirCliente(curp, formulario);
    }

    private Cliente construirCliente(String curp, ClienteFormulario formulario) {
        LocalDate fecha = parsearFecha(formulario.getFechaNacimiento());
        String nombres = limpiarObligatorio(formulario.getNombres());
        String primerApellido = limpiarObligatorio(formulario.getPrimerApellido());
        String segundoApellido = normalizarOpcional(formulario.getSegundoApellido());
        String direccion = limpiarObligatorio(formulario.getDireccion());
        return new Cliente(
                curp,
                nombres,
                primerApellido,
                segundoApellido,
                direccion,
                fecha);
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

    private void validarCurpConsulta(String curp) {
        if (curp.contains("/") || curp.contains("\\")) {
            throw new ReglaNegocioException(AlertaWeb.mensajeCurpSinDiagonales());
        }
        if (curp.length() > LONGITUD_CURP) {
            throw new ReglaNegocioException(AlertaWeb.mensajeCurpLongitud());
        }
    }

    private void cargarFiltrosLista(
            Model model,
            String nombres,
            String primerApellido,
            String segundoApellido,
            int pagina,
            int tamano) {
        model.addAttribute("filtroNombres", nombres != null ? nombres : "");
        model.addAttribute("filtroPrimerApellido", primerApellido != null ? primerApellido : "");
        model.addAttribute(
                "filtroSegundoApellido", segundoApellido != null ? segundoApellido : "");
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("tamanoActual", tamano);
        boolean hayFiltroNombre = nombres != null
                || primerApellido != null
                || segundoApellido != null;
        model.addAttribute("hayFiltroNombre", hayFiltroNombre);
    }

    private Pagina<Cliente> paginaVacia(int tamano) {
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
