package com.campus.Campus_Connect.features.metadata.clubs;

import com.campus.Campus_Connect.features.club.entity.Club;
import com.campus.Campus_Connect.features.club.repository.ClubRepository;
import com.campus.Campus_Connect.features.metadata.clubs.dto.ClubMetadataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubMetadataService {

    private final ClubRepository clubRepository;

    @Transactional(readOnly = true)
    public List<ClubMetadataResponse> getClubs() {
        return clubRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ClubMetadataResponse toResponse(Club club) {
        return ClubMetadataResponse.builder()
                .clubId(club.getId())
                .name(club.getName())
                .build();
    }
}
