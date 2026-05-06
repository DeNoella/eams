package com.eams.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardResponseDto {
    private long totalAssets;
    private long totalUsers;
    private Map<String, Long> assetCountByStatus;
    private Map<String, Long> assetCountByCriticality;
    private Map<String, Long> assetCountByCategory;
    private long certificatesExpiring30Days;
    private long certificatesExpiring90Days;
    private long certificatesExpiring180Days;
    private long licencesAboveSeatThreshold;
    private long overdueRevertsCount;
    private long accessGrantsExpiringSoon;
    private long openAiAnomaliesCount;
}
