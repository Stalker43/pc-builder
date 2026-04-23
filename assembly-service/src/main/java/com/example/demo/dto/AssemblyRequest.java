package com.example.demo.dto;

import lombok.Data;

@Data
public class AssemblyRequest {
    private Long telegramId;
    private Long cpuId;
    private Long mbId;
    private Long ramId;
    private Long gpuId;
    private Long psuId;
    private Long caseId;
}