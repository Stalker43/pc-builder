package com.example.demo.repository;

import com.example.demo.entity.PcCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PcCaseRepository extends JpaRepository<PcCase, Long> {
}
