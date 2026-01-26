package com.xhs.repository;

import com.xhs.entity.BrowserEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrowserEnvironmentRepository extends JpaRepository<BrowserEnvironment, Long> {

    List<BrowserEnvironment> findByUserId(Long userId);
    Optional<BrowserEnvironment> findByUserIdAndIsDefaultTrue(Long userId);
    Optional<BrowserEnvironment> findByUserIdAndIsActiveTrue(Long userId);
}