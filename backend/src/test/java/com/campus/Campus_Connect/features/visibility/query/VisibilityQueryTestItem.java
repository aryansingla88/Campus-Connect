package com.campus.Campus_Connect.features.visibility.query;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Test-only resource mapping used to exercise visibility Specifications against a real database. */
@Entity
@Table(name = "visibility_query_test_items")
public class VisibilityQueryTestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer ownerId;

    private String visibilityType;
    private String visibilityValue;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean deleted;

    protected VisibilityQueryTestItem() {
    }

    VisibilityQueryTestItem(Integer ownerId, String visibilityType, String visibilityValue,
                            String title, boolean deleted) {
        this.ownerId = ownerId;
        this.visibilityType = visibilityType;
        this.visibilityValue = visibilityValue;
        this.title = title;
        this.deleted = deleted;
    }

    public Integer getId() {
        return id;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public String getVisibilityType() {
        return visibilityType;
    }

    public String getVisibilityValue() {
        return visibilityValue;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
