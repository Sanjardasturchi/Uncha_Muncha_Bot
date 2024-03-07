package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.MediaDTO;
import com.example.Uncha_Muncha_Bot.entity.MediaEntity;
import com.example.Uncha_Muncha_Bot.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class MediaService {
    @Autowired
    private MediaRepository mediaRepository;

    public List<MediaDTO> getByOwnerId(Long ownerId) {
        List<MediaDTO> dtoList = new LinkedList<>();
        Iterable<MediaEntity> byOwnerId = mediaRepository.getByOwnerId(ownerId);
        for (MediaEntity mediaEntity : byOwnerId) {
            dtoList.add(toDTO(mediaEntity));
        }
        return dtoList;
    }

    private MediaDTO toDTO(MediaEntity entity) {
        MediaDTO dto = new MediaDTO();
        dto.setId(entity.getId());
        dto.setMediaType(entity.getMediaType());
        dto.setFId(entity.getFId());
        dto.setOwnerId(entity.getOwnerId());
        if (entity.getMediaUrl() != null) {
            dto.setMediaUrl(entity.getMediaUrl());
        }
        return dto;
    }

    public void save(MediaDTO media) {
        MediaEntity entity = new MediaEntity();
        entity.setFId(media.getFId());
        entity.setMediaType(media.getMediaType());
        entity.setOwnerId(media.getOwnerId());
        mediaRepository.save(entity);
    }

    public void deleteByOwnerId(Long changingElementId) {
        mediaRepository.deleteByOwnerId(changingElementId);
    }

    public MediaDTO getByFId(String fId) {
        Optional<MediaEntity> optional=mediaRepository.findByFId(fId);
        return optional.map(this::toDTO).orElse(null);
    }
}
