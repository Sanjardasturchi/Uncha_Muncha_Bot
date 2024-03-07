package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.AdvertisingEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AdvertisingRepository extends CrudRepository<AdvertisingEntity,Long> {

    @Transactional
    @Modifying
    @Query("update AdvertisingEntity set sharedCount=?1 where id=?2")
    void setSharedCount(int count, Long advertisingId);
}
