package com.studyroom.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 分页数据封装类
 * 用于封装分页查询的结果
 * 
 * @param <T> 数据项类型
 * @author Developer
 * @version 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageData<T> {
    
    /**
     * 数据列表
     */
    @JsonProperty("content")
    private List<T> content;
    
    /**
     * 当前页码（从0开始）
     */
    @JsonProperty("pageNumber")
    private int page;
    
    /**
     * 每页大小
     */
    @JsonProperty("pageSize")
    private int size;
    
    /**
     * 总记录数
     */
    @JsonProperty("totalElements")
    private long totalElements;
    
    /**
     * 总页数
     */
    @JsonProperty("totalPages")
    private int totalPages;
    
    /**
     * 是否为第一页
     */
    @JsonProperty("isFirst")
    private boolean first;
    
    /**
     * 是否为最后一页
     */
    @JsonProperty("isLast")
    private boolean last;
    
    /**
     * 是否有下一页
     */
    @JsonProperty("hasNext")
    private boolean hasNext;
    
    /**
     * 是否有上一页
     */
    @JsonProperty("hasPrevious")
    private boolean hasPrevious;

    // 默认构造函数
    public PageData() {
    }

    // 构造函数
    public PageData(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.first = page == 0;
        this.last = page >= totalPages - 1;
        this.hasNext = !last;
        this.hasPrevious = !first;
    }

    /**
     * 判断是否有数据
     */
    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    /**
     * 获取数据大小
     */
    public int getContentSize() {
        return content != null ? content.size() : 0;
    }

    /**
     * 判断是否为空页
     */
    public boolean isEmpty() {
        return !hasContent();
    }

    // Getter 和 Setter 方法
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    @Override
    public String toString() {
        return "PageData{" +
                "contentSize=" + getContentSize() +
                ", page=" + page +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", first=" + first +
                ", last=" + last +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
} 