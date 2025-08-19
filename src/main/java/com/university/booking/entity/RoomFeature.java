package com.university.booking.entity;

//public enum RoomFeature {
//    PROJECTOR,
//    WHITEBOARD,
//    SMARTBOARD,
//    COMPUTER,
//    AUDIO_SYSTEM,
//    VIDEO_CONFERENCING,
//    AIR_CONDITIONING,
//    WIFI,
//    LABORATORY_EQUIPMENT,
//    MICROSCOPES,
//    CHEMISTRY_EQUIPMENT,
//    PHYSICS_EQUIPMENT,
//    COMPUTER_LAB,
//    ACCESSIBLE
//}

import jakarta.persistence.*;

@Entity
public class RoomFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; //"PROJECTOR", "WHITEBOARD"

    public RoomFeature() {}

    public RoomFeature(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
