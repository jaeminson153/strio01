package com.example.strio01.diagnosis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.entity.DiagnosisEntity;

@Repository
public interface DiagnosisRepository extends JpaRepository<DiagnosisEntity, Long> {

    @Query(value = """
    SELECT b.* FROM (
      SELECT rownum AS rm, a.*
      FROM (SELECT * FROM DIAGNOSIS ORDER BY DIAG_ID DESC) a
    ) b
    WHERE b.rm >= :#{#pv.startRow} AND b.rm <= :#{#pv.endRow}
    """, nativeQuery = true)
    List<DiagnosisEntity> findPagedDiagnosis(@Param("pv") PageDTO pv);

    @Query(value = "SELECT DIAG_ID_SEQ.NEXTVAL FROM dual", nativeQuery = true)
    long getNextVal();

    @Query(value = "SELECT * FROM DIAGNOSIS WHERE DIAG_ID = :id", nativeQuery = true)
    DiagnosisEntity findByDiagId(@Param("id") long id);

    @Query(value = "SELECT * FROM DIAGNOSIS WHERE XRAY_ID = :xrayId ORDER BY DIAG_ID DESC", nativeQuery = true)
    List<DiagnosisEntity> findByXrayId(@Param("xrayId") long xrayId);

    // 최신 1건
    @Query(value = """
      SELECT * FROM (
        SELECT * FROM DIAGNOSIS WHERE XRAY_ID = :xrayId ORDER BY DIAG_ID DESC
      ) WHERE ROWNUM = 1
    """, nativeQuery = true)
    DiagnosisEntity findLatestByXrayId(@Param("xrayId") long xrayId);
}
