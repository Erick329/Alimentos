<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<c:set var="volverA" value="/producto?id=${producto.idProducto}"/>
<div class="container">
    <nav aria-label="breadcrumb"><ol class="breadcrumb small">
        <li class="breadcrumb-item"><a href="${ctx}/catalogo">Catálogo</a></li>
        <li class="breadcrumb-item"><a href="${ctx}/catalogo?categoria=${producto.idCategoria}"><c:out value="${producto.categoriaNombre}"/></a></li>
        <li class="breadcrumb-item active"><c:out value="${producto.nombre}"/></li>
    </ol></nav>
    <div class="row g-4 bg-white rounded-4 shadow-sm p-3 p-md-4 mx-0">
        <div class="col-md-5">
            <div class="img-producto img-grande rounded-3 icono-cat-${producto.idCategoria}">${producto.icono}</div>
        </div>
        <div class="col-md-7">
            <span class="badge text-bg-light border mb-2"><c:out value="${producto.categoriaNombre}"/></span>
            <h1 class="h3 fw-bold"><c:out value="${producto.nombre}"/></h1>
            <p class="text-muted small">Código: <c:out value="${producto.codigo}"/></p>
            <p class="precio fs-2">Q <fmt:formatNumber value="${producto.precio}" pattern="#,##0.00"/> <small class="fs-6 text-muted fw-normal">IVA incluido</small></p>
            <p><c:out value="${producto.descripcion}"/></p>
            <p>
                <c:choose>
                    <c:when test="${producto.agotado}"><span class="text-danger fw-semibold"><i class="bi bi-x-circle"></i> Agotado</span></c:when>
                    <c:when test="${producto.bajoStock}"><span class="text-warning fw-semibold"><i class="bi bi-exclamation-circle"></i> ¡Últimas ${producto.stock} unidades!</span></c:when>
                    <c:otherwise><span class="text-success fw-semibold"><i class="bi bi-check-circle"></i> Disponible (${producto.stock} unidades)</span></c:otherwise>
                </c:choose>
                <c:if test="${not empty producto.proximoVencimiento}"><br><small class="text-muted">Consumir antes de: <fmt:formatDate value="${producto.proximoVencimiento}" pattern="dd/MM/yyyy"/> (lote más próximo)</small></c:if>
            </p>
            <c:if test="${not producto.agotado}">
                <form action="${ctx}/carrito" method="post" class="d-flex gap-2 align-items-center" style="max-width: 320px">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="accion" value="agregar">
                    <input type="hidden" name="idProducto" value="${producto.idProducto}">
                    <input type="hidden" name="volver" value="${volverA}">
                    <input type="number" name="cantidad" value="1" min="1" max="${producto.stock}" class="form-control" aria-label="Cantidad">
                    <button class="btn btn-naranja text-nowrap"><i class="bi bi-cart-plus"></i> Agregar al carrito</button>
                </form>
                <c:if test="${enCarrito > 0}"><p class="small text-muted mt-2">Ya tiene ${enCarrito} en su <a href="${ctx}/carrito">carrito</a>.</p></c:if>
            </c:if>
        </div>
    </div>
    <c:if test="${not empty relacionados}">
        <h2 class="h5 fw-bold mt-4 mb-3">También te puede interesar</h2>
        <div class="row row-cols-2 row-cols-md-4 g-3">
            <c:forEach var="p" items="${relacionados}">
                <div class="col"><%@ include file="/WEB-INF/vistas/comun/tarjeta-producto.jspf" %></div>
            </c:forEach>
        </div>
    </c:if>
</div>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
