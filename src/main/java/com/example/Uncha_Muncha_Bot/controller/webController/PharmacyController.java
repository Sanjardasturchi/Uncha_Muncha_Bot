package com.example.Uncha_Muncha_Bot.controller.webController;

import com.example.Uncha_Muncha_Bot.dto.PharmacyDTO;
import com.example.Uncha_Muncha_Bot.service.PharmacyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


@Controller
@RequestMapping("/pharmacy")
public class PharmacyController {

    @Autowired
    private PharmacyService pharmacyService;


    @GetMapping("/list")
    public String list(Model model) {
        List<PharmacyDTO> list1 = pharmacyService.getList();
        model.addAttribute("pharmacyList", list1);
        return "pharmacy/pharmacy-list";
    }


    @GetMapping("/go-to-add")
    public String goToAdd(Model model) {
        model.addAttribute("pharmacy", new PharmacyDTO());
        model.addAttribute("isEdit", false);
        return "pharmacy/pharmacy-add";
    }


    @PostMapping("/save")
    public String save(Model model, @ModelAttribute PharmacyDTO pharmacyDTO) {
        return "redirect:/pharmacy/list";
    }

    @GetMapping("/go-to-edit/{pharmacyId}")
    public String save(Model model, @PathVariable("pharmacyId") Long pharmacyId) {
        List<PharmacyDTO> list = pharmacyService.getList();
        Optional<PharmacyDTO> optional = list.stream().filter(dto -> dto.getId().equals(pharmacyId)).findAny();
        if (optional.isEmpty()) {
            return "404";
        }
        model.addAttribute("pharmacy", optional.get());
        model.addAttribute("isEdit", true);
        return "pharmacy/pharmacy-add";
    }

    @PostMapping("/edit/{pharmacyId}")
    public String edit(Model model, @PathVariable("pharmacyId") Long pharmacyId,
                       @ModelAttribute PharmacyDTO pharmacy) {
        List<PharmacyDTO> list = pharmacyService.getList();
        Optional<PharmacyDTO> optional = list.stream().filter(dto -> dto.getId().equals(pharmacyId)).findAny();
        if (optional.isEmpty()) {
            return "404";
        }
        pharmacy.setId(pharmacyId);
        pharmacyService.update(pharmacy);

        return "redirect:/pharmacy/list";
    }


}
