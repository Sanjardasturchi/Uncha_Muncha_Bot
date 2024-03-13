package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.AutoSalonDTO;
import com.example.Uncha_Muncha_Bot.entity.AutoSalonEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.repository.AutoSalonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class AutoSalonService {
    @Autowired
    private AutoSalonRepository autoSalonRepository;

    public void changeStatus(ActiveStatus status, Long autoSalonId) {
        autoSalonRepository.changeStatus(status, autoSalonId);
    }

    public List<AutoSalonDTO> getAll() {
        return toDTOList(autoSalonRepository.findAll());
    }

    private List<AutoSalonDTO> toDTOList(Iterable<AutoSalonEntity> all) {
        List<AutoSalonDTO> dtoList = new LinkedList<>();
        for (AutoSalonEntity autoSalonEntity : all) {
            AutoSalonDTO dto = toDTO(autoSalonEntity);
            if (dto != null) {
                dtoList.add(dto);
            }
        }
        return dtoList;
    }

    private AutoSalonDTO toDTO(AutoSalonEntity entity) {
        if (entity.getLatitude() != null) {
            AutoSalonDTO dto = new AutoSalonDTO();
            dto.setId(entity.getId());
            dto.setActiveStatus(entity.getActiveStatus());
            dto.setCreatedDateTime(entity.getCreatedDateTime());
            dto.setOwnerChatId(entity.getOwnerChatId());

            if (entity.getStartTime() != null) {
                dto.setStartTime(entity.getStartTime());
            }
            if (entity.getEndTime() != null) {
                dto.setEndTime(entity.getEndTime());
            }
            if (entity.getUsername() != null) {
                dto.setUsername(entity.getUsername());
            }
            if (entity.getStartTime() != null) {
                dto.setStartTime(entity.getStartTime());
            }
            if (entity.getPhone() != null) {
                dto.setPhone(entity.getPhone());
            }
            if (entity.getInfoUz() != null) {
                dto.setInfoUz(entity.getInfoUz());
            }
            if (entity.getInfoTr() != null) {
                dto.setInfoTr(entity.getInfoTr());
            }
            if (entity.getInfoRu() != null) {
                dto.setInfoRu(entity.getInfoRu());
            }
            if (entity.getInfoEn() != null) {
                dto.setInfoEn(entity.getInfoEn());
            }
            dto.setLatitude(entity.getLatitude());
            if (entity.getLongitude() != null) {
                dto.setLongitude(entity.getLongitude());
            }
            return dto;
        }
        return null;
    }

    public AutoSalonDTO getById(Long autoSalonId) {
        Optional<AutoSalonEntity> optional = autoSalonRepository.findById(autoSalonId);
        return optional.map(this::toDTO).orElse(null);
    }

    public Long createSalon(String chatId) {
        AutoSalonEntity entity=new AutoSalonEntity();
        entity.setActiveStatus(ActiveStatus.BLOCK);
        entity.setCreatedDateTime(LocalDateTime.now());
        entity.setOwnerChatId(chatId);
        autoSalonRepository.save(entity);
        return entity.getId();
    }

    public void setStartTime(String time, Long autoSalonId) {
        autoSalonRepository.setStartTime(LocalTime.parse(time),autoSalonId);
    }

    public void setEndTime(String time, Long autoSalonId) {
        autoSalonRepository.setEndTime(LocalTime.parse(time),autoSalonId);
    }

    public void setUsername(String username, Long autoSalonId) {
        autoSalonRepository.setUsername(username,autoSalonId);
    }

    public void setPhone(String phone, Long autoSalonId) {
        autoSalonRepository.setPhone(phone,autoSalonId);
    }

    public void setBrand(String brandName, Long autoSalonId) {
        autoSalonRepository.setBrand(brandName,autoSalonId);
    }

    public void setInfoUz(String info, Long autoSalonId) {
        autoSalonRepository.setInfoUz(info,autoSalonId);
    }
    public void setInfoTr(String info, Long autoSalonId) {
        autoSalonRepository.setInfoTr(info,autoSalonId);
    }
    public void setInfoRu(String info, Long autoSalonId) {
        autoSalonRepository.setInfoRu(info,autoSalonId);
    }
    public void setInfoEn(String info, Long autoSalonId) {
        autoSalonRepository.setInfoEn(info,autoSalonId);
    }

    public void setLocation(Location location, Long autoSalonId) {
        autoSalonRepository.setLocation(location.getLatitude(),location.getLongitude(),autoSalonId);
    }
}
