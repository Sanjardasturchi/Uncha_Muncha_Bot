package com.example.Uncha_Muncha_Bot.dto;

import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
public class HomeServiceDTO {
    private Long id;
    private String city;
    private String district;
    private List<HomeServiceTypeDTO> homeServiceType;
    private LocalTime startTime;
    private LocalTime endTime;
    private String username;
    private String phone;
    private String companyBrandName;
    private String info;
    private Double latitude;
    private Double longitude;
    private ActiveStatus activeStatus;
    private LocalDateTime createdDateTime;
    private String ownerChatId;
}
