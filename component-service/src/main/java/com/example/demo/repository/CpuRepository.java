package com.example.demo.repository;

import com.example.demo.entity.Cpu; // ЕСЛИ ЭТА СТРОЧКА ГОРИТ КРАСНЫМ, удали её и нажми Alt+Enter на слове Cpu ниже
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CpuRepository extends JpaRepository<Cpu, Long> {
}