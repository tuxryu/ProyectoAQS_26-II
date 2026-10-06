package mx.uacm.aqs.polizas.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Poliza(UUID id, TipoPoliza tipo, BigDecimal montoAsegurado, String clienteCurp) {

    public Poliza {
        if (id == null) {
            throw new IllegalArgumentException("El id de poliza es obligatorio");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de poliza es obligatorio");
        }
        if (montoAsegurado == null || montoAsegurado.signum() <= 0) {
            throw new IllegalArgumentException("El monto asegurado debe ser mayor a cero");
        }
        if (clienteCurp == null || clienteCurp.isBlank()) {
            throw new IllegalArgumentException("La asociacion con cliente es obligatoria");
        }
    }
}
