package com.studyroom.server.repository;

import com.studyroom.server.entity.Seat;
import com.studyroom.server.entity.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 座位数据访问接口
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    /**
     * 根据座位号查找座位
     * @param seatNumber 座位号
     * @return 座位信息
     */
    Optional<Seat> findBySeatNumber(String seatNumber);

    /**
     * 根据自习室查找所有座位
     * @param studyRoom 自习室
     * @return 座位列表
     */
    List<Seat> findByStudyRoom(StudyRoom studyRoom);

    /**
     * 根据自习室ID查找所有座位
     * @param studyRoomId 自习室ID
     * @return 座位列表
     */
    List<Seat> findByStudyRoomId(Long studyRoomId);

    /**
     * 根据座位状态查找座位
     * @param status 座位状态
     * @return 座位列表
     */
    List<Seat> findByStatus(Seat.SeatStatus status);

    /**
     * 根据座位类型查找座位
     * @param type 座位类型
     * @return 座位列表
     */
    List<Seat> findByType(Seat.SeatType type);

    /**
     * 查找可用座位
     * @return 可用座位列表
     */
    @Query("SELECT s FROM Seat s WHERE s.status = 'AVAILABLE'")
    List<Seat> findAvailableSeats();

    /**
     * 根据自习室查找可用座位
     * @param studyRoom 自习室
     * @return 可用座位列表
     */
    @Query("SELECT s FROM Seat s WHERE s.studyRoom = :studyRoom AND s.status = 'AVAILABLE'")
    List<Seat> findAvailableSeatsByStudyRoom(@Param("studyRoom") StudyRoom studyRoom);

    /**
     * 根据自习室ID查找可用座位
     * @param studyRoomId 自习室ID
     * @return 可用座位列表
     */
    @Query("SELECT s FROM Seat s WHERE s.studyRoom.id = :studyRoomId AND s.status = 'AVAILABLE'")
    List<Seat> findAvailableSeatsByStudyRoomId(@Param("studyRoomId") Long studyRoomId);

    /**
     * 查找有窗户的座位
     * @return 有窗座位列表
     */
    List<Seat> findByHasWindowTrue();

    /**
     * 查找有电源插座的座位
     * @return 有电源座位列表
     */
    List<Seat> findByHasPowerOutletTrue();

    /**
     * 查找有台灯的座位
     * @return 有台灯座位列表
     */
    List<Seat> findByHasLampTrue();

    /**
     * 根据设备信息模糊查询
     * @param equipment 设备关键字
     * @return 座位列表
     */
    List<Seat> findByEquipmentContaining(String equipment);

    /**
     * 查找指定自习室中指定类型的座位
     * @param studyRoomId 自习室ID
     * @param type 座位类型
     * @return 座位列表
     */
    @Query("SELECT s FROM Seat s WHERE s.studyRoom.id = :studyRoomId AND s.type = :type")
    List<Seat> findByStudyRoomIdAndType(@Param("studyRoomId") Long studyRoomId, 
                                        @Param("type") Seat.SeatType type);

    /**
     * 查找指定自习室中指定状态的座位
     * @param studyRoomId 自习室ID
     * @param status 座位状态
     * @return 座位列表
     */
    @Query("SELECT s FROM Seat s WHERE s.studyRoom.id = :studyRoomId AND s.status = :status")
    List<Seat> findByStudyRoomIdAndStatus(@Param("studyRoomId") Long studyRoomId, 
                                          @Param("status") Seat.SeatStatus status);

    /**
     * 统计指定自习室的座位总数
     * @param studyRoomId 自习室ID
     * @return 座位数量
     */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.studyRoom.id = :studyRoomId")
    long countByStudyRoomId(@Param("studyRoomId") Long studyRoomId);

    /**
     * 统计指定自习室的可用座位数
     * @param studyRoomId 自习室ID
     * @return 可用座位数量
     */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.studyRoom.id = :studyRoomId AND s.status = 'AVAILABLE'")
    long countAvailableSeatsByStudyRoomId(@Param("studyRoomId") Long studyRoomId);

    /**
     * 根据座位号和自习室ID查找座位
     * @param seatNumber 座位号
     * @param studyRoomId 自习室ID
     * @return 座位信息
     */
    @Query("SELECT s FROM Seat s WHERE s.seatNumber = :seatNumber AND s.studyRoom.id = :studyRoomId")
    Optional<Seat> findBySeatNumberAndStudyRoomId(@Param("seatNumber") String seatNumber, 
                                                   @Param("studyRoomId") Long studyRoomId);

    /**
     * 查找满足多个条件的座位（窗户、电源、台灯）
     * @param hasWindow 是否有窗户
     * @param hasPowerOutlet 是否有电源
     * @param hasLamp 是否有台灯
     * @return 座位列表
     */
    @Query("SELECT s FROM Seat s WHERE " +
           "(:hasWindow IS NULL OR s.hasWindow = :hasWindow) AND " +
           "(:hasPowerOutlet IS NULL OR s.hasPowerOutlet = :hasPowerOutlet) AND " +
           "(:hasLamp IS NULL OR s.hasLamp = :hasLamp) AND " +
           "s.status = 'AVAILABLE'")
    List<Seat> findAvailableSeatsWithFeatures(@Param("hasWindow") Boolean hasWindow,
                                               @Param("hasPowerOutlet") Boolean hasPowerOutlet,
                                               @Param("hasLamp") Boolean hasLamp);

    /**
     * 检查座位号在指定自习室中是否存在
     * @param seatNumber 座位号
     * @param studyRoomId 自习室ID
     * @return 是否存在
     */
    @Query("SELECT COUNT(s) > 0 FROM Seat s WHERE s.seatNumber = :seatNumber AND s.studyRoom.id = :studyRoomId")
    boolean existsBySeatNumberAndStudyRoomId(@Param("seatNumber") String seatNumber, 
                                             @Param("studyRoomId") Long studyRoomId);
} 