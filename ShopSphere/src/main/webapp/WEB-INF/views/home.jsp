<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<section class="ss-hero p-4 p-lg-5 mb-4">
    <div class="row align-items-center g-4 position-relative">
        <div class="col-lg-7">
            <span class="ss-eyebrow">New season collection</span>
            <h1 class="mt-3 mb-3">Everything you need, from one dependable shop.</h1>
            <p class="mb-4" style="max-width: 46ch; color: rgba(255,255,255,.85)">
                Curated audio, wearables, workspace gear and home essentials with honest
                pricing, fast delivery and a 30 day return window.
            </p>
            <div class="d-flex flex-wrap gap-2">
                <a class="btn btn-light btn-lg fw-semibold px-4" href="${ctx}/products">Shop the catalogue</a>
                <a class="btn btn-outline-light btn-lg fw-semibold px-4" href="${ctx}/products?sort=featured">See what's new</a>
            </div>
            <div class="d-flex flex-wrap gap-3 mt-4">
                <div class="ss-stat">
                    <strong>8</strong>
                    <span class="small">Categories</span>
                </div>
                <div class="ss-stat">
                    <strong>14+</strong>
                    <span class="small">Products</span>
                </div>
                <div class="ss-stat">
                    <strong>4.6&#9733;</strong>
                    <span class="small">Average rating</span>
                </div>
            </div>
        </div>
        <div class="col-lg-5 d-none d-lg-block text-center">
            <img src="${ctx}/images/hero.svg" alt="A selection of ShopSphere products" class="img-fluid" style="max-height:320px">
        </div>
    </div>
</section>

<section class="mb-5">
    <div class="ss-section-title">
        <div>
            <h2 class="h4 mb-1">Shop by category</h2>
            <p>Find what you need without digging through the whole catalogue.</p>
        </div>
        <a class="btn ss-btn-ghost" href="${ctx}/products">All products</a>
    </div>
    <div class="row g-3">
        <c:forEach items="${categories}" var="category">
            <div class="col-6 col-md-4 col-xl-3">
                <a class="ss-category-tile" href="${ctx}/products?category=<c:out value='${category.slug}'/>">
                    <c:choose>
                        <c:when test="${not empty category.imageUrl}">
                            <img src="${ctx}/<c:out value='${category.imageUrl}'/>" alt="<c:out value='${category.name}'/>" loading="lazy">
                        </c:when>
                        <c:otherwise>
                            <img src="${ctx}/images/placeholder.svg" alt="<c:out value='${category.name}'/>" loading="lazy">
                        </c:otherwise>
                    </c:choose>
                    <figcaption>
                        <c:out value="${category.name}"/>
                        <span class="d-block small" style="font-weight:500;opacity:.85">
                            <c:out value="${category.productCount}"/> products
                        </span>
                    </figcaption>
                </a>
            </div>
        </c:forEach>
    </div>
</section>

<section class="mb-5">
    <div class="ss-section-title">
        <div>
            <h2 class="h4 mb-1">Featured products</h2>
            <p>Hand-picked by our buying team.</p>
        </div>
        <a class="btn ss-btn-ghost" href="${ctx}/products?sort=featured">See all</a>
    </div>
    <div class="row g-4">
        <c:forEach items="${featuredProducts}" var="product">
            <div class="col-6 col-md-4 col-xl-3">
                <c:set var="product" value="${product}" scope="request"/><jsp:include page="/WEB-INF/views/fragments/product-card.jsp"/>
            </div>
        </c:forEach>
    </div>
</section>

<section class="mb-4">
    <div class="ss-section-title">
        <div>
            <h2 class="h4 mb-1">New arrivals</h2>
            <p>The latest additions to the shelf.</p>
        </div>
        <a class="btn ss-btn-ghost" href="${ctx}/products?sort=newest">See all</a>
    </div>
    <div class="row g-4">
        <c:forEach items="${latestProducts}" var="product">
            <div class="col-6 col-md-3">
                <c:set var="product" value="${product}" scope="request"/><jsp:include page="/WEB-INF/views/fragments/product-card.jsp"/>
            </div>
        </c:forEach>
    </div>
</section>

<section class="ss-card-lg p-4 p-lg-5 mb-4">
    <div class="row align-items-center g-4">
        <div class="col-lg-7">
            <h2 class="h4">Built with a real back end</h2>
            <p class="text-muted mb-3">
                ShopSphere is a conventional Java web application: Jakarta Servlets for the
                controllers, JSP and JSTL for the views, JDBC with HikariCP for pooled
                database access and MySQL for the data. Every price shown here is calculated
                on the server from the database.
            </p>
            <div class="d-flex flex-wrap gap-2">
                <span class="ss-chip ss-chip-muted">BCrypt passwords</span>
                <span class="ss-chip ss-chip-muted">CSRF protected</span>
                <span class="ss-chip ss-chip-muted">Role based access</span>
                <span class="ss-chip ss-chip-muted">Transactional checkout</span>
                <span class="ss-chip ss-chip-muted">Prepared statements</span>
            </div>
        </div>
        <div class="col-lg-5">
            <div class="ss-card p-3">
                <div class="d-flex justify-content-between small mb-1">
                    <span>Order integrity</span><strong>100%</strong>
                </div>
                <div class="progress mb-3" style="height:8px"><div class="progress-bar bg-success" style="width:100%"></div></div>
                <div class="d-flex justify-content-between small mb-1">
                    <span>Server side validation</span><strong>Every request</strong>
                </div>
                <div class="progress mb-3" style="height:8px"><div class="progress-bar" style="width:100%"></div></div>
                <div class="d-flex justify-content-between small mb-1">
                    <span>Payment</span><strong>Demo only</strong>
                </div>
                <div class="progress" style="height:8px"><div class="progress-bar bg-warning" style="width:35%"></div></div>
            </div>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
