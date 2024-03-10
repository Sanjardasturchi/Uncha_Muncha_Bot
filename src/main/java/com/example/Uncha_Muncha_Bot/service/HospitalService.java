package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.HospitalDTO;
import com.example.Uncha_Muncha_Bot.dto.HospitalServiceDTO;
import com.example.Uncha_Muncha_Bot.dto.PharmacyDTO;
import com.example.Uncha_Muncha_Bot.entity.HospitalEntity;
import com.example.Uncha_Muncha_Bot.entity.HospitalServiceEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private HospitalServicesService hospitalServicesService;

    public Long create(String chatId) {
        HospitalEntity entity=new HospitalEntity();
        entity.setActiveStatus(ActiveStatus.BLOCK);
        entity.setCreatedDateTime(LocalDateTime.now());
        entity.setOwnerChatId(chatId);
        hospitalRepository.save(entity);
        return entity.getId();
    }

    public void setStartTime(LocalTime time, Long hospitalId) {
        hospitalRepository.setStartTime(time,hospitalId);
    }

    public void setEndTime(LocalTime time, Long hospitalId) {
        hospitalRepository.setEndTime(time,hospitalId);
    }

    public void setUserName(String username, Long hospitalId) {
        hospitalRepository.setUsername(username,hospitalId);
    }

    public void setPhone(String phone, Long hospitalId) {
        hospitalRepository.setPhone(phone,hospitalId);
    }

    public void setName(String name, Long hospitalId) {
        hospitalRepository.setName(name,hospitalId);
    }

    public void setInfoUz(String infoUz, Long hospitalId) {
        hospitalRepository.setInfoUz(infoUz,hospitalId);
    }

    public void setInfoTr(String infoTr, Long hospitalId) {
        hospitalRepository.setInfoTr(infoTr,hospitalId);
    }

    public void setInfoRu(String infoRu, Long hospitalId) {
        hospitalRepository.setInfoRu(infoRu,hospitalId);
    }

    public void setInfoEn(String infoEn, Long hospitalId) {
        hospitalRepository.setInfoEn(infoEn,hospitalId);
    }

    public void setLocation(Location location, Long id) {
        hospitalRepository.setLocation(location.getLatitude(),location.getLongitude(),id);
    }

    public List<HospitalDTO> getAll() {
        List<HospitalDTO> dtoList=new LinkedList<>();
        Iterable<HospitalEntity> all = hospitalRepository.findAll();
        for (HospitalEntity entity : all) {
            dtoList.add(toDTO(entity));
        }
        return dtoList;
    }

    private HospitalDTO toDTO(HospitalEntity entity) {
        HospitalDTO dto = new HospitalDTO();
        dto.setId(entity.getId());
        dto.setActiveStatus(entity.getActiveStatus());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setPhone(entity.getPhone());
        dto.setInfoUz(entity.getInfoUz());
        dto.setInfoTr(entity.getInfoTr());
        dto.setInfoRu(entity.getInfoRu());
        dto.setInfoEn(entity.getInfoEn());
        dto.setUsername(entity.getUsername());
        dto.setPhone(entity.getPhone());
        dto.setCreatedDateTime(entity.getCreatedDateTime());
        dto.setHospitalName(entity.getHospitalName());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setOwnerChatId(entity.getOwnerChatId());
        List<HospitalServiceDTO> dtoList=new LinkedList<>();
        for (HospitalServiceEntity hospitalServiceEntity : hospitalServicesService.getByHospitalId(entity.getId())) {
            HospitalServiceDTO dto1=new HospitalServiceDTO();
            dto1.setId(hospitalServiceEntity.getId());
            dto1.setHospitalId(hospitalServiceEntity.getHospitalId());
            dto1.setServiceName(hospitalServiceEntity.getServiceName());
            dtoList.add(dto1);
        }
        dto.setHospitalService(dtoList);
        return dto;
    }

    public HospitalDTO getById(Long hospitalId) {
        return hospitalRepository.findById(hospitalId).map(this::toDTO).orElse(null);
    }

    public void changeStatus(ActiveStatus status, Long hospitalId) {
        hospitalRepository.changeStatus(status,hospitalId);
    }
}
