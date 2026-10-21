<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
    <div>
        <h1 class="h3 mb-1">Products</h1>
        <p class="text-muted mb-0">Create, edit and deactivate the items in the catalogue.</p>
    </div>
    <a class="btn ss-btn-primary" href="${ctx}/admin/products/new">Add product</a>
</div>

<div class="ss-card mb-4">
    <div class="p-3 border-bottom">
        <form method="get" action="${ctx}/admin/products" class="row g-2 align-items-end">
            <div class="col-md-6">
                <label class="ss-form-label" for="admin-search">Search products</label>
                <input class="form-control" id="admin-search" name="q" placeholder="Name, SKU or description"
                       value="<c:out value='${searchTerm}'/>">
            </div>
            <div class="col-md-3">
                <label class="ss-form-label" for="admin-category">Category</label>
                <select class="form-select" id="admin-category" name="category" data-auto-submit>
                    <option value="">All categories</option>
                    <c:forEach items="${categories}" var="category">
                        <option value="${category.id}" <c:if test="${query.categoryId == category.id}">selected</c:if>>
                            <c:out value="${category.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <label class="ss-form-label" for="admin-sort">Sort</label>
                <select class="form-select" id="admin-sort" name="sort" data-auto-submit>
                    <c:forEach items="${sortOptions}" var="option">
                        <option value="${option.name()}" <c:if test="${option == query.sort}">selected</c:if>>
                            <c:out value="${option.label}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        </form>
    </div>

    <div class="table-responsive">
        <table class="table ss-table align-middle">
            <thead>
            <tr>
                <th scope="col">Product</th>
                <th scope="col">Category</th>
                <th scope="col">Price</th>
                <th scope="col">Stock</th>
                <th scope="col">State</th>
                <th scope="col" class="text-end">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${products.isEmpty()}">
                    <tr>
                        <td colspan="6" class="text-center text-muted py-4">No products match your search.</td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${products.items}" var="product">
                        <tr>
                            <td>
                                <div class="d-flex align-items-center gap-3">
                                    <img class="ss-thumb" style="width:48px;height:48px"
                                         src="${ctx}/<c:out value='${product.imageUrl}'/>"
                                         alt="<c:out value='${product.name}'/>">
                                    <div>
                                        <div class="fw-semibold"><c:out value="${product.name}"/></div>
                                        <div class="small text-muted"><c:out value="${product.sku}"/></div>
                                    </div>
                                </div>
                            </td>
                            <td class="text-muted"><c:out value="${product.categoryName}"/></td>
                            <td>
                                <c:out value="${currencySymbol}"/><c:out value="${product.price}"/>
                            </td>
                            <td>
                                <form method="post" action="${ctx}/admin/products" class="d-flex align-items-center gap-1">
                                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                    <input type="hidden" name="action" value="setStock">
                                    <input type="hidden" name="id" value="${product.id}">
                                    <input class="form-control form-control-sm" type="number" name="stock" min="0"
                                           value="${product.stock}" style="width:5rem" aria-label="Stock for <c:out value='${product.name}'/>">
                                    <button class="btn ss-btn-ghost btn-sm" type="submit">Save</button>
                                </form>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${product.active}">
                                        <span class="ss-chip ss-chip-success">Active</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ss-chip ss-chip-muted">Hidden</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end">
                                <div class="d-inline-flex gap-1">
                                    <a class="btn ss-btn-ghost btn-sm" href="${ctx}/admin/products/edit?id=${product.id}">Edit</a>
                                    <form method="post" action="${ctx}/admin/products" class="d-inline">
                                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                        <input type="hidden" name="action" value="toggleActive">
                                        <input type="hidden" name="id" value="${product.id}">
                                        <button class="btn ss-btn-ghost btn-sm" type="submit">
                                            <c:choose>
                                                <c:when test="${product.active}">Hide</c:when>
                                                <c:otherwise>Show</c:otherwise>
                                            </c:choose>
                                        </button>
                                    </form>
                                    <form method="post" action="${ctx}/admin/products" class="d-inline"
                                          onsubmit="return confirm('Delete this product?');">
                                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${product.id}">
                                        <button class="btn btn-link btn-sm text-danger text-decoration-none" type="submit">
                                            Delete
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>

<c:if test="${products.totalPages > 1}">
    <nav aria-label="Product pages">
        <ul class="pagination ss-pagination justify-content-center">
            <li class="page-item ${products.hasPrevious ? '' : 'disabled'}">
                <a class="page-link" href="${ctx}/admin/products?q=<c:out value='${searchTerm}'/>&page=${products.previousPage}">Previous</a>
            </li>
            <c:forEach begin="1" end="${products.totalPages}" var="pageNo">
                <li class="page-item ${pageNo == products.page ? 'active' : ''}">
                    <a class="page-link" href="${ctx}/admin/products?q=<c:out value='${searchTerm}'/>&page=${pageNo}">
                        <c:out value="${pageNo}"/>
                    </a>
                </li>
            </c:forEach>
            <li class="page-item ${products.hasNext ? '' : 'disabled'}">
                <a class="page-link" href="${ctx}/admin/products?q=<c:out value='${searchTerm}'/>&page=${products.nextPage}">Next</a>
            </li>
        </ul>
    </nav>
</c:if>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
