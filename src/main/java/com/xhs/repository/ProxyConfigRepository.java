package com.xhs.repository;

import com.xhs.entity.ProxyConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProxyConfigRepository extends JpaRepository<ProxyConfig, Long> {

    List<ProxyConfig> findByUserId(Long userId);
    Optional<ProxyConfig> findByUserIdAndIsDefaultTrue(Long userId);
    Optional<ProxyConfig> findByUserIdAndIsActiveTrue(Long userId);
}