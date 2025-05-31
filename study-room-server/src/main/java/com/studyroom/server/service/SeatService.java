package com.studyroom.server.service;

import com.studyroom.server.entity.Seat;
import com.studyroom.server.entity.StudyRoom;

import java.util.List;
import java.util.Optional;

/**
 * 座位服务接口
 * 定义座位管理相关的业务逻辑
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
public interface SeatService {
    
    /**
     * 创建新座位
     * @param seatNumber 座位号
     * @param studyRoomId 自习室ID
     * @param type 座位类型
     * @param hasWindow 是否有窗户
     * @param hasPowerOutlet 是否有电源插座
     * @param hasLamp 是否有台灯
     * @param description 描述
     * @param equipment 设备
     * @return 创建的座位对象
     */
    Seat createSeat(String seatNumber, Long studyRoomId, Seat.SeatType type,
                    Boolean hasWindow, Boolean hasPowerOutlet, Boolean hasLamp,
                    String description, String equipment, Integer rowNum, Integer colNum);
    
    /**
     * 根据ID查找座位
     * @param seatId 座位ID
     * @return 座位对象
     */
    Optional<Seat> findById(Long seatId);
    
    /**
     * 获取所有可用座位
     * @return 可用座位列表
     */
    List<Seat> findAvailableSeats();
    
    /**
     * 根据自习室查找座位
     * @param studyRoomId 自习室ID
     * @return 座位列表
     */
    List<Seat> findSeatsByStudyRoom(Long studyRoomId);
    
    /**
     * 根据自习室查找可用座位
     * @param studyRoomId 自习室ID
     * @return 可用座位列表
     */
    List<Seat> findAvailableSeatsByStudyRoom(Long studyRoomId);
    
    /**
     * 根据座位类型查找座位
     * @param type 座位类型
     * @return 座位列表
     */
    List<Seat> findSeatsByType(Seat.SeatType type);
    
    /**
     * 查找有窗户的座位
     * @return 有窗户的座位列表
     */
    List<Seat> findSeatsWithWindow();
    
    /**
     * 根据特征查找可用座位
     * @param hasWindow 是否需要窗户
     * @param hasPowerOutlet 是否需要电源插座
     * @param hasLamp 是否需要台灯
     * @return 符合条件的可用座位列表
     */
    List<Seat> findAvailableSeatsWithFeatures(Boolean hasWindow, Boolean hasPowerOutlet, Boolean hasLamp);
    
    /**
     * 更新座位信息
     * @param seatId 座位ID
     * @param seatNumber 新座位号
     * @param type 新座位类型
     * @param hasWindow 是否有窗户
     * @param hasPowerOutlet 是否有电源插座
     * @param hasLamp 是否有台灯
     * @param description 新描述
     * @param equipment 新设备
     * @return 更新后的座位对象
     */
    Seat updateSeat(Long seatId, String seatNumber, Seat.SeatType type,
                    Boolean hasWindow, Boolean hasPowerOutlet, Boolean hasLamp,
                    String description, String equipment);
    
    /**
     * 更新座位状态
     * @param seatId 座位ID
     * @param status 新状态
     * @return 更新后的座位对象
     */
    Seat updateSeatStatus(Long seatId, Seat.SeatStatus status);
    
    /**
     * 删除座位（软删除，设置为OUT_OF_ORDER状态）
     * @param seatId 座位ID
     */
    void deleteSeat(Long seatId);
    
    /**
     * 检查座位是否可用
     * @param seatId 座位ID
     * @return 是否可用
     */
    boolean isSeatAvailable(Long seatId);
    
    /**
     * 占用座位
     * @param seatId 座位ID
     * @return 是否成功占用
     */
    boolean occupySeat(Long seatId);
    
    /**
     * 释放座位
     * @param seatId 座位ID
     * @return 是否成功释放
     */
    boolean releaseSeat(Long seatId);
    
    /**
     * 预订座位
     * @param seatId 座位ID
     * @return 是否成功预订
     */
    boolean reserveSeat(Long seatId);
    
    /**
     * 取消座位预订
     * @param seatId 座位ID
     * @return 是否成功取消
     */
    boolean cancelSeatReservation(Long seatId);
    
    /**
     * 获取座位统计信息
     * @param seatId 座位ID
     * @return 统计信息Map
     */
    java.util.Map<String, Object> getSeatStatistics(Long seatId);
    
    /**
     * 获取自习室座位统计
     * @param studyRoomId 自习室ID
     * @return 座位统计信息
     */
    java.util.Map<String, Object> getStudyRoomSeatStatistics(Long studyRoomId);
    
    /**
     * 按座位号排序查找座位
     * @param studyRoomId 自习室ID
     * @return 排序后的座位列表
     */
    List<Seat> findSeatsOrderBySeatNumber(Long studyRoomId);
    
    /**
     * 分页查询座位
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param studyRoomId 自习室ID筛选（可选）
     * @param status 状态筛选（可选）
     * @param type 类型筛选（可选）
     * @return 座位分页结果
     */
    org.springframework.data.domain.Page<Seat> findSeatsWithPagination(
        int page, int size, Long studyRoomId, Seat.SeatStatus status, Seat.SeatType type);
} 