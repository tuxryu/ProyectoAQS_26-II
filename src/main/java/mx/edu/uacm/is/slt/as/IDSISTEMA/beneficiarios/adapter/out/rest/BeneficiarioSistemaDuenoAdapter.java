package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.out.rest;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.out.rest.dto.BeneficiarioIdRemotoDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.out.rest.dto.BeneficiarioRemotoDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out.BeneficiarioSistemaDuenoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.BeneficiarioId;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http.FechasSistemaDueno;

@Component
public class BeneficiarioSistemaDuenoAdapter
        implements BeneficiarioSistemaDuenoPort {

    private final RestClient restClient;

    public BeneficiarioSistemaDuenoAdapter(RestClient restClient) {
        this.restClient = Objects.requireNonNull(
                restClient,
                "El cliente HTTP RestClient es obligatorio.");
    }

    @Override
    public Optional<Beneficiario> buscarPorDatos(
            LocalDate fechaNacimiento,
            UUID clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellido) {

        validarSegmento(FechasSistemaDueno.formatearParaRuta(fechaNacimiento));
        Objects.requireNonNull(clavePoliza, "La clave de póliza es obligatoria.");
        validarSegmento(nombres);
        validarSegmento(primerApellido);

        boolean tieneSegundoApellido =
                segundoApellido != null && !segundoApellido.trim().isEmpty();

        if (tieneSegundoApellido) {
            validarSegmento(segundoApellido);
        }

        try {
            BeneficiarioRemotoDto respuesta;

            if (tieneSegundoApellido) {
                respuesta = restClient.get()
                        .uri(
                                "/beneficiario/{fecha}/{clavePoliza}/{nombres}/{primerApellido}/{segundoApellido}",
                                FechasSistemaDueno.formatearParaRuta(fechaNacimiento),
                                clavePoliza,
                                nombres,
                                primerApellido,
                                segundoApellido)
                        .retrieve()
                        .body(BeneficiarioRemotoDto.class);
            } else {
                respuesta = restClient.get()
                        .uri(
                                "/beneficiario/{fecha}/{clavePoliza}/{nombres}/{primerApellido}",
                                FechasSistemaDueno.formatearParaRuta(fechaNacimiento),
                                clavePoliza,
                                nombres,
                                primerApellido)
                        .retrieve()
                        .body(BeneficiarioRemotoDto.class);
            }

            if (respuesta == null || respuesta.id() == null) {
                return Optional.empty();
            }

            return Optional.of(convertirADominio(respuesta));

        } catch (ResourceAccessException ex) {
            throw new SistemaDuenoNoDisponibleException(
                    "El Sistema Dueno no esta disponible por fallo de conexion o timeout.",
                    ex);
        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();

            if (status == 502 || status == 503 || status == 504) {
                throw new SistemaDuenoNoDisponibleException(
                        "El Sistema Dueno no esta disponible. HTTP " + status + ".", ex);
            }

            throw new SistemaDuenoRechazoException(
                    "El Sistema Dueno rechazo la consulta. HTTP " + status + ".", ex);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new SistemaDuenoRechazoException(
                    "El Sistema Dueno devolvio datos invalidos para el beneficiario.", ex);
        }
    }

    private Beneficiario convertirADominio(BeneficiarioRemotoDto dto) {
        BeneficiarioIdRemotoDto id = dto.id();

        LocalDate fecha = FechasSistemaDueno.convertirALocalDate(id.fechaNacimiento());
        UUID clave = UUID.fromString(id.clavePoliza());

        BeneficiarioId beneficiarioId = new BeneficiarioId(
                id.nombres(),
                id.primerApellido(),
                id.segundoApellido(),
                fecha,
                clave);

        return new Beneficiario(beneficiarioId, dto.porcentaje());
    }

    private void validarSegmento(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Los segmentos obligatorios de la consulta no pueden estar vacios.");
        }

        if (valor.contains("/") || valor.contains("\\")) {
            throw new IllegalArgumentException(
                    "Los datos de la consulta no pueden contener diagonales.");
        }
    }
}
