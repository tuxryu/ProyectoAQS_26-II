package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.adapter.out.rest;

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
 * Adaptador de salida REST hacia el Sistema Dueno para consultar clientes
 * mediante GET /cliente/{curp}, manejando respuestas 200 vacias como
 * Optional.empty() y parseando fechas UTC-6 con FechasSistemaDueno.
 */
@Component
public class ClienteSistemaDuenoAdapter {

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
        validarCurpRuta(curp);
        try {
            ClienteRemotoDto respuesta = restClient.get()
                    .uri("/cliente/{curp}", curp)
                    .retrieve()
                    .body(ClienteRemotoDto.class);

            if (respuesta == null
                    || respuesta.getCurp() == null
                    || respuesta.getCurp().trim().isEmpty()) {
                return Optional.empty();
            }
            FechasSistemaDueno.aLocalDate(respuesta.getFechaNacimiento());
            return Optional.of(respuesta);
        } catch (ResourceAccessException ex) {
            throw new SistemaDuenoNoDisponibleException(
                    "El Sistema Dueno no esta disponible por fallo de conexion o timeout.",
                    ex);
        } catch (RestClientResponseException ex) {
            throw traducirErrorHttp(ex);
        }
    }

    private void validarCurpRuta(String curp) {
        if (curp == null || curp.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La CURP a consultar no puede ser nula ni vacia.");
        }
        if (curp.contains("/") || curp.contains("\\")) {
            throw new IllegalArgumentException(
                    "La CURP no puede contener diagonales.");
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
