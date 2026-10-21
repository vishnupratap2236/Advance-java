<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${empty contextPath ? pageContext.request.contextPath : contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="csrf-token" content="<c:out value='${csrfToken}'/>">
    <title><c:out value="${pageTitle}"/> &middot; <c:out value="${appName}"/></title>
    <link rel="icon" href="${ctx}/images/favicon.svg" type="image/svg+xml">
    <link rel="stylesheet" href="${ctx}/vendor/bootstrap/bootstrap.min.css">
    <link rel="stylesheet" href="${ctx}/css/app.css">
</head>
<body>
<a class="visually-hidden-focusable" href="#ss-main">Skip to main content</a>

<div class="ss-topbar">
    <div class="container d-flex flex-wrap align-items-center justify-content-between gap-2">
        <span>Free delivery on orders over &#8377;4,999 &middot; 30 day returns</span>
        <span class="d-none d-md-inline">Test checkout &mdash; no real payment is processed</span>
    </div>
</div>

<nav class="ss-navbar sticky-top">
    <div class="container">
        <div class="d-flex align-items-center gap-3 py-2">
            <a class="ss-brand-mark" href="${ctx}/">
                <span class="ss-brand-dot" aria-hidden="true">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M3 3h2l.4 2M7 13h10l3-8H5.4M7 13L5.4 5M7 13l-2.2 4.6A1 1 0 0 0 5.7 19H19"/>
                        <circle cx="9" cy="20" r="1.4"/><circle cx="17" cy="20" r="1.4"/>
                    </svg>
                </span>
                <c:out value="${appName}"/>
            </a>

            <form class="ss-searchbar flex-grow-1 d-none d-md-flex" method="get" action="${ctx}/products"
                  role="search">
                <label class="visually-hidden" for="header-search">Search products</label>
                <input type="search" id="header-search" name="q" placeholder="Search for products, brands and categories"
                       value="<c:out value='${param.q}'/>">
                <button class="btn btn-sm ss-btn-primary px-3" type="submit">Search</button>
            </form>

            <div class="d-flex align-items-center gap-2 ms-auto">
                <a class="ss-icon-btn" href="${ctx}/cart" title="Shopping cart" aria-label="Shopping cart">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="9" cy="21" r="1.4"/><circle cx="19" cy="21" r="1.4"/>
                        <path d="M1 1h4l2.7 13.4a2 2 0 0 0 2 1.6h9.7a2 2 0 0 0 2-1.6L23 6H6"/>
                    </svg>
                    <span class="ss-cart-badge" data-cart-count
                          style="<c:if test='${empty cartCount}'>display:none;</c:if>"><c:out value="${cartCount}"/></span>
                </a>

                <c:choose>
                    <c:when test="${not empty currentUser}">
                        <div class="dropdown">
                            <button class="btn d-flex align-items-center gap-2" data-bs-toggle="dropdown" aria-expanded="false">
                                <span class="ss-avatar"><c:out value="${currentUser.initials}"/></span>
                                <span class="d-none d-lg-inline text-start lh-sm">
                                    <span class="d-block fw-semibold" style="font-size:.9rem"><c:out value="${currentUser.fullName}"/></span>
                                    <span class="d-block text-muted" style="font-size:.75rem">My account</span>
                                </span>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 mt-2">
                                <li><a class="dropdown-item" href="${ctx}/account">Profile</a></li>
                                <li><a class="dropdown-item" href="${ctx}/account/addresses">Address book</a></li>
                                <li><a class="dropdown-item" href="${ctx}/orders">My orders</a></li>
                                <c:if test="${currentUser.admin}">
                                    <li><hr class="dropdown-divider"></li>
                                    <li><a class="dropdown-item" href="${ctx}/admin">Administration</a></li>
                                </c:if>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <form method="post" action="${ctx}/logout" class="px-3 py-1">
                                        <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                        <button type="submit" class="btn btn-sm btn-link p-0 text-danger text-decoration-none">Sign out</button>
                                    </form>
                                </li>
                            </ul>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <a class="btn ss-btn-ghost d-none d-sm-inline-block" href="${ctx}/login">Sign in</a>
                        <a class="btn ss-btn-primary d-none d-sm-inline-block" href="${ctx}/register">Create account</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <form class="ss-searchbar d-flex d-md-none my-2" method="get" action="${ctx}/products" role="search">
            <label class="visually-hidden" for="mobile-search">Search products</label>
            <input type="search" id="mobile-search" name="q" placeholder="Search products"
                   value="<c:out value='${param.q}'/>">
            <button class="btn btn-sm ss-btn-primary px-3" type="submit">Search</button>
        </form>

        <div class="d-flex align-items-center gap-1 pb-2 flex-wrap">
            <a class="ss-nav-link" href="${ctx}/">Home</a>
            <a class="ss-nav-link" href="${ctx}/products">All products</a>
            <a class="ss-nav-link" href="${ctx}/products?sort=featured">Featured</a>
            <a class="ss-nav-link" href="${ctx}/products?sort=newest">New arrivals</a>
            <c:if test="${not empty currentUser}">
                <a class="ss-nav-link" href="${ctx}/wishlist">Wishlist</a>
            </c:if>
            <c:if test="${not empty currentUser and currentUser.admin}">
                <a class="ss-nav-link" href="${ctx}/admin">Admin</a>
            </c:if>
        </div>
    </div>
</nav>

<main id="ss-main" class="container py-4">
    <c:if test="${not empty flashMessages}">
        <div class="mb-3">
            <c:forEach items="${flashMessages}" var="flash">
                <div class="alert alert-${flash[0]} alert-dismissible fade show d-flex align-items-start gap-2"
                     role="alert">
                    <c:out value="${flash[1]}"/>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:forEach>
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger d-flex align-items-start gap-2" role="alert">
            <c:out value="${errorMessage}"/>
        </div>
    </c:if>
