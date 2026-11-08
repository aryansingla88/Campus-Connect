package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.feature.registrations.data.remote.RegistrationApi
import java.lang.reflect.Proxy

internal fun registrationApiStub(
    handler: (method: String, args: List<Any?>) -> Any?,
): RegistrationApi = Proxy.newProxyInstance(
    RegistrationApi::class.java.classLoader,
    arrayOf(RegistrationApi::class.java),
) { _, method, args -> handler(method.name, args?.toList().orEmpty()) } as RegistrationApi
