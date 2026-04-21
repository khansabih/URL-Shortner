package com.sabih.url_shortner.repository;

import com.sabih.url_shortner.entity.Click;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ClickRepository extends JpaRepository<Click, Long> {

    long countByShortCode(String shortCode);

    @Query("SELECT c.deviceType, COUNT(c) FROM Click c WHERE c.shortCode = :shortCode GROUP BY c.deviceType")
    List<Object[]> countByDeviceType(@Param("shortCode") String shortCode);

    @Query("SELECT CAST(c.clickedAt AS date), COUNT(c) FROM Click c WHERE c.shortCode = :shortCode AND c.clickedAt >= :since GROUP BY CAST(c.clickedAt AS date) ORDER BY CAST(c.clickedAt AS date)")
    List<Object[]> clickedPerDay(@Param("shortCode") String shortCode,
                                 @Param("since") Instant since);
}
