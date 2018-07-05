package com.rsmaxwell.pyrunner;

public class Result {

    private Integer count;
    private Double total;

    public Result(Integer count, Double total) {
        this.count = count;
        this.total = total;
    }

    public Integer getCount() {
        return count;
    }

    public Double getTotal() {
        return total;
    }

}
