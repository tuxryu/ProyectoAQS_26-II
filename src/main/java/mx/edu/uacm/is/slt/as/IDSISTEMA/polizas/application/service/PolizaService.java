package mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.ConsultaPolizaUseCase;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.in.FiltroPoliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.application.port.out.PolizaRemotaPort;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.exception.PolizaNoEncontradaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.polizas.domain.model.Poliza;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.FuncionalidadNoDisponibleException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.exception.SolicitudInvalidaException;
import mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model.Pagina;

/**
 * Caso de uso de consultas de polizas. Delega la lectura al Sistema Dueno a
 * traves de {@link PolizaRemotaPort} y pagina en memoria, ordenando por clave
 * para que las paginas sean estables entre llamadas.
 */
@Service
public class PolizaService implements ConsultaPolizaUseCase {

    /** Tamano maximo de pagina permitido. */
    public static final int TAMANO_MAXIMO = 100;

    private static final Comparator<Poliza> POR_CLAVE
            = Comparator.comparing(poliza -> poliza.getClave().toString());

    private final PolizaRemotaPort polizaRemotaPort;

    public PolizaService(PolizaRemotaPort polizaRemotaPort) {
        this.polizaRemotaPort = Objects.requireNonNull(
                polizaRemotaPort, "El puerto PolizaRemotaPort es obligatorio.");
    }

    @Override
    public Poliza consultarPorClave(UUID clave) {
        if (clave == null) {
            throw new SolicitudInvalidaException("La clave de la poliza es obligatoria.");
        }
        return polizaRemotaPort.buscarPorClave(clave)
                .orElseThrow(() -> new PolizaNoEncontradaException(clave));
    }

    @Override
    public Pagina<Poliza> consultar(FiltroPoliza filtro, int pagina, int tamano) {
        if (filtro == null) {
            throw new SolicitudInvalidaException("El filtro de la consulta es obligatorio.");
        }
        validarPaginacion(pagina, tamano);
        List<Poliza> ordenadas = buscar(filtro).stream()
                .sorted(POR_CLAVE)
                .toList();
        return Pagina.de(ordenadas, pagina, tamano);
    }

    private List<Poliza> buscar(FiltroPoliza filtro) {
        if (filtro instanceof FiltroPoliza.Todas) {
            return polizaRemotaPort.listarTodas();
        }
        if (filtro instanceof FiltroPoliza.PorCurp porCurp) {
            return polizaRemotaPort.buscarPorCurp(porCurp.curp());
        }
        if (filtro instanceof FiltroPoliza.PorTipo porTipo) {
            return polizaRemotaPort.buscarPorTipo(porTipo.tipo());
        }
        if (filtro instanceof FiltroPoliza.PorCliente porCliente) {
            return polizaRemotaPort.buscarPorCliente(
                    porCliente.nombres(),
                    porCliente.primerApellido(),
                    porCliente.segundoApellido());
        }
        if (filtro instanceof FiltroPoliza.PorBeneficiario porBeneficiario) {
            return polizaRemotaPort.buscarPorBeneficiario(
                    porBeneficiario.nombres(),
                    porBeneficiario.primerApellido(),
                    porBeneficiario.segundoApellido());
        }
        FiltroPoliza.PorFechaNacimientoBeneficiario porFecha
                = (FiltroPoliza.PorFechaNacimientoBeneficiario) filtro;
        throw new FuncionalidadNoDisponibleException(
                "La consulta por fecha de nacimiento del beneficiario ("
                + porFecha.fechaNacimiento()
                + ") aun no esta disponible.");
    }

    private void validarPaginacion(int pagina, int tamano) {
        if (pagina < 0) {
            throw new SolicitudInvalidaException(
                    "El parametro pagina debe ser mayor o igual a 0.");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new SolicitudInvalidaException(
                    "El parametro tamano debe estar entre 1 y " + TAMANO_MAXIMO + ".");
        }
    }
}
