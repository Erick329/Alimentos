<%@ include file="/WEB-INF/vistas/admin/_encabezado.jspf" %>
<c:set var="p" value="${producto}"/>
<h1 class="h3 fw-bold mb-3"><c:out value="${titulo}"/></h1>
<c:if test="${not empty errores}">
    <div class="alert alert-danger small"><ul class="mb-0"><c:forEach var="e" items="${errores}"><li><c:out value="${e}"/></li></c:forEach></ul></div>
</c:if>
<form method="post" action="${ctx}/admin/productos" class="caja" style="max-width: 860px">
    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
    <input type="hidden" name="accion" value="guardar">
    <input type="hidden" name="id" value="${p.idProducto}">
    <div class="row g-3">
        <div class="col-md-4"><label class="form-label">Código *</label><input name="codigo" class="form-control" maxlength="20" required value="<c:out value='${p.codigo}'/>" placeholder="GB-010"></div>
        <div class="col-md-8"><label class="form-label">Nombre *</label><input name="nombre" class="form-control" maxlength="100" required value="<c:out value='${p.nombre}'/>"></div>
        <div class="col-md-6"><label class="form-label">Categoría *</label>
            <select name="idCategoria" class="form-select" required><option value="">Seleccione...</option>
                <c:forEach var="cat" items="${categorias}"><option value="${cat.idCategoria}" ${cat.idCategoria == p.idCategoria ? 'selected' : ''}><c:out value="${cat.nombre}"/></option></c:forEach>
            </select></div>
        <div class="col-md-6"><label class="form-label">Precio (Q, IVA incluido) *</label><input name="precio" type="number" step="0.01" min="0.01" class="form-control" required value="${p.precio > 0 ? p.precio : ''}"></div>
        <div class="col-md-6"><label class="form-label">Stock actual</label>
            <input type="number" class="form-control" value="${p.stock}" disabled>
            <div class="form-text">El stock es la suma de los lotes activos. Se maneja en
                <c:choose><c:when test="${p.idProducto != 0}"><a href="${ctx}/admin/inventario?producto=${p.idProducto}">Inventario por lotes</a>.</c:when><c:otherwise>Inventario después de crear el producto.</c:otherwise></c:choose></div></div>
        <div class="col-md-6"><label class="form-label">Stock mínimo *</label><input name="stockMinimo" type="number" min="0" class="form-control" required value="${p.stockMinimo}"></div>
        <div class="col-12"><label class="form-label">Descripción *</label><textarea name="descripcion" class="form-control" rows="3" maxlength="255" required><c:out value="${p.descripcion}"/></textarea></div>
        <div class="col-12"><div class="form-check form-switch"><input class="form-check-input" type="checkbox" name="activo" id="activo" ${p.activo ? 'checked' : ''}><label class="form-check-label" for="activo">Producto activo (visible en el catálogo)</label></div></div>
    </div>
    <div class="mt-4">
        <button class="btn btn-verde"><i class="bi bi-save"></i> Guardar</button>
        <a href="${ctx}/admin/productos" class="btn btn-outline-secondary ms-2">Cancelar</a>
    </div>
</form>
<%@ include file="/WEB-INF/vistas/admin/_pie.jspf" %>
