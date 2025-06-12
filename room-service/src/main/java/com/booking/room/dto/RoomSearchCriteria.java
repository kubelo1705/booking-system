package com.booking.room.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RoomSearchCriteria {
    private Double latitude;
    private Double longitude;
    private Double radiusInKm;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double minPrice;
    private Double maxPrice;
    private Integer minCapacity;
    private String roomType;
    private String city;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "rating";
    private String sortDirection = "DESC";
} 