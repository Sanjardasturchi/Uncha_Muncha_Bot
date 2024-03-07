package com.example.Uncha_Muncha_Bot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "home_service_type")
public class HomeServiceTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "home_service_id")
    private Integer homeServiceId;

    @Column(name = "service_name")
    private String serviceName;
}
