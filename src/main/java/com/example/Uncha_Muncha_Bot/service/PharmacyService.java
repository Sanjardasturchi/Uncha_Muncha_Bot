package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.PharmacyDTO;
import com.example.Uncha_Muncha_Bot.entity.PharmacyEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.repository.PharmacyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.time.LocalTime;

@Service
public class PharmacyService {

    @Autowired
    private PharmacyRepository pharmacyRepository;

    //===============USER==============


    //===============ADMIN==============
    public Long save(PharmacyDTO pharmacy) {
        PharmacyEntity pharmacyEntity=new PharmacyEntity();
        pharmacyEntity.setPharmacyType(pharmacy.getPharmacyType());
        pharmacyEntity.setOwnerChatId(pharmacy.getOwnerChatId());
        pharmacyRepository.save(pharmacyEntity);
        return pharmacyEntity.getId();
    }

    public void setStartTime(LocalTime startTime, Long pharmacyId) {
        pharmacyRepository.setStartTime(startTime,pharmacyId);
    }

    public void setEndTime(LocalTime endTime, Long pharmacyId) {
        pharmacyRepository.setEndTime(endTime,pharmacyId);
    }

    public void setUsername(String username, Long pharmacyId) {
        pharmacyRepository.setUsername(username,pharmacyId);
    }

    public void setPharmacyPhone(String phone, Long pharmacyId) {
        pharmacyRepository.setPharmacyPhone(phone,pharmacyId);
    }

    public void setPharmacyName(String pharmacyName, Long pharmacyId) {
        pharmacyRepository.setPharmacyName(pharmacyName,pharmacyId);
    }

    public void setUzInfo(String uzInfo, Long pharmacyId) {
        pharmacyRepository.setUzInfo(uzInfo,pharmacyId);
    }

    public void setTrInfo(String trInfo, Long pharmacyId) {
        pharmacyRepository.setTrInfo(trInfo,pharmacyId);
    }

    public void setRuInfo(String ruInfo, Long pharmacyId) {
        pharmacyRepository.setRuInfo(ruInfo,pharmacyId);
    }

    public void setEnInfo(String enInfo, Long pharmacyId) {
        pharmacyRepository.setEnInfo(enInfo,pharmacyId);
    }

    public void setLocation(Location location, Long pharmacyId) {
        pharmacyRepository.setLocation(location.getLatitude(),location.getLongitude(),pharmacyId);
    }

    public void markAsDone(Long pharmacyId) {
        pharmacyRepository.markAsDone(ActiveStatus.ACTIVE,pharmacyId);
    }
}
