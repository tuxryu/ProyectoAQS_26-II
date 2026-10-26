package mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.application.port.out;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import mx.edu.uacm.is.slt.as.IDSISTEMA.beneficiarios.domain.model.Beneficiario;

public interface BeneficiarioSistemaDuenoPort {

    Optional<Beneficiario> buscarPorDatos(
            LocalDate fechaNacimiento,
            UUID clavePoliza,
            String nombres,
            String primerApellido,
            String segundoApellido);
}
