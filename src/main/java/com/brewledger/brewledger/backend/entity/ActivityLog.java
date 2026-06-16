package com.brewledger.brewledger.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "activity_logs")
public class ActivityLog extends BaseEntity {

    @Column(nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String username;

    @Column(nullable = false)
    private String action;

    @Column(columnDefinition = "TEXT")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String details;

    private String actorRole;

    private String entityType;

    private Long entityId;

    @com.fasterxml.jackson.annotation.JsonProperty("actorUsername")
    public String getActorUsername() {
        return username;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("actorUsername")
    public void setActorUsername(String actorUsername) {
        this.username = actorUsername;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("description")
    public String getDescription() {
        return details;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("description")
    public void setDescription(String description) {
        this.details = description;
    }
}
