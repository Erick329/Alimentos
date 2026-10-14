<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<div class="container">
    <div class="row justify-content-center">
        <div class="col-lg-7">
            <div class="bg-white rounded-4 shadow-sm p-4 p-md-5">
                <h1 class="h4 fw-bold mb-1">Crear cuenta</h1>
                <p class="text-muted small">Regístrese para comprar y dar seguimiento a sus pedidos.</p>
                <c:if test="${not empty errores}">
                    <div class="alert alert-danger small"><ul class="mb-0"><c:forEach var="e" items="${errores}"><li><c:out value="${e}"/></li></c:forEach></ul></div>
                </c:if>
                <form method="post" action="${ctx}/registro">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="next" value="<c:out value='${next}'/>">
                    <div class="row g-3">
                        <div class="col-md-6"><label class="form-label">Nombre *</label>
                            <input name="nombre" class="form-control" maxlength="60" required value="<c:out value='${form.nombre}'/>"></div>
                        <div class="col-md-6"><label class="form-label">Apellido *</label>
                            <input name="apellido" class="form-control" maxlength="60" required value="<c:out value='${form.apellido}'/>"></div>
                        <div class="col-md-6"><label class="form-label">Correo electrónico *</label>
                            <input name="email" type="email" class="form-control" maxlength="120" required value="<c:out value='${form.email}'/>"></div>
                        <div class="col-md-6"><label class="form-label">Teléfono</label>
                            <input name="telefono" class="form-control" maxlength="20" placeholder="5555-5555" value="<c:out value='${form.telefono}'/>"></div>
                        <div class="col-md-6"><label class="form-label">NIT</label>
                            <input name="nit" class="form-control" maxlength="20" placeholder="1234567-8 o CF" value="<c:out value='${form.nit}'/>">
                            <div class="form-text">Déjelo vacío para consumidor final (CF).</div></div>
                        <div class="col-md-6"><label class="form-label">Dirección de entrega *</label>
                            <input name="direccion" class="form-control" maxlength="255" required value="<c:out value='${form.direccion}'/>"></div>
                        <div class="col-md-6"><label class="form-label">Contraseña *</label>
                            <input name="password" type="password" class="form-control" minlength="8" required>
                            <div class="form-text">Mínimo 8 caracteres, con letras y números.</div></div>
                        <div class="col-md-6"><label class="form-label">Confirmar contraseña *</label>
                            <input name="confirmar" type="password" class="form-control" minlength="8" required></div>
                    </div>
                    <button class="btn btn-verde w-100 mt-4">Crear cuenta</button>
                </form>
                <p class="text-center small mt-3 mb-0">¿Ya tiene cuenta? <a href="${ctx}/login">Inicie sesión</a></p>
            </div>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
