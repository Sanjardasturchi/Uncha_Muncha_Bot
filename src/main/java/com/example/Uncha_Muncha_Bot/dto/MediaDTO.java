package com.example.Uncha_Muncha_Bot.dto;

import com.example.Uncha_Muncha_Bot.enums.MediaType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MediaDTO {
    private Integer id;
    private String fId;
    private Long ownerId;
    private MediaType mediaType;
    private String mediaUrl;
}
