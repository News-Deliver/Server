package Baemin.News_Deliver.Domain.Mypage.Controller;

import Baemin.News_Deliver.Domain.Mypage.DTO.SettingDTO;
import Baemin.News_Deliver.Domain.Mypage.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 용자 맞춤 뉴스 설정 API
 *
 * <p>사용자는 원하는 뉴스 배달 시간을 설정하고, 키워드와 요일 등을 기반으로 맞춤형 뉴스 수신을 설정할 수 있습니다.</p>
 */
@RestController
@RequestMapping("/api/setting")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    /**
     * 특정 사용자의 모든 설정 조회
     *
     * @return 설정 리스트
     */
    @GetMapping
    public ResponseEntity<List<SettingDTO>> getAllSetting() { // 인증 객체어서 userId 뽑아 올 것.
        // FIXME
        // TODO
        Long userId = 1L;
        List<SettingDTO> settings = settingService.getAllSettingsByUserId(userId);
        return ResponseEntity.ok(settings);
    }

    /**
     *  사용자 설정 저장
     *
     * @param settingDTO 설정 정보
     * @return 생성된 설정 ID
     */
    @PostMapping
    public ResponseEntity<Long> saveSetting(@RequestBody SettingDTO settingDTO) {

        return settingService.saveSetting(settingDTO);
    }

    /**
     * 사용자 설정 수정
     *
     * @param settingDTO 수정할 설정 정보
     * @return 없음 (204 No Content)
     */
    @PutMapping
    public ResponseEntity<Void> updateSetting(@RequestBody SettingDTO settingDTO) {

        return settingService.updateSetting(settingDTO);
    }

    /**
     * 사용자 설정 삭제 (Soft Delete)
     *
     * @param id 설정 ID
     * @return 없음 (204 No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSetting(@PathVariable("id") Long id) {

        return settingService.deleteSetting(id);
    }

}