package com.example.Uncha_Muncha_Bot.entity;

import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
@Entity
@Table(name = "auto_spare_parts_shop")
public class AutoSparePartsShopEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "city")
    private String city;
    @Column(name = "info")
    private String info;
    @Column(name = "start_time")
    private LocalTime startTime;
    @Column(name = "end_time")
    private LocalTime endTime;
    @Column(name = "phone")
    private String phone;
    @Column(name = "username")
    private String username;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;
    @Column(name = "active_status")
    private ActiveStatus activeStatus;
    @Column(name = "owner_chat_id")
    private String ownerChatId;
}
