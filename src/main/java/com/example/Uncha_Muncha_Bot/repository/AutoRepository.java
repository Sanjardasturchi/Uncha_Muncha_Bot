package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.AutomobileEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.CarType;
import com.example.Uncha_Muncha_Bot.enums.SalaryType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AutoRepository extends CrudRepository<AutomobileEntity,Long> {
    @Transactional
    @Modifying
    @Query("update AutomobileEntity set salaryType=?1 where id=?2")
    void setSaleType(SalaryType saleType, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set carType=?1 where id=?2")
    void setCarType(CarType carType, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set brandName=?1 where id=?2")
    void setBrandName(String brandName, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set city=?1 where id=?2")
    void setCity(String city, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set district=?1 where id=?2")
    void setDistrict(String district, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set latitude=?1, longitude=?2 where id=?3")
    void setLocation(Double latitude, Double longitude, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set activeStatus=?1 where id=?2")
    void changeStatus(ActiveStatus activeStatus, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set infoUz=?1 where id=?2")
    void setInfoUz(String infoUz, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set infoTr=?1 where id=?2")
    void setInfoTr(String infoTr, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set infoRu=?1 where id=?2")
    void setInfoRu(String infoRu, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set infoEn=?1 where id=?2")
    void setInfoEn(String infoEn, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set model=?1 where id=?2")
    void setModelName(String model, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set price=?1 where id=?2")
    void setPrice(Double price, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set phone=?1 where id=?2")
    void setPhone(String phone, Long autoId);

    @Transactional
    @Modifying
    @Query("update AutomobileEntity set username=?1 where id=?2")
    void setUsername(String username, Long autoId);
}
