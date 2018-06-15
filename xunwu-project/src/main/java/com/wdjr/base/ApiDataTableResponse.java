package com.wdjr.base;

public class ApiDataTableResponse extends ApiResponse{

    private int draw;
    private long recordsTotal;
    private long recordsFiltered;

    //自定义构造函数
    public ApiDataTableResponse(ApiResponse.Status status){
        this(status.getCode(),status.getStandardMessage(),null);
    }

    public ApiDataTableResponse(int code,String message,Object data) {
        super(code,message,data);
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public long getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public long getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }
}
