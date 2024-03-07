package com.example.Uncha_Muncha_Bot.repository;

import com.example.Uncha_Muncha_Bot.entity.ProfileEntity;
import com.example.Uncha_Muncha_Bot.enums.ActiveStatus;
import com.example.Uncha_Muncha_Bot.enums.Language;
import com.example.Uncha_Muncha_Bot.enums.ProfileRole;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends CrudRepository<ProfileEntity,Long>, PagingAndSortingRepository<ProfileEntity,Long> {
    Optional<ProfileEntity> findByChatId(String chatId);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set language=?2 where chatId=?1")
    void changeLanguage(String chatId, Language language);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set currentStep=?1 where chatId=?2")
    void changeStep(String step, String chatId);

    @Query("from ProfileEntity  where role='SUPER_ADMIN' order by createdDateTime ASC ")
    Iterable<ProfileEntity> getSuperAdmin();

    @Query("from ProfileEntity  where chatId in ('994001445','5617276833','5344927336','5035317446','1507437565')order by createdDateTime ASC ")
    Iterable<ProfileEntity> getOwnersList();

    @Query(value = "select count(*) from profile",nativeQuery = true)
    Long getCount();

    @Query("from ProfileEntity  where role='ADMIN' order by createdDateTime ASC ")
    Iterable<ProfileEntity> getAdminList();

    @Transactional
    @Modifying
    @Query("update ProfileEntity set username=?1 where chatId=?2")
    void updateUsername(String userName, String chatId);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set role=?2 where chatId=?1")
    void changeRole(String chatId, ProfileRole profileRole);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set activeStatus=?2 where chatId=?1")
    void changeStatus(String chatId, ActiveStatus activeStatus);

    @Transactional
    @Modifying
    @Query("update ProfileEntity set changingElementId=?2 where chatId=?1")
    void changeChangingElementId(String chatId, Long advertisingId);
}
