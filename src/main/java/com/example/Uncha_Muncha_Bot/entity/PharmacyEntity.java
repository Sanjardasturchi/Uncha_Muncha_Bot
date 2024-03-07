package com.example.Uncha_Muncha_Bot.entity;

import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.PharmacyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
@Entity
@Table(name = "pharmacy")
public class PharmacyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "pharmacy_type")
    private PharmacyType pharmacyType;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "username")
    private String username;

    @Column(name = "phone")
    private String phone;

    @Column(name = "pharmacy_name")
    private String pharmacyName;

    @Column(name = "info_uz")
    private String infoUz;

    @Column(name = "info_tr")
    private String infoTr;

    @Column(name = "info_ru")
    private String infoRu;

    @Column(name = "info_en")
    private String infoEn;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status")
    private ActiveStatus activeStatus=ActiveStatus.BLOCK;

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @Column(name = "owner_chat_id")
    private String ownerChatId;
}
