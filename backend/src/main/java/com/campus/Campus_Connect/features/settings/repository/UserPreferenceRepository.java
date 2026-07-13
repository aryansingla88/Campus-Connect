package com.campus.Campus_Connect.features.settings.repository;

import com.campus.Campus_Connect.features.settings.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository
        extends JpaRepository<UserPreference, Integer> {

}