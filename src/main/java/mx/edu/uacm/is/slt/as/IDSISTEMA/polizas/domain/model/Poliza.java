package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model;

import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.MontoInvalidoException;

@Entity
public class Poliza {

    private static final byte TIPO_MINIMO = 0;
    private static final byte TIPO_MAXIMO = 2;

    @Id
    private UUID clave; // Identificador único de la póliza (Integer)
    private byte tipo;
    private float monto; // Cobertura financiera
    private String descripcion; // Detalle de la cobertura
    private String curpCliente; // FK lógica al Cliente

    // Constructor
    public Poliza() {
    }

    public Poliza(UUID clave, byte tipo, float monto, String descripcion, String curpCliente) {
        validarTipo(tipo);
        validarMonto(monto);
        validarDescripcion(descripcion);
        validarCurpCliente(curpCliente);
        this.clave = clave;
        this.tipo = tipo;
        this.monto = monto;
        this.descripcion = descripcion;
        this.curpCliente = curpCliente;
    }

    private void validarTipo(byte valorTipo) {
        if (valorTipo < TIPO_MINIMO || valorTipo > TIPO_MAXIMO) {
            throw new IllegalArgumentException(
                    "El tipo de poliza debe estar entre 0 y 2.");
        }
    }

    private void validarMonto(float valorMonto) {
        if (Float.isNaN(valorMonto)
                || Float.isInfinite(valorMonto)
                || valorMonto <= 0.0f) {
            throw new MontoInvalidoException(valorMonto);
        }
    }

    private void validarDescripcion(String valorDescripcion) {
        if (valorDescripcion != null
                && (valorDescripcion.contains("/") || valorDescripcion.contains("\\"))) {
            throw new IllegalArgumentException(
                    "La descripcion no puede contener diagonales.");
        }
    }

    private void validarCurpCliente(String valorCurp) {
        if (valorCurp == null || valorCurp.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El CURP del cliente titular es obligatorio.");
        }
        if (valorCurp.contains("/") || valorCurp.contains("\\")) {
            throw new IllegalArgumentException(
                    "El CURP del cliente no puede contener diagonales.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Poliza poliza)) {
            return false;
        }
        return tipo == poliza.tipo
                && Float.compare(monto, poliza.monto) == 0
                && Objects.equals(clave, poliza.clave)
                && Objects.equals(descripcion, poliza.descripcion)
                && Objects.equals(curpCliente, poliza.curpCliente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clave, tipo, monto, descripcion, curpCliente);
    }

    public UUID getClave() {
        return clave;
    }

    public void setClave(UUID claves) {
        this.clave = claves;
    }

    public byte getTipo() {
        return tipo;
    }

    public void setTipo(byte tipos) {
        validarTipo(tipos);
        this.tipo = tipos;
    }

    public float getMonto() {
        return monto;
    }

    public void setMonto(float montos) {
        validarMonto(montos);
        this.monto = montos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcions) {
        validarDescripcion(descripcions);
        this.descripcion = descripcions;
    }

    public String getCurpCliente() {
        return curpCliente;
    }

    public void setCurpCliente(String curpClientes) {
        validarCurpCliente(curpClientes);
        this.curpCliente = curpClientes;
    }
}
