package com.studyroom.server.service.impl;

import com.studyroom.server.entity.User;
import com.studyroom.server.entity.Reservation;
import com.studyroom.server.repository.UserRepository;
import com.studyroom.server.repository.ReservationRepository;
import com.studyroom.server.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 用户服务实现类
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(String username, String password, String email, String phone, String realName) {
        // 检查用户名和邮箱是否已存在
        if (!isUsernameAvailable(username)) {
            throw new RuntimeException("用户名已存在");
        }
        if (!isEmailAvailable(email)) {
            throw new RuntimeException("邮箱已被使用");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 加密密码
        user.setEmail(email);
        user.setPhone(phone);
        user.setRealName(realName);
        user.setRole(User.UserRole.USER);
        user.setStatus(User.UserStatus.ACTIVE);
        
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // 验证加密后的密码
            if (passwordEncoder.matches(password, user.getPassword()) && user.getStatus() == User.UserStatus.ACTIVE) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findActiveUsers() {
        return userRepository.findActiveUsers();
    }

    @Override
    public User updateUserProfile(Long userId, String email, String phone, String realName) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        
        // 检查新邮箱是否可用（如果邮箱有变化）
        if (!email.equals(user.getEmail()) && !isEmailAvailable(email)) {
            throw new RuntimeException("邮箱已被使用");
        }
        
        user.setEmail(email);
        user.setPhone(phone);
        user.setRealName(realName);
        
        return userRepository.save(user);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return false;
        }
        
        User user = userOpt.get();
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        
        // 设置新密码（加密）
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    @Override
    public User updateUserStatus(Long userId, User.UserStatus status) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户不存在");
        }
        
        User user = userOpt.get();
        user.setStatus(status);
        
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        // 软删除：设置状态为INACTIVE
        updateUserStatus(userId, User.UserStatus.INACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getUserReservationHistory(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getUserActiveReservations(Long userId) {
        return reservationRepository.findActiveReservationsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总预订次数 - 使用现有方法
        int totalReservations = (int) reservationRepository.countByUserId(userId);
        stats.put("totalReservations", totalReservations);
        
        // 已完成预订次数 - 通过状态过滤
        List<Reservation> userReservations = reservationRepository.findByUserId(userId);
        int completedReservations = (int) userReservations.stream()
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED)
            .count();
        stats.put("completedReservations", completedReservations);
        
        // 取消预订次数 - 通过状态过滤
        int cancelledReservations = (int) userReservations.stream()
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.CANCELLED)
            .count();
        stats.put("cancelledReservations", cancelledReservations);
        
        // 当前活跃预订
        int activeReservations = (int) reservationRepository.countActiveReservationsByUserId(userId);
        stats.put("activeReservations", activeReservations);
        
        // 总学习时长（小时）- 通过计算获得
        Double totalStudyHours = userReservations.stream()
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED && 
                        r.getStartTime() != null && r.getEndTime() != null)
            .mapToDouble(r -> java.time.Duration.between(r.getStartTime(), r.getEndTime()).toHours())
            .sum();
        stats.put("totalStudyHours", totalStudyHours);
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findUsersWithPagination(int page, int size, User.UserRole role, User.UserStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // 简化分页实现，使用手动过滤
        List<User> allUsers = userRepository.findAll();
        List<User> filteredUsers = allUsers.stream()
            .filter(user -> role == null || user.getRole() == role)
            .filter(user -> status == null || user.getStatus() == status)
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(java.util.stream.Collectors.toList());
        
        // 手动分页
        int start = page * size;
        int end = Math.min(start + size, filteredUsers.size());
        List<User> pageContent = start < filteredUsers.size() ? 
            filteredUsers.subList(start, end) : new ArrayList<>();
        
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filteredUsers.size());
    }
} 