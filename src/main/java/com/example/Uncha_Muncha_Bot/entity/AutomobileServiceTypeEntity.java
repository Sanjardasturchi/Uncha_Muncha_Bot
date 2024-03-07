package com.example.Uncha_Muncha_Bot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "automobile_service_type")
public class AutomobileServiceTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "auto_service_id")
    private Integer autoServiceId;
    @Column(name = "service_name")
    private String serviceName;
}
