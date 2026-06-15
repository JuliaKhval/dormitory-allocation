package com.example.dormitory.repository;

import com.example.dormitory.entity.RequestPreference;
import com.example.dormitory.enums.RequestPreferenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RequestPreferenceRepository extends JpaRepository<RequestPreference, Long> {
    List<RequestPreference> findByRequestId(Long requestId);
    List<RequestPreference> findByPreferredUserId(Long userId);
    boolean existsByRequestIdAndPreferredUserId(Long requestId, Long preferredUserId);

    @Query("SELECT rp FROM RequestPreference rp WHERE rp.requester.id = :requesterId AND rp.preferredUser.id = :preferredUserId AND rp.request.year = :year")
    Optional<RequestPreference> findByRequesterAndPreferred(@Param("requesterId") Long requesterId,
                                                            @Param("preferredUserId") Long preferredUserId,
                                                            @Param("year") Integer year);
}