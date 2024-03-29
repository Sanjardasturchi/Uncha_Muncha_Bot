package com.example.Uncha_Muncha_Bot.dto;

import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.CarType;
import com.example.Uncha_Muncha_Bot.enums.SalaryType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
public class AutomobileDTO {
    private Long id;
    private String city;
    private CarType carType;
    private SalaryType salaryType;
    private String brandName;
    private String model;
    private Double price;
    private String phone;
    private String username;
    private String infoUz;
    private String infoTr;
    private String infoRu;
    private String infoEn;
    private String district;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double  latitude;
    private Double longitude;
    private LocalDateTime createdDateTime;
    private ActiveStatus activeStatus;
    private String ownerChatId;
}
