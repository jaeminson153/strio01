package com.example.strio01.diagnosis.service;

import java.util.List;
import java.util.Map;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.dto.DiagnosisDTO;

public interface DiagnosisService {
    long countProcess();
    List<DiagnosisDTO> listProcess(PageDTO pv);
    void insertProcess(DiagnosisDTO dto);
    DiagnosisDTO contentProcess(long diagId);
    void updateProcess(DiagnosisDTO dto);
    void deleteProcess(long diagId);
    List<DiagnosisDTO> findByXrayId(long xrayId);

    // ★ 추가: by-id 자동 분석 게이트웨이 (스프링→파이썬→DB upsert→프론트)
    Map<String, Object> analyzeByXrayId(long xrayId, Double threshold);

    // ★ 추가: 저장된 최신 결과를 프론트 스키마로 반환
    Map<String, Object> latestResultView(long xrayId);
}
