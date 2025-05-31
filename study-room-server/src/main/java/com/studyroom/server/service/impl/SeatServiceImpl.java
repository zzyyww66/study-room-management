package com.studyroom.server.service.impl;

import com.studyroom.server.entity.Seat;
import com.studyroom.server.entity.StudyRoom;
import com.studyroom.server.entity.Reservation;
import com.studyroom.server.repository.SeatRepository;
import com.studyroom.server.repository.StudyRoomRepository;
import com.studyroom.server.repository.ReservationRepository;
import com.studyroom.server.service.SeatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 座位服务实现类
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@Service
@Transactional
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatRepository seatRepository;
    
    @Autowired
    private StudyRoomRepository studyRoomRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;

    @Override
    public Seat createSeat(String seatNumber, Long studyRoomId, Seat.SeatType type,
                          Boolean hasWindow, Boolean hasPowerOutlet, Boolean hasLamp,
                          String description, String equipment, Integer rowNum, Integer colNum) {
        // 验证自习室是否存在
        Optional<StudyRoom> studyRoomOpt = studyRoomRepository.findById(studyRoomId);
        if (!studyRoomOpt.isPresent()) {
            throw new RuntimeException("自习室不存在");
        }
        
        StudyRoom studyRoom = studyRoomOpt.get();
        
        // 检查座位号是否已存在
        if (seatRepository.existsBySeatNumberAndStudyRoomId(seatNumber, studyRoomId)) {
            throw new RuntimeException("座位号已存在");
        }
        
        Seat seat = new Seat();
        seat.setSeatNumber(seatNumber);
        seat.setStudyRoom(studyRoom);
        seat.setType(type);
        seat.setStatus(Seat.SeatStatus.AVAILABLE);
        seat.setHasWindow(hasWindow != null ? hasWindow : false);
        seat.setHasPowerOutlet(hasPowerOutlet != null ? hasPowerOutlet : false);
        seat.setHasLamp(hasLamp != null ? hasLamp : false);
        seat.setDescription(description);
        seat.setEquipment(equipment);
        seat.setRowNum(rowNum);
        seat.setColNum(colNum);
        
        return seatRepository.save(seat);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Seat> findById(Long seatId) {
        return seatRepository.findById(seatId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> findAvailableSeats() {
        return seatRepository.findAvailableSeats();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> findSeatsByStudyRoom(Long studyRoomId) {
        return seatRepository.findByStudyRoomId(studyRoomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> findAvailableSeatsByStudyRoom(Long studyRoomId) {
        return seatRepository.findAvailableSeatsByStudyRoomId(studyRoomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> findSeatsByType(Seat.SeatType type) {
        return seatRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> findSeatsWithWindow() {
        return seatRepository.findByHasWindowTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> findAvailableSeatsWithFeatures(Boolean hasWindow, Boolean hasPowerOutlet, Boolean hasLamp) {
        return seatRepository.findAvailableSeatsWithFeatures(hasWindow, hasPowerOutlet, hasLamp);
    }

    @Override
    public Seat updateSeat(Long seatId, String seatNumber, Seat.SeatType type,
                          Boolean hasWindow, Boolean hasPowerOutlet, Boolean hasLamp,
                          String description, String equipment) {
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        if (!seatOpt.isPresent()) {
            throw new RuntimeException("座位不存在");
        }
        
        Seat seat = seatOpt.get();
        
        // 如果座位号有变化，检查新座位号是否已存在
        if (!seat.getSeatNumber().equals(seatNumber)) {
            Long studyRoomId = seat.getStudyRoom().getId();
            if (seatRepository.existsBySeatNumberAndStudyRoomId(seatNumber, studyRoomId)) {
                throw new RuntimeException("新座位号已存在");
            }
            seat.setSeatNumber(seatNumber);
        }
        
        seat.setType(type);
        seat.setHasWindow(hasWindow != null ? hasWindow : seat.getHasWindow());
        seat.setHasPowerOutlet(hasPowerOutlet != null ? hasPowerOutlet : seat.getHasPowerOutlet());
        seat.setHasLamp(hasLamp != null ? hasLamp : seat.getHasLamp());
        seat.setDescription(description);
        seat.setEquipment(equipment);
        
        return seatRepository.save(seat);
    }

    @Override
    public Seat updateSeatStatus(Long seatId, Seat.SeatStatus status) {
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        if (!seatOpt.isPresent()) {
            throw new RuntimeException("座位不存在");
        }
        
        Seat seat = seatOpt.get();
        seat.setStatus(status);
        
        return seatRepository.save(seat);
    }

    @Override
    public void deleteSeat(Long seatId) {
        // 软删除：设置状态为OUT_OF_ORDER
        updateSeatStatus(seatId, Seat.SeatStatus.OUT_OF_ORDER);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSeatAvailable(Long seatId) {
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        return seatOpt.isPresent() && seatOpt.get().getStatus() == Seat.SeatStatus.AVAILABLE;
    }

    @Override
    public boolean occupySeat(Long seatId) {
        if (!isSeatAvailable(seatId)) {
            return false;
        }
        
        try {
            updateSeatStatus(seatId, Seat.SeatStatus.OCCUPIED);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean releaseSeat(Long seatId) {
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        if (!seatOpt.isPresent()) {
            return false;
        }
        
        Seat seat = seatOpt.get();
        if (seat.getStatus() == Seat.SeatStatus.OCCUPIED || 
            seat.getStatus() == Seat.SeatStatus.RESERVED) {
            try {
                updateSeatStatus(seatId, Seat.SeatStatus.AVAILABLE);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }

    @Override
    public boolean reserveSeat(Long seatId) {
        if (!isSeatAvailable(seatId)) {
            return false;
        }
        
        try {
            updateSeatStatus(seatId, Seat.SeatStatus.RESERVED);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean cancelSeatReservation(Long seatId) {
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        if (!seatOpt.isPresent()) {
            return false;
        }
        
        Seat seat = seatOpt.get();
        if (seat.getStatus() == Seat.SeatStatus.RESERVED) {
            try {
                updateSeatStatus(seatId, Seat.SeatStatus.AVAILABLE);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSeatStatistics(Long seatId) {
        Map<String, Object> stats = new HashMap<>();
        
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        if (!seatOpt.isPresent()) {
            stats.put("error", "座位不存在");
            return stats;
        }
        
        Seat seat = seatOpt.get();
        stats.put("seatId", seat.getId());
        stats.put("seatNumber", seat.getSeatNumber());
        stats.put("studyRoomId", seat.getStudyRoom().getId());
        stats.put("type", seat.getType().toString());
        stats.put("status", seat.getStatus().toString());
        
        // 统计预订信息
        List<Reservation> reservations = reservationRepository.findBySeatId(seatId);
        stats.put("totalReservations", reservations.size());
        
        // 统计活跃预订
        List<Reservation> activeReservations = reservationRepository.findActiveReservations().stream()
            .filter(r -> r.getSeat().getId().equals(seatId))
            .collect(java.util.stream.Collectors.toList());
        stats.put("activeReservations", activeReservations.size());
        
        // 统计已完成预订
        long completedReservations = reservations.stream()
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED)
            .count();
        stats.put("completedReservations", completedReservations);
        
        // 计算使用率（假设以最近30天为准）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Reservation> recentReservations = reservations.stream()
            .filter(r -> r.getCreatedAt().isAfter(thirtyDaysAgo))
            .collect(java.util.stream.Collectors.toList());
        stats.put("recentReservations", recentReservations.size());
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStudyRoomSeatStatistics(Long studyRoomId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计总座位数
        long totalSeats = seatRepository.countByStudyRoomId(studyRoomId);
        stats.put("totalSeats", totalSeats);
        
        // 统计可用座位数
        List<Seat> allSeats = seatRepository.findByStudyRoomId(studyRoomId);
        long availableSeats = allSeats.stream()
            .filter(seat -> seat.getStatus() == Seat.SeatStatus.AVAILABLE)
            .count();
        stats.put("availableSeats", availableSeats);
        
        // 统计占用座位数
        long occupiedSeats = allSeats.stream()
            .filter(seat -> seat.getStatus() == Seat.SeatStatus.OCCUPIED)
            .count();
        stats.put("occupiedSeats", occupiedSeats);
        
        // 统计预订座位数
        long reservedSeats = allSeats.stream()
            .filter(seat -> seat.getStatus() == Seat.SeatStatus.RESERVED)
            .count();
        stats.put("reservedSeats", reservedSeats);
        
        // 统计故障座位数
        long outOfOrderSeats = allSeats.stream()
            .filter(seat -> seat.getStatus() == Seat.SeatStatus.OUT_OF_ORDER)
            .count();
        stats.put("outOfOrderSeats", outOfOrderSeats);
        
        // 计算占用率
        double occupancyRate = totalSeats > 0 ? (double)(occupiedSeats + reservedSeats) / totalSeats * 100 : 0;
        stats.put("occupancyRate", occupancyRate);
        
        // 按类型统计
        Map<String, Long> seatsByType = new HashMap<>();
        for (Seat.SeatType type : Seat.SeatType.values()) {
            long count = allSeats.stream()
                .filter(seat -> seat.getType() == type)
                .count();
            seatsByType.put(type.toString(), count);
        }
        stats.put("seatsByType", seatsByType);
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> findSeatsOrderBySeatNumber(Long studyRoomId) {
        // 使用现有方法获取座位，然后在Service层排序
        List<Seat> seats = seatRepository.findByStudyRoomId(studyRoomId);
        seats.sort((s1, s2) -> s1.getSeatNumber().compareTo(s2.getSeatNumber()));
        return seats;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Seat> findSeatsWithPagination(int page, int size, Long studyRoomId, 
                                              Seat.SeatStatus status, Seat.SeatType type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("seatNumber").ascending());
        
        // 基于现有Repository方法的简化实现
        if (studyRoomId != null) {
            // 获取指定自习室的座位，然后在内存中过滤
            List<Seat> seats = seatRepository.findByStudyRoomId(studyRoomId);
            
            // 应用过滤条件
            if (status != null) {
                seats = seats.stream()
                    .filter(seat -> seat.getStatus() == status)
                    .collect(java.util.stream.Collectors.toList());
            }
            if (type != null) {
                seats = seats.stream()
                    .filter(seat -> seat.getType() == type)
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 排序
            seats.sort((s1, s2) -> s1.getSeatNumber().compareTo(s2.getSeatNumber()));
            
            // 手动分页
            int start = page * size;
            int end = Math.min(start + size, seats.size());
            List<Seat> pageContent = seats.subList(start, end);
            
            return new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, seats.size());
        } else {
            // 没有指定自习室，使用现有方法
            if (status != null) {
                List<Seat> seats = seatRepository.findByStatus(status);
                if (type != null) {
                    seats = seats.stream()
                        .filter(seat -> seat.getType() == type)
                        .collect(java.util.stream.Collectors.toList());
                }
                seats.sort((s1, s2) -> s1.getSeatNumber().compareTo(s2.getSeatNumber()));
                
                int start = page * size;
                int end = Math.min(start + size, seats.size());
                List<Seat> pageContent = seats.subList(start, end);
                
                return new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, seats.size());
            } else if (type != null) {
                List<Seat> seats = seatRepository.findByType(type);
                seats.sort((s1, s2) -> s1.getSeatNumber().compareTo(s2.getSeatNumber()));
                
                int start = page * size;
                int end = Math.min(start + size, seats.size());
                List<Seat> pageContent = seats.subList(start, end);
                
                return new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, seats.size());
            } else {
                return seatRepository.findAll(pageable);
            }
        }
    }
} 