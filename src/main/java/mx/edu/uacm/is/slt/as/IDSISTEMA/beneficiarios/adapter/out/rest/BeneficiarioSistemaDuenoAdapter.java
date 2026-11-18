package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.out.rest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.out.rest.dto.BeneficiarioIdRemotoDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.adapter.out.rest.dto.BeneficiarioRemotoDto;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out.BeneficiarioSistemaDuenoPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.BeneficiarioId;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.exception.SistemaDuenoRechazoException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http.FechasSistemaDueno;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http.TraductorExcepcionesSistemaDueno;

/**
 * Adaptador REST de salida hacia el Sistema Dueno para consultas, listado
 * general y por poliza (GET /beneficiarios) y alta de beneficiarios (POST).
 */
@Component
public class BeneficiarioSistemaDuenoAdapter
        implements BeneficiarioSistemaDuenoPort {

    private static final Logger LOG
            = LoggerFactory.getLogger(BeneficiarioSistemaDuenoAdapter.class);

    private static final String RUTA_POST_CON_SEGUNDO_APELLIDO
            = "/beneficiario/{fecha}/{clavePoliza}/{porcentaje}/{nombres}"
                    + "/{primerApellido}/{segundoApellido}";

    private static final String RUTA_POST_SIN_SEGUNDO_APELLIDO
            = "/beneficiario/{fecha}/{clavePoliza}/{porcentaje}/{nombres}"
                    + "/{primerApellido}";

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
        Objects.requireNonNull(clavePoliza, "La clave de poliza es obligatoria.");
        validarSegmento(nombres);
        validarSegmento(primerApellido);

        boolean tieneSegundoApellido
                = segundoApellido != null && !segundoApellido.trim().isEmpty();

        if (tieneSegundoApellido) {
            validarSegmento(segundoApellido);
        }

        BeneficiarioRemotoDto respuesta = TraductorExcepcionesSistemaDueno.ejecutar(
                () -> consultarRemoto(
                        fechaNacimiento,
                        clavePoliza,
                        nombres,
                        primerApellido,
                        segundoApellido,
                        tieneSegundoApellido));

        if (respuesta == null || respuesta.id() == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(convertirADominio(respuesta));
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new SistemaDuenoRechazoException(
                    "El Sistema Dueno devolvio datos invalidos para el beneficiario.", ex);
        }
    }

    @Override
    public List<Beneficiario> listarTodos() {
        List<?> filas = TraductorExcepcionesSistemaDueno.ejecutar(
                () -> restClient.get()
                        .uri("/beneficiarios")
                        .retrieve()
                        .body(List.class));

        if (filas == null || filas.isEmpty()) {
            return List.of();
        }

        List<Beneficiario> resultado = new ArrayList<>(filas.size());
        for (Object elemento : filas) {
            agregarFilaSiEsValida(resultado, elemento);
        }
        return List.copyOf(resultado);
    }

    @Override
    public List<Beneficiario> listarPorPoliza(UUID clavePoliza) {
        Objects.requireNonNull(clavePoliza, "La clave de poliza es obligatoria.");
        return listarTodos().stream()
                .filter(beneficiario -> clavePoliza.equals(beneficiario.getClavePoliza()))
                .toList();
    }

    @Override
    public Beneficiario guardar(Beneficiario beneficiario) {
        Objects.requireNonNull(beneficiario, "El beneficiario es obligatorio.");
        final String fechaRuta = FechasSistemaDueno.formatearParaRuta(
                beneficiario.getFechaNacimiento());
        validarSegmento(fechaRuta);
        final UUID clavePoliza = Objects.requireNonNull(
                beneficiario.getClavePoliza(), "La clave de poliza es obligatoria.");
        String nombres = beneficiario.getNombres();
        String primerApellido = beneficiario.getPrimerApellido();
        String segundoApellido = beneficiario.getSegundoApellido();
        validarSegmento(nombres);
        validarSegmento(primerApellido);

        boolean tieneSegundoApellido
                = segundoApellido != null && !segundoApellido.trim().isEmpty();
        if (tieneSegundoApellido) {
            validarSegmento(segundoApellido);
        }

        BeneficiarioRemotoDto respuesta = TraductorExcepcionesSistemaDueno.ejecutar(
                () -> enviarPostRemoto(
                        fechaRuta,
                        clavePoliza,
                        beneficiario.getPorcentaje(),
                        nombres,
                        primerApellido,
                        segundoApellido,
                        tieneSegundoApellido));

        if (respuesta == null || respuesta.id() == null) {
            throw new SistemaDuenoRechazoException(
                    "El Sistema Dueno rechazo el registro del beneficiario (200 vacio).");
        }

        try {
            return convertirADominio(respuesta);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new SistemaDuenoRechazoException(
                    "El Sistema Dueno devolvio datos invalidos al guardar el beneficiario.",
                    ex);
        }
    }

    private void agregarFilaSiEsValida(List<Beneficiario> resultado, Object elemento) {
        if (!(elemento instanceof Map<?, ?> fila)) {
            LOG.warn("Se omite un beneficiario invalido del Sistema Dueno: {}", elemento);
            return;
        }
        try {
            BeneficiarioRemotoDto dto = convertirMapaADto(fila);
            resultado.add(convertirADominio(dto));
        } catch (RuntimeException ex) {
            LOG.warn(
                    "Se omite un beneficiario invalido del Sistema Dueno ({}): {}",
                    ex.getMessage(),
                    elemento);
        }
    }

    private BeneficiarioRemotoDto convertirMapaADto(Map<?, ?> fila) {
        Map<?, ?> origenId = fila.get("id") instanceof Map<?, ?> idMapa ? idMapa : fila;
        String nombres = leerTexto(origenId.get("nombres"));
        String primerApellido = leerTexto(origenId.get("primerApellido"));
        String segundoApellido = normalizarSegundoApellido(
                leerTexto(origenId.get("segundoApellido")));
        String fechaNacimiento = leerTexto(origenId.get("fechaNacimiento"));
        String clavePoliza = leerTexto(origenId.get("clavePoliza"));
        int porcentaje = leerPorcentaje(fila.get("porcentaje"));
        BeneficiarioIdRemotoDto idDto = new BeneficiarioIdRemotoDto(
                nombres, primerApellido, segundoApellido, fechaNacimiento, clavePoliza);
        return new BeneficiarioRemotoDto(idDto, porcentaje);
    }

    private String leerTexto(Object valor) {
        return valor != null ? valor.toString() : null;
    }

    private int leerPorcentaje(Object valor) {
        if (valor instanceof Number numero) {
            return new BigDecimal(numero.toString()).intValueExact();
        }
        if (valor instanceof String texto && !texto.trim().isEmpty()) {
            return Integer.parseInt(texto.trim());
        }
        throw new IllegalArgumentException("porcentaje ausente o no numerico");
    }

    private BeneficiarioRemotoDto consultarRemoto(
            LocalDate fechaNacimiento,
            UUID clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellido,
            boolean tieneSegundoApellido) {
        if (tieneSegundoApellido) {
            return restClient.get()
                    .uri(
                            "/beneficiario/{fecha}/{clavePoliza}/{nombres}/{primerApellido}"
                                    + "/{segundoApellido}",
                            FechasSistemaDueno.formatearParaRuta(fechaNacimiento),
                            clavePoliza,
                            nombres,
                            primerApellido,
                            segundoApellido)
                    .retrieve()
                    .body(BeneficiarioRemotoDto.class);
        }
        return restClient.get()
                .uri(
                        "/beneficiario/{fecha}/{clavePoliza}/{nombres}/{primerApellido}",
                        FechasSistemaDueno.formatearParaRuta(fechaNacimiento),
                        clavePoliza,
                        nombres,
                        primerApellido)
                .retrieve()
                .body(BeneficiarioRemotoDto.class);
    }

    private BeneficiarioRemotoDto enviarPostRemoto(
            String fechaRuta,
            UUID clavePoliza,
            int porcentaje,
            String nombres,
            String primerApellido,
            String segundoApellido,
            boolean tieneSegundoApellido) {
        if (tieneSegundoApellido) {
            return restClient.post()
                    .uri(
                            RUTA_POST_CON_SEGUNDO_APELLIDO,
                            fechaRuta,
                            clavePoliza,
                            porcentaje,
                            nombres,
                            primerApellido,
                            segundoApellido)
                    .retrieve()
                    .body(BeneficiarioRemotoDto.class);
        }
        return restClient.post()
                .uri(
                        RUTA_POST_SIN_SEGUNDO_APELLIDO,
                        fechaRuta,
                        clavePoliza,
                        porcentaje,
                        nombres,
                        primerApellido)
                .retrieve()
                .body(BeneficiarioRemotoDto.class);
    }

    private Beneficiario convertirADominio(BeneficiarioRemotoDto dto) {
        BeneficiarioIdRemotoDto id = dto.id();

        LocalDate fecha = FechasSistemaDueno.convertirALocalDate(id.fechaNacimiento());
        UUID clave = UUID.fromString(id.clavePoliza());
        String segundoApellido = normalizarSegundoApellido(id.segundoApellido());

        BeneficiarioId beneficiarioId = new BeneficiarioId(
                id.nombres(),
                id.primerApellido(),
                segundoApellido,
                fecha,
                clave);

        return new Beneficiario(beneficiarioId, dto.porcentaje());
    }

    private String normalizarSegundoApellido(String segundoApellido) {
        if (segundoApellido == null || segundoApellido.trim().isEmpty()) {
            return null;
        }
        return segundoApellido;
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
