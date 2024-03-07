package com.example.Uncha_Muncha_Bot.dto;

import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
public class HospitalDTO {
    private Long id;
    private List<HospitalServiceDTO> hospitalService;
    private LocalTime startTime;
    private LocalTime endTime;
    private String username;
    private String phone;
    private String hospitalName;
    private String info;
    private Double latitude;
    private Double longitude;
    private ActiveStatus activeStatus;
    private LocalDateTime createdDateTime;
    private String ownerChatId;
}
