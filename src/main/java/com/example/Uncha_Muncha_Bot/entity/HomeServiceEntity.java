package com.example.Uncha_Muncha_Bot.entity;

import com.example.Uncha_Muncha_Bot.dto.HomeServiceTypeDTO;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "home_service")
public class HomeServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "username")
    private String username;

    @Column(name = "phone")
    private String phone;

    @Column(name = "company_brand_name")
    private String companyBrandName;

    @Column(name = "info")
    private String info;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status")
    private ActiveStatus activeStatus;

    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    @Column(name = "owner_chat_id")
    private String ownerChatId;
}
