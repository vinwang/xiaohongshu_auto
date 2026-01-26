package com.xhs.repository;

import com.xhs.entity.PublishHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PublishHistoryRepository extends JpaRepository<PublishHistory, Long> {

    // 根据用户ID查找发布历史
    List<PublishHistory> findByUserId(Long userId);

    // 根据状态查找发布历史
    List<PublishHistory> findByStatus(String status);

    // 根据用户ID和状态查找发布历史
    List<PublishHistory> findByUserIdAndStatus(Long userId, String status);

    // 统计指定时间范围内的发布数量
    long countByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
}