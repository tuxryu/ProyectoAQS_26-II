package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.in;

import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;

/**
 * Puerto de entrada para el caso de uso de alta de beneficiario vinculado a una
 * poliza.
 */
public interface AltaBeneficiarioUseCase {

    /**
     * Registra un nuevo beneficiario vinculado a una poliza existente,
     * verificando que no este duplicado y que la suma de porcentajes de la
     * poliza no rebase el 100%.
     *
     * @param beneficiario entidad de dominio a registrar
     * @return beneficiario registrado y verificado
     */
    Beneficiario registrar(Beneficiario beneficiario);
}
