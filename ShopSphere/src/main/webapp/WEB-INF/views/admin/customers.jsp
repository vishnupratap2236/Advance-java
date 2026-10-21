<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${contextPath}"/>
<%@ include file="/WEB-INF/views/layout/header.jsp" %>

<h1 class="h3 mb-1">Customers</h1>
<p class="text-muted mb-4">Registered customer accounts and their status.</p>

<div class="ss-card">
    <div class="table-responsive">
        <table class="table ss-table align-middle">
            <thead>
            <tr>
                <th scope="col">Customer</th>
                <th scope="col">Phone</th>
                <th scope="col">Registered</th>
                <th scope="col">Status</th>
                <th scope="col" class="text-end">Actions</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${customers.isEmpty()}">
                    <tr><td colspan="5" class="text-center text-muted py-4">No customers yet.</td></tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${customers.items}" var="customer">
                        <tr>
                            <td>
                                <div class="d-flex align-items-center gap-2">
                                    <span class="ss-avatar"><c:out value="${customer.initials}"/></span>
                                    <div>
                                        <div class="fw-semibold"><c:out value="${customer.fullName}"/></div>
                                        <div class="small text-muted"><c:out value="${customer.email}"/></div>
                                    </div>
                                </div>
                            </td>
                            <td class="text-muted"><c:out value="${customer.phone}"/></td>
                            <td class="text-muted">
                                <c:out value="${com.shopsphere.util.Money.formatDate(customer.createdAt)}"/>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${customer.status == 'ACTIVE'}">
                                        <span class="ss-chip ss-chip-success">Active</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ss-chip ss-chip-danger">Suspended</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-end">
                                <form method="post" action="${ctx}/admin/customers" class="d-inline-flex gap-1">
                                    <input type="hidden" name="_csrf" value="<c:out value='${csrfToken}'/>">
                                    <input type="hidden" name="action" value="setStatus">
                                    <input type="hidden" name="id" value="${customer.id}">
                                    <c:choose>
                                        <c:when test="${customer.status == 'ACTIVE'}">
                                            <input type="hidden" name="status" value="SUSPENDED">
                                            <button class="btn ss-btn-ghost btn-sm" type="submit">Suspend</button>
                                        </c:when>
                                        <c:otherwise>
                                            <input type="hidden" name="status" value="ACTIVE">
                                            <button class="btn ss-btn-ghost btn-sm" type="submit">Activate</button>
                                        </c:otherwise>
                                    </c:choose>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>

<c:if test="${customers.totalPages > 1}">
    <nav class="mt-4" aria-label="Customer pages">
        <ul class="pagination ss-pagination justify-content-center">
            <li class="page-item ${customers.hasPrevious ? '' : 'disabled'}">
                <a class="page-link" href="${ctx}/admin/customers?page=${customers.previousPage}">Previous</a>
            </li>
            <c:forEach begin="1" end="${customers.totalPages}" var="pageNo">
                <li class="page-item ${pageNo == customers.page ? 'active' : ''}">
                    <a class="page-link" href="${ctx}/admin/customers?page=${pageNo}"><c:out value="${pageNo}"/></a>
                </li>
            </c:forEach>
            <li class="page-item ${customers.hasNext ? '' : 'disabled'}">
                <a class="page-link" href="${ctx}/admin/customers?page=${customers.nextPage}">Next</a>
            </li>
        </ul>
    </nav>
</c:if>

<%@ include file="/WEB-INF/views/layout/footer.jsp" %>
