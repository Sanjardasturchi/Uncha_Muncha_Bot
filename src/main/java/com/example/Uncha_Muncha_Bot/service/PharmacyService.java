package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.PharmacyDTO;
import com.example.Uncha_Muncha_Bot.entity.PharmacyEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.PharmacyType;
import com.example.Uncha_Muncha_Bot.mapper.PharmacyMapper;
import com.example.Uncha_Muncha_Bot.repository.PharmacyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

@Service
public class PharmacyService {

    @Autowired
    private PharmacyRepository pharmacyRepository;

    //===============USER==============


    //===============ADMIN==============
    public Long save(PharmacyDTO pharmacy) {
        PharmacyEntity pharmacyEntity = new PharmacyEntity();
        pharmacyEntity.setPharmacyType(pharmacy.getPharmacyType());
        pharmacyEntity.setOwnerChatId(pharmacy.getOwnerChatId());
        pharmacyRepository.save(pharmacyEntity);
        return pharmacyEntity.getId();
    }

    public void setStartTime(LocalTime startTime, Long pharmacyId) {
        pharmacyRepository.setStartTime(startTime, pharmacyId);
    }

    public void setEndTime(LocalTime endTime, Long pharmacyId) {
        pharmacyRepository.setEndTime(endTime, pharmacyId);
    }

    public void setUsername(String username, Long pharmacyId) {
        pharmacyRepository.setUsername(username, pharmacyId);
    }

    public void setPharmacyPhone(String phone, Long pharmacyId) {
        pharmacyRepository.setPharmacyPhone(phone, pharmacyId);
    }

    public void setPharmacyName(String pharmacyName, Long pharmacyId) {
        pharmacyRepository.setPharmacyName(pharmacyName, pharmacyId);
    }

    public void setUzInfo(String uzInfo, Long pharmacyId) {
        pharmacyRepository.setUzInfo(uzInfo, pharmacyId);
    }

    public void setTrInfo(String trInfo, Long pharmacyId) {
        pharmacyRepository.setTrInfo(trInfo, pharmacyId);
    }

    public void setRuInfo(String ruInfo, Long pharmacyId) {
        pharmacyRepository.setRuInfo(ruInfo, pharmacyId);
    }

    public void setEnInfo(String enInfo, Long pharmacyId) {
        pharmacyRepository.setEnInfo(enInfo, pharmacyId);
    }

    public void setLocation(Location location, Long pharmacyId) {
        pharmacyRepository.setLocation(location.getLatitude(), location.getLongitude(), pharmacyId);
    }

    public void markAsDone(Long pharmacyId) {
        pharmacyRepository.markAsDone(ActiveStatus.ACTIVE, pharmacyId);
    }

    public List<PharmacyDTO> getAll() {
        List<PharmacyDTO> dtoList = new LinkedList<>();
        Iterable<PharmacyEntity> all = pharmacyRepository.findAll();
        for (PharmacyEntity entity : all) {
            dtoList.add(toDTO(entity));
        }
        return dtoList;
    }

    private PharmacyDTO toDTO(PharmacyEntity entity) {
        PharmacyDTO dto = new PharmacyDTO();
        dto.setId(entity.getId());
        dto.setPharmacyType(entity.getPharmacyType());
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
        if (entity.getPharmacyName() != null) {
            dto.setPharmacyName(entity.getPharmacyName());
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
        if (entity.getLatitude() != null) {
            dto.setLatitude(entity.getLatitude());
        }
        if (entity.getLongitude() != null) {
            dto.setLongitude(entity.getLongitude());
        }
        return dto;
    }

    public PharmacyDTO findById(Long pharmacyId) {
        return pharmacyRepository.findById(pharmacyId).map(this::toDTO).orElse(null);
    }

    public void changeStatus(ActiveStatus status, Long pharmacyId) {
        pharmacyRepository.changeStatus(status,pharmacyId);
    }

    public List<PharmacyMapper> get10pharmacy(Double latitude, Double longitude, PharmacyType pharmacy) {
        return pharmacyRepository.get10pharmacy(latitude, longitude,pharmacy);
    }
}
