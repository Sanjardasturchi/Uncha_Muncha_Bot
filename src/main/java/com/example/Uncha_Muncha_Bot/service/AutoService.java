package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.entity.AutomobileEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.CarType;
import com.example.Uncha_Muncha_Bot.enums.SalaryType;
import com.example.Uncha_Muncha_Bot.repository.AutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.time.LocalDateTime;

@Service
public class AutoService {
    @Autowired
    private AutoRepository autoRepository;
    public Long createAuto(String chatId) {
        AutomobileEntity entity=new AutomobileEntity();
        entity.setActiveStatus(ActiveStatus.BLOCK);
        entity.setCreatedDateTime(LocalDateTime.now());
        entity.setOwnerChatId(chatId);
        autoRepository.save(entity);
        return entity.getId();
    }

    public void setSaleType(String saleType, Long autoId) {
        autoRepository.setSaleType(SalaryType.valueOf(saleType),autoId);
    }

    public void setCarType(String carType, Long autoId) {
        autoRepository.setCarType(CarType.valueOf(carType),autoId);
    }

    public void setBrandName(String brandName, Long autoId) {
        autoRepository.setBrandName(brandName,autoId);
    }

    public void setCity(String city, Long autoId) {
        autoRepository.setCity(city,autoId);
    }

    public void setDistrict(String district, Long autoId) {
        autoRepository.setDistrict(district,autoId);
    }

    public void setLocation(Location location, Long autoId) {
        autoRepository.setLocation(location.getLatitude(),location.getLongitude(),autoId);
    }

    public void changeStatus(ActiveStatus activeStatus, Long autoId) {
        autoRepository.changeStatus(activeStatus,autoId);
    }

    public void setInfoUz(String infoUz, Long autoId) {
        autoRepository.setInfoUz(infoUz,autoId);
    }

    public void setInfoTr(String infoTr, Long autoId) {
        autoRepository.setInfoTr(infoTr,autoId);
    }

    public void setInfoRu(String infoRu, Long autoId) {
        autoRepository.setInfoRu(infoRu,autoId);
    }

    public void setInfoEn(String infoEn, Long autoId) {
        autoRepository.setInfoEn(infoEn,autoId);
    }

    public void setModelName(String model, Long autoId) {
        autoRepository.setModelName(model,autoId);
    }

    public void setPrice(String price, Long autoId) {
        autoRepository.setPrice(Double.valueOf(price),autoId);
    }

    public void setPhone(String phone, Long autoId) {
        autoRepository.setPhone(phone,autoId);
    }

    public void setUsername(String username, Long autoId) {
        autoRepository.setUsername(username,autoId);
    }
}
