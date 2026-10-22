package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model;

import java.util.Objects;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Poliza {

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
        this.clave = clave;
        this.tipo = tipo;
        this.monto = monto;
        this.descripcion = descripcion;
        this.curpCliente = curpCliente;
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
        this.tipo = tipos;
    }

    public float getMonto() {
        return monto;
    }

    public void setMonto(float montos) {
        this.monto = montos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcions) {
        this.descripcion = descripcions;
    }

    public String getCurpCliente() {
        return curpCliente;
    }

    public void setCurpCliente(String curpClientes) {
        this.curpCliente = curpClientes;
    }
}
