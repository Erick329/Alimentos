<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<c:set var="volverA" value="/catalogo?categoria=${categoriaSel}"/>
<div class="container">
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-2">
        <h1 class="h3 fw-bold mb-0">Catálogo</h1>
        <form class="d-flex gap-2" method="get" action="${ctx}/catalogo">
            <select name="categoria" class="form-select form-select-sm" aria-label="Categoría">
                <option value="0">Todas las categorías</option>
                <c:forEach var="cat" items="${categorias}">
                    <option value="${cat.idCategoria}" ${cat.idCategoria == categoriaSel ? 'selected' : ''}><c:out value="${cat.nombre}"/></option>
                </c:forEach>
            </select>
            <input type="search" name="q" class="form-control form-control-sm" placeholder="Nombre o código" value="<c:out value='${q}'/>">
            <button class="btn btn-verde btn-sm"><i class="bi bi-search"></i></button>
        </form>
    </div>
    <div class="d-flex flex-wrap gap-2 mb-3">
        <a href="${ctx}/catalogo" class="btn btn-sm rounded-pill ${categoriaSel == 0 ? 'btn-verde' : 'btn-outline-success'}">Todos</a>
        <c:forEach var="cat" items="${categorias}">
            <a href="${ctx}/catalogo?categoria=${cat.idCategoria}" class="btn btn-sm rounded-pill ${cat.idCategoria == categoriaSel ? 'btn-verde' : 'btn-outline-success'}"><c:out value="${cat.nombre}"/></a>
        </c:forEach>
    </div>
    <c:if test="${not empty q}">
        <p class="text-muted">Resultados para "<strong><c:out value="${q}"/></strong>": ${fn:length(productos)} producto(s).</p>
    </c:if>
    <c:choose>
        <c:when test="${empty productos}">
            <div class="text-center py-5 text-muted"><i class="bi bi-search fs-1"></i><p class="mt-2">No se encontraron productos.</p></div>
        </c:when>
        <c:otherwise>
            <div class="row row-cols-2 row-cols-md-3 row-cols-lg-4 g-3">
                <c:forEach var="p" items="${productos}">
                    <div class="col"><%@ include file="/WEB-INF/vistas/comun/tarjeta-producto.jspf" %></div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
