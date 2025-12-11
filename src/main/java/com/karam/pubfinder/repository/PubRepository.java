package com.karam.pubfinder.repository;

import com.karam.pubfinder.entity.Pub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PubRepository extends JpaRepository<Pub, Long> {

    // Find pubs ordered by rating ascending
    List<Pub> findAllByOrderByRatingAsc();

    // Find pubs ordered by rating descending
    List<Pub> findAllByOrderByRatingDesc();

    // Search by title (optional, if you want search functionality)
    List<Pub> findByTitleContainingIgnoreCase(String title);
}