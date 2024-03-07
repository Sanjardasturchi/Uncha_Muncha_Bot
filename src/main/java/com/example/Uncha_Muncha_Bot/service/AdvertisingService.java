package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.AdvertisingDTO;
import com.example.Uncha_Muncha_Bot.entity.AdvertisingEntity;
import com.example.Uncha_Muncha_Bot.repository.AdvertisingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdvertisingService {
    @Autowired
    private AdvertisingRepository advertisingRepository;

    public Long create(String chatId, String text) {
        AdvertisingEntity advertisingEntity = new AdvertisingEntity();
        advertisingEntity.setText(text);
        advertisingEntity.setOwnerChatId(chatId);
        advertisingRepository.save(advertisingEntity);
        return advertisingEntity.getId();
    }

    public AdvertisingDTO getById(Long advertisingId) {
        Optional<AdvertisingEntity> optional = advertisingRepository.findById(advertisingId);
        return optional.map(this::toDTO).orElse(null);
    }

    private AdvertisingDTO toDTO(AdvertisingEntity entity) {
        AdvertisingDTO dto = new AdvertisingDTO();
        dto.setText(entity.getText());
        dto.setOwnerChatId(entity.getOwnerChatId());
        dto.setId(entity.getId());
        dto.setCreatedDate(entity.getCreatedDate());
        if (entity.getSharedCount() != null) {
            dto.setSharedCount(entity.getSharedCount());
        }
        return dto;
    }

    public void setSharedCount(int count, Long advertisingId) {
        advertisingRepository.setSharedCount(count,advertisingId);
    }
}
