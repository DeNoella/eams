package com.eams.util;

import com.eams.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class AssetIdGenerator {

    private final AssetRepository assetRepository;

    /**
     * Generates asset ID from template like {CODE}-{LOC}-{YYYY}-{SEQ:4}
     * Example output: APP-NBI-2026-0042
     */
    public synchronized String generate(String template, String categoryCode,
            String locationCode, UUID organisationId) {
        String year = String.valueOf(Year.now().getValue());

        // Get next sequence number for this org+category+year
        int nextSeq = getNextSequence(organisationId, categoryCode, year);

        return template
            .replace("{CODE}", categoryCode.toUpperCase())
            .replace("{LOC}", locationCode != null ? locationCode.toUpperCase() : "GEN")
            .replace("{YYYY}", year)
            .replace("{YY}", year.substring(2))
            .replaceAll("\\{SEQ:(\\d+)\\}", padded(nextSeq, extractPadding(template)));
    }

    private int getNextSequence(UUID organisationId, String categoryCode, String year) {
        String prefix = categoryCode.toUpperCase();
        // Count existing assets with this prefix for this org to determine next seq
        // In production, use a dedicated sequence table for better performance
        long count = assetRepository.countByOrganisationIdAndAssetIdDisplayStartingWith(
            organisationId, prefix);
        return (int) count + 1;
    }

    private String padded(int number, int width) {
        return String.format("%0" + width + "d", number);
    }

    private int extractPadding(String template) {
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("\\{SEQ:(\\d+)\\}").matcher(template);
        return m.find() ? Integer.parseInt(m.group(1)) : 4;
    }
}