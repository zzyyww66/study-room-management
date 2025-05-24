package com.studyroom.server.service.impl;

import com.studyroom.server.entity.StudyRoom;
import com.studyroom.server.entity.Seat;
import com.studyroom.server.entity.Reservation;
import com.studyroom.server.repository.StudyRoomRepository;
import com.studyroom.server.repository.SeatRepository;
import com.studyroom.server.repository.ReservationRepository;
import com.studyroom.server.service.StudyRoomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 自习室服务实现类
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@Service
@Transactional
public class StudyRoomServiceImpl implements StudyRoomService {

    @Autowired
    private StudyRoomRepository studyRoomRepository;
    
    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    public StudyRoom createStudyRoom(String name, String description, Integer capacity,
                                   BigDecimal hourlyRate, LocalTime openTime, LocalTime closeTime,
                                   String location, String facilities) {
        StudyRoom studyRoom = new StudyRoom();
        studyRoom.setName(name);
        studyRoom.setDescription(description);
        studyRoom.setCapacity(capacity);
        studyRoom.setHourlyRate(hourlyRate);
        studyRoom.setOpenTime(openTime);
        studyRoom.setCloseTime(closeTime);
        studyRoom.setLocation(location);
        studyRoom.setFacilities(facilities);
        studyRoom.setStatus(StudyRoom.RoomStatus.AVAILABLE);
        
        return studyRoomRepository.save(studyRoom);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudyRoom> findById(Long roomId) {
        return studyRoomRepository.findById(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyRoom> findAvailableRooms() {
        return studyRoomRepository.findAvailableRooms();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyRoom> findRoomsByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        return studyRoomRepository.findByCapacityBetween(minCapacity, maxCapacity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyRoom> findRoomsByPriceRange(BigDecimal minRate, BigDecimal maxRate) {
        return studyRoomRepository.findByHourlyRateBetween(minRate, maxRate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyRoom> findRoomsByNameContaining(String nameKeyword) {
        return studyRoomRepository.findByNameContaining(nameKeyword);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> getRoomSeats(Long roomId) {
        return seatRepository.findByStudyRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getAvailableSeatsCount(Long roomId) {
        return (int) seatRepository.countAvailableSeatsByStudyRoomId(roomId);
    }

    @Override
    public StudyRoom updateStudyRoom(Long roomId, String name, String description, Integer capacity,
                                   BigDecimal hourlyRate, LocalTime openTime, LocalTime closeTime,
                                   String location, String facilities) {
        Optional<StudyRoom> roomOpt = studyRoomRepository.findById(roomId);
        if (!roomOpt.isPresent()) {
            throw new RuntimeException("自习室不存在");
        }
        
        StudyRoom studyRoom = roomOpt.get();
        studyRoom.setName(name);
        studyRoom.setDescription(description);
        studyRoom.setCapacity(capacity);
        studyRoom.setHourlyRate(hourlyRate);
        studyRoom.setOpenTime(openTime);
        studyRoom.setCloseTime(closeTime);
        studyRoom.setLocation(location);
        studyRoom.setFacilities(facilities);
        
        return studyRoomRepository.save(studyRoom);
    }

    @Override
    public StudyRoom updateRoomStatus(Long roomId, StudyRoom.RoomStatus status) {
        Optional<StudyRoom> roomOpt = studyRoomRepository.findById(roomId);
        if (!roomOpt.isPresent()) {
            throw new RuntimeException("自习室不存在");
        }
        
        StudyRoom studyRoom = roomOpt.get();
        studyRoom.setStatus(status);
        
        return studyRoomRepository.save(studyRoom);
    }

    @Override
    public void deleteStudyRoom(Long roomId) {
        // 软删除：设置状态为CLOSED
        updateRoomStatus(roomId, StudyRoom.RoomStatus.CLOSED);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomOpenAtTime(Long roomId, LocalTime time) {
        Optional<StudyRoom> roomOpt = studyRoomRepository.findById(roomId);
        if (!roomOpt.isPresent()) {
            return false;
        }
        
        StudyRoom room = roomOpt.get();
        if (room.getStatus() != StudyRoom.RoomStatus.AVAILABLE) {
            return false;
        }
        
        LocalTime openTime = room.getOpenTime();
        LocalTime closeTime = room.getCloseTime();
        
        // 处理跨天的情况（如晚上22:00到早上6:00）
        if (closeTime.isBefore(openTime)) {
            return time.isAfter(openTime) || time.isBefore(closeTime);
        } else {
            return time.isAfter(openTime) && time.isBefore(closeTime);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRoomStatistics(Long roomId) {
        Map<String, Object> stats = new HashMap<>();
        
        Optional<StudyRoom> roomOpt = studyRoomRepository.findById(roomId);
        if (!roomOpt.isPresent()) {
            throw new RuntimeException("自习室不存在");
        }
        
        StudyRoom room = roomOpt.get();
        
        // 基本信息
        stats.put("roomId", roomId);
        stats.put("roomName", room.getName());
        stats.put("capacity", room.getCapacity());
        
        // 座位统计
        int totalSeats = (int) seatRepository.countByStudyRoomId(roomId);
        int availableSeats = (int) seatRepository.countAvailableSeatsByStudyRoomId(roomId);
        int occupiedSeats = totalSeats - availableSeats;
        
        stats.put("totalSeats", totalSeats);
        stats.put("availableSeats", availableSeats);
        stats.put("occupiedSeats", occupiedSeats);
        stats.put("occupancyRate", totalSeats > 0 ? (double) occupiedSeats / totalSeats * 100 : 0.0);
        
        // 预订统计 - 使用现有的Repository方法
        List<Seat> roomSeats = seatRepository.findByStudyRoomId(roomId);
        int totalReservations = 0;
        int activeReservations = 0;
        
        for (Seat seat : roomSeats) {
            totalReservations += (int) reservationRepository.countBySeatId(seat.getId());
            // 使用findActiveReservationsByUserId查询活跃预订，但这里我们需要查询座位的活跃预订
            // 改为使用findBySeatId然后过滤状态
            List<Reservation> seatReservations = reservationRepository.findBySeatId(seat.getId());
            activeReservations += (int) seatReservations.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.ACTIVE)
                .count();
        }
        
        stats.put("totalReservations", totalReservations);
        stats.put("activeReservations", activeReservations);
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRoomsUtilizationStats() {
        List<StudyRoom> allRooms = studyRoomRepository.findAll();
        List<Map<String, Object>> utilizationStats = new ArrayList<>();
        
        for (StudyRoom room : allRooms) {
            Map<String, Object> roomStats = new HashMap<>();
            roomStats.put("roomId", room.getId());
            roomStats.put("roomName", room.getName());
            roomStats.put("capacity", room.getCapacity());
            roomStats.put("hourlyRate", room.getHourlyRate());
            roomStats.put("status", room.getStatus());
            
            // 计算利用率
            int totalSeats = (int) seatRepository.countByStudyRoomId(room.getId());
            int availableSeats = (int) seatRepository.countAvailableSeatsByStudyRoomId(room.getId());
            double utilizationRate = totalSeats > 0 ? (double) (totalSeats - availableSeats) / totalSeats * 100 : 0.0;
            
            roomStats.put("totalSeats", totalSeats);
            roomStats.put("availableSeats", availableSeats);
            roomStats.put("utilizationRate", utilizationRate);
            
            utilizationStats.add(roomStats);
        }
        
        return utilizationStats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyRoom> findRoomsOrderByPrice(boolean ascending) {
        if (ascending) {
            return studyRoomRepository.findAllByOrderByHourlyRateAsc();
        } else {
            // 使用现有方法的降序查询
            return studyRoomRepository.findAllByOrderByHourlyRateAsc()
                .stream()
                .sorted((a, b) -> b.getHourlyRate().compareTo(a.getHourlyRate()))
                .collect(java.util.stream.Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyRoom> findRoomsOrderByCapacity(boolean ascending) {
        if (ascending) {
            // 使用现有方法的升序查询
            return studyRoomRepository.findAll()
                .stream()
                .sorted((a, b) -> Integer.compare(a.getCapacity(), b.getCapacity()))
                .collect(java.util.stream.Collectors.toList());
        } else {
            return studyRoomRepository.findAll()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getCapacity(), a.getCapacity()))
                .collect(java.util.stream.Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudyRoom> findRoomsWithPagination(int page, int size, StudyRoom.RoomStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        if (status != null) {
            List<StudyRoom> rooms = studyRoomRepository.findByStatus(status);
            int start = page * size;
            int end = Math.min(start + size, rooms.size());
            List<StudyRoom> pageContent = start < rooms.size() ? rooms.subList(start, end) : new ArrayList<>();
            return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, rooms.size());
        } else {
            return studyRoomRepository.findAll(pageable);
        }
    }
} 