package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest.dto.ClienteRemotoDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http.FechasSistemaDueno;

/**
 * Adaptador de salida REST hacia el Sistema Dueno para operaciones de clientes
 * (GET, POST y PUT), manejando respuestas 200 vacias, errores HTTP y parseo de
 * fechas UTC-6 con FechasSistemaDueno.
 */
@Component
public class ClienteSistemaDuenoAdapter {

    private static final String RUTA_CON_SEGUNDO_APELLIDO
            = "/cliente/{curp}/{direccion}/{fecha}/{nombres}/{primerApellido}/{segundoApellido}";

    private static final String RUTA_SIN_SEGUNDO_APELLIDO
            = "/cliente/{curp}/{direccion}/{fecha}/{nombres}/{primerApellido}";

    private final RestClient restClient;

    public ClienteSistemaDuenoAdapter(RestClient restClient) {
        this.restClient = Objects.requireNonNull(
                restClient, "El cliente HTTP RestClient es obligatorio.");
    }

    /**
     * Consulta un cliente en el Sistema Dueno por su CURP tal cual (respetando
     * mayusculas y minusculas). Si el servidor responde 200 con cuerpo vacio,
     * devuelve Optional.empty().
     *
     * @param curp CURP del cliente a consultar
     * @return Optional con los datos del cliente o Optional.empty() si no existe
     */
    public Optional<ClienteRemotoDto> buscarPorCurp(String curp) {
        validarSegmentoObligatorio(curp, "La CURP");
        try {
            ClienteRemotoDto respuesta = restClient.get()
                    .uri("/cliente/{curp}", curp)
                    .retrieve()
                    .body(ClienteRemotoDto.class);

            if (esRespuestaVacia(respuesta)) {
                return Optional.empty();
            }
            FechasSistemaDueno.convertirALocalDate(respuesta.getFechaNacimiento());
            return Optional.of(respuesta);
        } catch (ResourceAccessException ex) {
            throw new SistemaDuenoNoDisponibleException(
                    "El Sistema Dueno no esta disponible por fallo de conexion o timeout.",
                    ex);
        } catch (RestClientResponseException ex) {
            throw traducirErrorHttp(ex);
        }
    }

    /**
     * Registra un cliente en el Sistema Dueno mediante POST enviando los
     * atributos como segmentos de ruta codificados. Si responde 200 vacio,
     * lanza SistemaDuenoRechazoException.
     *
     * @param cliente datos del cliente a guardar
     * @return DTO devuelto por el Sistema Dueno
     */
    public ClienteRemotoDto guardar(ClienteRemotoDto cliente) {
        Objects.requireNonNull(cliente, "Los datos del cliente son obligatorios.");
        return enviarEscritura(cliente.getCurp(), cliente, false);
    }

    /**
     * Actualiza un cliente en el Sistema Dueno. Usa PUT cuando tiene segundo
     * apellido y POST cuando no tiene segundo apellido (hallazgo 8).
     *
     * @param curp CURP del cliente a actualizar
     * @param cliente datos actualizados del cliente
     * @return DTO devuelto por el Sistema Dueno
     */
    public ClienteRemotoDto actualizar(String curp, ClienteRemotoDto cliente) {
        Objects.requireNonNull(cliente, "Los datos del cliente son obligatorios.");
        boolean usarPut = cliente.getSegundoApellido() != null;
        return enviarEscritura(curp, cliente, usarPut);
    }

    private ClienteRemotoDto enviarEscritura(
            String curp, ClienteRemotoDto cliente, boolean usarPut) {
        validarSegmentoObligatorio(curp, "La CURP");
        validarSegmentoObligatorio(cliente.getDireccion(), "La direccion");
        validarSegmentoObligatorio(cliente.getNombres(), "El nombre");
        validarSegmentoObligatorio(cliente.getPrimerApellido(), "El primer apellido");
        String segundoApellido = cliente.getSegundoApellido();
        if (segundoApellido != null) {
            validarSinDiagonales(segundoApellido, "El segundo apellido");
        }
        LocalDate fechaLocal = cliente.obtenerFechaNacimientoLocal();
        String fechaRuta = FechasSistemaDueno.formatearParaRuta(fechaLocal);

        try {
            ClienteRemotoDto respuesta = ejecutarPeticionEscritura(
                    curp, cliente, fechaRuta, segundoApellido, usarPut);
            if (esRespuestaVacia(respuesta)) {
                throw new SistemaDuenoRechazoException(
                        "El Sistema Dueno rechazo la escritura del cliente (200 vacio).");
            }
            FechasSistemaDueno.convertirALocalDate(respuesta.getFechaNacimiento());
            return respuesta;
        } catch (ResourceAccessException ex) {
            throw new SistemaDuenoNoDisponibleException(
                    "El Sistema Dueno no esta disponible por fallo de conexion o timeout.",
                    ex);
        } catch (RestClientResponseException ex) {
            throw traducirErrorHttp(ex);
        }
    }

    private ClienteRemotoDto ejecutarPeticionEscritura(
            String curp,
            ClienteRemotoDto cliente,
            String fechaRuta,
            String segundoApellido,
            boolean usarPut) {
        if (segundoApellido != null) {
            if (usarPut) {
                return restClient.put()
                        .uri(
                                RUTA_CON_SEGUNDO_APELLIDO,
                                curp,
                                cliente.getDireccion(),
                                fechaRuta,
                                cliente.getNombres(),
                                cliente.getPrimerApellido(),
                                segundoApellido)
                        .retrieve()
                        .body(ClienteRemotoDto.class);
            }
            return restClient.post()
                    .uri(
                            RUTA_CON_SEGUNDO_APELLIDO,
                            curp,
                            cliente.getDireccion(),
                            fechaRuta,
                            cliente.getNombres(),
                            cliente.getPrimerApellido(),
                            segundoApellido)
                    .retrieve()
                    .body(ClienteRemotoDto.class);
        }
        return restClient.post()
                .uri(
                        RUTA_SIN_SEGUNDO_APELLIDO,
                        curp,
                        cliente.getDireccion(),
                        fechaRuta,
                        cliente.getNombres(),
                        cliente.getPrimerApellido())
                .retrieve()
                .body(ClienteRemotoDto.class);
    }

    private boolean esRespuestaVacia(ClienteRemotoDto respuesta) {
        return respuesta == null
                || respuesta.getCurp() == null
                || respuesta.getCurp().trim().isEmpty();
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
        HttpStatusCode status = ex.getStatusCode();
        int codigo = status.value();
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
