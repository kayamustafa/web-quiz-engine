package com.kaya.webquizengine.repository;

import com.kaya.webquizengine.model.Completion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompletionRepository extends JpaRepository<Completion, Integer> {
    @Query("SELECT c FROM Completion c WHERE c.user.email = ?1 order by c.completedAt desc")
    Page<Completion> findAllByUserOrderByCompletedAtDesc(String email, Pageable pageable);
}
