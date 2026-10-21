<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
</main>

<footer class="ss-footer">
    <div class="container">
        <div class="row g-4">
            <div class="col-lg-4">
                <div class="ss-brand-mark text-white mb-2">
                    <span class="ss-brand-dot" aria-hidden="true">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M3 3h2l.4 2M7 13h10l3-8H5.4M7 13L5.4 5M7 13l-2.2 4.6A1 1 0 0 0 5.7 19H19"/>
                            <circle cx="9" cy="20" r="1.4"/><circle cx="17" cy="20" r="1.4"/>
                        </svg>
                    </span>
                    <c:out value="${appName}"/>
                </div>
                <p class="mb-0" style="max-width: 34ch;">
                    A modern e-commerce demo built with Java Servlets, JSP, JDBC and MySQL.
                    Cash on delivery and demo card payments are simulated for this portfolio project.
                </p>
            </div>
            <div class="col-6 col-lg-2">
                <h6>Shop</h6>
                <a href="${ctx}/products">All products</a>
                <a href="${ctx}/products?sort=featured">Featured</a>
                <a href="${ctx}/products?sort=newest">New arrivals</a>
                <a href="${ctx}/cart">Your cart</a>
            </div>
            <div class="col-6 col-lg-2">
                <h6>Account</h6>
                <a href="${ctx}/account">My account</a>
                <a href="${ctx}/orders">My orders</a>
                <a href="${ctx}/account/addresses">Addresses</a>
                <a href="${ctx}/wishlist">Wishlist</a>
                <a href="${ctx}/login">Sign in</a>
            </div>
            <div class="col-lg-4">
                <h6>Customer care</h6>
                <p class="mb-2">Questions about an order? Reach the team at
                    <a href="mailto:support@shopsphere.local">support@shopsphere.local</a>.</p>
                <p class="mb-0">Monday to Saturday, 9:00 &ndash; 18:00.</p>
            </div>
        </div>
        <div class="ss-footer-bottom d-flex flex-wrap justify-content-between gap-2">
            <span>&copy; <c:out value="${appName}"/>. Demonstration project &mdash; not a real shop.</span>
            <span>Java 21 &middot; Jakarta Servlets &middot; JSP &middot; JDBC &middot; MySQL</span>
        </div>
    </div>
</footer>

<div class="ss-toast-holder"></div>
<script src="${ctx}/vendor/bootstrap/bootstrap.bundle.min.js"></script>
<script src="${ctx}/js/app.js"></script>
</body>
</html>
