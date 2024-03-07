package com.example.Uncha_Muncha_Bot.entity;

import com.example.Uncha_Muncha_Bot.dto.AutomobileServiceTypeDTO;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.CarType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "automobile_service")
public class AutomobileServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "city")
    private String city;
    @Column(name = "info")
    private String info;
    @Column(name = "phone")
    private String phone;
    @Column(name = "username")
    private String username;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "car_type")
    private CarType carType;
    @Column(name = "start_time")
    private LocalTime startTime;
    @Column(name = "end_time")
    private LocalTime endTime;
    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;
    @Column(name = "active_status")
    private ActiveStatus activeStatus;
    @Column(name = "owner_chat_id")
    private String ownerChatId;
}
