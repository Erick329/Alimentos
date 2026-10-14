<%@ include file="/WEB-INF/vistas/comun/encabezado.jspf" %>
<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
            <div class="bg-white rounded-4 shadow-sm p-4 p-md-5">
                <h1 class="h4 fw-bold text-center mb-4"><i class="bi bi-person-circle text-success"></i> Iniciar sesión</h1>
                <c:if test="${not empty error}"><div class="alert alert-danger small"><c:out value="${error}"/></div></c:if>
                <form method="post" action="${ctx}/login">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="next" value="<c:out value='${next}'/>">
                    <div class="mb-3"><label class="form-label" for="email">Correo electrónico</label>
                        <input id="email" name="email" type="email" class="form-control" required autofocus value="<c:out value='${email}'/>"></div>
                    <div class="mb-3"><label class="form-label" for="password">Contraseña</label>
                        <input id="password" name="password" type="password" class="form-control" required></div>
                    <button class="btn btn-verde w-100">Ingresar</button>
                </form>
                <div class="d-flex justify-content-between mt-3 small">
                    <a href="${ctx}/recuperar">¿Olvidó su contraseña?</a>
                    <a href="${ctx}/registro?next=<c:out value='${next}'/>">Crear una cuenta</a>
                </div>
            </div>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/vistas/comun/pie.jspf" %>
