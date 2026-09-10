package com.example.campusconnect.feature.profile.data.mapper

import com.example.campusconnect.core.utils.AcademicUtils
import com.example.campusconnect.feature.metadata.courses.CourseRepository
import com.example.campusconnect.feature.profile.data.remote.response.ProfileResponse
import com.example.campusconnect.feature.profile.model.PublicUserProfile

object ProfileMapper {

    suspend fun toPublicUserProfile(
        response: ProfileResponse,
        courseRepository: CourseRepository
    ): PublicUserProfile {

        val course = response.courseId?.let { courseId ->
            courseRepository.getCourseById(courseId)
        }

        val programName = course?.programName.orEmpty()

        val academicYear = course?.let { courseData ->
            response.admissionYear?.let { admissionYear ->
                AcademicUtils.getAcademicStatus(
                    admissionYear = admissionYear,
                    durationYears = courseData.durationYears
                )
            }
        }.orEmpty()

        val batch = course?.let {
            response.admissionYear?.let { admissionYear ->
                AcademicUtils.getBatch(
                    admissionYear = admissionYear,
                    durationYears = it.durationYears
                )
            }
        }.orEmpty()

        return PublicUserProfile(
            // Identity
            userId = response.userId ?: 0,
            username = response.username.orEmpty(),
            email = response.email.orEmpty(),

            // Profile header
            fullName = response.fullName.orEmpty(),
            bio = response.bio.orEmpty(),

            // Keep null so AppAvatar can show initials
            avatarUrl = response.avatarUrl,

            // Academic
            programName = programName,
            academicYear = academicYear,
            batch = batch,

            // Other profile details
            hostel = response.hostel.orEmpty(),
            hometown = response.hometown.orEmpty(),

            // Personal
            gender = response.gender.orEmpty(),
            dob = response.dob.orEmpty(),

            // Contact
            phone = response.phone.orEmpty(),

            // Social
            github = response.github.orEmpty(),
            linkedin = response.linkedin.orEmpty(),
            instagram = response.instagram.orEmpty(),

            // Metadata
            memberSince = response.memberSince.orEmpty(),

            // Visibility
            showPhone = response.showPhone ?: false,
            showSocials = response.showSocials ?: true
        )
    }
}