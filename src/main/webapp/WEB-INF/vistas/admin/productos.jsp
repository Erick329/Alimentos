<%@ include file="/WEB-INF/vistas/admin/_encabezado.jspf" %>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h1 class="h3 fw-bold mb-0">Productos</h1>
    <a href="${ctx}/admin/productos?accion=nuevo" class="btn btn-verde"><i class="bi bi-plus-lg"></i> Nuevo producto</a>
</div>
<form class="row g-2 mb-3" method="get">
    <div class="col-md-4"><select name="categoria" class="form-select form-select-sm"><option value="0">Todas las categorías</option>
        <c:forEach var="cat" items="${categorias}"><option value="${cat.idCategoria}" ${cat.idCategoria == categoriaSel ? 'selected' : ''}><c:out value="${cat.nombre}"/></option></c:forEach></select></div>
    <div class="col-md-5"><input name="q" class="form-control form-control-sm" placeholder="Buscar por nombre o código" value="<c:out value='${q}'/>"></div>
    <div class="col-md-3"><button class="btn btn-sm btn-outline-success w-100"><i class="bi bi-funnel"></i> Filtrar</button></div>
</form>
<div class="caja table-responsive">
    <table class="table table-sm align-middle mb-0">
        <thead><tr><th>Código</th><th>Producto</th><th>Categoría</th><th class="text-end">Precio</th><th class="text-center">Stock</th><th>Estado</th><th class="text-end">Acciones</th></tr></thead>
        <tbody>
        <c:forEach var="p" items="${productos}">
            <tr class="${p.activo ? '' : 'text-muted'}">
                <td><c:out value="${p.codigo}"/></td>
                <td>${p.icono} <c:out value="${p.nombre}"/></td>
                <td><c:out value="${p.categoriaNombre}"/></td>
                <td class="text-end">Q <fmt:formatNumber value="${p.precio}" pattern="#,##0.00"/></td>
                <td class="text-center ${p.bajoStock ? 'text-danger fw-bold' : ''}">${p.stock}</td>
                <td><span class="badge ${p.activo ? 'text-bg-success' : 'text-bg-secondary'}">${p.activo ? 'Activo' : 'Inactivo'}</span></td>
                <td class="text-end text-nowrap">
                    <a href="${ctx}/admin/productos?accion=editar&id=${p.idProducto}" class="btn btn-sm btn-outline-primary" title="Editar"><i class="bi bi-pencil"></i></a>
                    <a href="${ctx}/admin/inventario?producto=${p.idProducto}" class="btn btn-sm btn-outline-success" title="Ver lotes"><i class="bi bi-boxes"></i></a>
                    <form method="post" class="d-inline" data-confirmar="${p.activo ? '¿Desactivar este producto? Dejará de mostrarse en el catálogo.' : '¿Activar este producto?'}">
                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                        <input type="hidden" name="accion" value="estado">
                        <input type="hidden" name="id" value="${p.idProducto}">
                        <input type="hidden" name="activo" value="${p.activo ? 0 : 1}">
                        <button class="btn btn-sm ${p.activo ? 'btn-outline-danger' : 'btn-outline-success'}" title="${p.activo ? 'Desactivar' : 'Activar'}"><i class="bi ${p.activo ? 'bi-eye-slash' : 'bi-eye'}"></i></button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty productos}"><tr><td colspan="7" class="text-center text-muted py-4">No hay productos.</td></tr></c:if>
        </tbody>
    </table>
</div>
<%@ include file="/WEB-INF/vistas/admin/_pie.jspf" %>
