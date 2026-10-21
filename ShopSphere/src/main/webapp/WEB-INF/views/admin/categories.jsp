<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<h1 class="h3 mb-1">Categories</h1>
<p class="text-muted mb-4">Categories group the products in the storefront navigation.</p>

<c:if test="${not empty formError}">
    <div class="alert alert-danger" role="alert"><c:out value="${formError}"/></div>
</c:if>

<div class="row g-4">
    <div class="col-lg-8">
        <div class="ss-card">
            <div class="table-responsive">
                <table class="table ss-table align-middle">
                    <thead>
                    <tr>
                        <th scope="col">Category</th>
                        <th scope="col">Slug</th>
                        <th scope="col">Products</th>
                        <th scope="col">State</th>
                        <th scope="col" class="text-end">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${categories}" var="category">
                        <tr>
                            <td class="fw-semibold"><c:out value="${category.name}"/></td>
                            <td class="text-muted"><c:out value="${category.slug}"/></td>
                            <td><c:out value="${category.productCount}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${category.active}">
                                        <span class="ss-chip ss-chip-success">Active</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ss-chip ss-chip-muted">Hidden</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end">
                                <div class="d-inline-flex gap-1 align-items-center">
                                    <form method="post" action="${ctx}/admin/categories" class="d-inline-flex gap-1">
                                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                        <input type="hidden" name="action" value="update">
                                        <input type="hidden" name="id" value="${category.id}">
                                        <input class="form-control form-control-sm" name="name" value="<c:out value='${category.name}'/>"
                                               style="width:11rem" aria-label="Name of <c:out value='${category.name}'/>">
                                        <button class="btn ss-btn-ghost btn-sm" type="submit">Rename</button>
                                    </form>
                                    <form method="post" action="${ctx}/admin/categories" class="d-inline"
                                          onsubmit="return confirm('Delete this category?');">
                                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${category.id}">
                                        <button class="btn btn-link btn-sm text-danger text-decoration-none" type="submit">
                                            Delete
                                        </button>
                                    </form>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <div class="ss-card p-4">
            <h2 class="h5 mb-3">Add a category</h2>
            <form method="post" action="${ctx}/admin/categories" class="row g-3">
                <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="create">
                <div class="col-12">
                    <label class="ss-form-label" for="name">Name</label>
                    <input class="form-control" id="name" name="name" required maxlength="120">
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="description">Description</label>
                    <textarea class="form-control" id="description" name="description" rows="3" maxlength="500"></textarea>
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="imageUrl">Image path (optional)</label>
                    <input class="form-control" id="imageUrl" name="imageUrl" maxlength="500"
                           placeholder="images/categories/example.svg">
                </div>
                <div class="col-12">
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" id="active" name="active" value="true" checked>
                        <label class="form-check-label" for="active">Visible in the shop</label>
                    </div>
                </div>
                <div class="col-12">
                    <button class="btn ss-btn-primary w-100" type="submit">Create category</button>
                </div>
            </form>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
