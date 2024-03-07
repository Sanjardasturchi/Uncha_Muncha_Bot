package com.example.Uncha_Muncha_Bot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "shop_type")
public class ShopTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "shop_id")
    private Integer shopId;

    @Column(name = "shop_type")
    private String shopType;
}
