package com.ifpr.backend.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Hidden
public class SpaController {
    @GetMapping({"/login", "/cadastrar-conta", "/recuperar-senha", "/redefinir-senha",
        "/redefinir-senha/{token}", "/app", "/app/**"})
    public String frontend() {
        return "forward:/index.html";
    }
}
