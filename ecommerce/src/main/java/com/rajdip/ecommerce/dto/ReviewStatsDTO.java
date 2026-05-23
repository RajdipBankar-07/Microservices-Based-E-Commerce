package com.rajdip.ecommerce.dto;

/**
 * Aggregated review statistics across all products.
 */
public class ReviewStatsDTO {

    private long   totalReviews;
    private double overallAverageRating;
    private long   verifiedPurchaseReviews;

    public ReviewStatsDTO(long totalReviews, double overallAverageRating, long verifiedPurchaseReviews) {
        this.totalReviews            = totalReviews;
        this.overallAverageRating    = Math.round(overallAverageRating * 10.0) / 10.0;
        this.verifiedPurchaseReviews = verifiedPurchaseReviews;
    }

    public long   getTotalReviews()            { return totalReviews; }
    public double getOverallAverageRating()    { return overallAverageRating; }
    public long   getVerifiedPurchaseReviews() { return verifiedPurchaseReviews; }
}
