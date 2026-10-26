package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.adapter.in.web.dto;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path) {
}
