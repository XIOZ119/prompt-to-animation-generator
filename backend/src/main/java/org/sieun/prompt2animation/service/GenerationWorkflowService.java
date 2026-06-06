package org.sieun.prompt2animation.service;

import lombok.RequiredArgsConstructor;
import org.sieun.prompt2animation.event.GenerationCreatedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class GenerationWorkflowService {

    private static final long STEP_DELAY_MS = 3000L;

    private final GenerationWorkflowProcessor processor;

    @Async("workflowExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGenerationCreated(GenerationCreatedEvent event) {
        Long generationId = event.generationId();
        try {
            processor.markProcessing(generationId);
            sleep();

            processor.generateScene(generationId);
            sleep();

            processor.generateCutImages(generationId);
            sleep();

            processor.generateCutVideos(generationId);
            sleep();

            processor.mergeVideos(generationId);
        } catch (Exception e) {
            processor.markFailed(generationId, e.getMessage());
        }
    }

    private void sleep() {
        try {
            Thread.sleep(STEP_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
