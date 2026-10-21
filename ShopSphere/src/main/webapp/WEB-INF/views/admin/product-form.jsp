<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<c:set var="p" value="${product}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<h1 class="h3 mb-1">
    <c:choose>
        <c:when test="${p.id > 0}">Edit product</c:when>
        <c:otherwise>New product</c:otherwise>
    </c:choose>
</h1>
<p class="text-muted mb-4">All fields are validated on the server before the product is stored.</p>

<c:if test="${not empty formError}">
    <div class="alert alert-danger" role="alert"><c:out value="${formError}"/></div>
</c:if>

<form method="post" action="${ctx}/admin/products" class="row g-4">
    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
    <input type="hidden" name="action" value="save">
    <input type="hidden" name="id" value="${p.id}">

    <div class="col-lg-8">
        <div class="ss-card p-4 mb-4">
            <h2 class="h5 mb-3">Basics</h2>
            <div class="row g-3">
                <div class="col-md-8">
                    <label class="ss-form-label" for="name">Product name</label>
                    <input class="form-control" id="name" name="name" required maxlength="200"
                           value="<c:out value='${p.name}'/>">
                </div>
                <div class="col-md-4">
                    <label class="ss-form-label" for="sku">SKU</label>
                    <input class="form-control" id="sku" name="sku" required maxlength="60"
                           placeholder="SS-AUD-1001" value="<c:out value='${p.sku}'/>">
                </div>
                <div class="col-md-6">
                    <label class="ss-form-label" for="categoryId">Category</label>
                    <select class="form-select" id="categoryId" name="categoryId" required>
                        <option value="">Choose a category</option>
                        <c:forEach items="${categories}" var="category">
                            <option value="${category.id}" <c:if test="${p.categoryId == category.id}">selected</c:if>>
                                <c:out value="${category.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="shortDescription">Short description</label>
                    <input class="form-control" id="shortDescription" name="shortDescription" maxlength="500"
                           value="<c:out value='${p.shortDescription}'/>">
                </div>
                <div class="col-12">
                    <label class="ss-form-label" for="description">Full description</label>
                    <textarea class="form-control" id="description" name="description" rows="5"><c:out
                        value="${p.description}"/></textarea>
                </div>
            </div>
        </div>

        <div class="ss-card p-4 mb-4">
            <h2 class="h5 mb-3">Pricing and stock</h2>
            <div class="row g-3">
                <div class="col-md-4">
                    <label class="ss-form-label" for="price">Price</label>
                    <input class="form-control" type="number" step="0.01" min="0" id="price" name="price" required
                           value="<c:out value='${p.price}'/>">
                </div>
                <div class="col-md-4">
                    <label class="ss-form-label" for="comparePrice">Compare-at price</label>
                    <input class="form-control" type="number" step="0.01" min="0" id="comparePrice" name="comparePrice"
                           value="<c:out value='${p.comparePrice}'/>">
                </div>
                <div class="col-md-4">
                    <label class="ss-form-label" for="stock">Stock quantity</label>
                    <input class="form-control" type="number" min="0" id="stock" name="stock" required
                           value="${p.id > 0 ? p.stock : 0}">
                </div>
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <div class="ss-card p-4 mb-4">
            <h2 class="h5 mb-3">Image</h2>
            <label class="ss-form-label" for="imageUrl">Image path</label>
            <input class="form-control mb-2" id="imageUrl" name="imageUrl" maxlength="500"
                   placeholder="images/products/example.svg" value="<c:out value='${p.imageUrl}'/>">
            <div class="form-text mb-3">
                Relative paths such as <code>images/products/aurora-headphones.svg</code> are served from
                the web application. Uploads are stored outside the deployment.
            </div>
            <label class="ss-form-label" for="image-file">Upload an image</label>
            <input class="form-control" id="image-file" type="file" accept="image/*"
                   data-image-input="true"
                   data-image-target="#imageUrl"
                   data-upload-url="${ctx}/admin/products/upload">
            <div class="form-text">PNG, JPEG, GIF or WEBP, up to 2 MB.</div>
            <c:if test="${not empty p.imageUrl}">
                <img class="img-fluid rounded mt-3" src="${ctx}/<c:out value='${p.imageUrl}'/>"
                     alt="Current product image">
            </c:if>
        </div>

        <div class="ss-card p-4 mb-4">
            <h2 class="h5 mb-3">Visibility</h2>
            <div class="form-check mb-2">
                <input class="form-check-input" type="checkbox" id="active" name="active" value="true"
                       <c:if test="${p.id == 0 or p.active}">checked</c:if>>
                <label class="form-check-label" for="active">Visible in the shop</label>
            </div>
            <div class="form-check">
                <input class="form-check-input" type="checkbox" id="featured" name="featured" value="true"
                       <c:if test="${p.featured}">checked</c:if>>
                <label class="form-check-label" for="featured">Show on the home page</label>
            </div>
        </div>

        <div class="d-grid gap-2">
            <button class="btn ss-btn-primary" type="submit">Save product</button>
            <a class="btn ss-btn-ghost" href="${ctx}/admin/products">Cancel</a>
        </div>
    </div>
</form>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
