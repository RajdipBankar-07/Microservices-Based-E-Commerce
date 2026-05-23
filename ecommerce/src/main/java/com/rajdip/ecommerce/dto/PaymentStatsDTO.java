package com.rajdip.ecommerce.dto;

/**
 * Breakdown of payments by status + total revenue.
 */
public class PaymentStatsDTO {

    private long   total;
    private long   pending;
    private long   success;
    private long   failed;
    private long   refunded;
    private double totalRevenue; // sum of SUCCESS payments

    public PaymentStatsDTO(long total, long pending, long success,
                           long failed, long refunded, double totalRevenue) {
        this.total        = total;
        this.pending      = pending;
        this.success      = success;
        this.failed       = failed;
        this.refunded     = refunded;
        this.totalRevenue = totalRevenue;
    }

    public long   getTotal()        { return total; }
    public long   getPending()      { return pending; }
    public long   getSuccess()      { return success; }
    public long   getFailed()       { return failed; }
    public long   getRefunded()     { return refunded; }
    public double getTotalRevenue() { return totalRevenue; }
}
