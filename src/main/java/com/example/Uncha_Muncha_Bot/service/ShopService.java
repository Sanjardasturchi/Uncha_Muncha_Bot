package com.example.Uncha_Muncha_Bot.service;

import com.example.Uncha_Muncha_Bot.dto.ShopDTO;
import com.example.Uncha_Muncha_Bot.dto.ShopTypeDTO;
import com.example.Uncha_Muncha_Bot.entity.ShopEntity;
import com.example.Uncha_Muncha_Bot.entity.ShopTypeEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.repository.ShopRepository;
import com.example.Uncha_Muncha_Bot.repository.ShopTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Location;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class ShopService {
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private ShopTypeRepository shopTypeRepository;

    public Long createShop(String chatId) {
        ShopEntity entity=new ShopEntity();
        entity.setOwnerChatId(chatId);
        shopRepository.save(entity);
        return entity.getId();
    }

    public void setShopType(String shopType,Long shopId) {
        ShopTypeEntity entity=new ShopTypeEntity();
        entity.setShopId(shopId);
        entity.setShopType(shopType);
        shopTypeRepository.save(entity);
    }

    public void setStartTime(String time, Long shopId) {
        shopRepository.setStartTime(LocalTime.parse(time),shopId);
    }

    public void setEndTime(String time, Long shopId) {
        shopRepository.setEndTime(LocalTime.parse(time),shopId);
    }

    public void setUsername(String username, Long shopId) {
        shopRepository.setUsername(username,shopId);
    }

    public void setCity(String city, Long shopId) {
        shopRepository.setCity(city,shopId);
    }

    public void setDistrict(String district, Long shopId) {
        shopRepository.setDistrict(district,shopId);
    }

    public void setPhone(String phone, Long shopId) {
        shopRepository.setPhone(phone,shopId);
    }

    public void setBrand(String brand, Long shopId) {
        shopRepository.setBrand(brand,shopId);
    }

    public void setInfoUz(String info, Long shopId) {
        shopRepository.setInfoUz(info,shopId);
    }

    public void setInfoTr(String info, Long shopId) {
        shopRepository.setInfoTr(info,shopId);
    }

    public void setInfoRu(String info, Long shopId) {
        shopRepository.setInfoRu(info,shopId);
    }

    public void setInfoEn(String info, Long shopId) {
        shopRepository.setInfoEn(info,shopId);
    }

    public void setLocation(Location location, Long shopId) {
        shopRepository.setLocation(location.getLatitude(),location.getLongitude(),shopId);
    }

    public ShopDTO getById(Long shopId) {
        return shopRepository.findById(shopId).map(this::toDTO).orElse(null);
    }

    private ShopDTO toDTO(ShopEntity entity) {
        ShopDTO dto=new ShopDTO();
        dto.setId(entity.getId());
        dto.setCity(entity.getCity());
        dto.setDistrict(entity.getDistrict());
        dto.setBrand(entity.getBrand());
        dto.setActiveStatus(entity.getActiveStatus());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setPhone(entity.getPhone());
        dto.setInfoUz(entity.getInfoUz());
        dto.setInfoTr(entity.getInfoTr());
        dto.setInfoRu(entity.getInfoRu());
        dto.setInfoEn(entity.getInfoEn());
        dto.setUsername(entity.getUsername());
        dto.setCreatedDateTime(entity.getCreatedDateTime());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setOwnerChatId(entity.getOwnerChatId());
        List <ShopTypeDTO> shopTypes=new LinkedList<>();
        Iterable<ShopTypeEntity> shopTypeEntities = shopTypeRepository.findByOwnerId(entity.getId());
        for (ShopTypeEntity shopTypeEntity : shopTypeEntities) {
            ShopTypeDTO typeDTO=new ShopTypeDTO();
            typeDTO.setId(shopTypeEntity.getId());
            typeDTO.setShopId(shopTypeEntity.getShopId());
            typeDTO.setShopType(shopTypeEntity.getShopType());
            shopTypes.add(typeDTO);
        }
        dto.setShopTypes(shopTypes);
        return dto;
    }

    public void changeStatus(ActiveStatus status, Long shopId) {
        shopRepository.changeStatus(status,shopId);
    }

    public List<ShopDTO> getAll() {
        Iterable<ShopEntity> all = shopRepository.findAll();
        List<ShopDTO> dtos=new LinkedList<>();
        for (ShopEntity shopEntity : all) {
            dtos.add(toDTO(shopEntity));
        }
        return dtos;
    }
}
