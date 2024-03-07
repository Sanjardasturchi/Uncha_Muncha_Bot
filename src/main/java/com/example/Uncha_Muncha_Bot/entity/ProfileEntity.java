package com.example.Uncha_Muncha_Bot.entity;


import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.Language;
import com.example.Uncha_Muncha_Bot.enums.ProfileRole;
import com.example.Uncha_Muncha_Bot.enums.SalaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "profile")
public class ProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status")
    private ActiveStatus activeStatus;

    @Column(name = "phone")
    private String phone;

    @Column(name = "username")
    private String username;

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "name")
    private String name;

    @Column(name = "surname")
    private String surname;

    @Column(name = "chat_id")
    private String chatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private ProfileRole role;

    @Column(name = "current_step")
    private String currentStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_purchase_type")
    private SalaryType selectedPurchaseType;

    @Column(name = "changing_element_id")
    private Long changingElementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private Language language;

    public ProfileEntity(Long id, String username, String chatId) {
        this.id = id;
        this.username = username;
        this.chatId = chatId;
    }
}
