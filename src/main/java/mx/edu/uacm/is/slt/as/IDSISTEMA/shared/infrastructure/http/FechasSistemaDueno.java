package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.infrastructure.http;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utileria para convertir fechas del Sistema Dueno hacia y desde LocalDate
 * considerando la zona horaria America/Mexico_City (UTC-6 y desfase historico).
 */
public final class FechasSistemaDueno {

    public static final ZoneId ZONA_CDMX = ZoneId.of("America/Mexico_City");

    private static final LocalTime MEDIANOCHE_UTC_MENOS_SEIS = LocalTime.of(6, 0);

    private static final DateTimeFormatter FORMATO_FECHA_RUTA = DateTimeFormatter.ISO_LOCAL_DATE;

    private FechasSistemaDueno() {
    }

    /**
     * Convierte una cadena de fecha o timestamp ISO-8601 devuelta por el
     * Sistema Dueno (por ejemplo 1990-01-01T06:00:00.000+00:00,
     * 1885-05-25T06:00:00.000+00:00 o 1920-01-01T06:36:36.000+00:00) a
     * LocalDate en zona America/Mexico_City sin desplazar un dia atras las
     * fechas anteriores a 1922 almacenadas en medianoche UTC-6 fijo.
     *
     * @param valor cadena de texto con la fecha o timestamp del servidor remoto
     * @return fecha local correspondiente en Ciudad de Mexico
     */
    public static LocalDate convertirALocalDate(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El valor de fecha del Sistema Dueno no puede ser nulo ni vacio.");
        }
        String limpio = valor.trim();
        if (limpio.length() == 10) {
            return LocalDate.parse(limpio, FORMATO_FECHA_RUTA);
        }
        try {
            OffsetDateTime fechaUtc = OffsetDateTime.parse(limpio)
                    .withOffsetSameInstant(ZoneOffset.UTC);
            return resolverFechaLocalCdmx(fechaUtc);
        } catch (DateTimeParseException ex) {
            OffsetDateTime fechaUtc = Instant.parse(limpio).atOffset(ZoneOffset.UTC);
            return resolverFechaLocalCdmx(fechaUtc);
        }
    }

    private static LocalDate resolverFechaLocalCdmx(OffsetDateTime fechaUtc) {
        if (MEDIANOCHE_UTC_MENOS_SEIS.equals(fechaUtc.toLocalTime())) {
            return fechaUtc.toLocalDate();
        }
        return fechaUtc.atZoneSameInstant(ZONA_CDMX).toLocalDate();
    }

    /**
     * Formatea un LocalDate en formato yyyy-MM-dd para enviarlo como segmento
     * de ruta hacia el Sistema Dueno.
     *
     * @param fecha fecha a formatear
     * @return representacion yyyy-MM-dd
     */
    public static String formatearParaRuta(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha a formatear es obligatoria.");
        }
        return fecha.format(FORMATO_FECHA_RUTA);
    }
}
