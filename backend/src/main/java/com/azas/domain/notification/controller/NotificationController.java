package com.azas.domain.notification.controller;

import com.azas.domain.notification.dto.NotificationListResponse;
import com.azas.domain.notification.dto.NotificationUnreadCountResponse;
import com.azas.domain.notification.service.NotificationService;
import com.azas.global.security.AccessTokenMemberResolver;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "알림")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AccessTokenMemberResolver accessTokenMemberResolver;

    @ApiOperation("알림 목록 조회 및 신규 알림 폴링")
    @GetMapping
    public ResponseEntity<NotificationListResponse> getNotifications(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader,

            @RequestParam(
                    value = "child_id",
                    required = false
            ) Long childId,

            @RequestParam(
                    value = "category",
                    required = false
            ) String category,

            @RequestParam(
                    value = "notification_type",
                    required = false
            ) String notificationType,

            @RequestParam(
                    value = "is_read",
                    required = false
            ) String isRead,

            @RequestParam(
                    value = "cursor",
                    required = false
            ) String cursor,

            @RequestParam(
                    value = "after_id",
                    required = false
            ) String afterId,

            @RequestParam(
                    value = "size",
                    required = false
            ) String size
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        NotificationListResponse response = notificationService.getNotifications(
                memberId, childId, category, notificationType, isRead, cursor, afterId, size
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }

    @ApiOperation("읽지 않은 알림 수 조회")
    @GetMapping("/unread-count")
    public ResponseEntity<NotificationUnreadCountResponse>
    getUnreadCount(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader
    ) {
        long memberId =
                accessTokenMemberResolver.resolveMemberId(
                        authorizationHeader
                );

        NotificationUnreadCountResponse response =
                notificationService.getUnreadCount(memberId);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }
}