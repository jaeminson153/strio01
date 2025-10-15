package com.example.strio01.diagnosis.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.strio01.diagnosis.dto.DiagnosisDTO;
import com.example.strio01.board.dto.PageDTO;
import com.example.strio01.diagnosis.repository.DiagnosisRepository;
import com.example.strio01.diagnosis.service.DiagnosisService;
import com.example.strio01.common.file.FileUpload;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.proxy.annotation.GetProxy;


//@CrossOrigin("*")

@Slf4j
@RestController
public class DiagnosisController {

    private final DiagnosisRepository diagnosisRepository;
    @Autowired
	private DiagnosisService diagnosisService;
    
    private int currentPage;
    private PageDTO pdto;
   
    
    public DiagnosisController(DiagnosisRepository diagnosisRepository) {
        this.diagnosisRepository = diagnosisRepository;

    }
    
    // http://localhost:8090/notice/list/1
    @GetMapping(value="/notice/list/{currentPage}")
    public ResponseEntity<Map<String, Object>> listExecute(@PathVariable("currentPage") int currentPage){
        Map<String, Object> map = new HashMap<>();
        
    	long totalRecord = diagnosisService.countProcess();
    	log.info("totalRecord: {}", totalRecord);

    	
    	if(totalRecord >=1) {
    		this.currentPage = currentPage;
    		this.pdto = new PageDTO(this.currentPage, totalRecord);
    		
    		map.put("noticeList", diagnosisService.listProcess(pdto));
    		map.put("pv", this.pdto);    		
    	}
    	return ResponseEntity.ok().body(map);
    }//end listExecute()///////////////////////////////////////////////////////////////
    
    //첨부파일이 있을 때 @RequestBody을 선언하면 안된다.
    //답변글일때 ref, reStep, reLevel 담아서 넘겨야 한다.
    @PostMapping("/notice/write")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<String> writeProExecute(DiagnosisDTO dto, HttpServletRequest req){
            
    	diagnosisService.insertProcess(dto);    	
    	return ResponseEntity.ok(String.valueOf(1));
    }//end writeProExecute()//////////////////////////////////////////////////////    
  
    @GetMapping(value="/notice/view/{num}")
    public ResponseEntity<DiagnosisDTO> viewExecute(@PathVariable("num") Long num){
    	DiagnosisDTO noticeDTO = diagnosisService.contentProcess(num);
    	return ResponseEntity.ok(noticeDTO);
    }
    
    @PutMapping(value="/notice/update")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Void> updateExecute(DiagnosisDTO dto, HttpServletRequest req){

    	diagnosisService.updateProcess(dto);
    	return ResponseEntity.ok(null);
    }
    
    @DeleteMapping(value="/notice/delete/{num}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Void> deleteExecute(@PathVariable("num") Long num){
    	diagnosisService.deleteProcess(num);
    	return ResponseEntity.ok(null);
    }
    
    
}//end class











