package Baemin.News_Deliver.Global.Scheduler;

import Baemin.News_Deliver.Domain.Auth.Entity.Auth;
import Baemin.News_Deliver.Domain.Auth.Entity.User;
import Baemin.News_Deliver.Domain.Auth.Repository.AuthRepository;
import Baemin.News_Deliver.Domain.Auth.Repository.UserRepository;
import Baemin.News_Deliver.Domain.Kakao.service.KakaoMessageService;
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

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public void scheduleUser(Long userId, String cron) {

        // 이미 등록된 경우 기존 스케줄 취소 후 재등록
        if (scheduledTasks.containsKey(userId)) {
            cancelUser(userId);
        }

        log.info("[Scheduler] 유저 {} 토큰확인 - {}", userId, LocalDateTime.now());
        String refreshAccessToken = userRepository.findById(userId)
                .flatMap(user -> authRepository.findByUser(user)
                        .map(Auth::getKakaoRefreshToken))
                .orElseThrow(() -> new IllegalArgumentException("유저 또는 리프레시 토큰이 존재하지 않습니다."));

        log.info("[Scheduler] 토큰 확인 {}", refreshAccessToken);

        Runnable task = () -> {
            log.info("[Scheduler] 유저 {} 메시지 발송 시작 - {}", userId, LocalDateTime.now());
            kakaoMessageService.sendKakaoMessage(refreshAccessToken, userId);
        };

        CronTrigger trigger = new CronTrigger(cron);
        ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);
        scheduledTasks.put(userId, future);
    }

    public void cancelUser(Long userId) {
        ScheduledFuture<?> future = scheduledTasks.get(userId);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(userId);
        }
    }

    public void rescheduleUser(Long userId, String newCron) {
        cancelUser(userId);
        scheduleUser(userId, newCron);
    }

}
