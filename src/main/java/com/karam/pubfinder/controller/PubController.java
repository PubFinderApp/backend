package com.karam.pubfinder.controller;

import com.karam.pubfinder.dto.PubResponse;
import com.karam.pubfinder.service.PubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pubs")
@RequiredArgsConstructor
@Tag(name = "Pubs", description = "Pub management endpoints")
public class PubController {

    private final PubService pubService;

    @GetMapping
    @Operation(summary = "Get all pubs",
            description = "Get all pubs. Can be sorted by rating using 'sortBy' parameter: 'asc' or 'desc'")
    public ResponseEntity<List<PubResponse>> getAllPubs(
            @Parameter(description = "Sort by rating: 'asc' or 'desc'")
            @RequestParam(required = false) String sortBy) {
        List<PubResponse> pubs = pubService.getAllPubs(sortBy);
        return ResponseEntity.ok(pubs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pub by ID")
    public ResponseEntity<PubResponse> getPubById(@PathVariable Long id) {
        PubResponse pub = pubService.getPubById(id);
        return ResponseEntity.ok(pub);
    }
}