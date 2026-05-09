package it.michelepal.rubrica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

    @RequestMapping(value = {"/", "/login", "/home", "/error", "/contacts", "/contacts/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}

