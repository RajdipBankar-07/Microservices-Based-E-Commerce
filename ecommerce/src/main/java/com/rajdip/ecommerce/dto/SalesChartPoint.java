package com.rajdip.ecommerce.dto;

public class SalesChartPoint {
    private String label;
    private double revenue;
    private long count;

    public SalesChartPoint(String label, double revenue, long count) {
        this.label = label;
        this.revenue = revenue;
        this.count = count;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
