package com.sbpb.ddobak.server.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 프로필 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequest {

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 1, max = 50, message = "이름은 1자 이상 50자 이하여야 합니다")
    private String name;

    @Size(max = 200, message = "자기소개는 200자 이하여야 합니다")
    private String bio;

    @Size(max = 100, message = "위치는 100자 이하여야 합니다")
    private String location;

    @Size(max = 100, message = "웹사이트는 100자 이하여야 합니다")
    private String website;
} 