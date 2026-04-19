package com.redledger.dto;

import java.math.BigDecimal;

public record BalanceResponse(Long accountId, BigDecimal balance) {}