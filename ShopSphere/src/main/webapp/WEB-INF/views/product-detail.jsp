<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<c:set var="p" value="${product}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<nav class="ss-breadcrumb mb-3" aria-label="Breadcrumb">
    <a href="${ctx}/">Home</a> /
    <a href="${ctx}/products">Products</a> /
    <a href="${ctx}/products?category=<c:out value='${p.categorySlug}'/>"><c:out value="${p.categoryName}"/></a> /
    <span><c:out value="${p.name}"/></span>
</nav>

<div class="row g-4 g-lg-5">
    <div class="col-lg-5">
        <div class="ss-gallery-main mb-3">
            <c:choose>
                <c:when test="${not empty p.images}">
                    <img id="ss-gallery-main-image" src="${ctx}/<c:out value='${p.images[0].imageUrl}'/>"
                         alt="<c:out value='${p.images[0].altText}'/>">
                </c:when>
                <c:when test="${not empty p.imageUrl}">
                    <img id="ss-gallery-main-image" src="${ctx}/<c:out value='${p.imageUrl}'/>"
                         alt="<c:out value='${p.name}'/>">
                </c:when>
                <c:otherwise>
                    <img id="ss-gallery-main-image" src="${ctx}/images/placeholder.svg" alt="<c:out value='${p.name}'/>">
                </c:otherwise>
            </c:choose>
        </div>
        <c:if test="${p.images.size() > 1}">
            <div class="d-flex gap-2 flex-wrap">
                <c:forEach items="${p.images}" var="image">
                    <img class="ss-gallery-thumb ${image.sortOrder == 0 ? 'active' : ''}"
                         src="${ctx}/<c:out value='${image.imageUrl}'/>"
                         data-full="${ctx}/<c:out value='${image.imageUrl}'/>"
                         alt="<c:out value='${image.altText}'/>">
                </c:forEach>
            </div>
        </c:if>
    </div>

    <div class="col-lg-7">
        <span class="ss-chip ss-chip-brand mb-2 d-inline-block"><c:out value="${p.categoryName}"/></span>
        <h1 class="h3 mb-2"><c:out value="${p.name}"/></h1>
        <p class="text-muted">SKU: <c:out value="${p.sku}"/></p>

        <div class="d-flex align-items-center gap-3 mb-3">
            <div class="d-flex align-items-center gap-1" style="color:#f59e0b">
                <span aria-hidden="true">&#9733;&#9733;&#9733;&#9733;&#9733;</span>
                <strong style="color:var(--ss-ink)"><c:out value="${p.rating}"/></strong>
                <span class="text-muted small">(<c:out value="${p.reviewCount}"/> reviews)</span>
            </div>
        </div>

        <div class="d-flex align-items-baseline gap-3 mb-3">
            <span class="ss-price" style="font-size:2rem"><c:out value="${currencySymbol}"/><c:out value="${p.price}"/></span>
            <c:if test="${p.onSale}">
                <span class="ss-price-strike" style="font-size:1.05rem">
                    <c:out value="${currencySymbol}"/><c:out value="${p.comparePrice}"/>
                </span>
                <span class="ss-chip ss-chip-success">Save <c:out value="${p.discountPercent}"/>%</span>
            </c:if>
        </div>

        <p class="mb-3"><c:out value="${p.shortDescription}"/></p>
        <p class="text-muted mb-4"><c:out value="${p.description}"/></p>

        <div class="d-flex align-items-center gap-3 mb-4">
            <c:choose>
                <c:when test="${p.inStock}">
                    <span class="ss-chip ss-chip-success">In stock</span>
                    <span class="small text-muted"><c:out value="${p.stock}"/> available</span>
                </c:when>
                <c:otherwise>
                    <span class="ss-chip ss-chip-danger">Out of stock</span>
                    <span class="small text-muted">We will notify you when it is back</span>
                </c:otherwise>
            </c:choose>
        </div>

        <c:choose>
            <c:when test="${empty currentUser}">
                <div class="ss-card p-3 d-flex flex-wrap align-items-center justify-content-between gap-3">
                    <span>Sign in to add this product to your cart and check out.</span>
                    <a class="btn ss-btn-primary" href="${ctx}/login?redirect=<c:out value='/product?id=${p.id}'/>">Sign in</a>
                </div>
            </c:when>
            <c:otherwise>
                <div class="d-flex flex-wrap align-items-end gap-2">
                    <form method="post" action="${ctx}/cart" class="d-flex flex-wrap align-items-end gap-2"
                          data-ajax-cart="true">
                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                        <input type="hidden" name="action" value="add">
                        <input type="hidden" name="productId" value="${p.id}">
                        <div>
                            <label class="ss-form-label" for="detail-qty">Quantity</label>
                            <div class="ss-qty-box">
                                <button type="button" data-qty-step="-1" aria-label="Decrease quantity">&minus;</button>
                                <input type="number" id="detail-qty" name="quantity" value="1" min="1" max="20"
                                       <c:if test="${p.stock < 1}">disabled</c:if>>
                                <button type="button" data-qty-step="1" aria-label="Increase quantity">+</button>
                            </div>
                        </div>
                        <button class="btn ss-btn-primary px-4" type="submit" <c:if test="${p.outOfStock}">disabled</c:if>>
                            Add to cart
                        </button>
                    </form>
                    <a class="btn ss-btn-ghost" href="${ctx}/cart">Go to cart</a>
                    <form method="post" action="${ctx}/wishlist" class="d-inline">
                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                        <input type="hidden" name="productId" value="${p.id}">
                        <button class="btn ss-btn-ghost" type="submit">
                            <c:choose>
                                <c:when test="${inWishlist}">&#9829; Saved</c:when>
                                <c:otherwise>&#9825; Save</c:otherwise>
                            </c:choose>
                        </button>
                    </form>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="row g-3 mt-3">
            <div class="col-sm-4">
                <div class="ss-card p-3 h-100">
                    <strong class="d-block mb-1">Free delivery</strong>
                    <span class="small text-muted">On every order above &#8377;4,999.</span>
                </div>
            </div>
            <div class="col-sm-4">
                <div class="ss-card p-3 h-100">
                    <strong class="d-block mb-1">30 day returns</strong>
                    <span class="small text-muted">Changed your mind? That is fine.</span>
                </div>
            </div>
            <div class="col-sm-4">
                <div class="ss-card p-3 h-100">
                    <strong class="d-block mb-1">Demo checkout</strong>
                    <span class="small text-muted">No real payment is processed.</span>
                </div>
            </div>
        </div>
    </div>
</div>

<section class="mt-5">
    <div class="ss-section-title">
        <div>
            <h2 class="h4 mb-1">Customer reviews</h2>
            <p>
                <c:choose>
                    <c:when test="${not empty reviews}">
                        <c:out value="${reviews.size()}"/> review(s) with an average of
                        <c:out value="${p.rating}"/> out of 5.
                    </c:when>
                    <c:otherwise>No reviews yet. Be the first to write one.</c:otherwise>
                </c:choose>
            </p>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-lg-7">
            <c:choose>
                <c:when test="${empty reviews}">
                    <div class="ss-card p-4 text-muted">No reviews for this product yet.</div>
                </c:when>
                <c:otherwise>
                    <div class="d-flex flex-column gap-3">
                        <c:forEach items="${reviews}" var="review">
                            <div class="ss-card p-3">
                                <div class="d-flex justify-content-between align-items-start gap-2">
                                    <div>
                                        <div style="color:#f59e0b;letter-spacing:1px">
                                            <c:out value="${review.stars}"/>
                                        </div>
                                        <c:if test="${not empty review.title}">
                                            <strong class="d-block mt-1"><c:out value="${review.title}"/></strong>
                                        </c:if>
                                    </div>
                                    <span class="small text-muted">
                                        <c:out value="${com.shopsphere.util.Money.formatDate(review.createdAt)}"/>
                                    </span>
                                </div>
                                <c:if test="${not empty review.comment}">
                                    <p class="mb-2 mt-2"><c:out value="${review.comment}"/></p>
                                </c:if>
                                <div class="d-flex justify-content-between align-items-center">
                                    <span class="small text-muted">
                                        <c:out value="${review.authorName}"/>
                                    </span>
                                    <c:if test="${currentUser.id == review.userId}">
                                        <form method="post" action="${ctx}/review"
                                              onsubmit="return confirm('Delete your review?');">
                                            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="reviewId" value="${review.id}">
                                            <input type="hidden" name="productId" value="${p.id}">
                                            <button class="btn btn-link btn-sm text-danger p-0 text-decoration-none"
                                                    type="submit">Delete</button>
                                        </form>
                                    </c:if>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="col-lg-5">
            <div class="ss-card p-4">
                <c:choose>
                    <c:when test="${empty currentUser}">
                        <h3 class="h6 mb-2">Write a review</h3>
                        <p class="text-muted small mb-3">Sign in to share your experience with this product.</p>
                        <a class="btn ss-btn-primary" href="${ctx}/login?redirect=<c:out value='/product?id=${p.id}'/>">Sign in</a>
                    </c:when>
                    <c:otherwise>
                        <h3 class="h6 mb-3">
                            <c:choose>
                                <c:when test="${not empty ownReview}">Update your review</c:when>
                                <c:otherwise>Write a review</c:otherwise>
                            </c:choose>
                        </h3>
                        <form method="post" action="${ctx}/review" class="row g-3">
                            <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="productId" value="${p.id}">
                            <div class="col-12">
                                <label class="ss-form-label" for="rating">Rating</label>
                                <select class="form-select" id="rating" name="rating" required>
                                    <option value="5" <c:if test="${ownReview.rating == 5}">selected</c:if>>5 - Excellent</option>
                                    <option value="4" <c:if test="${ownReview.rating == 4}">selected</c:if>>4 - Good</option>
                                    <option value="3" <c:if test="${ownReview.rating == 3}">selected</c:if>>3 - Average</option>
                                    <option value="2" <c:if test="${ownReview.rating == 2}">selected</c:if>>2 - Poor</option>
                                    <option value="1" <c:if test="${ownReview.rating == 1}">selected</c:if>>1 - Bad</option>
                                </select>
                            </div>
                            <div class="col-12">
                                <label class="ss-form-label" for="review-title">Title</label>
                                <input class="form-control" id="review-title" name="title" maxlength="150"
                                       value="<c:out value='${ownReview.title}'/>">
                            </div>
                            <div class="col-12">
                                <label class="ss-form-label" for="review-comment">Your review</label>
                                <textarea class="form-control" id="review-comment" name="comment" rows="4"
                                          maxlength="2000"><c:out value="${ownReview.comment}"/></textarea>
                            </div>
                            <div class="col-12">
                                <button class="btn ss-btn-primary" type="submit">Publish review</button>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</section>

<c:if test="${not empty relatedProducts}">
    <section class="mt-5">
        <div class="ss-section-title">
            <div>
                <h2 class="h4 mb-1">You might also like</h2>
                <p>More products from <c:out value="${p.categoryName}"/>.</p>
            </div>
        </div>
        <div class="row g-4">
            <c:forEach items="${relatedProducts}" var="related">
                <div class="col-6 col-md-3">
                    <c:set var="product" value="${related}" scope="request"/><jsp:include page="/WEB-INF/views/fragments/product-card.jsp"/>
                </div>
            </c:forEach>
        </div>
    </section>
</c:if>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
