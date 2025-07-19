package Baemin.News_Deliver.Global.Scheduler;

import Baemin.News_Deliver.Domain.Auth.Entity.Auth;
import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Auth.Repository.AuthRepository;
import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Domain.Kakao.service.KakaoMessageService;
import Baemin.News_Deliver.Domain.Kakao.service.KakaoSchedulerService;
import Baemin.News_Deliver.Domain.Mypage.Entity.Setting;
import Baemin.News_Deliver.Domain.Mypage.Repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerService {

    @Qualifier("customTaskScheduler") //커스텀한 테스크 스케쥴러로 등록함.
    private final TaskScheduler taskScheduler;

    private final KakaoMessageService kakaoMessageService;
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final SettingRepository settingRepository;

    // userId-settingId 조합으로 key 관리
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final KakaoSchedulerService kakaoSchedulerService;

    @Transactional
    public void scheduleUser(Setting setting) {
        Long settingId = setting.getId();

        // Fetch Join으로 days 미리 가져와서 LazyInitializationException 방지
        setting = settingRepository.findByIdWithDays(settingId)
                .orElseThrow(() -> new IllegalArgumentException("설정 정보가 존재하지 않습니다."));

        Long userId = setting.getUser().getId();
        String taskKey = generateTaskKey(userId, settingId);

        // 이미 등록된 경우 기존 스케줄 취소
        if (scheduledTasks.containsKey(taskKey)) {
            cancelUser(userId, settingId);
        }

        log.info("[Scheduler] 유저 {} / setting {} 토큰 확인 중 - {}", userId, settingId, LocalDateTime.now());

        String refreshAccessToken = userRepository.findById(userId)
                .flatMap(user -> authRepository.findByUser(user)
                        .map(Auth::getKakaoRefreshToken))
                .orElseThrow(() -> new IllegalArgumentException("유저 또는 리프레시 토큰이 존재하지 않습니다."));

        log.info("[Scheduler] 토큰 확인 완료 - {}", refreshAccessToken);

        String cron = kakaoSchedulerService.getCron(setting);

        Runnable task = () -> {
            log.info("[Scheduler] 유저 {} / setting {} 메시지 발송 트리거 - {}", userId, settingId, LocalDateTime.now());

            try {
                Optional<Setting> optionalSetting = settingRepository.findById(settingId);

                if (optionalSetting.isEmpty()) {
                    log.warn("[Scheduler] 유저 {} / setting {} 설정 정보가 없음", userId, settingId);
                    return;
                }

                Setting settings = optionalSetting.get();

                log.info("[Scheduler] 유저 {} / setting {} 키워드: {}, 제외 키워드: {}",
                        userId, settingId, settings.getKeywords(), settings.getBlockKeywords());

                kakaoMessageService.sendKakaoMessage(refreshAccessToken, userId);

            } catch (Exception e) {
                log.error("[Scheduler] 유저 {} / setting {} 메시지 발송 중 예외 발생: {}", userId, settingId, e.getMessage(), e);
            }
        };

        CronTrigger trigger = new CronTrigger(cron);
        ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);
        scheduledTasks.put(taskKey, future);

        log.info("[Scheduler] 유저 {} / setting {} 스케줄 등록 완료 (cron: {})", userId, settingId, cron);
    }


    public void cancelUser(Long userId, Long settingId) {
        String taskKey = generateTaskKey(userId, settingId);
        ScheduledFuture<?> future = scheduledTasks.get(taskKey);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(taskKey);
            log.info("[Scheduler] 유저 {} / setting {} 스케줄 취소됨", userId, settingId);
        }
    }

    //중복 스케쥴러 삭제 메서드용
    private String generateTaskKey(Long userId, Long settingId) {
        return userId + "-" + settingId;
    }

}
