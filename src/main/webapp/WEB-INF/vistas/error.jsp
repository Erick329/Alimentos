<%@ page isErrorPage="true" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="codigo" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Error ${codigo} | La Cosecha</title>
    <link href="${ctx}/lib/bootstrap/bootstrap.min.css" rel="stylesheet">
    <link href="${ctx}/css/estilos.css" rel="stylesheet">
</head>
<body class="d-flex align-items-center justify-content-center min-vh-100">
<div class="text-center p-4">
    <div class="display-1 fw-bold text-verde">${empty codigo ? 'Error' : codigo}</div>
    <h1 class="h4">
        <c:choose>
            <c:when test="${codigo == 404}">No encontramos la página que busca.</c:when>
            <c:when test="${codigo == 403}">Acceso denegado.</c:when>
            <c:otherwise>Ocurrió un problema al procesar su solicitud.</c:otherwise>
        </c:choose>
    </h1>
    <c:if test="${empty requestScope['jakarta.servlet.error.exception']}">
        <p class="text-muted"><c:out value="${requestScope['jakarta.servlet.error.message']}"/></p>
    </c:if>
    <a href="${ctx}/inicio" class="btn btn-verde">Volver al inicio</a>
</div>
</body>
</html>
