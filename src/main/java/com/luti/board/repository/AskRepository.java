package com.luti.board.repository;

import com.luti.board.entity.Ask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Ask 엔티티에 대한 JPA Repository
 */
@Repository
public interface AskRepository extends JpaRepository<Ask, Long> {
}
