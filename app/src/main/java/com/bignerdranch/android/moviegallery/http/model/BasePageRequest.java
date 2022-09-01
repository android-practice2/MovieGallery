package com.bignerdranch.android.moviegallery.http.model;

import com.bignerdranch.android.moviegallery.constants.Constants;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;




public abstract class BasePageRequest {
    @Min(1)
    @NotNull
    protected Integer pageNumber = Constants.DEFAULT_PAGE;
    @Max(100)
    @NotNull
    protected Integer pageSize= Constants.PAGE_SIZE;


    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
