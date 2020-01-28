package microservices.book.gamification.event;

import lombok.extern.slf4j.Slf4j;
import microservices.book.gamification.service.GameService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 이벤트를 받고 연관된 비즈니스 로직을 동작시킴
 */
@Component
@Slf4j
public class EventHandler {

    private GameService gameService;

    EventHandler(final GameService gameService) {
        this.gameService = gameService;
    }

    @RabbitListener(queues = "${multiplication.queue}")
    void handleMultiplicationSolved(final MultiplicationSolvedEvent event) { // 메시지 컨버터에서 JSON을 객체로 역직렬화
        log.info("Multiplication Solved Event 수신: {}", event.getMultiplicationResultAttemptId());

        try {
            gameService.newAttemptForUser(event.getUserId(),
                    event.getMultiplicationResultAttemptId(),
                    event.isCorrect());
        } catch (final Exception e) {
            log.error("MultiplicationSolvedEvent 처리 시 에러", e);
            // 해당 이벤트가 다시 큐로 들어가거나 두 번 처리되지 않도록 AmqpRejectAndDontRequeueException 예외를 발생시킴 -> 해당 이벤트는 폐기됨
            // 기본적으로는 exception이 발생하면 해당 이벤트가 큐에서 제거되지 않음
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
