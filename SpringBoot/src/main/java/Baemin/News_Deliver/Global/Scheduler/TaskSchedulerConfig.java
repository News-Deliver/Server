package Baemin.News_Deliver.Global.Scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class TaskSchedulerConfig {
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
