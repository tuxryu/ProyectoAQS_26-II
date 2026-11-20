package mx.edu.uacm.is.slt.as.IDSISTEMA.ui.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Pruebas WebMvc para {@link HomeViewController}: verifica la vista principal y
 * los enlaces de navegacion al modulo de clientes.
 */
@WebMvcTest(HomeViewController.class)
class HomeViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debeRenderizarVistaPrincipalConMenuDeClientes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("/web/clientes")))
                .andExpect(content().string(containsString("/web/clientes/nuevo")))
                .andExpect(content().string(containsString("/web/clientes/editar")));
    }
}
