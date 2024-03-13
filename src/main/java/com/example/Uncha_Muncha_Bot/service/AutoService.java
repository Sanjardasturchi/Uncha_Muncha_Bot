package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.AutomobileDTO;
import com.example.Uncha_Muncha_Bot.entity.AutomobileEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.CarType;
import com.example.Uncha_Muncha_Bot.enums.SalaryType;
import com.example.Uncha_Muncha_Bot.repository.AutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class AutoService {
    @Autowired
    private AutoRepository autoRepository;

    public Long createAuto(String chatId) {
        AutomobileEntity entity = new AutomobileEntity();
        entity.setActiveStatus(ActiveStatus.BLOCK);
        entity.setCreatedDateTime(LocalDateTime.now());
        entity.setOwnerChatId(chatId);
        autoRepository.save(entity);
        return entity.getId();
    }

    public void setSaleType(String saleType, Long autoId) {
        autoRepository.setSaleType(SalaryType.valueOf(saleType), autoId);
    }

    public void setCarType(String carType, Long autoId) {
        autoRepository.setCarType(CarType.valueOf(carType), autoId);
    }

    public void setBrandName(String brandName, Long autoId) {
        autoRepository.setBrandName(brandName, autoId);
    }

    public void setCity(String city, Long autoId) {
        autoRepository.setCity(city, autoId);
    }

    public void setDistrict(String district, Long autoId) {
        autoRepository.setDistrict(district, autoId);
    }

    public void setLocation(Location location, Long autoId) {
        autoRepository.setLocation(location.getLatitude(), location.getLongitude(), autoId);
    }

    public void changeStatus(ActiveStatus activeStatus, Long autoId) {
        autoRepository.changeStatus(activeStatus, autoId);
    }

    public void setInfoUz(String infoUz, Long autoId) {
        autoRepository.setInfoUz(infoUz, autoId);
    }

    public void setInfoTr(String infoTr, Long autoId) {
        autoRepository.setInfoTr(infoTr, autoId);
    }

    public void setInfoRu(String infoRu, Long autoId) {
        autoRepository.setInfoRu(infoRu, autoId);
    }

    public void setInfoEn(String infoEn, Long autoId) {
        autoRepository.setInfoEn(infoEn, autoId);
    }

    public void setModelName(String model, Long autoId) {
        autoRepository.setModelName(model, autoId);
    }

    public void setPrice(String price, Long autoId) {
        autoRepository.setPrice(Double.valueOf(price), autoId);
    }

    public void setPhone(String phone, Long autoId) {
        autoRepository.setPhone(phone, autoId);
    }

    public void setUsername(String username, Long autoId) {
        autoRepository.setUsername(username, autoId);
    }

    public void setStartTime(String time, Long autoId) {
        autoRepository.setStartTime(LocalTime.parse(time), autoId);
    }

    public void setEndTime(String time, Long autoId) {
        autoRepository.setEndTime(LocalTime.parse(time), autoId);
    }

    public List<AutomobileDTO> getAll() {
        return toDTOList(autoRepository.findAll());
    }

    private List<AutomobileDTO> toDTOList(Iterable<AutomobileEntity> all) {
        List<AutomobileDTO> dtoList = new LinkedList<>();
        for (AutomobileEntity entity : all) {
            AutomobileDTO dto = toDTO(entity);
            if (dto != null) {
                dtoList.add(dto);
            }
        }
        return dtoList;
    }

    private AutomobileDTO toDTO(AutomobileEntity entity) {
        try {
            AutomobileDTO dto = new AutomobileDTO();
            dto.setId(entity.getId());
            dto.setUsername(entity.getUsername());
            dto.setCity(entity.getCity());
            dto.setDistrict(entity.getDistrict());
            dto.setInfoUz(entity.getInfoUz());
            dto.setInfoTr(entity.getInfoTr());
            dto.setInfoRu(entity.getInfoRu());
            dto.setInfoEn(entity.getInfoEn());
            dto.setActiveStatus(entity.getActiveStatus());
            dto.setBrandName(entity.getBrandName());
            dto.setModel(entity.getModel());
            dto.setLatitude(entity.getLatitude());
            dto.setLongitude(entity.getLongitude());
            dto.setCarType(entity.getCarType());
            dto.setCreatedDateTime(entity.getCreatedDateTime());
            dto.setStartTime(entity.getStartTime());
            dto.setEndTime(entity.getEndTime());
            dto.setOwnerChatId(entity.getOwnerChatId());
            dto.setPhone(entity.getPhone());
            dto.setPrice(entity.getPrice());
            dto.setSalaryType(entity.getSalaryType());
            return dto;
        } catch (Exception e) {
            return null;
        }
    }

    public AutomobileDTO getById(Long autoId) {
        Optional<AutomobileEntity> byId = autoRepository.findById(autoId);
        return byId.map(this::toDTO).orElse(null);
    }
}
