<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<div class="container">
    <div class="row justify-content-center">
        <div class="col-lg-7">
            <div class="bg-white rounded-4 shadow-sm p-4 p-md-5">
                <h1 class="h4 fw-bold"><i class="bi bi-person"></i> Mi cuenta</h1>
                <p class="text-muted small">Correo: <strong><c:out value="${u.email}"/></strong> · Rol: ${u.rol} ·
                    Cliente desde <fmt:formatDate value="${u.fechaRegistro}" pattern="dd/MM/yyyy"/></p>
                <form method="post" action="${ctx}/cliente/perfil">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <div class="row g-3">
                        <div class="col-md-6"><label class="form-label">Nombre</label><input name="nombre" class="form-control" maxlength="60" required value="<c:out value='${u.nombre}'/>"></div>
                        <div class="col-md-6"><label class="form-label">Apellido</label><input name="apellido" class="form-control" maxlength="60" required value="<c:out value='${u.apellido}'/>"></div>
                        <div class="col-md-6"><label class="form-label">Teléfono</label><input name="telefono" class="form-control" maxlength="20" value="<c:out value='${u.telefono}'/>"></div>
                        <div class="col-md-6"><label class="form-label">NIT</label><input name="nit" class="form-control" maxlength="20" placeholder="CF" value="<c:out value='${u.nit}'/>"></div>
                        <div class="col-12"><label class="form-label">Dirección de entrega *</label><input name="direccion" class="form-control" maxlength="255" required value="<c:out value='${u.direccion}'/>"></div>
                    </div>
                    <button class="btn btn-verde mt-4">Guardar cambios</button>
                    <a href="${ctx}/cliente/pedidos" class="btn btn-outline-success mt-4 ms-2">Ver mis pedidos</a>
                </form>
            </div>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
