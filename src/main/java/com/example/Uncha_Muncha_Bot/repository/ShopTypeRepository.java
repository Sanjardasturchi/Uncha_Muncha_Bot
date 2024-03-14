package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.ShopTypeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ShopTypeRepository extends CrudRepository<ShopTypeEntity, Long> {
    @Query("from ShopTypeEntity where shopId=?1")
    Iterable<ShopTypeEntity> findByOwnerId(Long id);
}
