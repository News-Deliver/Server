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

@Service
@Slf4j
@RequiredArgsConstructor
public class SettingService {

    private final UserRepository userRepository;
    private final SettingRepository settingRepository;
    private final SettingKeywordRepository settingKeywordRepository;
    private final SettingBlockKeywordRepository settingBlockKeywordRepository;
    private final DaysRepository daysRepository;

    @Transactional
    public ResponseEntity<Long> saveSetting(SettingDTO settingDTO) {

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

        return ResponseEntity.ok(setting.getId());
    }

    @Transactional
    public ResponseEntity<Void> updateSetting(SettingDTO settingDTO) {
        Setting setting = settingRepository.findById(settingDTO.getId())
                .orElseThrow(() -> new SettingException(ErrorCode.SETTING_NOT_FOUND));

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

        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @Transactional(readOnly = true)
    public List<SettingDTO> getAllSettingsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SettingException(ErrorCode.USER_NOT_FOUND));

        List<Setting> settingList = settingRepository.findActiveSettings(user, LocalDateTime.now());

        return settingList.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public ResponseEntity<Void> deleteSetting(Long id) {

        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new SettingException(ErrorCode.SETTING_NOT_FOUND));

        settingKeywordRepository.deleteBySetting(setting);
        settingBlockKeywordRepository.deleteBySetting(setting);
        daysRepository.deleteBySetting(setting);

        setting.setIsDeleted(true);
        setting.setEndDate(LocalDateTime.now());
        settingRepository.save(setting);

        return ResponseEntity.noContent().build();
    }

    private void saveDays(SettingDTO settingDTO, Setting setting) {
        List<Days> daysList = settingDTO.getDays().stream()
                .map(day -> Days.builder()
                        .setting(setting)
                        .deliveryDay(day)
                        .build())
                .toList();

        daysRepository.saveAll(daysList);
    }

    private void saveBlockKeyword(SettingDTO settingDTO, Setting setting) {
        List<SettingBlockKeyword> blockKeywords = settingDTO.getBlockKeywords().stream()
                .map(keyword -> SettingBlockKeyword.builder()
                        .setting(setting)
                        .blockKeyword(keyword)
                        .build())
                .toList();

        settingBlockKeywordRepository.saveAll(blockKeywords);
    }

    private void saveSettingKeyword(SettingDTO settingDTO, Setting setting) {
        List<SettingKeyword> settingKeywords = settingDTO.getSettingKeywords().stream()
                .map(keyword -> SettingKeyword.builder()
                        .setting(setting)
                        .settingKeyword(keyword)
                        .build())
                .toList();

        settingKeywordRepository.saveAll(settingKeywords);
    }

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
}
