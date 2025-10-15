package com.example.strio01.notice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.notice.entity.NoticeEntity;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {

	@Query(value = """
			SELECT b.* FROM
			            (SELECT rownum AS rm, a.*  FROM
			                (SELECT * FROM notice ORDER BY notice_id DESC )a)b
			          WHERE  b.rm >= :#{#pv.startRow} AND b.rm <= :#{#pv.endRow}
			""", nativeQuery = true)
	List<NoticeEntity> findPagedNoticeByRownum(@Param("pv") PageDTO pv);

	// 시퀀스 증가
	@Query(value = "SELECT notice_id_seq.NEXTVAL FROM dual", nativeQuery = true)
	long getNextVal();
	
	
	//게시글 상세보기	
	@Query(value="SELECT b FROM NoticeEntity b WHERE b.noticeId = :noticeId")
	NoticeEntity findWithNoticeByNoticeId(@Param("noticeId") long noticeId);
	
		
	//  파일첨부 가져오기
	@Query(value = "SELECT b.noticeId  FROM NoticeEntity b WHERE b.noticeId = :noticeId")
	String getUploadFilename(@Param("noticeId") long noticeId);
	
	//게시글 수정
	@Modifying
	@Query("""
			UPDATE NoticeEntity b 
			SET b.title = :#{#noticeEntity.title},
			    b.cont = :#{#noticeEntity.cont}			    
			WHERE b.noticeId = :#{#noticeEntity.noticeId} 
			""")
	void updateNotice(@Param("noticeEntity") NoticeEntity noticeEntity);
	
	
}







