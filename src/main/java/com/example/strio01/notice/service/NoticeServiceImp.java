package com.example.strio01.notice.service;

import java.io.File;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.strio01.notice.dto.NoticeDTO;
import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.notice.entity.NoticeEntity;
import com.example.strio01.notice.repository.NoticeRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NoticeServiceImp implements NoticeService {
	@Autowired
	private NoticeRepository noticeRepository;

	public NoticeServiceImp() {

	}

	@Transactional
	@Override
	public long countProcess() {
		long cnt = noticeRepository.count();
		return cnt;
	}

	@Transactional
	@Override
	public List<NoticeDTO> listProcess(PageDTO pv) {
		List<NoticeEntity> listNoticeEntity = noticeRepository.findPagedNoticeByRownum(pv);
		List<NoticeDTO> listNoticeDTO = listNoticeEntity.stream().map(NoticeDTO::toDTO).collect(Collectors.toList());

		return listNoticeDTO;
	}

	@Transactional
	@Override
	public void insertProcess(NoticeDTO dto) {
		long newId = noticeRepository.getNextVal(); // 별도 시퀀스 조회 필요
		dto.setNoticeId(newId);

		dto.setCreatedAt(new Date(System.currentTimeMillis()));

		NoticeEntity noticeEntity = dto.toEntity();
		noticeRepository.save(noticeEntity);
	}

	@Transactional
	@Override
	public NoticeDTO contentProcess(long noticeId) {

		NoticeEntity noticeEntity = noticeRepository.findWithNoticeByNoticeId(noticeId);
		return NoticeDTO.toDTO(noticeEntity);
	}

	@Transactional
	@Override
	public void updateProcess(NoticeDTO dto, String tempDir) {
		String filename = dto.getUpload();
		// 수정할 첨부파일이 있으면
		if (filename != null) {
			String uploadFilename = noticeRepository.getUploadFilename(dto.getNoticeId());
			// 기존에 저장된 첨부파일 있으면
			if (uploadFilename != null) {
				File file = new File(tempDir, uploadFilename);
				file.delete();
			}
		}
		
		NoticeEntity noticeEntity = dto.toEntity();
		noticeRepository.updateNotice(noticeEntity);		
	}

	@Transactional
	@Override
	public void deleteProcess(long noticeId, String tempDir) {
		String  uploadFilename = noticeRepository.getUploadFilename(noticeId);
		
		//파일 첨부가 있으면
		if(uploadFilename != null) {
			File file = new File(tempDir, uploadFilename);
			file.delete();
		}
		noticeRepository.deleteById(noticeId);
	}
}





