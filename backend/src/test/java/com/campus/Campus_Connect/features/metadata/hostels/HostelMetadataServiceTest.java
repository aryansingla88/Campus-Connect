package com.campus.Campus_Connect.features.metadata.hostels;

import com.campus.Campus_Connect.features.metadata.hostels.entity.Hostel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HostelMetadataServiceTest {
    @Mock private HostelRepository hostelRepository;
    @InjectMocks private HostelMetadataService service;

    @Test
    void returnsOnlyActiveHostelsWithTheFullMetadataContract() {
        when(hostelRepository.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(
                Hostel.builder().id(2).hostel("DS").name("Day Scholar").active(true).build(),
                Hostel.builder().id(1).hostel("H1").name("Abhimanyu Bhawan").active(true).build()
        ));

        var response = service.getHostels();

        assertEquals(2, response.size());
        assertEquals(2, response.get(0).getId());
        assertEquals("DS", response.get(0).getHostel());
        assertEquals("Day Scholar", response.get(0).getName());
        assertEquals(true, response.get(0).isActive());
        assertEquals("H1", response.get(1).getHostel());
        verify(hostelRepository).findAllByActiveTrueOrderByNameAsc();
    }
}
