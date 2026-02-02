package com.xhs.repository;

import com.xhs.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    Optional<SystemConfig> findByConfigKeyAndIsActive(String configKey, Boolean isActive);

    List<SystemConfig> findByConfigTypeAndIsActive(String configType, Boolean isActive);

    Optional<SystemConfig> findFirstByConfigKeyOrderByUpdatedAtDesc(String configKey);
}