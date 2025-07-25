package Baemin.News_Deliver.Domain.Mypage.Controller;

import Baemin.News_Deliver.Domain.Auth.Service.AuthService;
import Baemin.News_Deliver.Domain.Mypage.DTO.SettingDTO;
import Baemin.News_Deliver.Domain.Mypage.Exception.SettingException;
import Baemin.News_Deliver.Domain.Mypage.service.SettingService;
import Baemin.News_Deliver.Global.Exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 맞춤 뉴스 설정 API
 *
 * <p>사용자는 원하는 뉴스 배달 시간을 설정하고, 키워드와 요일 등을 기반으로 맞춤형 뉴스 수신을 설정할 수 있습니다.</p>
 */
@RestController
@RequestMapping("/api/setting")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "뉴스 설정 관리", description = "사용자 맞춤 뉴스 배달 설정 API")
public class SettingController {

    private final SettingService settingService;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "내 뉴스 설정 목록 조회", description = "현재 로그인한 사용자의 모든 뉴스 설정을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "설정 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<List<SettingDTO>> getAllSetting(Authentication authentication) {
        String kakaoId = authentication.getName();
        Long userId = authService.findByKakaoId(kakaoId).getId();
        List<SettingDTO> settings = settingService.getAllSettingsByUserId(userId);
        return ResponseEntity.ok(settings);
    }

    /**
     * 사용자 설정 저장
     *
     * @param settingDTO 설정 정보
     * @param authentication 인증 객체 (JWT 토큰에서 추출)
     * @return 생성된 설정 ID
     */
    @PostMapping
    @Operation(summary = "뉴스 설정 저장", description = "새로운 뉴스 배달 설정을 저장합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "설정 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Long> saveSetting(@RequestBody SettingDTO settingDTO,
                                            Authentication authentication) {
        String kakaoId = authentication.getName();
        Long userId = authService.findByKakaoId(kakaoId).getId();
        settingDTO.setUserId(userId);
        Long settingId = settingService.saveSetting(settingDTO);
        return ResponseEntity.ok(settingId);
    }

    /**
     * 사용자 설정 수정
     *
     * @param settingDTO 수정할 설정 정보
     * @param authentication 인증 객체 (JWT 토큰에서 추출)
     * @return 없음 (204 No Content)
     */
    @PutMapping
    @Operation(summary = "뉴스 설정 수정", description = "기존 뉴스 배달 설정을 수정합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "설정 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 설정)"),
            @ApiResponse(responseCode = "404", description = "설정을 찾을 수 없음")
    })
    public ResponseEntity<Void> updateSetting(@RequestBody SettingDTO settingDTO,
                                              Authentication authentication) {
        String kakaoId = authentication.getName();
        Long userId = authService.findByKakaoId(kakaoId).getId();
        settingDTO.setUserId(userId);
        settingService.updateSetting(settingDTO);
        return ResponseEntity.noContent().build();
    }

    /**
     * 사용자 설정 삭제 (Soft Delete)
     *
     * @param id 설정 ID
     * @param authentication 인증 객체 (JWT 토큰에서 추출)
     * @return 없음 (204 No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "뉴스 설정 삭제", description = "지정된 뉴스 배달 설정을 삭제합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "설정 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (다른 사용자의 설정)"),
            @ApiResponse(responseCode = "404", description = "설정을 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteSetting(@PathVariable("id") Long id,
                                              Authentication authentication) {
        String kakaoId = authentication.getName();
        Long userId = authService.findByKakaoId(kakaoId).getId();
        settingService.deleteSetting(id, userId);
        return ResponseEntity.noContent().build();
    }

}