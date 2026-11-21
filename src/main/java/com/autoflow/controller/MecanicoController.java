package com.autoflow.controller;

import com.autoflow.service.MecanicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mecanico")
public class MecanicoController {

    private final MecanicoService mecanicoService;

    @GetMapping("/listado")
    public String listado(Model model) {
        model.addAttribute("mecanicos", mecanicoService.listar());
        return "mecanico/listado";
    }
}
