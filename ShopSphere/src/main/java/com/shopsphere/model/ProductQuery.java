package com.shopsphere.model;

import java.math.BigDecimal;

/**
 * Search and filter criteria for the product catalogue.
 */
public class ProductQuery {

    public enum Sort {
        NEWEST("Newest first", "p.id DESC"),
        PRICE_ASC("Price: low to high", "p.price ASC, p.id DESC"),
        PRICE_DESC("Price: high to low", "p.price DESC, p.id DESC"),
        NAME_ASC("Name: A to Z", "p.name ASC"),
        FEATURED("Featured", "p.featured DESC, p.rating DESC, p.id DESC"),
        RATING("Top rated", "p.rating DESC, p.review_count DESC, p.id DESC");

        private final String label;
        private final String orderBy;

        Sort(String label, String orderBy) {
            this.label = label;
            this.orderBy = orderBy;
        }

        public String getLabel() {
            return label;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public static Sort fromString(String value) {
            if (value != null) {
                for (Sort sort : values()) {
                    if (sort.name().equalsIgnoreCase(value.trim())) {
                        return sort;
                    }
                }
            }
            return NEWEST;
        }
    }

    private String search;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Sort sort = Sort.NEWEST;
    private int page = 1;
    private int pageSize = 9;
    private boolean includeInactive;
    private boolean onlyFeatured;
    private boolean onlyInStock;

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort == null ? Sort.NEWEST : sort;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(1, page);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.min(48, Math.max(1, pageSize));
    }

    public boolean isIncludeInactive() {
        return includeInactive;
    }

    public void setIncludeInactive(boolean includeInactive) {
        this.includeInactive = includeInactive;
    }

    public boolean isOnlyFeatured() {
        return onlyFeatured;
    }

    public void setOnlyFeatured(boolean onlyFeatured) {
        this.onlyFeatured = onlyFeatured;
    }

    public boolean isOnlyInStock() {
        return onlyInStock;
    }

    public void setOnlyInStock(boolean onlyInStock) {
        this.onlyInStock = onlyInStock;
    }

    public boolean hasFilters() {
        return (search != null && !search.isBlank())
                || categoryId != null
                || minPrice != null
                || maxPrice != null
                || onlyInStock;
    }
}
