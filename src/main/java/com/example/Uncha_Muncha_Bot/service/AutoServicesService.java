package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.AutomobileServiceTypeDTO;
import com.example.Uncha_Muncha_Bot.entity.AutomobileServiceEntity;
import com.example.Uncha_Muncha_Bot.entity.AutomobileServiceTypeEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.repository.AutoServiceTypeRepository;
import com.example.Uncha_Muncha_Bot.repository.AutoServicesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Service
public class AutoServicesService {
    @Autowired
    private AutoServicesRepository autoServicesRepository;
    @Autowired
    private AutoServiceTypeRepository autoServiceTypeRepository;

    public Long createService(String chatId) {
        AutomobileServiceEntity entity=new AutomobileServiceEntity();
        entity.setActiveStatus(ActiveStatus.BLOCK);
        entity.setCreatedDateTime(LocalDateTime.now());
        entity.setOwnerChatId(chatId);
        autoServicesRepository.save(entity);
        return entity.getId();
    }

    public void checkServiceType(String serviceType, Long serviceId) {
        for (AutomobileServiceTypeEntity typeEntity : autoServiceTypeRepository.findByServiceId(serviceId)) {
            if (typeEntity.getServiceName().equals(serviceType)) {
                autoServiceTypeRepository.deleteById(typeEntity.getId());
                return;
            }
        }
        AutomobileServiceTypeEntity entity = new AutomobileServiceTypeEntity();
        entity.setAutoServiceId(serviceId);
        entity.setServiceName(serviceType);
        autoServiceTypeRepository.save(entity);
    }

    public List<AutomobileServiceTypeDTO> getAll() {
        List<AutomobileServiceTypeDTO> dtoList=new LinkedList<>();
        Iterable<AutomobileServiceTypeEntity> all = autoServiceTypeRepository.findAll();
        for (AutomobileServiceTypeEntity entity : all) {
            AutomobileServiceTypeDTO dto=new AutomobileServiceTypeDTO();
            dto.setId(entity.getId());
            dto.setAutoServiceId(entity.getAutoServiceId());
            dto.setServiceName(entity.getServiceName());
            dtoList.add(dto);
        }
        return dtoList;
    }

    public List<AutomobileServiceTypeDTO> getAllById(Long serviceId) {
        List<AutomobileServiceTypeDTO> dtoList=new LinkedList<>();
        Iterable<AutomobileServiceTypeEntity> all = autoServiceTypeRepository.findByServiceId(serviceId);
        for (AutomobileServiceTypeEntity entity : all) {
            AutomobileServiceTypeDTO dto=new AutomobileServiceTypeDTO();
            dto.setId(entity.getId());
            dto.setAutoServiceId(entity.getAutoServiceId());
            dto.setServiceName(entity.getServiceName());
            dtoList.add(dto);
        }
        return dtoList;
    }
}
