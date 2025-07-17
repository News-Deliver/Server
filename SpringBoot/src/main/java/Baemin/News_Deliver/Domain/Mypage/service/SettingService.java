package Baemin.News_Deliver.Domain.Mypage.service;

import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Domain.Mypage.DTO.SettingDTO;
import Baemin.News_Deliver.Domain.Mypage.Entity.Days;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.Entity.SettingBlockKeyword;
import Baemin.News_Deliver.Domain.Mypage.Entity.SettingKeyword;
import Baemin.News_Deliver.Domain.Mypage.Exception.SettingException;
import Baemin.News_Deliver.Domain.Mypage.Repository.DaysRepository;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingBlockKeywordRepository;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingKeywordRepository;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingRepository;
import Baemin.News_Deliver.Global.Exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 뉴스 배달 설정(Setting) 도메인 서비스
 *
 * <p>사용자가 설정한 뉴스 수신 시간, 키워드, 차단 키워드, 요일 등을 저장·수정·조회·삭제하는 비즈니스 로직을 수행합니다.</p>
 *
 * 주요 기능:
 * <ul>
 *     <li>{@link #saveSetting(SettingDTO)} - 설정 저장</li>
 *     <li>{@link #updateSetting(SettingDTO)} - 설정 수정</li>
 *     <li>{@link #getAllSettingsByUserId(Long)} - 사용자별 설정 전체 조회</li>
 *     <li>{@link #deleteSetting(Long, Long)} - 설정 삭제(Soft Delete)</li>
 * </ul>
 *
 * <p>각 설정은 {@link Setting} 엔티티를 중심으로 연관된 키워드, 차단 키워드, 요일 데이터를 함께 처리합니다.</p>
 *
 * @author 김원중
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SettingService {

    private final UserRepository userRepository;
    private final SettingRepository settingRepository;
    private final SettingKeywordRepository settingKeywordRepository;
    private final SettingBlockKeywordRepository settingBlockKeywordRepository;
    private final DaysRepository daysRepository;

    /**
     * 설정 저장 (권한 체크 포함)
     *
     * <p>뉴스 수신 시간, 기간, 키워드/차단 키워드/요일 정보를 포함한 Setting을 저장합니다.</p>
     *
     * @param settingDTO 사용자 설정 DTO (userId가 포함되어야 함)
     * @return 생성된 Setting ID
     */
    @Transactional
    public ResponseEntity<Long> saveSetting(SettingDTO settingDTO) {
        try {
            Setting setting = new Setting();
            setting.setDeliveryTime(settingDTO.getDeliveryTime());
            setting.setStartDate(settingDTO.getStartDate());
            setting.setEndDate(settingDTO.getEndDate());
            setting.setIsDeleted(false);

            User user = userRepository.findById(settingDTO.getUserId())
                    .orElseThrow(() -> new SettingException(ErrorCode.USER_NOT_FOUND));
            setting.setUser(user);

            setting = settingRepository.save(setting);

            saveSettingKeyword(settingDTO, setting);
            saveBlockKeyword(settingDTO, setting);
            saveDays(settingDTO, setting);

            log.info("설정 저장 성공: userId={}, settingId={}", settingDTO.getUserId(), setting.getId());
            return ResponseEntity.ok(setting.getId());

        } catch (SettingException e) {
            throw e; // 이미 정의된 예외는 그대로 재던지기
        } catch (Exception e) {
            log.error("설정 저장 실패: userId={}, error={}", settingDTO.getUserId(), e.getMessage());
            throw new SettingException(ErrorCode.SETTING_CREATION_FAILED);
        }
    }

    /**
     * 설정 수정 (권한 체크 포함)
     *
     * <p>기존 설정의 기본 정보를 수정하고, 기존 키워드/차단 키워드/요일 정보를 삭제 후 새로 저장합니다.</p>
     *
     * @param settingDTO 수정할 설정 정보 (userId가 포함되어야 함)
     * @return 204 No Content 응답
     */
    @Transactional
    public ResponseEntity<Void> updateSetting(SettingDTO settingDTO) {
        try {
            Setting setting = settingRepository.findById(settingDTO.getId())
                    .orElseThrow(() -> new SettingException(ErrorCode.SETTING_NOT_FOUND));

            // 🔐 권한 체크: 현재 사용자가 이 설정의 소유자인지 확인
            if (!setting.getUser().getId().equals(settingDTO.getUserId())) {
                log.warn("권한 없는 설정 수정 시도: userId={}, settingId={}, 실제소유자={}",
                        settingDTO.getUserId(), settingDTO.getId(), setting.getUser().getId());
                throw new SettingException(ErrorCode.SETTING_ACCESS_DENIED);
            }

            // 1. 기본 설정 값 갱신
            setting.setDeliveryTime(settingDTO.getDeliveryTime());
            setting.setStartDate(settingDTO.getStartDate());
            setting.setEndDate(settingDTO.getEndDate());

            // 2. 서브 데이터 삭제
            settingKeywordRepository.deleteBySetting(setting);
            settingBlockKeywordRepository.deleteBySetting(setting);
            daysRepository.deleteBySetting(setting);

            // 3. 재사용 (insert)
            saveSettingKeyword(settingDTO, setting);
            saveBlockKeyword(settingDTO, setting);
            saveDays(settingDTO, setting);

            log.info("설정 수정 성공: userId={}, settingId={}", settingDTO.getUserId(), setting.getId());
            return ResponseEntity.noContent().build();

        } catch (SettingException e) {
            throw e; // 이미 정의된 예외는 그대로 재던지기
        } catch (Exception e) {
            log.error("설정 수정 실패: userId={}, settingId={}, error={}",
                    settingDTO.getUserId(), settingDTO.getId(), e.getMessage());
            throw new SettingException(ErrorCode.SETTING_UPDATE_FAILED);
        }
    }

    /**
     * 사용자별 설정 전체 조회
     *
     * <p>지금 시점을 기준으로 유효한(삭제되지 않은) 설정만 조회합니다.</p>
     *
     * @param userId 사용자 ID
     * @return 사용자 설정 리스트
     */
    @Transactional(readOnly = true)
    public List<SettingDTO> getAllSettingsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SettingException(ErrorCode.USER_NOT_FOUND));

        List<Setting> settingList = settingRepository.findActiveSettings(user, LocalDateTime.now());

        return settingList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 설정 삭제 (Soft Delete) - 권한 체크 포함
     *
     * <p>해당 설정의 키워드/차단 키워드/요일 정보를 먼저 삭제하고,
     * {@code isDeleted = true} 및 {@code endDate = now}로 상태를 변경합니다.</p>
     *
     * @param settingId 설정 ID
     * @param userId 현재 사용자 ID (권한 체크용)
     * @return 204 No Content 응답
     */
    @Transactional
    public ResponseEntity<Void> deleteSetting(Long settingId, Long userId) {
        try {
            Setting setting = settingRepository.findById(settingId)
                    .orElseThrow(() -> new SettingException(ErrorCode.SETTING_NOT_FOUND));

            // 🔐 권한 체크: 현재 사용자가 이 설정의 소유자인지 확인
            if (!setting.getUser().getId().equals(userId)) {
                log.warn("권한 없는 설정 삭제 시도: userId={}, settingId={}, 실제소유자={}",
                        userId, settingId, setting.getUser().getId());
                throw new SettingException(ErrorCode.SETTING_ACCESS_DENIED);
            }

            settingKeywordRepository.deleteBySetting(setting);
            settingBlockKeywordRepository.deleteBySetting(setting);
            daysRepository.deleteBySetting(setting);

            setting.setIsDeleted(true);
            setting.setEndDate(LocalDateTime.now());
            settingRepository.save(setting);

            log.info("설정 삭제 성공: userId={}, settingId={}", userId, settingId);
            return ResponseEntity.noContent().build();

        } catch (SettingException e) {
            throw e; // 이미 정의된 예외는 그대로 재던지기
        } catch (Exception e) {
            log.error("설정 삭제 실패: userId={}, settingId={}, error={}", userId, settingId, e.getMessage());
            throw new SettingException(ErrorCode.SETTING_DELETE_FAILED);
        }
    }

    /**
     * 요일 설정 저장 (내부 메서드)
     */
    private void saveDays(SettingDTO settingDTO, Setting setting) {
        List<Days> daysList = settingDTO.getDays().stream()
                .map(day -> Days.builder()
                        .setting(setting)
                        .deliveryDay(day)
                        .build())
                .toList();

        daysRepository.saveAll(daysList);
    }

    /**
     * 차단 키워드 저장 (내부 메서드)
     */
    private void saveBlockKeyword(SettingDTO settingDTO, Setting setting) {
        List<SettingBlockKeyword> blockKeywords = settingDTO.getBlockKeywords().stream()
                .map(keyword -> SettingBlockKeyword.builder()
                        .setting(setting)
                        .blockKeyword(keyword)
                        .build())
                .toList();

        settingBlockKeywordRepository.saveAll(blockKeywords);
    }

    /**
     * 키워드 저장 (내부 메서드)
     */
    private void saveSettingKeyword(SettingDTO settingDTO, Setting setting) {
        List<SettingKeyword> settingKeywords = settingDTO.getSettingKeywords().stream()
                .map(keyword -> SettingKeyword.builder()
                        .setting(setting)
                        .settingKeyword(keyword)
                        .build())
                .toList();

        settingKeywordRepository.saveAll(settingKeywords);
    }

    /**
     * Setting → SettingDTO 변환 (연관 엔티티 포함)
     */
    private SettingDTO convertToDTO(Setting setting) {
        return SettingDTO.builder()
                .id(setting.getId())
                .deliveryTime(setting.getDeliveryTime())
                .startDate(setting.getStartDate())
                .endDate(setting.getEndDate())
                .userId(setting.getUser().getId())
                .settingKeywords(setting.getKeywords().stream()
                        .map(SettingKeyword::getSettingKeyword)
                        .toList())
                .blockKeywords(setting.getBlockKeywords().stream()
                        .map(SettingBlockKeyword::getBlockKeyword)
                        .toList())
                .days(setting.getDays().stream()
                        .map(Days::getDeliveryDay)
                        .toList())
                .build();
    }

    public List<Setting> getAllSettings() {
        return settingRepository.findAll();
    }
}