package com.campus.Campus_Connect.features.registration.repository;

import com.campus.Campus_Connect.features.registration.entity.RegistrationAnswer;
import com.campus.Campus_Connect.features.registration.entity.RegistrationAnswerId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationAnswerRepository
        extends JpaRepository<RegistrationAnswer, RegistrationAnswerId> {

    List<RegistrationAnswer> findByIdRegistrationId(
            Integer registrationId
    );

    void deleteByIdRegistrationId(
            Integer registrationId
    );
}