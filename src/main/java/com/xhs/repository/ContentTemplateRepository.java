package com.xhs.repository;

import com.xhs.entity.ContentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentTemplateRepository extends JpaRepository<ContentTemplate, Long> {
}