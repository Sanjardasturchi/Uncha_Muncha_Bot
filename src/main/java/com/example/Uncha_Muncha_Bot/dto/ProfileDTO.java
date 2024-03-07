package com.example.Uncha_Muncha_Bot.dto;


import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.Language;
import com.example.Uncha_Muncha_Bot.enums.ProfileRole;
import com.example.Uncha_Muncha_Bot.enums.SalaryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ProfileDTO {
    private Long id;
    private ActiveStatus activeStatus;
    private String phone;
    private String username;
    private LocalDateTime createdDateTime;
    private Double latitude;
    private Double longitude;
    private String name;
    private String surname;
    private String chatId;
    private ProfileRole role;
    private String currentStep;
    private SalaryType selectedPurchaseType;
    private Long changingElementId;
    private Language language;

    public ProfileDTO(Long id, String username, String chatId) {
        this.id = id;
        this.username = username;
        this.chatId = chatId;
    }
}
