package microservices.book.gamification.repository;

import microservices.book.gamification.domain.LeaderBoardRow;
import microservices.book.gamification.domain.ScoreCard;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ScoreCard CRUD 작업 처리
 */
public interface ScoreCardRepository extends CrudRepository<ScoreCard, Long> {

	/**
	 * ScoreCard의 점수를 합해서 사용자의 총 점수를 조회
	 * @param userId 총 점수를 조회하고자 하는 사용자의 ID
	 * @return 사용자의 총 점수
	 */
	@Query(
		"SELECT SUM(s.score) " +
		"FROM ScoreCard s " +
		"WHERE s.userId = :userId " +
		"GROUP BY s.userId" // GROUP BY는 실제로 필요하지 않다.
	)
	int getTotalScoreForUser(@Param("userId") final Long userId);

	/**
	 * 사용자(userId)와 사용자의 총 점수(totalScore)를 나타내는 {@link LeaderBoardRow} 리스트를 조회
	 * @return 높은 점수순으로 정렬된 리더 보드
	 */
	@Query(
		"SELECT NEW microservices.book.gamification.domain.LeaderBoardRow(s.userId, SUM(s.score)) " +
		"FROM ScoreCard s " +
		"GROUP BY s.userId " +
		"ORDER BY SUM(s.score) DESC"
	)
	List<LeaderBoardRow> findFirst10();

	List<ScoreCard> findByUserIdOrderByScoreTimestampDesc(final Long userId);
}
