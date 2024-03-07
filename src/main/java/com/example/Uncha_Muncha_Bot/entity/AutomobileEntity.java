package com.example.Uncha_Muncha_Bot.entity;

import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.CarType;
import com.example.Uncha_Muncha_Bot.enums.SalaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
@Entity
@Table(name = "automobile")
public class AutomobileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "city")
    private String city;
    @Column(name = "car_type")
    private CarType carType;
    @Column(name = "salary_type")
    private SalaryType salaryType;
    @Column(name = "brand_name")
    private String brandName;
    @Column(name = "model")
    private String model;
    @Column(name = "price")
    private Double price;
    @Column(name = "phone")
    private String phone;
    @Column(name = "username")
    private String username;
    @Column(name = "info")
    private String info;
    @Column(name = "district")
    private String district;
    @Column(name = "start_time")
    private LocalTime startTime;
    @Column(name = "end_time")
    private LocalTime endTime;
    @Column(name = "latitude")
    private Double  latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;
    @Column(name = "active_status")
    private ActiveStatus activeStatus;
    @Column(name = "owner_chat_id")
    private String ownerChatId;
}
