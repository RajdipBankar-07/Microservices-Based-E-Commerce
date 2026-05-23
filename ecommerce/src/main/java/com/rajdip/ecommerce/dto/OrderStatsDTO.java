package com.rajdip.ecommerce.dto;

/**
 * Breakdown of orders by their status.
 */
public class OrderStatsDTO {

    private long total;
    private long placed;
    private long cancelled;
    private long refunded;

    public OrderStatsDTO(long total, long placed, long cancelled, long refunded) {
        this.total     = total;
        this.placed    = placed;
        this.cancelled = cancelled;
        this.refunded  = refunded;
    }

    public long getTotal()     { return total; }
    public long getPlaced()    { return placed; }
    public long getCancelled() { return cancelled; }
    public long getRefunded()  { return refunded; }
}
