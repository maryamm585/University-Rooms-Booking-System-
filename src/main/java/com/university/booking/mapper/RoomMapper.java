package com.university.booking.mapper;
import com.university.booking.dto.RoomDTO;
import com.university.booking.entity.Room;
import com.university.booking.entity.RoomFeature;
import java.util.Set;
import java.util.stream.Collectors;

public class RoomMapper {
    public static RoomDTO toDTO(Room room) {
        Set<Long> featureIds = room.getFeatures() == null ? null :
                room.getFeatures().stream().map( RoomFeature ::getId).collect(Collectors.toSet());

        return new RoomDTO(
                room.getId(),
                room.getName(),
                room.getRoomNumber(),
                room.getBuilding() != null ? room.getBuilding().getId() : null,
                room.getCapacity(),
                room.getDescription(),
                featureIds,
                room.getActive()
        );
    }

    public static Room toEntity(RoomDTO dto) {
        Room room = new Room();
        room.setId(dto.getId());
        room.setName(dto.getName());
        room.setRoomNumber(dto.getRoomNumber());
        room.setCapacity(dto.getCapacity());
        room.setDescription(dto.getDescription());
        room.setActive(dto.getActive());
        return room;
    }
}
