package microservices.book.gamification.service;

import lombok.extern.slf4j.Slf4j;
import microservices.book.gamification.domain.Badge;
import microservices.book.gamification.domain.BadgeCard;
import microservices.book.gamification.domain.GameStats;
import microservices.book.gamification.domain.ScoreCard;
import microservices.book.gamification.repository.BadgeCardRepository;
import microservices.book.gamification.repository.ScoreCardRepository;
import org.aspectj.weaver.BCException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class GameServiceImpl implements GameService {

	private ScoreCardRepository scoreCardRepository;
	private BadgeCardRepository badgeCardRepository;

	GameServiceImpl(ScoreCardRepository scoreCardRepository, BadgeCardRepository badgeCardRepository) {
		this.scoreCardRepository = scoreCardRepository;
		this.badgeCardRepository = badgeCardRepository;
	}

	@Override
	public GameStats newAttemptForUser(Long userId, Long attemptId, boolean correct) {
		// 답이 맞았을 때만 점수를 줌
		if (correct) {
			// 점수 저장
			ScoreCard scoreCard = new ScoreCard(userId, attemptId);
			scoreCardRepository.save(scoreCard);
			log.info("사용자 ID {}, 점수 {} 점, 답안 ID {}", userId, scoreCard.getScore(), attemptId);

			List<BadgeCard> badgeCards = processForBadges(userId, attemptId); // 이번 제출로 획득 가능한 Badge를 부여하고, 이번에 획득한 BadgeCard 목록이 반환됨

			return new GameStats(
				userId,
				scoreCard.getScore(),
				badgeCards.stream()
					.map(BadgeCard::getBadge)
					.collect(toList())
			);
		}
		return GameStats.emptyStats(userId);
	}

	/**
	 * 조건이 충족될 경우 새 배지를 제공하기 위해 얻은 총 점수와 점수 카드를 확인
	 * @return 새로 획득한 BadgeCard 목록을 반환한다.
	 */
	private List<BadgeCard> processForBadges(final Long userId, final Long attemptId) {
		List<BadgeCard> badgeCards = new ArrayList<>();

		int totalScore = scoreCardRepository.getTotalScoreForUser(userId);
		log.info("사용자 ID {} 의 새로운 점수 {}", userId, totalScore);

		List<ScoreCard> scoreCardList = scoreCardRepository.findByUserIdOrderByScoreTimestampDesc(userId);
		List<BadgeCard> badgeCardList = badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId);

		// 점수 기반 배지
		checkAndGiveBadgeBasedOnScore(badgeCardList, Badge.BRONZE_MULTIPLICATOR, totalScore, 100, userId)
			.ifPresent(badgeCards::add);
		checkAndGiveBadgeBasedOnScore(badgeCardList, Badge.SILVER_MULTIPLICATOR, totalScore, 500, userId)
			.ifPresent(badgeCards::add);
		checkAndGiveBadgeBasedOnScore(badgeCardList, Badge.GOLD_MULTIPLICATOR, totalScore, 999, userId)
			.ifPresent(badgeCards::add);

		// 첫번째 정답 배지
		if (scoreCardList.size() == 1 && !containsBadge(badgeCardList, Badge.FIRST_WON)) {
			BadgeCard firstWonBadge = giveBadgeToUser(Badge.FIRST_WON, userId);
			badgeCards.add(firstWonBadge);
		}

		return badgeCards;
	}

	/**
	 * 배지를 얻기 위한 조건을 넘는지 체크하는 편의성 메소드
	 * 또한 조건이 충족되면 사용자에게 배지를 부여
	 */
	private Optional<BadgeCard> checkAndGiveBadgeBasedOnScore(
		final List<BadgeCard> badgeCards,
		final Badge badge,
		final int score,
		final int scoreThreshold,
		final Long userId
	) {
		if (score >= scoreThreshold && !containsBadge(badgeCards, badge)) {
			return Optional.of(giveBadgeToUser(badge, userId));
		}
		return Optional.empty();
	}

	/**
	 * 주어진 사용자에게 새로운 배지를 부여하는 메서드
	 */
	private BadgeCard giveBadgeToUser(final Badge badge, final Long userId) {
		BadgeCard badgeCard = new BadgeCard(userId, badge);
		badgeCardRepository.save(badgeCard);
		log.info("사용자 ID {} 새로운 배지 획득: {}", userId, badge);
		return badgeCard;
	}

	/**
	 * BadgeCard 목록에 배지가 포함돼 있는지 확인하는 메서드
	 */
	private boolean containsBadge(final List<BadgeCard> badgeCards, final Badge badge) {
		return badgeCards.stream()
			.anyMatch(b -> b.getBadge().equals(badge));
	}

	@Override
	public GameStats retrieveStatsForUser(final Long userId) {
		int totalScore = scoreCardRepository.getTotalScoreForUser(userId);
		List<BadgeCard> badgeCards = badgeCardRepository.findByUserIdOrderByBadgeTimestampDesc(userId);
		return new GameStats(
			userId,
			totalScore,
			badgeCards.stream()
				.map(BadgeCard::getBadge)
				.collect(toList())
		);
	}

}
