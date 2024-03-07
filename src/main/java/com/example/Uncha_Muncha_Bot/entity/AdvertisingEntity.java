package com.example.Uncha_Muncha_Bot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "advertising")
public class AdvertisingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(name = "owner_chat_id")
    private String ownerChatId;
    @Column(name = "text")
    private String text;
    @Column(name = "shared_count")
    private Integer sharedCount;
    @Column(name = "created_date")
    private LocalDateTime createdDate=LocalDateTime.now();
}
