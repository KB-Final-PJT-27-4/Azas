package com.azas.domain.finance.transfer.mapper;

import com.azas.domain.finance.transfer.dto.*;
import com.azas.domain.finance.transfer.dto.TransferTransactionInsertCommand;
import com.azas.domain.finance.transfer.entity.TransferAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface TransferMapper {

    int countChildAccess(@Param("childId") Long childId, @Param("memberId") Long memberId);

    Long findTransferIdByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    TransferAccount findSourceAccountForUpdate(
            @Param("accountId") Long accountId,
            @Param("memberId") Long memberId
    );

    TransferAccount findDestinationAccountForUpdate(@Param("accountId") Long accountId);

    int insertTransfer(TransferInsertCommand command);

    int decreaseSourceBalance(
            @Param("accountId") Long accountId,
            @Param("amount") BigDecimal amount
    );

    int increaseDestinationBalance(
            @Param("accountId") Long accountId,
            @Param("amount") BigDecimal amount
    );

    int insertDestinationBalanceSnapshot(
            @Param("accountId") Long accountId,
            @Param("childId") Long childId,
            @Param("balance") BigDecimal balance,
            @Param("observedAt") LocalDateTime observedAt
    );

    int insertTransaction(TransferTransactionInsertCommand command);

    int markTransferSucceeded(
            @Param("transferId") Long transferId,
            @Param("debitTransactionId") Long debitTransactionId,
            @Param("completedAt") LocalDateTime completedAt
    );

    TransferDetailResponse findTransferDetail(
            @Param("transferId") Long transferId,
            @Param("memberId") Long memberId
    );

    List<ChildTransferListItemResponse> findChildTransfers(TransferListQuery query);

    List<MemberTransferListRow> findMemberTransfers(
            TransferListQuery query
    );
}
