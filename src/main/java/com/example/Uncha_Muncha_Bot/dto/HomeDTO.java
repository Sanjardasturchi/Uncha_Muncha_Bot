package com.example.Uncha_Muncha_Bot.dto;

import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.SalaryType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
public class HomeDTO {
    private Long id;
    private String city;
    private SalaryType salaryType;
    private Double price;
    private String phone;
    private String username;
    private String info;
    private String district;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdDateTime;
    private ActiveStatus activeStatus;
    private String ownerChatId;
}
