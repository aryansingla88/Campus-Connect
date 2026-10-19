package com.campus.Campus_Connect.features.registration.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.registration.dto.request.AnswerType;
import com.campus.Campus_Connect.features.registration.dto.request.SaveRegistrationAnswersRequest;
import com.campus.Campus_Connect.features.registration.dto.response.AvailableTeamResponse;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.registration.dto.response.SavedRegistrationAnswerResponse;
import com.campus.Campus_Connect.features.registration.service.UserRegistrationService;
import com.campus.Campus_Connect.features.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRegistrationControllerRoutingTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private RequestMappingHandlerMapping handlerMapping;
    @MockBean private UserRegistrationService service;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void componentScanRegistersAllParticipantRegistrationRoutes() {
        Set<String> patterns = handlerMapping.getHandlerMethods().keySet().stream()
                .flatMap(mapping -> mapping.getPatternValues().stream()).collect(Collectors.toSet());
        assertThat(patterns).contains(
                "/events/{eventId}/registration/questions",
                "/events/registrations/{registrationId}/answers",
                "/events/{eventId}/registration/teams",
                "/events/registrations/{registrationId}/team",
                "/events/registrations/{registrationId}/finalize",
                "/events/{eventId}/registration",
                "/events/registrations")
                .doesNotContain(
                        "/events/registrations/{registrationId}/answers/individual",
                        "/events/registrations/{registrationId}/answers/team");
    }

    @Test
    void individualAndTeamQuestionRequestsReachController() throws Exception {
        when(service.getPublishedFields(3, true)).thenReturn(ApiResponse.success(List.of(), "ok"));
        when(service.getPublishedFields(3, false)).thenReturn(ApiResponse.success(List.of(), "ok"));
        mockMvc.perform(get("/events/3/registration/questions").param("individual", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/events/3/registration/questions").param("individual", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        verify(service).getPublishedFields(3, true);
        verify(service).getPublishedFields(3, false);
    }

    @Test
    void unifiedAnswerRequestsReachServiceWithRequestedType() throws Exception {
        RegistrationResponse response = RegistrationResponse.builder().registrationId(19).status("PENDING").build();
        when(service.saveAnswers(eq(19), any(SaveRegistrationAnswersRequest.class)))
                .thenReturn(ApiResponse.success(response, "saved"));
        mockMvc.perform(put("/events/registrations/19/answers")
                        .contentType("application/json")
                        .content("{\"type\":\"INDIVIDUAL\",\"answers\":[{\"fieldId\":55,\"answer\":\"Example\"}]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.registered").doesNotExist());
        mockMvc.perform(put("/events/registrations/19/answers")
                        .contentType("application/json")
                        .content("{\"type\":\"TEAM\",\"answers\":[{\"fieldId\":56,\"answer\":\"Team answer\"}]}"))
                .andExpect(status().isOk());
        ArgumentCaptor<SaveRegistrationAnswersRequest> captor = ArgumentCaptor.forClass(SaveRegistrationAnswersRequest.class);
        verify(service, times(2)).saveAnswers(eq(19), captor.capture());
        assertThat(captor.getAllValues()).extracting(SaveRegistrationAnswersRequest::getType)
                .containsExactly(AnswerType.INDIVIDUAL, AnswerType.TEAM);
    }

    @Test
    void initializationAndEventSpecificRetrievalRemainAvailable() throws Exception {
        when(service.registerForEvent(eq(3), any())).thenReturn(ApiResponse.success(
                RegistrationResponse.builder().registrationId(21).eventId(3).status("PENDING").canResume(true).build(), "ok"));
        when(service.getRegistration(3)).thenReturn(ApiResponse.success(
                RegistrationResponse.builder().registrationId(21).eventId(3).status("PENDING").canResume(true)
                        .answers(List.of(SavedRegistrationAnswerResponse.builder().fieldId(55)
                                .answer("Example").type("INDIVIDUAL").build())).build(), "ok"));
        mockMvc.perform(post("/events/3/registration")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.registrationId").value(21))
                .andExpect(jsonPath("$.data.registered").doesNotExist());
        mockMvc.perform(get("/events/3/registration")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.answers[0].fieldId").value(55))
                .andExpect(jsonPath("$.data.answers[0].answer").value("Example"))
                .andExpect(jsonPath("$.data.answers[0].type").value("INDIVIDUAL"));
    }

    @Test
    void missingRegistrationReturnsEmptyAnswerArray() throws Exception {
        when(service.getRegistration(8)).thenReturn(ApiResponse.success(
                RegistrationResponse.builder().eventId(8).canResume(false).answers(List.of()).build(),
                "No registration found."));

        mockMvc.perform(get("/events/8/registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventId").value(8))
                .andExpect(jsonPath("$.data.canResume").value(false))
                .andExpect(jsonPath("$.data.answers").isArray())
                .andExpect(jsonPath("$.data.answers").isEmpty());
    }

    @Test
    void unauthenticatedRetrievalReturnsServiceFailure() throws Exception {
        when(service.getRegistration(3)).thenReturn(ApiResponse.failure("User is not authenticated."));

        mockMvc.perform(get("/events/3/registration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authenticated."));
    }

    @Test
    void remainingParticipantRoutesReachTheirHandlers() throws Exception {
        when(service.getMyRegistrations()).thenReturn(ApiResponse.success(List.of(), "ok"));
        when(service.cancelRegistration(3)).thenReturn(ApiResponse.success(null, "cancelled"));
        when(service.getAvailableTeams(3, "alpha")).thenReturn(ApiResponse.success(List.of(
                AvailableTeamResponse.builder().teamId(4).teamName("Alpha").memberCount(1).capacity(4).build()), "ok"));
        RegistrationResponse pending = RegistrationResponse.builder().registrationId(19).eventId(3).status("PENDING").build();
        when(service.createTeam(19, "Alpha Team")).thenReturn(ApiResponse.success(pending, "ok"));
        when(service.finalizeRegistration(19)).thenReturn(ApiResponse.success(
                RegistrationResponse.builder().registrationId(19).eventId(3).status("COMPLETE").build(), "ok"));

        mockMvc.perform(get("/events/registrations")).andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
        mockMvc.perform(delete("/events/3/registration")).andExpect(status().isOk());
        mockMvc.perform(get("/events/3/registration/teams").param("search", "alpha"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].teamName").value("Alpha"));
        mockMvc.perform(post("/events/registrations/19/team").contentType("application/json")
                        .content("{\"teamName\":\"Alpha Team\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.registrationId").value(19));
        mockMvc.perform(post("/events/registrations/19/finalize"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("COMPLETE"));

        verify(service).getMyRegistrations();
        verify(service).cancelRegistration(3);
        verify(service).getAvailableTeams(3, "alpha");
        verify(service).createTeam(19, "Alpha Team");
        verify(service).finalizeRegistration(19);
    }

    @Test
    void malformedAnswerJsonIsRejectedByMvc() throws Exception {
        mockMvc.perform(put("/events/registrations/19/answers")
                        .contentType("application/json").content("{broken"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void unsupportedAnswerTypeIsRejectedByMvc() throws Exception {
        mockMvc.perform(put("/events/registrations/19/answers")
                        .contentType("application/json")
                        .content("{\"type\":\"OTHER\",\"answers\":[]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void oldSplitAnswerRoutesAreNoLongerMapped() throws Exception {
        mockMvc.perform(put("/events/registrations/19/answers/individual"))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/events/registrations/19/answers/team"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownRouteReturnsNotFound() throws Exception {
        mockMvc.perform(get("/events/3/registration/not-a-route")).andExpect(status().isNotFound());
    }
}
