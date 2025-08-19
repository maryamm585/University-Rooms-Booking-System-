package com.university.booking.dto;

import java.util.Set;

public class RoomDTO {
    private Long id;

    public RoomDTO() {}

    // All-args constructor
    public RoomDTO(Long id, String name, String roomNumber, Long buildingId,
                   Integer capacity, String description, Set<Long> featureIds, Boolean active) {
        this.id = id;
        this.name = name;
        this.roomNumber = roomNumber;
        this.buildingId = buildingId;
        this.capacity = capacity;
        this.description = description;
        this.featureIds = featureIds;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Long> getFeatureIds() {
        return featureIds;
    }

    public void setFeatureIds(Set<Long> featureIds) {
        this.featureIds = featureIds;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    private String name;
    private String roomNumber;
    private Long buildingId;
    private Integer capacity;
    private String description;
    private Set<Long> featureIds;
    private Boolean active;
}
