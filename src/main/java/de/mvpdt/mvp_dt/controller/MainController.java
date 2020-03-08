package de.mvpdt.mvp_dt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = {"","/person"})
public class MainController {

    @RequestMapping(value = {"/","index"})
    public String index(Model model){
//        System.out.println(personService.findAll());
//        model.addAttribute("persons", personService.findAll());
        return "index";
    }
}
