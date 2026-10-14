<%@ include file="/WEB-INF/vistas/admin/_encabezado.jspf" %>
<h1 class="h3 fw-bold mb-3">Categorías</h1>
<div class="row g-3">
    <div class="col-lg-7">
        <div class="caja table-responsive">
            <table class="table table-sm align-middle mb-0">
                <thead><tr><th>Nombre</th><th>Descripción</th><th class="text-center">Productos</th><th>Estado</th><th></th></tr></thead>
                <tbody>
                <c:forEach var="cat" items="${categorias}">
                    <tr><td class="fw-semibold"><c:out value="${cat.nombre}"/></td><td class="small"><c:out value="${cat.descripcion}"/></td>
                        <td class="text-center">${cat.totalProductos}</td>
                        <td><span class="badge ${cat.activo ? 'text-bg-success' : 'text-bg-secondary'}">${cat.activo ? 'Activa' : 'Inactiva'}</span></td>
                        <td><a href="${ctx}/admin/categorias?id=${cat.idCategoria}" class="btn btn-sm btn-outline-primary"><i class="bi bi-pencil"></i></a></td></tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
    <div class="col-lg-5">
        <form method="post" action="${ctx}/admin/categorias" class="caja">
            <h2 class="h6 fw-bold">${form.idCategoria == 0 ? 'Nueva categoría' : 'Editar categoría'}</h2>
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="id" value="${form.idCategoria}">
            <div class="mb-2"><label class="form-label">Nombre *</label><input name="nombre" class="form-control" maxlength="60" required value="<c:out value='${form.nombre}'/>"></div>
            <div class="mb-2"><label class="form-label">Descripción</label><input name="descripcion" class="form-control" maxlength="200" value="<c:out value='${form.descripcion}'/>"></div>
            <div class="form-check form-switch mb-3"><input class="form-check-input" type="checkbox" id="act" name="activo" ${form.activo ? 'checked' : ''}><label class="form-check-label" for="act">Activa</label></div>
            <button class="btn btn-verde"><i class="bi bi-save"></i> Guardar</button>
            <c:if test="${form.idCategoria != 0}"><a href="${ctx}/admin/categorias" class="btn btn-outline-secondary ms-2">Cancelar</a></c:if>
        </form>
    </div>
</div>
<%@ include file="/WEB-INF/vistas/admin/_pie.jspf" %>
