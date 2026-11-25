package com.campus.Campus_Connect.features.visibility.domain;

/** Immutable custom visibility configuration. A null scope value is valid only for CONNECTIONS. */
public record VisibilityPolicy(VisibilityScope scope, String scopeValue) {
}
