package com.azas.domain.notification.dto;

import com.azas.domain.notification.entity.NotificationCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNotificationPreferencesRequest {

    @Valid
    @NotEmpty
    @Size(min = 6, max = 6)
    private List<Item> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "NotificationPreferenceUpdateItemRequest")
    public static class Item {

        @NotNull
        @JsonProperty("notification_category")
        private NotificationCategory notificationCategory;

        @NotNull
        private Boolean enabled;
    }
}
