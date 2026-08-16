package com.campus.Campus_Connect.features.honor.mapper;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.features.event.dto.response.ParticipantDisplayResponse;
import com.campus.Campus_Connect.features.event.entity.EventTeam;
import com.campus.Campus_Connect.features.event.mapper.ParticipantDisplayMapper;
import com.campus.Campus_Connect.features.event.repository.EventTeamRepository;
import com.campus.Campus_Connect.features.honor.dto.response.MedalRecipientResponse;
import com.campus.Campus_Connect.features.honor.dto.response.MedalResponse;
import com.campus.Campus_Connect.features.honor.entity.HonorItem;
import com.campus.Campus_Connect.features.honor.entity.UserHonor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HonorMapper {

    private final ParticipantDisplayMapper participantDisplayMapper;
    private final EventTeamRepository eventTeamRepository;

    public MedalResponse toMedalResponse(
            HonorItem honor,
            List<UserHonor> recipients
    ) {

        if (recipients.isEmpty()) {

            return MedalResponse.builder()
                    .title(honor.getTitle())
                    .awarded(false)
                    .recipient(null)
                    .build();
        }

        MedalRecipientResponse recipient;

        UserHonor recipientHonor = recipients.get(0);

        if (recipients.size() == 1) {

            recipient = buildSoloRecipient(
                    honor,
                    recipientHonor
            );

        } else {

            recipient = buildTeamRecipient(
                    honor
            );
        }

        return MedalResponse.builder()
                .title(honor.getTitle())
                .awarded(true)
                .recipient(recipient)
                .build();
    }

    private MedalRecipientResponse buildSoloRecipient(
            HonorItem honor,
            UserHonor userHonor
    ) {

        ParticipantDisplayResponse display =
                participantDisplayMapper.toDisplay(
                        userHonor.getUser()
                );

        return MedalRecipientResponse.builder()
                .honorId(honor.getId())
                .name(display.getName())
                .subtitle(display.getSubtitle())
                .team(false)
                .build();
    }

    private MedalRecipientResponse buildTeamRecipient(
            HonorItem honor
    ) {

        EventTeam team =
                eventTeamRepository
                        .findByEvent_IdAndTeamName(
                                honor.getEvent().getId(),
                                honor.getSubtitle()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Team not found."
                                )
                        );

        return MedalRecipientResponse.builder()
                .honorId(honor.getId())
                .name(team.getTeamName())
                .subtitle(
                        "Led by "
                                + team.getLeader()
                                .getProfile()
                                .getFullName()
                )
                .team(true)
                .build();
    }
}