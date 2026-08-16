package com.campus.Campus_Connect.features.registration.repository;

import com.campus.Campus_Connect.features.registration.entity.RegistrationField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationFieldRepository
        extends JpaRepository<RegistrationField, Integer> {

    List<RegistrationField> findByEventIdOrderByFieldOrderAsc(
            Integer eventId
    );

    Optional<RegistrationField> findByIdAndEventId(
            Integer fieldId,
            Integer eventId
    );
}