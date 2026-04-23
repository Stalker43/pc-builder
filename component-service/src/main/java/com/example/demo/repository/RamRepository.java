package com.example.demo.repository;

import com.example.demo.entity.Ram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RamRepository extends JpaRepository<Ram, Long> {
    // Этот метод достает из базы память по типу (DDR4 или DDR5)
    List<Ram> findByType(String type);
}