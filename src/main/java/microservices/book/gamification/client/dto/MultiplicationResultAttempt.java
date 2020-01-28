package microservices.book.gamification.client.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import microservices.book.gamification.client.MultiplicationResultAttemptDeserializer;

/**
 * User가 곱셈을 푼 답안을 정의한 클래스
 */
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
// @RestTemplate의 메시지 컨버터가 JSON 데이터를 읽어 역직렬화할 때 지정된 클래스를 사용하게 한다. JSON 구조가 자바 클래스 구조와 일치하지 않기 때문에 별도의 deserializer가 필요하다.
@JsonDeserialize(using = MultiplicationResultAttemptDeserializer.class)
public final class MultiplicationResultAttempt {

    private final String userAlias;
    private final int multiplicationFactorA;
    private final int multiplicationFactorB;
    private final int resultAttempt;

    private final boolean correct;

    // JSON 역직렬화 / JPA 를 위한 빈 생성자
    MultiplicationResultAttempt() {
        userAlias = null;
        multiplicationFactorA = -1;
        multiplicationFactorB = -1;
        resultAttempt = -1;
        correct = false;
    }
}
