package mx.edu.uacm.is.slt.as.IDSISTEMA.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeViewController {

    @GetMapping("/")
    public String index() {
        return "index"; // Devuelve templates/index.html
    }
}