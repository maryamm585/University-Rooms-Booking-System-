package com.university.booking.mapper;

import com.university.booking.dto.BuildingDTO;
import com.university.booking.entity.Building;

public class BuildingMapper {
    public static BuildingDTO toDTO(Building building) {
        return new BuildingDTO(
                building.getId(),
                building.getName(),
                building.getCode(),
                building.getAddress(),
                building.getDescription(),
                building.getActive()
        );
    }


    public static Building toEntity(BuildingDTO dto) {
        Building building = new Building();
        building.setId(dto.getId());
        building.setName(dto.getName());
        building.setCode(dto.getCode());
        building.setAddress(dto.getAddress());
        building.setDescription(dto.getDescription());
        building.setActive(dto.getActive());
        return building;
    }
}
