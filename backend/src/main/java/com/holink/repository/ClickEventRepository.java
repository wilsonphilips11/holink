package com.holink.repository;

import com.holink.analytics.LinkClickCount;
import com.holink.analytics.DailyClickCount;
import com.holink.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ClickEventRepository extends JpaRepository<ClickEvent, UUID> {

    @Query("""
            SELECT new com.holink.analytics.LinkClickCount(l.id, l.title, COUNT(ce))
            FROM ClickEvent ce
            JOIN ce.link l
            WHERE l.profile.id = :profileId AND l.deletedAt IS NULL
            GROUP BY l.id, l.title
            ORDER BY COUNT(ce) DESC
            """)
    List<LinkClickCount> countClicksByLink(@Param("profileId") UUID profileId);

    @Query("""
            SELECT COUNT(ce) FROM ClickEvent ce
            JOIN ce.link l
            WHERE l.profile.id = :profileId AND l.deletedAt IS NULL
            """)
    long countTotalClicks(@Param("profileId") UUID profileId);

    @Query("""
            SELECT CAST(ce.createdAt AS date), COUNT(ce)
            FROM ClickEvent ce
            JOIN ce.link l
            WHERE l.profile.id = :profileId AND l.deletedAt IS NULL AND ce.createdAt >= :since
            GROUP BY CAST(ce.createdAt AS date)
            ORDER BY CAST(ce.createdAt AS date) ASC
            """)
    List<Object[]> countClicksByDayRaw(@Param("profileId") UUID profileId, @Param("since") Instant since);

    default List<DailyClickCount> countClicksByDay(UUID profileId, Instant since) {
        return countClicksByDayRaw(profileId, since).stream()
                .map(row -> new DailyClickCount(formatDay(row[0]), ((Number) row[1]).longValue()))
                .toList();
    }

    private static String formatDay(Object day) {
        if (day instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().toString();
        }
        if (day instanceof java.time.LocalDate localDate) {
            return localDate.toString();
        }
        return String.valueOf(day);
    }

    long countByProfileUsername(String profileUsername);
}
