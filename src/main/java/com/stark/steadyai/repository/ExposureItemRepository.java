package com.stark.steadyai.repository;

import com.stark.steadyai.entity.ExposureItem;
import com.stark.steadyai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExposureItemRepository extends JpaRepository<ExposureItem, Long> {

    List<ExposureItem> findByUserOrderByCreatedAtDesc(User user);
}
