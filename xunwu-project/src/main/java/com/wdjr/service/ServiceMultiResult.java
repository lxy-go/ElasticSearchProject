package com.wdjr.service;

import java.util.List;

/**
 * 通用多结果Service返回结构,分页用
 * @param <T>
 */

public class ServiceMultiResult<T> {
    private Integer total;
    private List<T> result;

    public ServiceMultiResult(Integer total, List<T> result) {
        this.total = total;
        this.result = result;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public int getResultSize(){
        if(this.result==null){
            return 0;
        }
        return this.result.size();
    }
}
