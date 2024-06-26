package kernel.jdon.modulebatch.job.jd.reader.fetchmanager;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import kernel.jdon.modulebatch.global.config.ScrapingWantedProperties;
import kernel.jdon.modulebatch.global.exception.BatchException;
import kernel.jdon.modulebatch.global.exception.BatchServerErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@StepScope
@Component
@RequiredArgsConstructor
public class JobListFetchManager {
    private final ScrapingWantedProperties scrapingWantedProperties;
    private int offset = 0;
    private int sleepCount = 0;

    public int getOffset() {
        return this.offset;
    }

    public void incrementOffset() {
        this.offset += scrapingWantedProperties.getJobListOffset();
        incrementSleepCounter();
        if (isSleepRequired()) {
            performSleep();
            resetSleepCounter();
        }
    }

    private void incrementSleepCounter() {
        sleepCount++;
    }

    private void resetSleepCounter() {
        sleepCount = 0;
    }

    private boolean isSleepRequired() {
        return this.sleepCount == scrapingWantedProperties.getJobListSleepCount();
    }

    private void performSleep() {
        try {
            Thread.sleep(scrapingWantedProperties.getSleepTimeMillis());
        } catch (InterruptedException e) {
            log.error("JobListFetchManager.performSleep {}", "Thread sleep 중 Error 발생");
            throw new BatchException(BatchServerErrorCode.INTERNAL_SERVER_ERROR_THREAD_SLEEP);
        }
    }
}
