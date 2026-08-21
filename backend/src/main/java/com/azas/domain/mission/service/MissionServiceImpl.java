package com.azas.domain.mission.service;

import com.azas.domain.finance.transfer.dto.CreateTransferRequest;
import com.azas.domain.finance.transfer.dto.TransferCreateResponse;
import com.azas.domain.finance.transfer.service.TransferService;
import com.azas.domain.mission.dto.CreateMissionRequest;
import com.azas.domain.mission.dto.MissionCreateResponse;
import com.azas.domain.mission.dto.MissionInsertCommand;
import com.azas.domain.mission.entity.MissionAction;
import com.azas.domain.mission.entity.MissionStatus;
import com.azas.domain.mission.mapper.MissionMapper;
import com.azas.domain.notification.service.PushMessage;
import com.azas.domain.notification.service.PushNotificationPublisher;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.azas.domain.mission.dto.*;
import com.azas.domain.mission.entity.MissionListFilter;

import java.nio.charset.StandardCharsets;
import java.util.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class MissionServiceImpl implements MissionService {

    private final MissionMapper missionMapper;
    private final Clock clock;
    private static final int DEFAULT_LIST_SIZE = 20;
    private static final int MAX_LIST_SIZE = 100;
    private final TransferService transferService;
    private final PushNotificationPublisher pushNotificationPublisher;

    @Autowired
    public MissionServiceImpl(
            MissionMapper missionMapper,
            TransferService transferService,
            PushNotificationPublisher pushNotificationPublisher
    ) {
        this(
                missionMapper,
                transferService,
                Clock.systemUTC(),
                pushNotificationPublisher
        );
    }

    MissionServiceImpl(
            MissionMapper missionMapper,
            TransferService transferService,
            Clock clock,
            PushNotificationPublisher pushNotificationPublisher
    ) {
        this.missionMapper = missionMapper;
        this.transferService = transferService;
        this.clock = clock;
        this.pushNotificationPublisher = pushNotificationPublisher;
    }

    @Override
    @Transactional
    public MissionCreateResponse createMission(
            Long memberId,
            Long childId,
            CreateMissionRequest request
    ) {
        validateRequest(childId, request);

        if (missionMapper.findActiveChildId(childId) == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        if (missionMapper.countParentAccess(
                memberId,
                childId
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.PARENT_ACCESS_REQUIRED
            );
        }

        LocalDateTime createdAt =
                LocalDateTime.now(clock);

        MissionInsertCommand command =
                new MissionInsertCommand(
                        null,
                        childId,
                        memberId,
                        request.getTitle().trim(),
                        request.getDescription().trim(),
                        request.getRewardAmount(),
                        createdAt
                );

        if (missionMapper.insertMission(command) != 1
                || command.getMissionId() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        /*
         * 자녀 회원 계정이 연결된 경우에만 알림이 저장된다.
         * 알림 INSERT가 0건이어도 미션 생성은 성공한다.
         */
        int insertedNotificationCount =
                missionMapper.insertMissionAssignedNotification(
                        command.getMissionId(),
                        childId,
                        command.getTitle(),
                        command.getRewardAmount(),
                        createdAt
                );

        if (insertedNotificationCount > 0) {
            publishToChild(
                    childId,
                    "MISSION_ASSIGNED",
                    "새 용돈 미션이 도착했어요",
                    command.getTitle()
                            + " 미션을 확인해 보세요. 완료 보상은 "
                            + String.format(
                                    Locale.KOREA,
                                    "%,.0f",
                                    command.getRewardAmount()
                            )
                            + "원이에요.",
                    command.getMissionId()
            );
        }

        return new MissionCreateResponse(
                command.getMissionId(),
                childId,
                command.getTitle(),
                command.getDescription(),
                command.getRewardAmount(),
                MissionStatus.ASSIGNED,
                createdAt.toInstant(ZoneOffset.UTC)
        );
    }

    private void validateRequest(
            Long childId,
            CreateMissionRequest request
    ) {
        if (childId == null
                || childId <= 0
                || request == null
                || request.getTitle() == null
                || request.getTitle().trim().isEmpty()
                || request.getTitle().trim().length() > 100
                || request.getDescription() == null
                || request.getDescription().trim().isEmpty()
                || request.getDescription().trim().length() > 1000
                || request.getRewardAmount() == null
                || request.getRewardAmount().signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_MISSION
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MissionListResponse getMissions(
            Long memberId,
            Long childId,
            String filterValue,
            String cursorValue,
            Integer sizeValue
    ) {
        if (childId == null || childId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }

        if (missionMapper.findActiveChildId(childId) == null) {
            throw new BusinessException(
                    ErrorCode.CHILD_NOT_FOUND
            );
        }

        if (missionMapper.countMissionAccess(
                memberId,
                childId
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.CHILD_ACCESS_DENIED
            );
        }

        MissionListFilter filter =
                parseMissionFilter(filterValue);

        Long cursorId =
                parseMissionCursor(cursorValue);

        int pageSize =
                normalizeMissionListSize(sizeValue);

        MissionListQuery query =
                new MissionListQuery(
                        childId,
                        filter,
                        cursorId,
                        pageSize + 1
                );

        List<MissionListRow> rows =
                missionMapper.findMissions(query);

        if (rows == null) {
            rows = new ArrayList<>();
        }

        boolean hasNext =
                rows.size() > pageSize;

        List<MissionListRow> pageRows =
                hasNext
                        ? new ArrayList<>(
                        rows.subList(0, pageSize)
                )
                        : new ArrayList<>(rows);

        List<MissionListItemResponse> items =
                pageRows.stream()
                        .map(MissionListItemResponse::from)
                        .toList();

        Long nextCursor =
                hasNext && !pageRows.isEmpty()
                        ? pageRows.get(
                        pageRows.size() - 1
                ).getMissionId()
                        : null;

        MissionSummaryRow summaryRow =
                missionMapper.findMissionSummary(childId);

        return new MissionListResponse(
                MissionListSummaryResponse.from(
                        summaryRow
                ),
                items,
                nextCursor,
                hasNext
        );
    }

    private MissionListFilter parseMissionFilter(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return MissionListFilter.ALL;
        }

        try {
            return MissionListFilter.valueOf(
                    value.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }
    }

    private Long parseMissionCursor(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            long cursor = Long.parseLong(value);

            if (cursor <= 0) {
                throw new NumberFormatException();
            }

            return cursor;
        } catch (NumberFormatException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }
    }

    private int normalizeMissionListSize(
            Integer value
    ) {
        if (value == null) {
            return DEFAULT_LIST_SIZE;
        }

        if (value < 1 || value > MAX_LIST_SIZE) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }

        return value;
    }

    @Override
    @Transactional(readOnly = true)
    public MissionDetailResponse getMissionDetail(
            Long memberId,
            Long missionId
    ) {
        if (missionId == null || missionId <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_QUERY_PARAMETER
            );
        }

        MissionDetailRow row =
                missionMapper.findMissionDetail(
                        missionId
                );

        if (row == null) {
            throw new BusinessException(
                    ErrorCode.MISSION_NOT_FOUND
            );
        }

        if (missionMapper.countMissionAccess(
                memberId,
                row.getChildId()
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.MISSION_ACCESS_DENIED
            );
        }

        return MissionDetailResponse.from(row);
    }
    @Override
    @Transactional
    public MissionDetailResponse updateMissionStatus(
            Long memberId,
            Long missionId,
            UpdateMissionStatusRequest request
    ) {
        validateStatusRequest(
                missionId,
                request
        );

        MissionDetailRow mission =
                missionMapper.findMissionDetailForUpdate(
                        missionId
                );

        if (mission == null) {
            throw new BusinessException(
                    ErrorCode.MISSION_NOT_FOUND
            );
        }

        LocalDateTime updatedAt =
                LocalDateTime.now(clock);

        switch (request.getAction()) {
            case SUBMIT:
                submitMission(
                        memberId,
                        mission,
                        updatedAt
                );
                break;

            case APPROVE:
                approveMission(
                        memberId,
                        mission,
                        request,
                        updatedAt
                );
                break;

            case REJECT:
                rejectMission(
                        memberId,
                        mission,
                        updatedAt
                );
                break;

            case CANCEL:
                cancelMission(
                        memberId,
                        mission,
                        updatedAt
                );
                break;

            default:
                throw new BusinessException(
                        ErrorCode.INVALID_MISSION_ACTION
                );
        }

        mission.setUpdatedAt(updatedAt);

        return MissionDetailResponse.from(mission);
    }

    private void validateStatusRequest(
            Long missionId,
            UpdateMissionStatusRequest request
    ) {
        if (missionId == null
                || missionId <= 0
                || request == null
                || request.getAction() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_MISSION_ACTION
            );
        }

        if (request.getAction()
                == MissionAction.APPROVE) {
            if (request.getSourceAccountId() == null
                    || request.getSourceAccountId() <= 0
                    || request.getDestinationAccountId() == null
                    || request.getDestinationAccountId() <= 0) {
                throw new BusinessException(
                        ErrorCode.INVALID_MISSION_ACTION
                );
            }
        } else if (request.getSourceAccountId() != null
                || request.getDestinationAccountId() != null) {
            throw new BusinessException(
                    ErrorCode.INVALID_MISSION_ACTION
            );
        }
    }

    private void submitMission(
            Long memberId,
            MissionDetailRow mission,
            LocalDateTime updatedAt
    ) {
        requireChildSelfAccess(
                memberId,
                mission.getChildId()
        );

        if (!EnumSet.of(
                MissionStatus.ASSIGNED,
                MissionStatus.REJECTED
        ).contains(mission.getStatus())) {
            throw invalidMissionTransition();
        }

        changeMissionStatus(
                mission,
                MissionStatus.SUBMITTED,
                updatedAt
        );

        int insertedNotificationCount =
                missionMapper.insertMissionSubmittedNotification(
                mission.getMissionId(),
                mission.getChildId(),
                mission.getTitle(),
                updatedAt
        );

        if (insertedNotificationCount > 0) {
            publishToParents(
                    mission.getChildId(),
                    "MISSION_SUBMITTED",
                    "자녀가 미션 완료를 요청했어요",
                    mission.getTitle()
                            + " 미션을 확인하고 보상해 주세요.",
                    mission.getMissionId()
            );
        }
    }

    private void approveMission(
            Long memberId,
            MissionDetailRow mission,
            UpdateMissionStatusRequest request,
            LocalDateTime updatedAt
    ) {
        requireParentAccess(
                memberId,
                mission.getChildId()
        );

        if (mission.getStatus()
                != MissionStatus.SUBMITTED) {
            throw invalidMissionTransition();
        }

        String idempotencyKey =
                createMissionRewardIdempotencyKey(
                        mission.getMissionId()
                );

        CreateTransferRequest transferRequest =
                new CreateTransferRequest(
                        request.getSourceAccountId(),
                        request.getDestinationAccountId(),
                        mission.getRewardAmount(),
                        mission.getTitle() + " 미션 보상"
                );

        TransferCreateResponse transferResponse =
                transferService.createTransfer(
                        memberId,
                        idempotencyKey,
                        transferRequest
                );

        if (missionMapper.linkRewardTransfer(
                transferResponse.getFinancialTransferId(),
                mission.getMissionId(),
                memberId
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.TRANSFER_PROCESSING_FAILED
            );
        }

        changeMissionStatus(
                mission,
                MissionStatus.APPROVED,
                updatedAt
        );

        int insertedNotificationCount =
                missionMapper.insertChildMissionStatusNotification(
                mission.getMissionId(),
                mission.getChildId(),
                "MISSION_APPROVED",
                "미션 보상이 지급되었어요",
                mission.getTitle()
                        + " 미션이 승인되었어요.",
                updatedAt
        );

        if (insertedNotificationCount > 0) {
            publishToChild(
                    mission.getChildId(),
                    "MISSION_APPROVED",
                    "미션 보상이 지급되었어요",
                    mission.getTitle()
                            + " 미션이 승인되었어요.",
                    mission.getMissionId()
            );
        }
    }

    private void rejectMission(
            Long memberId,
            MissionDetailRow mission,
            LocalDateTime updatedAt
    ) {
        requireParentAccess(
                memberId,
                mission.getChildId()
        );

        if (mission.getStatus()
                != MissionStatus.SUBMITTED) {
            throw invalidMissionTransition();
        }

        changeMissionStatus(
                mission,
                MissionStatus.REJECTED,
                updatedAt
        );

        int insertedNotificationCount =
                missionMapper.insertChildMissionStatusNotification(
                mission.getMissionId(),
                mission.getChildId(),
                "MISSION_REJECTED",
                "미션을 다시 확인해 주세요",
                mission.getTitle()
                        + " 미션이 반려되었어요.",
                updatedAt
        );

        if (insertedNotificationCount > 0) {
            publishToChild(
                    mission.getChildId(),
                    "MISSION_REJECTED",
                    "미션을 다시 확인해 주세요",
                    mission.getTitle()
                            + " 미션이 반려되었어요.",
                    mission.getMissionId()
            );
        }
    }
    private void cancelMission(
            Long memberId,
            MissionDetailRow mission,
            LocalDateTime updatedAt
    ) {
        requireParentAccess(
                memberId,
                mission.getChildId()
        );

        if (!EnumSet.of(
                MissionStatus.ASSIGNED,
                MissionStatus.REJECTED,
                MissionStatus.SUBMITTED
        ).contains(mission.getStatus())) {
            throw invalidMissionTransition();
        }

        changeMissionStatus(
                mission,
                MissionStatus.CANCELED,
                updatedAt
        );

        int insertedNotificationCount =
                missionMapper.insertChildMissionStatusNotification(
                mission.getMissionId(),
                mission.getChildId(),
                "MISSION_CANCELED",
                "미션이 취소되었어요",
                mission.getTitle()
                        + " 미션이 취소되었어요.",
                updatedAt
        );

        if (insertedNotificationCount > 0) {
            publishToChild(
                    mission.getChildId(),
                    "MISSION_CANCELED",
                    "미션이 취소되었어요",
                    mission.getTitle()
                            + " 미션이 취소되었어요.",
                    mission.getMissionId()
            );
        }
    }

    private void changeMissionStatus(
            MissionDetailRow mission,
            MissionStatus status,
            LocalDateTime updatedAt
    ) {
        if (missionMapper.updateMissionStatus(
                mission.getMissionId(),
                status,
                updatedAt
        ) != 1) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        mission.setStatus(status);
    }

    private void requireParentAccess(
            Long memberId,
            Long childId
    ) {
        if (missionMapper.countParentAccess(
                memberId,
                childId
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.MISSION_ACCESS_DENIED
            );
        }
    }

    private void requireChildSelfAccess(
            Long memberId,
            Long childId
    ) {
        if (missionMapper.countChildSelfAccess(
                memberId,
                childId
        ) <= 0) {
            throw new BusinessException(
                    ErrorCode.MISSION_ACCESS_DENIED
            );
        }
    }

    private BusinessException invalidMissionTransition() {
        return new BusinessException(
                ErrorCode.INVALID_MISSION_STATUS_TRANSITION
        );
    }

    private void publishToChild(
            Long childId,
            String notificationType,
            String title,
            String content,
            Long missionId
    ) {
        Long recipientMemberId =
                missionMapper.findChildNotificationRecipient(
                        childId
                );

        publish(
                recipientMemberId,
                notificationType,
                title,
                content,
                "/child/missions",
                missionId
        );
    }

    private void publishToParents(
            Long childId,
            String notificationType,
            String title,
            String content,
            Long missionId
    ) {
        List<Long> recipientMemberIds =
                missionMapper.findParentNotificationRecipients(
                        childId
                );

        if (recipientMemberIds == null) {
            return;
        }

        recipientMemberIds.forEach(memberId -> publish(
                memberId,
                notificationType,
                title,
                content,
                "/missions",
                missionId
        ));
    }

    private void publish(
            Long memberId,
            String notificationType,
            String title,
            String content,
            String actionUrl,
            Long missionId
    ) {
        if (memberId == null) {
            return;
        }

        pushNotificationPublisher.publish(
                memberId,
                new PushMessage(
                        title,
                        content,
                        actionUrl,
                        Map.of(
                                "notification_type",
                                notificationType,
                                "reference_type",
                                "MISSION",
                                "reference_id",
                                String.valueOf(missionId)
                        )
                )
        );
    }

    private String createMissionRewardIdempotencyKey(
            Long missionId
    ) {
        return UUID.nameUUIDFromBytes(
                ("MISSION_REWARD:" + missionId)
                        .getBytes(StandardCharsets.UTF_8)
        ).toString();
    }
}
