package com.example.Uncha_Muncha_Bot.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class AdvertisingDTO {
    private Long id;
    private String ownerChatId;
    private String text;
    private Integer sharedCount;
    private LocalDateTime createdDate;
}
