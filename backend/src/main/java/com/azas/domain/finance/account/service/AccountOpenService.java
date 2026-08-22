package com.azas.domain.finance.account.service;

import com.azas.domain.finance.account.dto.*;
import com.azas.domain.finance.account.entity.FinancialAccountOwnerType;
import com.azas.domain.finance.account.mapper.FinancialAccountMapper;
import com.azas.domain.finance.product.entity.FinancialProduct;
import com.azas.domain.finance.product.mapper.FinancialProductMapper;
import com.azas.domain.member.entity.*;
import com.azas.domain.member.mapper.MemberMapper;
import com.azas.global.exception.*;
import com.azas.global.security.AccountNumberProtector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.util.HexFormat;
import java.util.Set;

@Service
public class AccountOpenService {
    private static final int MAX_NUMBER_ATTEMPTS = 10;
    private final MemberMapper memberMapper;
    private final FinancialAccountMapper accountMapper;
    private final FinancialProductMapper productMapper;
    private final AccountNumberProtector protector;
    private final MockAccountNumberGenerator numberGenerator;
    private final Clock clock;

    @Autowired
    public AccountOpenService(MemberMapper memberMapper,
                              FinancialAccountMapper accountMapper,
                              FinancialProductMapper productMapper,
                              AccountNumberProtector protector,
                              MockAccountNumberGenerator numberGenerator) {
        this(memberMapper, accountMapper, productMapper, protector,
                numberGenerator, Clock.systemUTC());
    }

    AccountOpenService(MemberMapper memberMapper,
                       FinancialAccountMapper accountMapper,
                       FinancialProductMapper productMapper,
                       AccountNumberProtector protector,
                       MockAccountNumberGenerator numberGenerator,
                       Clock clock) {
        this.memberMapper = memberMapper;
        this.accountMapper = accountMapper;
        this.productMapper = productMapper;
        this.protector = protector;
        this.numberGenerator = numberGenerator;
        this.clock = clock;
    }

    @Transactional
    public AccountOpenResult open(long memberId, AccountOpenRequest request) {
        validateParent(memberId);
        FinancialAccountOwnerType ownerType = parseOwner(request);
        validateScope(memberId, ownerType, request.getChildId());
        FinancialProduct product = findProduct(request.getFinancialProductId());
        validateTargetOwner(ownerType, product.getTargetOwnerType());
        String accountProductType = mapProductType(product.getProductType());
        BigDecimal deposit = validateDeposit(request.getInitialDepositAmount());

        boolean hasParentDemand = accountMapper
                .countActiveParentDemandDeposit(memberId) > 0;
        if (!hasParentDemand && !(ownerType == FinancialAccountOwnerType.PARENT
                && "DEMAND_DEPOSIT".equals(accountProductType))) {
            throw new BusinessException(ErrorCode.PARENT_DEMAND_DEPOSIT_REQUIRED);
        }

        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        String accountNumber = uniqueAccountNumber();
        Long ownerMemberId;
        if (ownerType == FinancialAccountOwnerType.PARENT) {
            ownerMemberId = memberId;
        } else {
            ownerMemberId = accountMapper.findActiveChildMemberIdByChildId(
                    request.getChildId()
            );
        }
        LocalDate maturityDate = "SAVINGS".equals(accountProductType)
                ? now.toLocalDate().plusMonths(product.getContractPeriodMonths())
                : null;
        String accountName = product.getName();
        boolean primary = ownerType == FinancialAccountOwnerType.PARENT
                && "DEMAND_DEPOSIT".equals(accountProductType)
                && !hasParentDemand;

        AccountOpenRecord account = new AccountOpenRecord(
                ownerType.name(),
                ownerMemberId,
                request.getChildId(),
                product.getFinancialProductId(),
                product.getBankName(),
                protector.encrypt(accountNumber),
                hash(accountNumber),
                accountName,
                accountProductType,
                deposit,
                primary,
                now,
                maturityDate
        );

        if (accountMapper.insertOpenedAccount(account) != 1 || account.getAccountId() == null)
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);

        return new AccountOpenResult(
                account.getAccountId(),
                ownerType.name(),
                request.getChildId(),
                product.getFinancialProductId(),
                product.getBankName(),
                accountName,
                accountNumber,
                accountProductType,
                deposit,
                primary,
                now
        );
    }

    private void validateParent(long id) {
        Member member = memberMapper.findById(id);
        if (member == null || member.getStatus() != MemberStatus.ACTIVE)
            throw new BusinessException(ErrorCode.INVALID_ACCESS_TOKEN);
        if (member.getMemberType() != MemberType.PARENT)
            throw new BusinessException(ErrorCode.PARENT_ACCESS_REQUIRED);
    }

    private FinancialAccountOwnerType parseOwner(AccountOpenRequest request) {
        if (request == null) throw new BusinessException(ErrorCode.INVALID_ACCOUNT_OPEN_REQUEST);
        try {
            FinancialAccountOwnerType type = FinancialAccountOwnerType.valueOf(request.getOwnerType());
            if ((type == FinancialAccountOwnerType.PARENT && request.getChildId() != null)
                    || (type == FinancialAccountOwnerType.CHILD
                    && (request.getChildId() == null || request.getChildId() < 1)))
                throw new IllegalArgumentException();
            return type;
        } catch (RuntimeException exception) {
            throw new BusinessException(ErrorCode.INVALID_ACCOUNT_OPEN_REQUEST);
        }
    }

    private void validateScope(long memberId, FinancialAccountOwnerType type, Long childId) {
        if (type == FinancialAccountOwnerType.PARENT) return;
        if (accountMapper.countActiveChildById(childId) < 1)
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        if (accountMapper.countActiveParentAccess(memberId, childId) < 1)
            throw new BusinessException(ErrorCode.CHILD_ACCESS_DENIED);
    }

    private FinancialProduct findProduct(Long id) {
        if (id == null || id < 1) throw new BusinessException(ErrorCode.INVALID_ACCOUNT_OPEN_REQUEST);
        FinancialProduct product = productMapper.findActiveProductById(id);
        if (product == null) throw new BusinessException(ErrorCode.FINANCIAL_PRODUCT_NOT_FOUND);
        return product;
    }

    private String mapProductType(String type) {
        if ("ACCOUNT".equals(type)) return "DEMAND_DEPOSIT";
        if ("SAVING".equals(type)) return "SAVINGS";
        throw new BusinessException(ErrorCode.INVALID_ACCOUNT_OPEN_REQUEST);
    }

    private void validateTargetOwner(FinancialAccountOwnerType ownerType,
                                     String targetOwnerType) {
        if (targetOwnerType == null
                || !Set.of("PARENT", "CHILD", "BOTH").contains(targetOwnerType)) {
            throw new BusinessException(ErrorCode.INVALID_ACCOUNT_OPEN_REQUEST);
        }
        if (ownerType == FinancialAccountOwnerType.PARENT
                || "BOTH".equals(targetOwnerType)
                || "CHILD".equals(targetOwnerType)) {
            return;
        }
        throw new BusinessException(ErrorCode.INVALID_ACCOUNT_OPEN_REQUEST);
    }

    private BigDecimal validateDeposit(BigDecimal value) {
        BigDecimal result = value == null ? BigDecimal.ZERO : value;
        if (result.signum() < 0) throw new BusinessException(ErrorCode.INVALID_ACCOUNT_OPEN_REQUEST);
        return result;
    }

    private String uniqueAccountNumber() {
        for (int i = 0; i < MAX_NUMBER_ATTEMPTS; i++) {
            String value = numberGenerator.generate();
            if (accountMapper.countAccountNumberHash(hash(value)) == 0) return value;
        }
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
