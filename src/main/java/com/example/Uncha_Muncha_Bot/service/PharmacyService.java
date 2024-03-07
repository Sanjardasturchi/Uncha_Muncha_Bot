package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.PharmacyDTO;
import com.example.Uncha_Muncha_Bot.entity.PharmacyEntity;
import com.example.Uncha_Muncha_Bot.repository.PharmacyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class PharmacyService {

    @Autowired
    private PharmacyRepository pharmacyRepository;

    public Long save(PharmacyDTO pharmacy) {
        PharmacyEntity pharmacyEntity=new PharmacyEntity();
        pharmacyEntity.setPharmacyType(pharmacy.getPharmacyType());
        pharmacyEntity.setOwnerChatId(pharmacy.getOwnerChatId());
        pharmacyRepository.save(pharmacyEntity);
        return pharmacyEntity.getId();
    }

    public void setStartTime(LocalTime startTime, Long pharmacyId) {

    }

    public void setEndTime(LocalTime endTime, Long pharmacyId) {

    }
    //===============USER==============


    //===============ADMIN==============
}
