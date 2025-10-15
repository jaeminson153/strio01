package com.example.strio01.diagnosis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.entity.DiagnosisEntity;

@Repository
public interface DiagnosisRepository extends JpaRepository<DiagnosisEntity, Long> {

	@Query(value = """
			SELECT b.* FROM
			            (SELECT rownum AS rm, a.*  FROM
			                (SELECT * FROM diagnosis ORDER BY diag_id DESC )a)b
			          WHERE  b.rm >= :#{#pv.startRow} AND b.rm <= :#{#pv.endRow}
			""", nativeQuery = true)
	List<DiagnosisEntity> findPagedDiagnosisByRownum(@Param("pv") PageDTO pv);

	// 시퀀스 증가
	@Query(value = "SELECT diag_id_seq.NEXTVAL FROM dual", nativeQuery = true)
	long getNextVal();
	
	
	//게시글 상세보기	
	@Query(value="SELECT b FROM DiagnosisEntity b WHERE b.diagId = :diagId")
	DiagnosisEntity findWithDiagnosisByDiagId(@Param("noticeId") long diagId);
	
		
	//  파일첨부 가져오기
	@Query(value = "SELECT b.diagId  FROM DiagnosisEntity b WHERE b.diagId = :diagId")
	String getUploadFilename(@Param("diagId") long diagId);
	
	//게시글 수정
	@Modifying
	@Query("""
			UPDATE DiagnosisEntity b 
			SET b.title = :#{#diagnosisEntity.doctorResult},
			    b.cont = :#{#diagnosisEntity.doctorImpression}			    
			WHERE b.noticeId = :#{#diagnosisEntity.diagId} 
			""")
	void updateDiagnosis(@Param("noticeEntity") DiagnosisEntity diagnosisEntity);
	
	
}







