package com.campus.Campus_Connect.features.registration.repository;

import com.campus.Campus_Connect.features.registration.entity.RegistrationFieldOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationFieldOptionRepository
        extends JpaRepository<RegistrationFieldOption, Integer> {

    List<RegistrationFieldOption>
    findByFieldIdOrderByOptionOrderAsc(
            Integer fieldId
    );
}