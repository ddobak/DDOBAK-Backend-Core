package com.sbpb.ddobak.server.domain.documentProcess.dto.ocr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrRequest {
    
    private List<MultipartFile> files;
    private String contractType;
} 