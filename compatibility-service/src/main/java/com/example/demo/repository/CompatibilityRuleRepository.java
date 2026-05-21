package com.example.demo.repository;

import com.example.demo.entity.CompatibilityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompatibilityRuleRepository extends JpaRepository<CompatibilityRule, Long> {
    Optional<CompatibilityRule> findByRuleTypeAndValueAAndValueB(String ruleType, String valueA, String valueB);
}