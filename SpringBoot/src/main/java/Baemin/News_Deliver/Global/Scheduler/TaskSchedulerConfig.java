package Baemin.News_Deliver.Global.Scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * {@code TaskSchedulerConfig}는 사용자 설정에 따라 동적으로 스케줄 작업을 등록하기 위한
 * Spring {@link TaskScheduler} Bean 설정 클래스입니다.
 *
 * <p>ThreadPool 기반의 {@link ThreadPoolTaskScheduler}를 생성하여,
 * 사용자별 뉴스 전송 스케줄 등 다양한 작업을 동시에 처리할 수 있도록 구성합니다.</p>
 */

@Configuration
public class TaskSchedulerConfig {

    /**
     * 사용자 맞춤형 스케줄링을 위한 {@link TaskScheduler} Bean을 등록합니다.
     *
     * <p>
     * Bean 이름은 "customTaskScheduler"이며, 내부적으로 쓰레드 풀을 사용하여
     * 동시 다발적인 스케줄링을 지원합니다.
     * </p>
     *
     * 설정된 내용:
     * <ul>
     *     <li><b>PoolSize:</b> 10 (최대 10개의 작업 동시 실행)</li>
     *     <li><b>ThreadNamePrefix:</b> "scheduled-task-" (디버깅용 식별자)</li>
     *     <li><b>Shutdown 시:</b> 현재 실행 중인 작업이 완료될 때까지 대기</li>
     *     <li><b>대기 시간:</b> 최대 30초까지 shutdown 대기</li>
     * </ul>
     *
     * @return 구성된 {@link TaskScheduler} 인스턴스
     */

    //동적인 스케쥴러를 사용자로부터 받아오기 위해 taskScheduler로 스케쥴을 동적으로 받아옴.
    @Bean("customTaskScheduler")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // 동시 작업 수
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true); //작업 완료까지 스프링종료를 대기
        scheduler.setAwaitTerminationSeconds(30); // 최대 30초 대기
        scheduler.initialize();
        return scheduler;
    }
}
