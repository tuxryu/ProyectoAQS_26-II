package mx.edu.uacm.is.slt.as.IDSISTEMA.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador web para la vista de inicio.
 */
@Controller
public class HomeViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
