package com.example.dormitory.repository;

import com.example.dormitory.entity.Request;
import com.example.dormitory.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByStudentId(Long studentId);
    List<Request> findByStatus(RequestStatus status);
    boolean existsByStudentIdAndStatusIn(Long studentId, List<RequestStatus> statuses);
}