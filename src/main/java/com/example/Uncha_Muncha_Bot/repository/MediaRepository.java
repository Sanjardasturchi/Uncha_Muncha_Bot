package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.MediaEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MediaRepository extends CrudRepository<MediaEntity,Integer> {
    @Query("from MediaEntity where ownerId=?1 and visible=true")
    Iterable<MediaEntity> getByOwnerId(Long ownerId);

    @Transactional
    @Modifying
    @Query("update MediaEntity set visible=false where ownerId=?1")
    void deleteByOwnerId(Long changingElementId);

    @Query("from MediaEntity where fId=?1")
    Optional<MediaEntity> findByFId(String fId);
}
