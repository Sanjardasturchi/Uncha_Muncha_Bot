package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.ProfileDTO;
import com.example.Uncha_Muncha_Bot.entity.ProfileEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.Language;
import com.example.Uncha_Muncha_Bot.enums.ProfileRole;
import com.example.Uncha_Muncha_Bot.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {
    @Autowired
    private ProfileRepository profileRepository;

    public ProfileDTO getByChatId(String chatId) {
        Optional<ProfileEntity> optional = profileRepository.findByChatId(chatId);
        if (optional.isEmpty()) {
            return null;
        }
        ProfileEntity entity = optional.get();
        ProfileDTO dto = new ProfileDTO(entity.getId(), entity.getUsername(), entity.getChatId());
        dto.setId(entity.getId());
        dto.setRole(entity.getRole());
        dto.setActiveStatus(entity.getActiveStatus());
        dto.setCurrentStep(entity.getCurrentStep());
        dto.setCreatedDateTime(entity.getCreatedDateTime());
        if (entity.getLanguage() != null) {
            dto.setLanguage(entity.getLanguage());
        }
        if (entity.getLatitude() != null) {
            dto.setLatitude(entity.getLatitude());
            dto.setLongitude(entity.getLongitude());
        }
        if (entity.getChangingElementId() != null) {
            dto.setChangingElementId(entity.getChangingElementId());
        }
        if (entity.getName() != null) {
            dto.setName(entity.getName());
        }
        if (entity.getSurname() != null) {
            dto.setSurname(entity.getSurname());
        }
        if (entity.getUsername() != null) {
            dto.setUsername(entity.getUsername());
        }
        if (entity.getPhone() != null) {
            dto.setPhone(entity.getPhone());
        }
        if (entity.getSelectedPurchaseType() != null) {
            dto.setSelectedPurchaseType(entity.getSelectedPurchaseType());
        }
        return dto;
    }

    public ProfileDTO save(ProfileDTO profile) {
        ProfileEntity entity = new ProfileEntity();
        entity.setChatId(profile.getChatId());
        entity.setUsername(profile.getUsername());
        entity.setRole(profile.getRole());
        entity.setActiveStatus(profile.getActiveStatus());
        entity.setCurrentStep(profile.getCurrentStep());
        entity.setCreatedDateTime(profile.getCreatedDateTime());
        profileRepository.save(entity);
        return profile;
    }

    public void changeLanguage(String chatId, Language language) {
        profileRepository.changeLanguage(chatId, language);
    }

    public ProfileDTO saveContact(ProfileDTO profile, String chatId) {
        Optional<ProfileEntity> optional = profileRepository.findByChatId(chatId);
        if (optional.isPresent()) {
            ProfileEntity entity = optional.get();
            entity.setActiveStatus(profile.getActiveStatus());
            entity.setName(profile.getName());
            entity.setSurname(profile.getSurname());
            entity.setPhone(profile.getPhone());
            profileRepository.save(entity);
            return profile;
        }
        return null;
    }

    public void changeStep(String chatId, String step) {
        profileRepository.changeStep(step, chatId);
    }

    public ProfileDTO getSuperAdmin() {
        Iterable<ProfileEntity> iterable = profileRepository.getSuperAdmin();
        for (ProfileEntity entity : iterable) {
            return toDTO(entity);
        }
        return null;
    }

    private static ProfileDTO toDTO(ProfileEntity entity) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(entity.getId());
        if (entity.getName() != null) {
            profileDTO.setName(entity.getName());
        }
        if (entity.getSurname() != null) {
            profileDTO.setSurname(entity.getSurname());
        }
        if (entity.getUsername() != null) {
            profileDTO.setUsername(entity.getUsername());
        }
        if (entity.getChangingElementId() != null) {
            profileDTO.setChangingElementId(entity.getChangingElementId());
        }
        profileDTO.setPhone(entity.getPhone());
        profileDTO.setLatitude(entity.getLatitude());
        profileDTO.setLongitude(entity.getLongitude());
        profileDTO.setSelectedPurchaseType(entity.getSelectedPurchaseType());
        profileDTO.setLanguage(entity.getLanguage());
        profileDTO.setCreatedDateTime(entity.getCreatedDateTime());
        profileDTO.setCurrentStep(entity.getCurrentStep());
        profileDTO.setRole(entity.getRole());
        profileDTO.setActiveStatus(entity.getActiveStatus());
        profileDTO.setChatId(entity.getChatId());
        return profileDTO;
    }

    public List<ProfileDTO> getOwnersList() {
        Iterable<ProfileEntity> ownersList = profileRepository.getOwnersList();
        return toDTOList(ownersList);
    }

    private List<ProfileDTO> toDTOList(Iterable<ProfileEntity> ownersList) {
        List<ProfileDTO> dtoList = new LinkedList<>();
        for (ProfileEntity entity : ownersList) {
            dtoList.add(toDTO(entity));
        }
        return dtoList;
    }

    public Long getCount() {
        return profileRepository.getCount();
    }

    public List<ProfileDTO> getAdminList() {
        List<ProfileDTO> dtoList = new LinkedList<>();
        for (ProfileEntity entity : profileRepository.getAdminList()) {
            dtoList.add(toDTO(entity));
        }
        return dtoList;
    }

    public void updateUsername(String chatId, String userName) {
        profileRepository.updateUsername(userName, chatId);
    }

    public List<ProfileDTO> getAllByRole(List<ProfileRole> roles, List<Language> languages) {
        Iterable<ProfileEntity> all = profileRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDateTime"));
        List<ProfileDTO> dtoList = new LinkedList<>();
        for (ProfileEntity entity : all) {
            if (entity.getLanguage()!=null&&languages.contains(entity.getLanguage())) {
                if (roles.contains(entity.getRole())) {
                    dtoList.add(toDTO(entity));
                }
            }
        }
        return dtoList;
    }

    public void changeRole(String chatId, ProfileRole profileRole) {
        profileRepository.changeRole(chatId, profileRole);
    }

    public void changeStatus(String chatId, ActiveStatus activeStatus) {
        profileRepository.changeStatus(chatId, activeStatus);
    }

    public void changeChangingElementId(String chatId, Long advertisingId) {
        profileRepository.changeChangingElementId(chatId, advertisingId);
    }
}
