package com.studyroom.server.repository;

import com.studyroom.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 
 * @author StudyRoom Management System
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名和密码查找用户（用于登录验证）
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    Optional<User> findByUsernameAndPassword(String username, String password);

    /**
     * 根据用户角色查找用户列表
     * @param role 用户角色
     * @return 用户列表
     */
    List<User> findByRole(User.UserRole role);

    /**
     * 根据用户状态查找用户列表
     * @param status 用户状态
     * @return 用户列表
     */
    List<User> findByStatus(User.UserStatus status);

    /**
     * 查找激活状态的用户
     * @return 激活用户列表
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    List<User> findActiveUsers();

    /**
     * 根据真实姓名模糊查询
     * @param realName 真实姓名关键字
     * @return 用户列表
     */
    List<User> findByRealNameContaining(String realName);

    /**
     * 查找指定时间之后注册的用户
     * @param date 注册时间
     * @return 用户列表
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * 查找最近登录的用户
     * @param date 最后登录时间
     * @return 用户列表
     */
    List<User> findByLastLoginAtAfter(LocalDateTime date);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 统计指定角色的用户数量
     * @param role 用户角色
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.UserRole role);

    /**
     * 根据用户名或邮箱查找用户
     * @param username 用户名
     * @param email 邮箱
     * @return 用户信息
     */
    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
} 