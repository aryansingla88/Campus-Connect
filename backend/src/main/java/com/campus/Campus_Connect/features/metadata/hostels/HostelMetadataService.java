package com.campus.Campus_Connect.features.metadata.hostels;

import com.campus.Campus_Connect.features.metadata.hostels.dto.HostelResponse;
import com.campus.Campus_Connect.features.metadata.hostels.entity.Hostel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HostelMetadataService {

    private final HostelRepository hostelRepository;

    @Transactional(readOnly = true)
    public List<HostelResponse> getHostels() {
        return hostelRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private HostelResponse toResponse(Hostel hostel) {
        return HostelResponse.builder()
                .id(hostel.getId())
                .hostel(hostel.getHostel())
                .name(hostel.getName())
                .active(hostel.isActive())
                .build();
    }
}
