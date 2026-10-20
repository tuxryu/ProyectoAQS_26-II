package mx.edu.uacm.is.slt.as.IDSISTEMA.clientes.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ClienteId implements Serializable {

    @Column(name = "curp", length = 18, nullable = false)
    private String curp;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "primer_apellido", nullable = false)
    private String primerApellido;

    // JPA requiere un constructor vacío
    protected ClienteId() {}

    public ClienteId(String curp, String nombres, String primerApellido) {
        this.curp = curp;
        this.nombres = nombres;
        this.primerApellido = primerApellido;
    }

    public String getCurp() { return curp; }
    public String getNombres() { return nombres; }
    public String getPrimerApellido() { return primerApellido; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClienteId clienteId = (ClienteId) o;
        return Objects.equals(curp, clienteId.curp) &&
                Objects.equals(nombres, clienteId.nombres) &&
                Objects.equals(primerApellido, clienteId.primerApellido);
    }

    @Override
    public int hashCode() {
        return Objects.hash(curp, nombres, primerApellido);
    }
}