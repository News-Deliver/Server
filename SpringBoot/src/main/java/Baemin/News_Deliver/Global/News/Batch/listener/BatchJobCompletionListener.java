package Baemin.News_Deliver.Global.News.Batch.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobCompletionListener implements JobExecutionListener {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            String sql = """
                DELETE FROM news
                WHERE id IN (
                    SELECT id FROM (
                        SELECT id,
                               ROW_NUMBER() OVER (PARTITION BY title, publisher ORDER BY id) AS rn
                        FROM news
                        WHERE DATE(published_at) = CURDATE() - INTERVAL 1 DAY
                    ) t
                    WHERE t.rn > 1
                );
                
                """;

            int deleted = jdbcTemplate.update(sql);
            log.info("✅ 중복 뉴스 삭제 완료: {}건", deleted);
        }
    }
}
