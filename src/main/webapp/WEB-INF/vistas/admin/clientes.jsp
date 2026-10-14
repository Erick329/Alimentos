<%@ include file="/WEB-INF/vistas/admin/_encabezado.jspf" %>
<h1 class="h3 fw-bold mb-3">Clientes y usuarios</h1>
<div class="caja table-responsive">
    <table class="table table-sm align-middle mb-0">
        <thead><tr><th>Nombre</th><th>Correo</th><th>Teléfono</th><th>Rol</th><th class="text-center">Pedidos</th><th>Registro</th><th>Estado</th><th class="text-end">Acciones</th></tr></thead>
        <tbody>
        <c:forEach var="x" items="${usuarios}">
            <tr><td><c:out value="${x.nombreCompleto}"/></td><td><c:out value="${x.email}"/></td><td><c:out value="${x.telefono}"/></td>
                <td><span class="badge ${x.rol == 'CLIENTE' ? 'text-bg-light border' : 'text-bg-primary'}">${x.rol}</span></td>
                <td class="text-center">${x.totalPedidos}</td>
                <td><fmt:formatDate value="${x.fechaRegistro}" pattern="dd/MM/yyyy"/></td>
                <td><c:choose>
                    <c:when test="${not x.activo}"><span class="badge text-bg-secondary">Inactivo</span></c:when>
                    <c:when test="${x.bloqueado}"><span class="badge text-bg-danger">Bloqueado</span></c:when>
                    <c:otherwise><span class="badge text-bg-success">Activo</span></c:otherwise></c:choose></td>
                <td class="text-end text-nowrap">
                    <c:if test="${x.idUsuario != u.idUsuario}">
                        <c:if test="${x.bloqueado}">
                            <form method="post" class="d-inline"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="id" value="${x.idUsuario}">
                                <button name="accion" value="desbloquear" class="btn btn-sm btn-outline-warning" title="Desbloquear"><i class="bi bi-unlock"></i></button></form>
                        </c:if>
                        <form method="post" class="d-inline"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="id" value="${x.idUsuario}">
                            <button name="accion" value="${x.activo ? 'desactivar' : 'activar'}" class="btn btn-sm ${x.activo ? 'btn-outline-danger' : 'btn-outline-success'}" title="${x.activo ? 'Desactivar' : 'Activar'}">
                                <i class="bi ${x.activo ? 'bi-person-x' : 'bi-person-check'}"></i></button></form>
                    </c:if>
                </td></tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<%@ include file="/WEB-INF/vistas/admin/_pie.jspf" %>
