package com.rajdip.ecommerce.dto;

import java.util.List;

/**
 * Aggregated product review summary:
 *  - reviews       : list of all Review objects
 *  - averageRating : mean of all ratings (0.0 if none)
 *  - totalReviews  : count of all reviews
 *  - ratingBreakdown: count per star (1★ to 5★)
 */
public class ProductReviewSummaryDTO {

    private List<?> reviews;
    private double  averageRating;
    private long    totalReviews;
    private long    star1Count;
    private long    star2Count;
    private long    star3Count;
    private long    star4Count;
    private long    star5Count;

    public ProductReviewSummaryDTO(List<?> reviews, double averageRating, long totalReviews,
                                   long s1, long s2, long s3, long s4, long s5) {
        this.reviews       = reviews;
        this.averageRating = Math.round(averageRating * 10.0) / 10.0; // 1 decimal place
        this.totalReviews  = totalReviews;
        this.star1Count    = s1;
        this.star2Count    = s2;
        this.star3Count    = s3;
        this.star4Count    = s4;
        this.star5Count    = s5;
    }

    public List<?>  getReviews()        { return reviews; }
    public double   getAverageRating()  { return averageRating; }
    public long     getTotalReviews()   { return totalReviews; }
    public long     getStar1Count()     { return star1Count; }
    public long     getStar2Count()     { return star2Count; }
    public long     getStar3Count()     { return star3Count; }
    public long     getStar4Count()     { return star4Count; }
    public long     getStar5Count()     { return star5Count; }
}
