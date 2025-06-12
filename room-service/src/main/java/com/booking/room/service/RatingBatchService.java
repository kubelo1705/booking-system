package com.booking.room.service;

import com.booking.room.model.RoomRating;
import com.booking.room.repository.RoomRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RatingBatchService {
    private final RoomRatingRepository roomRatingRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String PENDING_UPDATES_KEY = "room:ratings:pending";
    private static final long PENDING_UPDATE_TTL = 30; // 30 minutes

    @Cacheable(value = "roomRatings", key = "#roomId")
    public RoomRating getRoomRating(Long roomId) {
        return roomRatingRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalStateException("Room rating not found"));
    }

    @CachePut(value = "roomRatings", key = "#roomId")
    public RoomRating updateRoomRating(Long roomId, double rating, boolean isAdd) {
        String key = PENDING_UPDATES_KEY + ":" + roomId;
        RoomRating roomRating = (RoomRating) redisTemplate.opsForValue().get(key);
        
        if (roomRating == null) {
            roomRating = roomRatingRepository.findByRoomId(roomId)
                    .orElseGet(() -> new RoomRating(roomId));
        }

        if (isAdd) {
            roomRating.addRating(rating);
        } else {
            roomRating.removeRating(rating);
        }

        redisTemplate.opsForValue().set(key, roomRating, PENDING_UPDATE_TTL, TimeUnit.MINUTES);
        redisTemplate.opsForSet().add(PENDING_UPDATES_KEY, roomId.toString());
        
        return roomRating;
    }

    @CachePut(value = "roomRatings", key = "#roomId")
    public RoomRating updateRoomRating(Long roomId, double oldRating, double newRating) {
        String key = PENDING_UPDATES_KEY + ":" + roomId;
        RoomRating roomRating = (RoomRating) redisTemplate.opsForValue().get(key);
        
        if (roomRating == null) {
            roomRating = roomRatingRepository.findByRoomId(roomId)
                    .orElseGet(() -> new RoomRating(roomId));
        }

        roomRating.updateRating(oldRating, newRating);
        
        redisTemplate.opsForValue().set(key, roomRating, PENDING_UPDATE_TTL, TimeUnit.MINUTES);
        redisTemplate.opsForSet().add(PENDING_UPDATES_KEY, roomId.toString());
        
        return roomRating;
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    @CacheEvict(value = "roomRatings", allEntries = true)
    public void batchSaveRatings() {
        Set<String> pendingRoomIds = redisTemplate.opsForSet().members(PENDING_UPDATES_KEY);
        if (pendingRoomIds != null && !pendingRoomIds.isEmpty()) {
            for (String roomId : pendingRoomIds) {
                String key = PENDING_UPDATES_KEY + ":" + roomId;
                RoomRating roomRating = (RoomRating) redisTemplate.opsForValue().get(key);
                if (roomRating != null) {
                    roomRatingRepository.save(roomRating);
                    redisTemplate.delete(key);
                }
            }
            redisTemplate.delete(PENDING_UPDATES_KEY);
        }
    }

    @Transactional
    @CacheEvict(value = "roomRatings", allEntries = true)
    public void forceSaveRatings() {
        batchSaveRatings();
    }
} 