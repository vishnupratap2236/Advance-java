<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<nav class="ss-breadcrumb mb-3" aria-label="Breadcrumb">
    <a href="${ctx}/">Home</a> /
    <c:choose>
        <c:when test="${not empty activeCategory}"><c:out value="${activeCategory.name}"/></c:when>
        <c:otherwise>Products</c:otherwise>
    </c:choose>
</nav>

<div class="row g-4">
    <aside class="col-lg-3">
        <form class="ss-card p-3 mb-3" method="get" action="${ctx}/products">
            <h2 class="h6 mb-3">Filters</h2>

            <c:if test="${not empty searchTerm}">
                <input type="hidden" name="q" value="<c:out value='${searchTerm}'/>">
            </c:if>
            <c:if test="${not empty sort}">
                <input type="hidden" name="sort" value="<c:out value='${sort.name()}'/>">
            </c:if>

            <label class="ss-form-label" for="filter-category">Category</label>
            <select class="form-select mb-3" id="filter-category" name="category" data-auto-submit>
                <option value="">All categories</option>
                <c:forEach items="${categories}" var="category">
                    <option value="<c:out value='${category.slug}'/>"
                            <c:if test="${categorySlug == category.slug or categoryId == category.id}">selected</c:if>>
                        <c:out value="${category.name}"/> (<c:out value="${category.productCount}"/>)
                    </option>
                </c:forEach>
            </select>

            <label class="ss-form-label" for="filter-min">Minimum price</label>
            <input type="number" class="form-control mb-2" id="filter-min" name="minPrice" min="0" step="1"
                   placeholder="0" value="<c:out value='${minPrice}'/>">

            <label class="ss-form-label" for="filter-max">Maximum price</label>
            <input type="number" class="form-control mb-3" id="filter-max" name="maxPrice" min="0" step="1"
                   placeholder="Any" value="<c:out value='${maxPrice}'/>">

            <div class="form-check mb-3">
                <input class="form-check-input" type="checkbox" id="filter-stock" name="inStock" value="true"
                       <c:if test="${inStockOnly}">checked</c:if>>
                <label class="form-check-label" for="filter-stock">In stock only</label>
            </div>

            <button class="btn ss-btn-primary w-100" type="submit">Apply filters</button>
            <a class="btn ss-btn-ghost w-100 mt-2" href="${ctx}/products">Reset</a>
        </form>
    </aside>

    <section class="col-lg-9">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-3">
            <div>
                <h1 class="h4 mb-1">
                    <c:choose>
                        <c:when test="${not empty activeCategory}"><c:out value="${activeCategory.name}"/></c:when>
                        <c:when test="${not empty searchTerm}">Results for &ldquo;<c:out value="${searchTerm}"/>&rdquo;</c:when>
                        <c:otherwise>All products</c:otherwise>
                    </c:choose>
                </h1>
                <p class="text-muted mb-0">
                    Showing <c:out value="${products.firstItemIndex}"/>&ndash;<c:out value="${products.lastItemIndex}"/>
                    of <c:out value="${products.totalItems}"/> products
                </p>
            </div>
            <form method="get" action="${ctx}/products" class="d-flex align-items-center gap-2">
                <c:if test="${not empty searchTerm}">
                    <input type="hidden" name="q" value="<c:out value='${searchTerm}'/>">
                </c:if>
                <c:if test="${not empty categorySlug}">
                    <input type="hidden" name="category" value="<c:out value='${categorySlug}'/>">
                </c:if>
                <label class="ss-form-label mb-0" for="sort">Sort by</label>
                <select class="form-select" id="sort" name="sort" data-auto-submit>
                    <c:forEach items="${sortOptions}" var="option">
                        <option value="${option.name()}" <c:if test="${option == sort}">selected</c:if>>
                            <c:out value="${option.label}"/>
                        </option>
                    </c:forEach>
                </select>
            </form>
        </div>

        <c:choose>
            <c:when test="${products.isEmpty()}">
                <div class="ss-card ss-empty">
                    <div class="ss-empty-icon">
                        <svg width="30" height="30" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="1.8" stroke-linecap="round">
                            <circle cx="11" cy="11" r="7"/><path d="M20 20l-3.5-3.5"/>
                        </svg>
                    </div>
                    <h2 class="h5">No products match your search</h2>
                    <p class="mb-3">Try a different keyword, widen the price range or clear the filters.</p>
                    <a class="btn ss-btn-primary" href="${ctx}/products">Browse everything</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="row g-4">
                    <c:forEach items="${products.items}" var="product">
                        <div class="col-6 col-md-4">
                            <c:set var="product" value="${product}" scope="request"/><jsp:include page="/WEB-INF/views/fragments/product-card.jsp"/>
                        </div>
                    </c:forEach>
                </div>

                <c:if test="${products.totalPages > 1}">
                    <nav class="mt-4" aria-label="Product pages">
                        <ul class="pagination ss-pagination justify-content-center flex-wrap mb-0">
                            <li class="page-item ${products.hasPrevious ? '' : 'disabled'}">
                                <a class="page-link"
                                   href="${ctx}/products?q=<c:out value='${searchTerm}'/>&sort=<c:out value='${sort.name()}'/>&page=${products.previousPage}">Previous</a>
                            </li>
                            <c:forEach begin="1" end="${products.totalPages}" var="pageNo">
                                <li class="page-item ${pageNo == products.page ? 'active' : ''}">
                                    <a class="page-link"
                                       href="${ctx}/products?q=<c:out value='${searchTerm}'/>&sort=<c:out value='${sort.name()}'/>&page=${pageNo}">
                                        <c:out value="${pageNo}"/>
                                    </a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${products.hasNext ? '' : 'disabled'}">
                                <a class="page-link"
                                   href="${ctx}/products?q=<c:out value='${searchTerm}'/>&sort=<c:out value='${sort.name()}'/>&page=${products.nextPage}">Next</a>
                            </li>
                        </ul>
                    </nav>
                </c:if>
            </c:otherwise>
        </c:choose>
    </section>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
