package com.example.Uncha_Muncha_Bot.dto;

import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.CarType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
public class AutomobileServiceDTO {
    private Long id;
    private String city;
    private String info;
    private List<AutomobileServiceTypeDTO> automobileServiceTypes;
    private String phone;
    private String username;
    private Double latitude;
    private Double longitude;
    private CarType carType;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime createdDateTime;
    private ActiveStatus activeStatus;
    private String ownerChatId;
}
