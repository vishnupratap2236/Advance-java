package com.shopsphere.model;

import java.util.List;

/**
 * A page of results together with the pagination metadata.
 */
public class PageResult<T> {

    private final List<T> items;
    private final int page;
    private final int pageSize;
    private final long totalItems;

    public PageResult(List<T> items, int page, int pageSize, long totalItems) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        if (pageSize <= 0) {
            return 1;
        }
        return (int) Math.max(1, Math.ceil((double) totalItems / pageSize));
    }

    public boolean isHasPrevious() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getTotalPages();
    }

    public int getPreviousPage() {
        return Math.max(1, page - 1);
    }

    public int getNextPage() {
        return Math.min(getTotalPages(), page + 1);
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    /** 1-based index of the first item on the page (0 when the page is empty). */
    public int getFirstItemIndex() {
        return isEmpty() ? 0 : (page - 1) * pageSize + 1;
    }

    /** 1-based index of the last item on the page (0 when the page is empty). */
    public int getLastItemIndex() {
        return isEmpty() ? 0 : getFirstItemIndex() + items.size() - 1;
    }
}
