package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.AutomobileServiceTypeEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AutoServiceTypeRepository extends CrudRepository<AutomobileServiceTypeEntity,Long> {
    @Query("from AutomobileServiceTypeEntity where autoServiceId=?1")
    Iterable<AutomobileServiceTypeEntity> findByServiceId(Long serviceId);

}
