package com.ruolian.yw.coustomview.chart.chartdata;

/**
 * @author yangwang
 * @date 18-6-6 14:29
 * @company Beijing QiaoData Management Co.
 * @projectName code
 * @packageName com.yw.linechat.linechat
 */
public class LineChartData {
    public int salary;
    public int year;
    public LineChartData(int year, int salary) {
        this.year = year;
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "LineChartData{" +
                "salary=" + salary +
                ", year=" + year +
                '}';
    }
}
