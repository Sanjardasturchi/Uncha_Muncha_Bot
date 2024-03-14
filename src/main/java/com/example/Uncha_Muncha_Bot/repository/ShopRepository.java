package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.ShopEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalTime;

public interface ShopRepository extends CrudRepository<ShopEntity,Long> {
    @Transactional
    @Modifying
    @Query("update ShopEntity set startTime=?1 where id=?2")
    void setStartTime(LocalTime time, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set endTime=?1 where id=?2")
    void setEndTime(LocalTime time, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set username=?1 where id=?2")
    void setUsername(String username, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set city=?1 where id=?2")
    void setCity(String city, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set district=?1 where id=?2")
    void setDistrict(String district, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set phone=?1 where id=?2")
    void setPhone(String phone, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set brand=?1 where id=?2")
    void setBrand(String brand, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set infoUz=?1 where id=?2")
    void setInfoUz(String info, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set infoTr=?1 where id=?2")
    void setInfoTr(String info, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set infoRu=?1 where id=?2")
    void setInfoRu(String info, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set infoEn=?1 where id=?2")
    void setInfoEn(String info, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set latitude=?1, longitude=?2 where id=?3")
    void setLocation(Double latitude, Double longitude, Long shopId);

    @Transactional
    @Modifying
    @Query("update ShopEntity set activeStatus=?1 where id=?2")
    void changeStatus(ActiveStatus status, Long shopId);
}
