package  com.example.strio01.diagnosis.dto;

import java.sql.Date;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.strio01.diagnosis.entity.DiagnosisEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//import members.dto.MembersDTO;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Component
public class DiagnosisDTO {
	private Long diagId, xrayId;
	private String  doctorId, aiResult, aiImpression,doctorResult,doctorImpression;
	private Date createdAt, updatedAt;
	
	
	// DTO -> Entity
	public DiagnosisEntity toEntity() {
		return DiagnosisEntity.builder()
				.diagId(diagId)
				.xrayId(xrayId)
				.doctorId(doctorId)
				.aiResult(aiResult)
				.aiImpression(aiImpression)
				.doctorResult(doctorResult)
				.doctorImpression(doctorImpression)
				.createdAt(createdAt)
				.updatedAt(updatedAt)
				.build();
	}
	
	
	// Entity -> DTO
	public static DiagnosisDTO toDTO(DiagnosisEntity diagnosisEntity) {
		return DiagnosisDTO.builder()
				.diagId(diagnosisEntity.getDiagId())
				.xrayId(diagnosisEntity.getXrayId())
				.doctorId(diagnosisEntity.getDoctorId())
				.aiResult(diagnosisEntity.getAiResult())
				.aiImpression(diagnosisEntity.getAiImpression())
				.doctorResult(diagnosisEntity.getDoctorResult())
				.doctorImpression(diagnosisEntity.getDoctorImpression())
				.createdAt(diagnosisEntity.getCreatedAt())
				.updatedAt(diagnosisEntity.getUpdatedAt())
				.build();
	}

}//end class
