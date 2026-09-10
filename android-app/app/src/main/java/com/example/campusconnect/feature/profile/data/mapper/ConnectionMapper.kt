package com.example.campusconnect.feature.profile.data.mapper

import com.example.campusconnect.core.utils.AcademicUtils
import com.example.campusconnect.feature.metadata.courses.CourseRepository
import com.example.campusconnect.feature.profile.data.remote.response.ConnectionResponse
import com.example.campusconnect.feature.profile.model.Connection
import com.example.campusconnect.feature.profile.model.ConnectionRequest
import com.example.campusconnect.feature.profile.model.ConnectionStatus
import com.example.campusconnect.feature.profile.model.RequestType

object ConnectionMapper {

    suspend fun toConnection(
        response: ConnectionResponse,
        courseRepository: CourseRepository
    ): Connection {

        val course = response.courseId?.let { courseId ->
            courseRepository.getCourseById(courseId)
        }

        val courseName = course?.let {
            AcademicUtils.getCourseName(it)
        }.orEmpty()

        val academicYear = course?.let {
            response.admissionYear?.let { admissionYear ->
                AcademicUtils.getAcademicStatus(
                    admissionYear = admissionYear,
                    durationYears = it.durationYears
                )
            }
        }.orEmpty()

        return Connection(
            userId = response.userId,
            fullName = response.fullName,
            course = courseName,
            academicYear = academicYear,
            avatarUrl = response.avatarUrl,
            status = response.status.toConnectionStatus()
        )
    }

    suspend fun toConnectionRequest(
        response: ConnectionResponse,
        courseRepository: CourseRepository
    ): ConnectionRequest? {

        val requestType = response.status.toRequestType()
            ?: return null

        val course = response.courseId?.let { courseId ->
            courseRepository.getCourseById(courseId)
        }

        val courseName = course?.let {
            AcademicUtils.getCourseName(it)
        }.orEmpty()

        val academicYear = course?.let {
            response.admissionYear?.let { admissionYear ->
                AcademicUtils.getAcademicStatus(
                    admissionYear = admissionYear,
                    durationYears = it.durationYears
                )
            }
        }.orEmpty()

        return ConnectionRequest(
            userId = response.userId,
            fullName = response.fullName,
            course = courseName,
            academicYear = academicYear,
            avatarUrl = response.avatarUrl,
            type = requestType
        )
    }

    suspend fun toConnections(
        responses: List<ConnectionResponse>,
        courseRepository: CourseRepository
    ): List<Connection> {
        return responses.map { response ->
            toConnection(
                response = response,
                courseRepository = courseRepository
            )
        }
    }

    suspend fun toConnectionRequests(
        responses: List<ConnectionResponse>,
        courseRepository: CourseRepository
    ): List<ConnectionRequest> {
        return responses.mapNotNull { response ->
            toConnectionRequest(
                response = response,
                courseRepository = courseRepository
            )
        }
    }

    private fun String.toConnectionStatus(): ConnectionStatus {
        return when (this) {
            "CONNECTED" -> ConnectionStatus.CONNECTED

            "PENDING_SENT",
            "PENDING_RECEIVED" -> ConnectionStatus.PENDING

            "NOT_CONNECTED" -> ConnectionStatus.NOT_CONNECTED

            else -> ConnectionStatus.NOT_CONNECTED
        }
    }

    private fun String.toRequestType(): RequestType? {
        return when (this) {
            "PENDING_RECEIVED" -> RequestType.INCOMING
            "PENDING_SENT" -> RequestType.OUTGOING
            else -> null
        }
    }
}