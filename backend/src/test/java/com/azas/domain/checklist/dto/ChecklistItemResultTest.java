package com.azas.domain.checklist.dto;

import com.azas.domain.checklist.entity.ChecklistItemStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChecklistItemResultTest {

    @Test
    void 정보형_항목의_URL과_상세_내용을_매핑한다() {
        ChecklistInfoItemRow info = new ChecklistInfoItemRow();

        info.setChecklistItemDetailId(1L);
        info.setTitle("첫만남 이용권");
        info.setDescription("지원 내용을 확인해요.");
        info.setActionLabel("대상·조건 확인하기");
        info.setUrl(
                "https://www.bokjiro.go.kr/ssis-tbu/index.do"
        );

        ChecklistItemRow row = new ChecklistItemRow();

        row.setChecklistItemId(101L);
        row.setChecklistItemTemplateId(1L);
        row.setTemplateKey(
                "prenatal-support-after-birth"
        );
        row.setCategory("SUPPORT");
        row.setTitle(
                "출산 후 받을 수 있는 지원제도 확인하기"
        );
        row.setDescription(
                "출산·육아 관련 지원제도를 미리 확인해요."
        );
        row.setContent(
                "시기별 지원 정보를 먼저 확인해보세요."
        );
        row.setActionType("INFO");
        row.setUrl(null);
        row.setStatus(ChecklistItemStatus.PENDING);
        row.setInfoItems(List.of(info));

        ChecklistItemResult result =
                ChecklistItemResult.from(row);

        assertEquals("INFO", result.getActionType());
        assertNull(result.getUrl());
        assertEquals(1, result.getInfoItems().size());

        assertEquals(
                "https://www.bokjiro.go.kr/ssis-tbu/index.do",
                result.getInfoItems().get(0).getUrl()
        );
    }

    @Test
    void 이동형_항목은_내부_URL을_반환한다() {
        ChecklistItemRow row = new ChecklistItemRow();

        row.setChecklistItemId(102L);
        row.setChecklistItemTemplateId(2L);
        row.setTemplateKey(
                "prenatal-childcare-cost"
        );
        row.setCategory("ASSET");
        row.setTitle(
                "출산 후 예상 양육비 계산해보기"
        );
        row.setDescription(
                "출산 후 필요한 월 양육비를 미리 가늠해봐요."
        );
        row.setContent(
                "출산 후 필요한 월 양육비를 미리 가늠해봐요."
        );
        row.setActionType("ROUTE");
        row.setUrl("/reports");
        row.setStatus(ChecklistItemStatus.PENDING);

        ChecklistItemResult result =
                ChecklistItemResult.from(row);

        assertEquals("ROUTE", result.getActionType());
        assertEquals("/reports", result.getUrl());
        assertTrue(result.getInfoItems().isEmpty());
    }
}