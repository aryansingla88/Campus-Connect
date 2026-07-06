package com.campus.Campus_Connect.features.metadata.courses;

import com.campus.Campus_Connect.features.metadata.courses.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> findAllByIsActiveTrueOrderByDegreeAscProgramNameAsc();

    Optional<Course> findByIdAndIsActiveTrue(Integer id);

}