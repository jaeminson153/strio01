package com.example.strio01.diagnosis.service;

import java.io.File;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.strio01.diagnosis.dto.DiagnosisDTO;
import com.example.strio01.diagnosis.entity.DiagnosisEntity;
import com.example.strio01.diagnosis.repository.DiagnosisRepository;
import com.example.strio01.board.dto.PageDTO;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DiagnosisServiceImp implements DiagnosisService {
	@Autowired
	private DiagnosisRepository diagnosisRepository;

	public DiagnosisServiceImp() {

	}

	@Transactional
	@Override
	public long countProcess() {
		long cnt = diagnosisRepository.count();
		return cnt;
	}

	@Transactional
	@Override
	public List<DiagnosisDTO> listProcess(PageDTO pv) {
		List<DiagnosisEntity> listDiagnosisEntity = diagnosisRepository.findPagedDiagnosisByRownum(pv);
		List<DiagnosisDTO> listDiagnosisDTO = listDiagnosisEntity.stream().map(DiagnosisDTO::toDTO).collect(Collectors.toList());

		return listDiagnosisDTO;
	}

	@Transactional
	@Override
	public void insertProcess(DiagnosisDTO dto) {
		long newId = diagnosisRepository.getNextVal(); // 별도 시퀀스 조회 필요
		dto.setDiagId(newId);

		dto.setCreatedAt(new Date(System.currentTimeMillis()));

		DiagnosisEntity diagnosisEntity = dto.toEntity();
		diagnosisRepository.save(diagnosisEntity);
	}

	@Transactional
	@Override
	public DiagnosisDTO contentProcess(long diagId) {

		DiagnosisEntity diagnosisEntity = diagnosisRepository.findWithDiagnosisByDiagId(diagId);
		return DiagnosisDTO.toDTO(diagnosisEntity);
	}

	@Transactional
	@Override
	public void updateProcess(DiagnosisDTO dto) {
				
		DiagnosisEntity diagnosisEntity = dto.toEntity();
		diagnosisRepository.updateDiagnosis(diagnosisEntity);		
	}

	@Transactional
	@Override
	public void deleteProcess(long diagId) {
		String  uploadFilename = diagnosisRepository.getUploadFilename(diagId);

		diagnosisRepository.deleteById(diagId);
	}
}





