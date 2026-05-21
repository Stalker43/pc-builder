package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "compatibility_rules")
public class CompatibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ruleType; // "SOCKET" или "DDR"

    @Column(nullable = false)
    private String valueA;    // Например, "LGA1700" или "DDR4"

    @Column(nullable = false)
    private String valueB;    // Например, "LGA1700" или "DDR4"

    @Column(nullable = false)
    private boolean isCompatible;

    public CompatibilityRule() {}

    public CompatibilityRule(String ruleType, String valueA, String valueB, boolean isCompatible) {
        this.ruleType = ruleType;
        this.valueA = valueA;
        this.valueB = valueB;
        this.isCompatible = isCompatible;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getValueA() { return valueA; }
    public void setValueA(String valueA) { this.valueA = valueA; }
    public String getValueB() { return valueB; }
    public void setValueB(String valueB) { this.valueB = valueB; }
    public boolean isCompatible() { return isCompatible; }
    public void setCompatible(boolean compatible) { isCompatible = compatible; }
}