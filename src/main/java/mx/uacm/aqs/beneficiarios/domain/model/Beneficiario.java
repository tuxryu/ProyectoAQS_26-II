package mx.uacm.aqs.beneficiarios.domain.model;

public record Beneficiario(String clavePoliza, String curpBeneficiario, int porcentaje) {

    public Beneficiario {
        if (clavePoliza == null || clavePoliza.isBlank()) {
            throw new IllegalArgumentException("La clave de poliza es obligatoria");
        }
        if (curpBeneficiario == null || curpBeneficiario.isBlank()) {
            throw new IllegalArgumentException("La CURP del beneficiario es obligatoria");
        }
        if (porcentaje < 1 || porcentaje > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 1 y 100");
        }
    }

    public String llaveCompuesta() {
        return clavePoliza + ":" + curpBeneficiario;
    }
}
