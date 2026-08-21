package com.azas.domain.child.service;

import com.azas.domain.child.dto.ChildFeaturePermissionRequest;
import com.azas.domain.child.entity.ChildFeaturePermission;
import com.azas.domain.child.mapper.ChildMapper;
import com.azas.global.exception.BusinessException;
import com.azas.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildFeaturePermissionServiceTest {

    private static final long PARENT_MEMBER_ID = 8L;
    private static final long CHILD_ID = 6L;

    @Mock
    private ChildMapper childMapper;

    private ChildFeaturePermissionService service;

    @BeforeEach
    void setUp() {
        service = new ChildFeaturePermissionService(childMapper);
    }

    @Test
    void returnsPermissionForAccessibleChild() {
        when(childMapper.countChildAccess(CHILD_ID, PARENT_MEMBER_ID))
                .thenReturn(1);
        when(childMapper.findFeaturePermissionByChildId(CHILD_ID))
                .thenReturn(permission(true, false));

        ChildFeaturePermission result = service.getPermission(
                PARENT_MEMBER_ID,
                CHILD_ID
        );

        assertEquals(CHILD_ID, result.getChildId());
        assertEquals(true, result.isAllowanceRequestEnabled());
        assertEquals(false, result.isUsageLimitViewEnabled());
    }

    @Test
    void updatesBothPermissionsForAccessibleChild() {
        when(childMapper.countChildAccess(CHILD_ID, PARENT_MEMBER_ID))
                .thenReturn(1);
        when(childMapper.updateFeaturePermission(CHILD_ID, false, true))
                .thenReturn(1);
        when(childMapper.findFeaturePermissionByChildId(CHILD_ID))
                .thenReturn(permission(false, true));

        ChildFeaturePermission result = service.updatePermission(
                PARENT_MEMBER_ID,
                CHILD_ID,
                request(false, true)
        );

        assertEquals(false, result.isAllowanceRequestEnabled());
        assertEquals(true, result.isUsageLimitViewEnabled());
        verify(childMapper).updateFeaturePermission(CHILD_ID, false, true);
    }

    @Test
    void rejectsUnauthorizedParentAccess() {
        when(childMapper.countChildAccess(CHILD_ID, PARENT_MEMBER_ID))
                .thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.getPermission(PARENT_MEMBER_ID, CHILD_ID)
        );

        assertEquals(ErrorCode.CHILD_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    void rejectsDisabledAllowanceRequest() {
        when(childMapper.findFeaturePermissionByChildId(CHILD_ID))
                .thenReturn(permission(false, true));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.validateAllowanceRequestEnabled(CHILD_ID)
        );

        assertEquals(ErrorCode.ALLOWANCE_REQUEST_DISABLED, exception.getErrorCode());
    }

    @Test
    void rejectsDisabledUsageLimitView() {
        when(childMapper.findFeaturePermissionByChildId(CHILD_ID))
                .thenReturn(permission(true, false));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.validateUsageLimitViewEnabled(CHILD_ID)
        );

        assertEquals(
                ErrorCode.CHILD_USAGE_LIMIT_VIEW_DISABLED,
                exception.getErrorCode()
        );
    }

    private ChildFeaturePermission permission(
            boolean allowanceRequestEnabled,
            boolean usageLimitViewEnabled
    ) {
        ChildFeaturePermission permission = new ChildFeaturePermission();
        ReflectionTestUtils.setField(permission, "childId", CHILD_ID);
        ReflectionTestUtils.setField(
                permission,
                "allowanceRequestEnabled",
                allowanceRequestEnabled
        );
        ReflectionTestUtils.setField(
                permission,
                "usageLimitViewEnabled",
                usageLimitViewEnabled
        );
        return permission;
    }

    private ChildFeaturePermissionRequest request(
            boolean allowanceRequestEnabled,
            boolean usageLimitViewEnabled
    ) {
        ChildFeaturePermissionRequest request =
                new ChildFeaturePermissionRequest();
        ReflectionTestUtils.setField(
                request,
                "allowanceRequestEnabled",
                allowanceRequestEnabled
        );
        ReflectionTestUtils.setField(
                request,
                "usageLimitViewEnabled",
                usageLimitViewEnabled
        );
        return request;
    }
}
