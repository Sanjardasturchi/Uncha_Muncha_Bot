package com.example.Uncha_Muncha_Bot.controller.webController;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;


@Controller
@RequestMapping("")
public class InitController {

    @GetMapping("/greating")
    public String greating(Model model) {
        model.addAttribute("current_time", LocalDateTime.now());
        return "greating";
    }


    @GetMapping("/time")
    public ModelAndView greating() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("current_time", LocalDateTime.now());
        modelAndView.setViewName("greating");
        return modelAndView;
    }

}
