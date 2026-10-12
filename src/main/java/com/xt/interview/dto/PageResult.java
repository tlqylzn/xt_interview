package com.xt.interview.dto;

import java.util.List;

/**
 * 统一的分页返回结构。
 * 没有直接透出 Spring Data 的 Page：Page 的 JSON 里带 pageable/sort/first/last 等一堆
 * 前端用不上的字段，这里按接口约定只暴露需要的 5 个。
 *
 * @param <T> 列表元素类型
 */
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> list;
    /** 满足条件的总记录数 */
    private long totalElements;
    /** 总页数 */
    private int totalPages;
    /** 当前页码（从 0 开始） */
    private int currentPage;
    /** 每页条数 */
    private int pageSize;

    public PageResult() {}

    public PageResult(List<T> list, long totalElements, int totalPages, int currentPage, int pageSize) {
        this.list = list;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
