package com.rajdip.ecommerce.dto;

import java.util.List;

public class SalesDashboardData {
    private List<SalesChartPoint> day;
    private List<SalesChartPoint> week;
    private List<SalesChartPoint> month;
    private List<SalesChartPoint> year;

    public SalesDashboardData(List<SalesChartPoint> day, List<SalesChartPoint> week, List<SalesChartPoint> month, List<SalesChartPoint> year) {
        this.day = day;
        this.week = week;
        this.month = month;
        this.year = year;
    }

    public List<SalesChartPoint> getDay() {
        return day;
    }

    public void setDay(List<SalesChartPoint> day) {
        this.day = day;
    }

    public List<SalesChartPoint> getWeek() {
        return week;
    }

    public void setWeek(List<SalesChartPoint> week) {
        this.week = week;
    }

    public List<SalesChartPoint> getMonth() {
        return month;
    }

    public void setMonth(List<SalesChartPoint> month) {
        this.month = month;
    }

    public List<SalesChartPoint> getYear() {
        return year;
    }

    public void setYear(List<SalesChartPoint> year) {
        this.year = year;
    }
}
