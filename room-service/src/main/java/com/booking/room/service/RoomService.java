package com.booking.room.service;

import com.booking.room.dto.RoomSearchCriteria;
import com.booking.room.model.BookingInfo;
import com.booking.room.model.Hotel;
import com.booking.room.model.Room;
import com.booking.room.repository.BookingInfoRepository;
import com.booking.room.repository.HotelRepository;
import com.booking.room.repository.RoomRepository;
import com.booking.room.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final BookingInfoRepository bookingInfoRepository;
    private final ReviewRepository reviewRepository;

    @Cacheable(value = "rooms", key = "#id")
    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    @Cacheable(value = "hotelRooms", key = "#hotelId")
    public List<Room> getRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    @Transactional
    @CacheEvict(value = {"rooms", "hotelRooms", "roomSearch"}, allEntries = true)
    public Room createRoom(Room room, String userId) {
        // Verify user has permission to create room
        if (!hasRoomManagementPermission(userId)) {
            throw new IllegalStateException("User does not have permission to create rooms");
        }
        return roomRepository.save(room);
    }

    @Transactional
    @CacheEvict(value = {"rooms", "hotelRooms", "roomSearch"}, allEntries = true)
    public Room updateRoom(Long id, Room room, String userId) {
        if (!hasRoomManagementPermission(userId)) {
            throw new IllegalStateException("User does not have permission to update rooms");
        }
        if (roomRepository.existsById(id)) {
            room.setId(id);
            return roomRepository.save(room);
        }
        return null;
    }

    @Transactional
    @CacheEvict(value = {"rooms", "hotelRooms", "roomSearch"}, allEntries = true)
    public void deleteRoom(Long id, String userId) {
        if (!hasRoomManagementPermission(userId)) {
            throw new IllegalStateException("User does not have permission to delete rooms");
        }
        roomRepository.deleteById(id);
    }

    private boolean hasRoomManagementPermission(String userId) {
        // TODO: Implement proper permission check
        // For now, just return true
        return true;
    }

    @Cacheable(value = "roomSearch", key = "#startTime + '-' + #endTime + '-' + #minPrice + '-' + #maxPrice + '-' + #minCapacity + '-' + #roomType + '-' + #city + '-' + #page + '-' + #size")
    public Page<Room> getAvailableRooms(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Double minPrice,
            Double maxPrice,
            Integer minCapacity,
            String roomType,
            String city,
            int page,
            int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        return roomRepository.findAvailableRooms(
            startTime,
            endTime,
            minPrice,
            maxPrice,
            minCapacity,
            roomType,
            city,
            pageRequest
        );
    }

    @Cacheable(value = "suggestedRooms", key = "#minRating + '-' + #city + '-' + #page + '-' + #size")
    public Page<Room> getSuggestedRooms(Double minRating, String city, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        
        // If city is provided, use city-based filtering
        if (city != null) {
            return roomRepository.findRoomsByMinRatingAndCity(minRating, city, pageRequest);
        }
        
        // Fallback to rating-only filtering if no city parameter
        return roomRepository.findRoomsByMinRating(minRating, pageRequest);
    }
} 