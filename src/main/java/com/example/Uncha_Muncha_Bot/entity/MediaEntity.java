package com.example.Uncha_Muncha_Bot.entity;

import com.example.Uncha_Muncha_Bot.enums.MediaType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "media")
public class MediaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "f_id")
    private String fId;

    @Column(name = "owner_id")
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type")
    private MediaType mediaType;

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "visible")
    private Boolean visible=true;
}
