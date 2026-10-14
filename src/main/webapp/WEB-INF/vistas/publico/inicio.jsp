<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<c:set var="volverA" value="/inicio"/>
<div class="container">
    <section class="hero rounded-4 p-4 p-md-5 mb-4 text-white d-flex flex-column flex-md-row justify-content-between align-items-md-center">
        <div>
            <h1 class="fw-bold display-6">Productos frescos, directo a tu hogar</h1>
            <p class="lead mb-3">Compra en línea de forma segura y recibe tu pedido en Portales y alrededores.</p>
            <a href="${ctx}/catalogo" class="btn btn-naranja btn-lg">Ver catálogo</a>
        </div>
        <div class="hero-iconos d-none d-md-block">🥑🌽🥛</div>
    </section>

    <div class="d-flex flex-wrap gap-2 mb-4">
        <a href="${ctx}/catalogo" class="btn btn-verde rounded-pill btn-sm">Todos</a>
        <c:forEach var="cat" items="${categorias}">
            <a href="${ctx}/catalogo?categoria=${cat.idCategoria}" class="btn btn-outline-success rounded-pill btn-sm"><c:out value="${cat.nombre}"/></a>
        </c:forEach>
    </div>

    <h2 class="h4 fw-bold mb-3">Los más vendidos</h2>
    <div class="row row-cols-2 row-cols-md-3 row-cols-lg-4 g-3">
        <c:forEach var="p" items="${destacados}">
            <div class="col"><%@ include file="/WEB-INF/vistas/comun/tarjeta-producto.jspf" %></div>
        </c:forEach>
    </div>

    <div class="row g-3 mt-4 text-center">
        <div class="col-md-4"><div class="p-3 bg-white rounded-3 shadow-sm h-100"><i class="bi bi-truck fs-2 text-success"></i><h3 class="h6 mt-2">Envío gratis desde Q 200</h3><p class="small text-muted mb-0">Entregas en Portales y municipios cercanos.</p></div></div>
        <div class="col-md-4"><div class="p-3 bg-white rounded-3 shadow-sm h-100"><i class="bi bi-shield-check fs-2 text-success"></i><h3 class="h6 mt-2">Compra 100 % segura</h3><p class="small text-muted mb-0">Tus datos viajan cifrados y no guardamos tu tarjeta.</p></div></div>
        <div class="col-md-4"><div class="p-3 bg-white rounded-3 shadow-sm h-100"><i class="bi bi-basket fs-2 text-success"></i><h3 class="h6 mt-2">Productos de calidad</h3><p class="small text-muted mb-0">Seleccionados de productores locales.</p></div></div>
    </div>
</div>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
