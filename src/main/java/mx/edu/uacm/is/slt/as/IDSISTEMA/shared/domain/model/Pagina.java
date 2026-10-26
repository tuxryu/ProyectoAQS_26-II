package mx.edu.uacm.is.slt.as.IDSISTEMA.shared.domain.model;

import java.util.List;

/**
 * Pagina de resultados calculada en memoria (el Sistema Dueno no pagina).
 *
 * @param <T> tipo de los elementos de la pagina
 * @param contenido elementos de la pagina solicitada (vacia si esta fuera de rango)
 * @param pagina numero de pagina solicitada, empezando en 0
 * @param tamano numero maximo de elementos por pagina
 * @param totalElementos numero total de elementos sin paginar
 * @param totalPaginas numero total de paginas
 */
public record Pagina<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas) {

    /**
     * Construye la pagina indicada a partir de la lista completa de elementos.
     *
     * @param <T> tipo de los elementos
     * @param elementos lista completa, ya ordenada
     * @param pagina numero de pagina, empezando en 0
     * @param tamano elementos por pagina, al menos 1
     * @return pagina con el subconjunto correspondiente
     */
    public static <T> Pagina<T> de(List<T> elementos, int pagina, int tamano) {
        if (pagina < 0 || tamano < 1) {
            throw new IllegalArgumentException(
                    "La pagina debe ser mayor o igual a 0 y el tamano mayor o igual a 1.");
        }
        int total = elementos.size();
        final int paginas = (int) ((total + (long) tamano - 1) / tamano);
        long desde = (long) pagina * tamano;
        List<T> contenido = List.of();
        if (desde < total) {
            int hasta = (int) Math.min(desde + tamano, total);
            contenido = List.copyOf(elementos.subList((int) desde, hasta));
        }
        return new Pagina<>(contenido, pagina, tamano, total, paginas);
    }
}
