package com.example.Uncha_Muncha_Bot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "hospital_service")
public class HospitalServiceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "hospital_id")
    private Integer hospitalId;

    @Column(name = "service_name")
    private String serviceName;
}
