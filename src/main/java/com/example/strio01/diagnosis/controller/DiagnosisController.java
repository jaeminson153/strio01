package com.example.strio01.diagnosis.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.dto.DiagnosisDTO;
import com.example.strio01.diagnosis.service.DiagnosisService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("")  // 루트 경로, React .env: REACT_APP_API_PREFIX=""
public class DiagnosisController {

    @Autowired
    private DiagnosisService service;

    // ---------------- 기존 기능 ----------------
    @GetMapping("/diagnosis/list/{page}")
    public ResponseEntity<Map<String, Object>> list(@PathVariable("page") int page) {
        long total = service.countProcess();
        Map<String, Object> map = new HashMap<>();
        if (total > 0) {
            PageDTO pv = new PageDTO(page, total);
            map.put("diagnosisList", service.listProcess(pv));
            map.put("pv", pv);
        }
        return ResponseEntity.ok(map);
    }

    @GetMapping("/diagnosis/view/{id}")
    public ResponseEntity<DiagnosisDTO> view(@PathVariable("id") long id) {
        DiagnosisDTO dto = service.contentProcess(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/diagnosis/xray/{xrayId}")
    public ResponseEntity<List<DiagnosisDTO>> findByXray(@PathVariable("xrayId") long xrayId) {
        return ResponseEntity.ok(service.findByXrayId(xrayId));
    }

    @PostMapping("/diagnosis/write")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<String> write(@RequestBody DiagnosisDTO dto) {
        service.insertProcess(dto);
        return ResponseEntity.ok("1");
    }

    @PutMapping("/diagnosis/update")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<Void> update(@RequestBody DiagnosisDTO dto) {
        service.updateProcess(dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/diagnosis/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        service.deleteProcess(id);
        return ResponseEntity.ok().build();
    }

    // ---------------- 프론트 호환 게이트웨이 ----------------
    // React Diagnosis.js → POST /api/analyze/by-id
    @PostMapping("/api/analyze/by-id")
    public ResponseEntity<Map<String, Object>> analyzeById(@RequestBody AnalyzeReq req) {
        if (req == null || req.getXrayId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "xrayId required"));
        }

        log.info("📡 Received analyze request for XrayId={}", req.getXrayId());
        Map<String, Object> resp = service.analyzeByXrayId(req.getXrayId(), req.getThreshold());

        // Flask가 JSON을 반환하므로 그대로 프록시
        if (resp == null || resp.isEmpty()) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Python model server no response"));
        }

        return ResponseEntity.ok(resp);
    }

    // React Diagnosis.js → GET /api/analyze/result?xrayId=5012
    @GetMapping("/api/analyze/result")
    public ResponseEntity<Map<String, Object>> result(@RequestParam("xrayId") long xrayId) {
        Map<String, Object> resp = service.latestResultView(xrayId);
        if (resp == null || resp.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Result not found for XrayId=" + xrayId));
        }
        return ResponseEntity.ok(resp);
    }

    // (옵션) RESTful 대안
    @PostMapping("/api/diagnoses/{xrayId}/analyze")
    public ResponseEntity<Map<String, Object>> analyzeAlt(
            @PathVariable long xrayId,
            @RequestParam(value = "threshold", required = false) Double th) {
        Map<String, Object> resp = service.analyzeByXrayId(xrayId, th);
        return ResponseEntity.ok(resp);
    }

    // 의사 확정 입력 (doctorResult NOT NULL 보장)
    @PutMapping("/api/diagnoses/{diagId}/doctor")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<Void> doctorFinalize(@PathVariable long diagId,
                                               @RequestBody DoctorFinalizeReq req) {
        DiagnosisDTO cur = service.contentProcess(diagId);
        if (cur == null)
            return ResponseEntity.notFound().build();

        cur.setDoctorId(Optional.ofNullable(req.getDoctorId()).orElse(cur.getDoctorId()));
        cur.setDoctorResult(Optional.ofNullable(req.getDoctorResult()).orElse(cur.getDoctorResult()));
        cur.setDoctorImpression(req.getDoctorImpression());
        service.updateProcess(cur);
        return ResponseEntity.ok().build();
    }

    // ---------- Request DTOs ----------
    public record AnalyzeReq(Long xrayId, Double threshold) {
        public Long getXrayId() { return xrayId; }
        public Double getThreshold() { return threshold; }
    }

    public static class DoctorFinalizeReq {
        private String doctorId;
        private String doctorResult;
        private String doctorImpression;

        public String getDoctorId() { return doctorId; }
        public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
        public String getDoctorResult() { return doctorResult; }
        public void setDoctorResult(String doctorResult) { this.doctorResult = doctorResult; }
        public String getDoctorImpression() { return doctorImpression; }
        public void setDoctorImpression(String doctorImpression) { this.doctorImpression = doctorImpression; }
    }
}
