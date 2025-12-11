package com.karam.pubfinder.service;

import com.karam.pubfinder.dto.PubResponse;
import com.karam.pubfinder.entity.Pub;
import com.karam.pubfinder.repository.PubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PubService {

    private final PubRepository pubRepository;

    @Transactional(readOnly = true)
    public List<PubResponse> getAllPubs(String sortBy) {
        List<Pub> pubs;

        if ("asc".equalsIgnoreCase(sortBy)) {
            pubs = pubRepository.findAllByOrderByRatingAsc();
        } else if ("desc".equalsIgnoreCase(sortBy)) {
            pubs = pubRepository.findAllByOrderByRatingDesc();
        } else {
            pubs = pubRepository.findAll();
        }

        return pubs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PubResponse getPubById(Long id) {
        Pub pub = pubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pub not found with id: " + id));
        return mapToResponse(pub);
    }

    private PubResponse mapToResponse(Pub pub) {
        return PubResponse.builder()
                .id(pub.getId())
                .title(pub.getTitle())
                .shortDescription(pub.getShortDescription())
                .longDescription(pub.getLongDescription())
                .menuUrl(pub.getMenuUrl())
                .imageUrl(pub.getImageUrl())
                .rating(pub.getRating())
                .createdAt(pub.getCreatedAt())
                .updatedAt(pub.getUpdatedAt())
                .build();
    }
}
