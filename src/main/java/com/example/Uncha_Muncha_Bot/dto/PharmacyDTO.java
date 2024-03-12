package com.example.Uncha_Muncha_Bot.dto;

import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.PharmacyType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PharmacyDTO {
    private Long id;
    private PharmacyType pharmacyType;
    private LocalTime startTime;
    private LocalTime endTime;
    private String username;
    private String phone;
    private String pharmacyName;
    private ActiveStatus activeStatus;
    private String infoUz;
    private String infoTr;
    private String infoRu;
    private String infoEn;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdDateTime;
    private String ownerChatId;
}
