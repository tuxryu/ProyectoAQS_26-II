package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.out.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.adapter.out.rest.dto.PolizaRemotaDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.PolizaRemotaPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;

/**
 * Adaptador de salida REST hacia el Sistema Dueno para las consultas GET de
 * polizas (lectura por clave y listados con filtro). Implementa
 * {@link PolizaRemotaPort}: cada fila JSON se valida como {@link PolizaRemotaDto}
 * y despues se convierte a la entidad de dominio {@link Poliza}.
 *
 * <p>Reglas aplicadas (hallazgos del Sistema Dueno):
 * <ul>
 *   <li>CURP y nombres se envian tal cual, sin normalizar mayusculas.</li>
 *   <li>Los segmentos de ruta se codifican; se rechazan / y \ antes de llamar.</li>
 *   <li>Lectura individual con 200 vacio: Optional.empty().</li>
 *   <li>Listas vacias []: lista vacia, no es error.</li>
 *   <li>Filas invalidas de otros equipos se omiten y se registran en log.</li>
 *   <li>HTTP 500: rechazo (502); 502/503/504, timeout o red: no disponible (503).</li>
 * </ul>
 */
@Component
public class PolizaSistemaDuenoAdapter implements PolizaRemotaPort {

    private static final Logger LOG = LoggerFactory.getLogger(PolizaSistemaDuenoAdapter.class);

    private static final int TIPO_MINIMO = 0;

    private static final int TIPO_MAXIMO = 2;

    private final RestClient restClient;

    public PolizaSistemaDuenoAdapter(RestClient restClient) {
        this.restClient = Objects.requireNonNull(
                restClient, "El cliente HTTP RestClient es obligatorio.");
    }

    /**
     * Consulta una poliza por su clave (GET /poliza/{clave}).
     *
     * @param clave clave UUID de la poliza
     * @return Optional con la poliza o Optional.empty() si el servidor responde 200 vacio
     * @throws SistemaDuenoRechazoException si la poliza devuelta tiene datos inconsistentes
     */
    @Override
    public Optional<Poliza> buscarPorClave(UUID clave) {
        Objects.requireNonNull(clave, "La clave de la poliza es obligatoria.");
        Map<?, ?> fila = ejecutar(() -> restClient.get()
                .uri("/poliza/{clave}", clave)
                .retrieve()
                .body(Map.class));
        if (fila == null || fila.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(convertirADominio(convertirFila(fila)));
        } catch (IllegalArgumentException ex) {
            throw new SistemaDuenoRechazoException(
                    "El Sistema Dueno devolvio una poliza inconsistente: " + ex.getMessage(), ex);
        }
    }

    /**
     * Lista todas las polizas registradas en el Sistema Dueno (GET /polizas).
     *
     * @return polizas validas; lista vacia si no hay resultados
     */
    @Override
    public List<Poliza> listarTodas() {
        return consultarLista("/polizas");
    }

    /**
     * Lista las polizas de un cliente por su CURP (GET /polizas/{curp}). La CURP
     * se envia tal cual porque el servidor distingue mayusculas y minusculas.
     *
     * @param curp CURP del cliente titular
     * @return polizas validas del cliente; lista vacia si no hay resultados
     */
    @Override
    public List<Poliza> buscarPorCurp(String curp) {
        validarSegmentoObligatorio(curp, "La CURP");
        return consultarLista("/polizas/{curp}", curp);
    }

    /**
     * Lista las polizas de un tipo (GET /polizas/{tipo}).
     *
     * @param tipo tipo de poliza (0 = Auto, 1 = Vida, 2 = Medico)
     * @return polizas validas del tipo indicado; lista vacia si no hay resultados
     */
    @Override
    public List<Poliza> buscarPorTipo(int tipo) {
        if (tipo < TIPO_MINIMO || tipo > TIPO_MAXIMO) {
            throw new IllegalArgumentException(
                    "El tipo de poliza debe estar entre 0 y 2, se recibio " + tipo + ".");
        }
        return consultarLista("/polizas/{tipo}", tipo);
    }

    /**
     * Lista las polizas cuyo titular coincide con el nombre indicado
     * (GET /polizas/c/{nombres}/{primerApellido}[/{segundoApellido}]).
     *
     * @param nombres nombres del cliente
     * @param primerApellido primer apellido del cliente
     * @param segundoApellido segundo apellido del cliente (opcional, puede ser null)
     * @return polizas validas; lista vacia si no hay resultados
     */
    @Override
    public List<Poliza> buscarPorCliente(
            String nombres, String primerApellido, String segundoApellido) {
        return buscarPorPersona("c", nombres, primerApellido, segundoApellido);
    }

    /**
     * Lista las polizas que tienen un beneficiario con el nombre indicado
     * (GET /polizas/b/{nombres}/{primerApellido}[/{segundoApellido}]).
     *
     * @param nombres nombres del beneficiario
     * @param primerApellido primer apellido del beneficiario
     * @param segundoApellido segundo apellido del beneficiario (opcional, puede ser null)
     * @return polizas validas; lista vacia si no hay resultados
     */
    @Override
    public List<Poliza> buscarPorBeneficiario(
            String nombres, String primerApellido, String segundoApellido) {
        return buscarPorPersona("b", nombres, primerApellido, segundoApellido);
    }

    private List<Poliza> buscarPorPersona(
            String prefijo, String nombres, String primerApellido, String segundoApellido) {
        validarSegmentoObligatorio(nombres, "El nombre");
        validarSegmentoObligatorio(primerApellido, "El primer apellido");
        String ruta = "/polizas/" + prefijo + "/{nombres}/{primerApellido}";
        if (segundoApellido == null || segundoApellido.trim().isEmpty()) {
            return consultarLista(ruta, nombres, primerApellido);
        }
        validarSinDiagonales(segundoApellido, "El segundo apellido");
        return consultarLista(
                ruta + "/{segundoApellido}", nombres, primerApellido, segundoApellido);
    }

    private List<Poliza> consultarLista(String plantilla, Object... variables) {
        List<?> filas = ejecutar(() -> restClient.get()
                .uri(plantilla, variables)
                .retrieve()
                .body(List.class));
        if (filas == null) {
            return List.of();
        }
        List<Poliza> polizas = new ArrayList<>(filas.size());
        for (Object elemento : filas) {
            try {
                polizas.add(convertirADominio(convertirFila(elemento)));
            } catch (IllegalArgumentException ex) {
                LOG.warn("Se omite una poliza invalida del Sistema Dueno ({}): {}",
                        ex.getMessage(), elemento);
            }
        }
        return List.copyOf(polizas);
    }

    private Poliza convertirADominio(PolizaRemotaDto dto) {
        return new Poliza(
                dto.clave(),
                (byte) dto.tipo(),
                dto.monto().floatValue(),
                dto.descripcion(),
                dto.curpCliente());
    }

    private <T> T ejecutar(Supplier<T> peticion) {
        try {
            return peticion.get();
        } catch (ResourceAccessException ex) {
            throw new SistemaDuenoNoDisponibleException(
                    "El Sistema Dueno no esta disponible por fallo de conexion o timeout.",
                    ex);
        } catch (RestClientResponseException ex) {
            throw traducirErrorHttp(ex);
        } catch (RestClientException ex) {
            throw new SistemaDuenoRechazoException(
                    "El Sistema Dueno devolvio una respuesta ilegible.", ex);
        }
    }

    private PolizaRemotaDto convertirFila(Object elemento) {
        if (!(elemento instanceof Map<?, ?> fila)) {
            throw new IllegalArgumentException("la fila no es un objeto JSON");
        }
        final UUID clave = leerClave(fila.get("clave"));
        final int tipo = leerTipo(fila.get("tipo"));
        final BigDecimal monto = leerMonto(fila.get("monto"));
        final String descripcion = leerTextoOpcional(fila.get("descripcion"), "descripcion");
        final String curpCliente = leerTextoObligatorio(fila.get("curpCliente"), "curpCliente");
        return new PolizaRemotaDto(clave, tipo, monto, descripcion, curpCliente);
    }

    private UUID leerClave(Object valor) {
        if (!(valor instanceof String texto)) {
            throw new IllegalArgumentException("clave ausente o no es texto");
        }
        try {
            return UUID.fromString(texto);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("clave no es un UUID valido: " + texto, ex);
        }
    }

    private int leerTipo(Object valor) {
        if (!(valor instanceof Number numero)) {
            throw new IllegalArgumentException("tipo ausente o no numerico");
        }
        int tipo;
        try {
            tipo = new BigDecimal(numero.toString()).intValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("tipo no es entero: " + numero, ex);
        }
        if (tipo < TIPO_MINIMO || tipo > TIPO_MAXIMO) {
            throw new IllegalArgumentException("tipo fuera de rango: " + tipo);
        }
        return tipo;
    }

    private BigDecimal leerMonto(Object valor) {
        if (!(valor instanceof Number numero)) {
            throw new IllegalArgumentException("monto ausente o no numerico");
        }
        BigDecimal monto = new BigDecimal(numero.toString());
        if (monto.signum() < 0) {
            throw new IllegalArgumentException("monto negativo: " + monto);
        }
        return monto;
    }

    private String leerTextoOpcional(Object valor, String campo) {
        if (valor == null) {
            return null;
        }
        if (!(valor instanceof String texto)) {
            throw new IllegalArgumentException(campo + " no es texto");
        }
        return texto;
    }

    private String leerTextoObligatorio(Object valor, String campo) {
        String texto = leerTextoOpcional(valor, campo);
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " ausente o vacio");
        }
        return texto;
    }

    private void validarSegmentoObligatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    campo + " no puede ser nulo ni vacio.");
        }
        validarSinDiagonales(valor, campo);
    }

    private void validarSinDiagonales(String valor, String campo) {
        if (valor.contains("/") || valor.contains("\\")) {
            throw new IllegalArgumentException(
                    campo + " no puede contener diagonales.");
        }
    }

    private RuntimeException traducirErrorHttp(RestClientResponseException ex) {
        int codigo = ex.getStatusCode().value();
        if (codigo == 502 || codigo == 503 || codigo == 504) {
            return new SistemaDuenoNoDisponibleException(
                    "El Sistema Dueno no esta disponible (HTTP " + codigo + ").", ex);
        }
        if (codigo == 500) {
            return new SistemaDuenoRechazoException(
                    "El Sistema Dueno rechazo la peticion (HTTP 500).", ex);
        }
        return new SistemaDuenoRechazoException(
                "Error inesperado del Sistema Dueno (HTTP " + codigo + ").", ex);
    }
}
