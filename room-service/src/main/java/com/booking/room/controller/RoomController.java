package com.booking.room.controller;

import com.booking.room.model.Room;
import com.booking.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;
    private static final String USER_ID_HEADER = "X-User-ID";

    @GetMapping("/search")
    public ResponseEntity<Page<Room>> searchAvailableRooms(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(roomService.getAvailableRooms(
            startTime,
            endTime,
            minPrice,
            maxPrice,
            minCapacity,
            roomType,
            city,
            page,
            size
        ));
    }

    @GetMapping("/suggested")
    public ResponseEntity<Page<Room>> getSuggestedRooms(
            @RequestParam(defaultValue = "4.0") Double minRating,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(roomService.getSuggestedRooms(minRating, city, page, size));
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<Room>> getRoomsByHotelId(@PathVariable Long hotelId) {
        List<Room> rooms = roomService.getRoomsByHotelId(hotelId);
        return rooms.isEmpty() ? 
            ResponseEntity.notFound().build() : 
            ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.getRoomById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Room> createRoom(
            @Valid @RequestBody Room room,
            @RequestHeader(USER_ID_HEADER) String userId) {
        try {
            Room createdRoom = roomService.createRoom(room, userId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdRoom);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody Room room,
            @RequestHeader(USER_ID_HEADER) String userId) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Room updatedRoom = roomService.updateRoom(id, room, userId);
            return updatedRoom != null ? 
                    ResponseEntity.ok(updatedRoom) : 
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long id,
            @RequestHeader(USER_ID_HEADER) String userId) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            roomService.deleteRoom(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 