package com.studyroom.server.service;

import com.studyroom.server.entity.User;
import com.studyroom.server.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 * 定义用户管理相关的业务逻辑
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
public interface UserService {
    
    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @param phone 手机号
     * @param realName 真实姓名
     * @return 注册后的用户对象
     */
    User registerUser(String username, String password, String email, String phone, String realName);
    
    /**
     * 用户登录验证
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户对象，失败返回null
     */
    User authenticateUser(String username, String password);
    
    /**
     * 更新用户最后登录时间
     * @param userId 用户ID
     */
    void updateLastLoginTime(Long userId);
    
    /**
     * 根据ID查找用户
     * @param userId 用户ID
     * @return 用户对象
     */
    Optional<User> findById(Long userId);
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户对象
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 获取所有活跃用户
     * @return 活跃用户列表
     */
    List<User> findActiveUsers();
    
    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param email 新邮箱
     * @param phone 新手机号
     * @param realName 新真实姓名
     * @return 更新后的用户对象
     */
    User updateUserProfile(Long userId, String email, String phone, String realName);
    
    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 修改用户状态（管理员功能）
     * @param userId 用户ID
     * @param status 新状态
     * @return 更新后的用户对象
     */
    User updateUserStatus(Long userId, User.UserStatus status);
    
    /**
     * 删除用户（软删除，设置为INACTIVE状态）
     * @param userId 用户ID
     */
    void deleteUser(Long userId);
    
    /**
     * 获取用户的预订历史
     * @param userId 用户ID
     * @return 预订记录列表
     */
    List<Reservation> getUserReservationHistory(Long userId);
    
    /**
     * 获取用户的活跃预订
     * @param userId 用户ID
     * @return 活跃预订列表
     */
    List<Reservation> getUserActiveReservations(Long userId);
    
    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return 是否可用
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * 检查邮箱是否可用
     * @param email 邮箱
     * @return 是否可用
     */
    boolean isEmailAvailable(String email);
    
    /**
     * 获取用户统计信息
     * @param userId 用户ID
     * @return 统计信息Map
     */
    java.util.Map<String, Object> getUserStatistics(Long userId);
    
    /**
     * 分页查询用户
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param role 角色筛选（可选）
     * @param status 状态筛选（可选）
     * @return 用户分页结果
     */
    org.springframework.data.domain.Page<User> findUsersWithPagination(
        int page, int size, User.UserRole role, User.UserStatus status);
} 