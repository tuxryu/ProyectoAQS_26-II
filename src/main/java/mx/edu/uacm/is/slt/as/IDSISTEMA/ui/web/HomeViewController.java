package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador web de Capa 4 para la vista de inicio y el menu principal de
 * acceso a los casos de uso.
 */
@Controller
public class HomeViewController {

    /**
     * Muestra la pagina principal con el menu de navegacion del sistema.
     *
     * @return nombre de la plantilla Thymeleaf principal
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
