package com.example.strio01.diagnosis.service;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.sql.Date;
import java.time.Duration;
import java.util.*;

import com.example.strio01.diagnosis.dto.DiagnosisDTO;
import com.example.strio01.diagnosis.entity.DiagnosisEntity;
import com.example.strio01.diagnosis.repository.DiagnosisRepository;
import com.example.strio01.xray.entity.XrayImageEntity;
import com.example.strio01.xray.repository.XrayImageRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DiagnosisServiceImp implements DiagnosisService {

    @Autowired private DiagnosisRepository repository;
    @Autowired private XrayImageRepository xrayRepo;

    @Value("${python.base-url:http://localhost:8000}")
    private String pythonBaseUrl;

    private final ObjectMapper om = new ObjectMapper();

    @Transactional @Override
    public long countProcess() { return repository.count(); }

    @Transactional @Override
    public List<DiagnosisDTO> listProcess(com.example.strio01.board.dto.PageDTO pv) {
        return repository.findPagedDiagnosis(pv).stream().map(DiagnosisDTO::toDTO).toList();
    }

    @Transactional @Override
    public void insertProcess(DiagnosisDTO dto) {
        long newId = repository.getNextVal();
        dto.setDiagId(newId);
        dto.setCreatedAt(new Date(System.currentTimeMillis()));
        DiagnosisEntity entity = dto.toEntity();
        repository.save(entity);
    }

    @Transactional @Override
    public DiagnosisDTO contentProcess(long diagId) {
        DiagnosisEntity entity = repository.findByDiagId(diagId);
        return DiagnosisDTO.toDTO(entity);
    }

    @Transactional @Override
    public void updateProcess(DiagnosisDTO dto) {
        dto.setUpdatedAt(new Date(System.currentTimeMillis()));
        // 단순 필드 업데이트는 네이티브 업데이트 대신 save 사용도 가능하나 기존 로직 유지
        repository.save(dto.toEntity());
    }

    @Transactional @Override
    public void deleteProcess(long diagId) {
        repository.deleteById(diagId);
    }

    @Transactional @Override
    public List<DiagnosisDTO> findByXrayId(long xrayId) {
        return repository.findByXrayId(xrayId).stream().map(DiagnosisDTO::toDTO).toList();
    }

    // ---------- 게이트웨이 핵심 ----------

    @Transactional
    @Override
    public Map<String, Object> analyzeByXrayId(long xrayId, Double thresholdOpt) {
        // 1) XRAY_ID → 파일경로
        XrayImageEntity img = xrayRepo.findById(xrayId).orElse(null);
        if (img == null || img.getFilePath() == null) {
            throw new RuntimeException("XRAY_IMAGE 경로를 찾지 못했습니다. xrayId=" + xrayId);
        }
        Path path = Paths.get(img.getFilePath());
        if (!Files.exists(path)) {
            throw new RuntimeException("X-ray 파일이 존재하지 않습니다: " + path);
        }

        // 2) 파이썬 /api/analyze (multipart) 호출
        String boundary = "----StrioBoundary" + System.currentTimeMillis();
        byte[] body = buildMultipartBody(boundary, path, thresholdOpt);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(java.net.URI.create(pythonBaseUrl + "/api/analyze"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        Map<String, Object> py = doJson(client, req);

        // 3) 파이썬 응답 파싱
        boolean ok = Boolean.TRUE.equals(py.get("ok"));
        if (!ok) throw new RuntimeException("파이썬 분석 실패: " + safeTrunc(py.get("error"), 300));

        Double predProb = toDouble(py.get("pred_prob"));
        String predLabel = (String) py.get("pred_label");
        Double usedTh = toDouble(py.get("threshold_used"));
        String camLayer = (String) py.get("target_layer_used");

        Map<String, Object> images = asMap(py.get("images"));
        String originalUrl = images != null ? (String) images.get("original") : null;
        String overlayUrl  = images != null ? (String) images.get("overlay")  : null;

        // 4) (선택) LLM 요약 → AI_IMPRESSION
        String aiImpression = null;
        try {
            if (originalUrl != null) {
                aiImpression = callLlmSummarize(client, predLabel, predProb, originalUrl);
            }
        } catch (Exception e) {
            log.warn("LLM summarize 실패(무시): {}", e.toString());
        }

        // 5) DB INSERT (정책: SYSTEM/PENDING)
        long newId = repository.getNextVal();
        DiagnosisEntity toSave = DiagnosisEntity.builder()
                .diagId(newId)
                .xrayId(xrayId)
                .doctorId("SYSTEM")                 // 정책
                .aiResult(predLabel)
                .aiImpression(aiImpression)
                .doctorResult("PENDING")            // 정책
                .doctorImpression(null)
                .createdAt(new Date(System.currentTimeMillis()))
                .updatedAt(null)
                .build();
        repository.save(toSave);

        // 6) 프론트 기대 스키마로 매핑해서 반환
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("xrayId", xrayId);
        resp.put("pred", predLabel);
        resp.put("prob", predProb);
        resp.put("overlayUrl", overlayUrl);
        resp.put("originalUrl", originalUrl);
        resp.put("camLayer", camLayer);
        resp.put("threshold", usedTh);
        return resp;
    }

    @Transactional
    @Override
    public Map<String, Object> latestResultView(long xrayId) {
        DiagnosisEntity e = repository.findLatestByXrayId(xrayId);
        if (e == null) return Collections.emptyMap();

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("xrayId", xrayId);
        resp.put("pred", e.getAiResult()); // 확정전 기본은 AI 결과
        resp.put("prob", null);            // DB에 확률 저장 스키마가 없으므로 null
        resp.put("overlayUrl", null);      // 이미지 Base64는 DB 저장 대상이 아니므로 null
        resp.put("originalUrl", null);
        resp.put("camLayer", null);
        resp.put("threshold", null);
        return resp;
    }

    // ---------- Helpers ----------

    private String safeTrunc(Object o, int n) {
        String s = String.valueOf(o);
        return s.length() > n ? s.substring(0, n) + "..." : s;
    }

    private Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number num) return num.doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private Map<String, Object> asMap(Object o) {
        if (o == null) return null;
        if (o instanceof Map<?,?> m) {
            //noinspection unchecked
            return (Map<String, Object>) m;
        }
        return null;
    }

    private Map<String, Object> doJson(HttpClient client, HttpRequest req) {
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                throw new RuntimeException("HTTP " + res.statusCode() + " " + safeTrunc(res.body(), 300));
            }
            return om.readValue(res.body(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("파이썬 호출 실패: " + e.getMessage(), e);
        }
    }

    private byte[] buildMultipartBody(String boundary, Path file, Double threshold) {
        // 간단한 수동 multipart 생성
        try {
            String sep = "--" + boundary + "\r\n";
            String end = "--" + boundary + "--\r\n";

            byte[] fileBytes = Files.readAllBytes(file);
            String fileHeader =
                    "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getFileName() + "\"\r\n" +
                    "Content-Type: " + MediaType.IMAGE_PNG_VALUE + "\r\n\r\n";

            StringBuilder sb = new StringBuilder();
            if (threshold != null) {
                sb.append(sep);
                sb.append("Content-Disposition: form-data; name=\"threshold\"\r\n\r\n");
                sb.append(threshold).append("\r\n");
            }
            sb.append(sep).append(fileHeader);

            byte[] head = sb.toString().getBytes();
            byte[] tail = ("\r\n" + end).getBytes();

            byte[] all = new byte[head.length + fileBytes.length + tail.length];
            System.arraycopy(head, 0, all, 0, head.length);
            System.arraycopy(fileBytes, 0, all, head.length, fileBytes.length);
            System.arraycopy(tail, 0, all, head.length + fileBytes.length, tail.length);
            return all;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String callLlmSummarize(HttpClient client, String predLabel, Double predProb, String dataUrl) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("pred_label", predLabel);
            body.put("pred_prob", predProb);
            body.put("image_data_url", dataUrl);

            String json = om.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(pythonBaseUrl + "/api/llm-summarize"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            Map<String, Object> res = doJson(client, req);
            if (Boolean.TRUE.equals(res.get("ok"))) {
                Object s = res.get("summary");
                return s != null ? String.valueOf(s) : null;
            }
        } catch (Exception e) {
            log.warn("LLM summarize 호출 오류: {}", e.toString());
        }
        return null;
    }
}
