package com.xhs.repository;

import com.xhs.entity.BrowserFingerprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrowserFingerprintRepository extends JpaRepository<BrowserFingerprint, Long> {

    List<BrowserFingerprint> findByUserId(Long userId);
    Optional<BrowserFingerprint> findByUserIdAndIsDefaultTrue(Long userId);
    Optional<BrowserFingerprint> findByUserIdAndIsActiveTrue(Long userId);
}