package com.example.Uncha_Muncha_Bot.dto;

import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
public class ShopDTO {
    private Long id;
    private List <ShopTypeDTO> shopTypes;
    private String city;
    private String district;
    private String info;
    private LocalTime startTime;
    private LocalTime endTime;
    private String phone;
    private String username;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdDateTime;
    private ActiveStatus activeStatus;
    private String ownerChatId;
}
