package com.example.campusconnect.feature.profile.data.mapper

import com.example.campusconnect.core.utils.AcademicUtils
import com.example.campusconnect.feature.metadata.courses.CourseRepository
import com.example.campusconnect.feature.profile.data.remote.response.ProfileResponse
import com.example.campusconnect.feature.profile.model.PublicUserProfile

suspend fun ProfileResponse.toPublicUserProfile(
    courseRepository: CourseRepository
): PublicUserProfile {

    val course =
        courseRepository.getCourseById(courseId)

    val programName =
        course?.programName.orEmpty()

    val academicYear =
        course?.let {
            AcademicUtils.getAcademicStatus(
                admissionYear = admissionYear,
                durationYears = it.durationYears
            )
        }.orEmpty()

    val batch =
        course?.let {
            AcademicUtils.getBatch(
                admissionYear = admissionYear,
                durationYears = it.durationYears
            )
        }.orEmpty()

    return PublicUserProfile(
        userId = userId,
        username = username,
        email = email,

        fullName = fullName,
        bio = bio.orEmpty(),
        avatarUrl = avatarUrl.orEmpty(),

        programName = programName,
        academicYear = academicYear,
        batch = batch,

        hostel = hostel,
        hometown = hometown.orEmpty(),

        gender = gender.orEmpty(),
        dob = dob.orEmpty(),

        phone = phone.orEmpty(),

        github = github.orEmpty(),
        linkedin = linkedin.orEmpty(),
        instagram = instagram.orEmpty(),

        memberSince = memberSince,

        showPhone = showPhone,
        showSocials = showSocials
    )
}