package com.example.campusconnect.feature.metadata.courses

import com.example.campusconnect.feature.metadata.courses.local.CourseDao
import com.example.campusconnect.feature.metadata.courses.local.toCourse
import com.example.campusconnect.feature.metadata.courses.local.toEntity
import com.example.campusconnect.feature.metadata.courses.remote.CourseApi

class CourseRepository(
    private val courseApi: CourseApi,
    private val courseDao: CourseDao,
    private val courseCacheManager: CourseCacheManager
){

    suspend fun getAllCourses(): List<Course> {
        return courseDao.getAllCourses()
            .map { it.toCourse() }
    }

    suspend fun getCourseById(courseId: Int): Course? {
        return courseDao.getCourseById(courseId)
            ?.toCourse()
    }
    //------------
    suspend fun refreshCourses(): Result<Unit> {
        return try {
            val response = courseApi.getCourses()

            if (response.isSuccessful) {
                val courses = response.body()?.data.orEmpty()

                courseDao.clearCourses()

                courseDao.insertCourses(
                    courses
                        .map { it.toCourse() }
                        .map { it.toEntity() }
                )

                courseCacheManager.updateLastRefreshTime()

                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("Failed to fetch courses")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
//------------
    suspend fun ensureCoursesCached(): Result<Unit> {

        val cachedCourses = courseDao.getAllCourses()

        val lastRefreshTime =
            courseCacheManager.getLastRefreshTime()

        val cacheIsEmpty = cachedCourses.isEmpty()

        val cacheExpired =
            System.currentTimeMillis() - lastRefreshTime >
                    CACHE_DURATION_MS

        if (cacheIsEmpty || cacheExpired) {
            return refreshCourses()
        }

        return Result.success(Unit)
    }
//------------
    companion object {
        private const val CACHE_DURATION_MS =
            24 * 60 * 60 * 1000L
    }
}