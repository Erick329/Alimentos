# AlimentosWeb – Sistema de Ventas en Línea (Alimentos La Cosecha)

Proyecto final del curso Programación II (UMG, Centro Universitario de Portales).
Aplicación **web** hecha con **Java (Jakarta EE: Servlets + JSP), MySQL y Maven**, para abrirse en **Apache NetBeans** y ejecutarse en **Apache Tomcat 10.1**.
Usa la base de datos **AlimentosSA** (9 tablas, 3 triggers, inventario por lotes).

---

## 1. Programas necesarios (instalar una sola vez)

| Programa | Versión | Para qué |
|---|---|---|
| JDK | 17 o 21 | Compilar y ejecutar Java |
| Apache NetBeans | 21 o superior | Editor del proyecto (ya trae Maven) |
| **Apache Tomcat** | **10.1.x** (zip) | Servidor web que ejecuta las páginas Java. ⚠️ Tomcat 9 o anterior NO sirve |
| MySQL Server + Workbench **o** XAMPP | MySQL 8 / XAMPP 8 | Base de datos |

### ¿Necesito XAMPP?
No es obligatorio. XAMPP trae Apache + MySQL (MariaDB) + PHP; **para Java solo sirve su MySQL**.
- Si ya usan XAMPP: abran el *XAMPP Control Panel* y den **Start solo a MySQL**. No necesitan su Apache ni su Tomcat (las versiones de Tomcat que trae XAMPP son viejas y no sirven con Jakarta).
- Si no lo tienen: instalen MySQL Server + MySQL Workbench.
- No usen los dos al mismo tiempo (ambos usan el puerto 3306).

Las páginas **siempre** las ejecuta **Tomcat 10.1**, que se agrega a NetBeans (paso 3).

---

## 2. Crear la base de datos

1. **MySQL Workbench:** File > Open SQL Script > `database/AlimentosSA.sql` y ejecútelo completo con el rayo ⚡.
2. **O con XAMPP:** abra http://localhost/phpmyadmin > pestaña **Importar** > elija `database/AlimentosSA.sql` > Continuar.
Se crea la BD `AlimentosSA` con 9 tablas, 3 triggers, 20 productos, 24 lotes, 7 usuarios y 46 pedidos de ejemplo.

---

## 3. Abrir y ejecutar en NetBeans

1. Descomprima Tomcat 10.1 en una carpeta sin espacios, por ejemplo `C:\tomcat10`.
2. NetBeans: pestaña **Services > Servers > clic derecho > Add Server… > Apache Tomcat or TomEE**.
   Elija `C:\tomcat10`, escriba un usuario y contraseña para el manager (los que quiera) y marque *Create user if it does not exist*.
3. **File > Open Project…** y elija la carpeta `AlimentosWeb` (ícono de Maven).
4. Edite `Other Sources > src/main/resources > db.properties`:
   ```properties
   db.user=root
   db.password=SU_CONTRASEÑA      # en XAMPP se deja vacío
   ```
5. Clic derecho en el proyecto > **Properties > Run** > Server: **Apache Tomcat 10.1**.
6. Clic derecho > **Clean and Build** (la primera vez necesita internet: descarga el driver de MySQL, JSTL y BCrypt).
7. Clic derecho > **Run**. Se abre **http://localhost:8080/AlimentosWeb/**

Probar solo la conexión: abra `dao/ConexionBD.java` > clic derecho > **Run File** → “Conexión exitosa”.

---

## 4. Usuarios y datos de prueba

| Rol | Correo | Contraseña | Qué puede hacer |
|---|---|---|---|
| Administrador | admin@alimentossa.com | Admin123* | Todo el panel |
| Vendedor | vendedor@alimentossa.com | Vendedor123* | Dashboard y pedidos |
| Cliente | cliente@correo.com | Cliente123* | Comprar, ver sus pedidos y su perfil |

Pasarela de pago **simulada**:
- `4111 1111 1111 1111`, venc. `12/28`, CVV `123` → **aprobada**
- `4000 0000 0000 0002` → **rechazada** (demuestra el ROLLBACK: no se crea pedido ni se descuenta nada)

---

## 5. Cómo funcionan los lotes

| Acción | Qué pasa en la BD |
|---|---|
| Entrada (Inventario) | Nuevo registro en **Lotes** (número y vencimiento) + ENTRADA en **Movimientos** |
| Venta | Descuenta primero del lote que **vence antes** (FEFO). Una SALIDA en Movimientos por cada lote usado. **Los lotes vencidos no se venden** |
| Cancelar pedido | Las unidades regresan a los **mismos lotes**; el pago queda RECHAZADO |
| Salida / ajuste | Merma, producto vencido o conteo físico sobre un lote |
| Stock | `Productos.stock` = suma de los lotes ACTIVO (triggers + `ProductoDAO.recalcularStock`) |

---

## 6. Páginas del sistema

**Públicas:** Inicio, Catálogo (filtro y búsqueda), Detalle de producto, Nosotros, Contacto, Iniciar sesión, Crear cuenta (con NIT), Carrito.
**Cliente:** Checkout (tarjeta, transferencia o efectivo), Mis pedidos, Detalle del pedido, Mi cuenta.
**Panel (/admin):** Dashboard (indicadores, stock bajo, lotes por vencer), Pedidos (cambio de estado), Productos, Categorías, Inventario por lotes, Clientes y usuarios, Reportes R1–R10.

| Reporte | Contenido |
|---|---|
| Indicadores | Ventas, pedidos, ticket promedio, unidades, clientes |
| R1 | Ventas por período (gráfica) |
| R2 | Top 10 productos (gráfica) |
| R3 | Baja rotación |
| R4 | Ventas por categoría (gráfica) |
| R5 | Bajo stock mínimo |
| R6 | Clientes frecuentes |
| R7 | Pedidos por estado (gráfica) |
| R8 | Detalle de pedidos con **TOTAL VENDIDO** |
| R9 | Inventario actual con **TOTAL EN STOCK** (unidades y Q) |
| R10 | Lotes vencidos o por vencer en 30 días |

Todos se exportan a CSV (se abre en Excel).

---

## 7. Estructura (MVC + DAO)

```
AlimentosWeb/
├── pom.xml
├── database/AlimentosSA.sql
└── src/main/
    ├── java/com/lacosecha/
    │   ├── modelo/       Usuario, Categoria, Producto, Lote, MovimientoInventario, Pedido, DetallePedido, Carrito…
    │   ├── dao/          ConexionBD, UsuarioDAO, CategoriaDAO, ProductoDAO, LoteDAO, PedidoDAO, ReporteDAO
    │   ├── servicio/     PedidoService (venta, cancelación y lotes en transacciones), PagoService, CorreoService
    │   ├── controlador/  Servlets públicos ·  cliente/ (requieren sesión) ·  admin/ (panel)
    │   ├── filtro/       SeguridadFilter (UTF-8, CSRF, cabeceras) · AutenticacionFilter (roles)
    │   └── util/         Seguridad (BCrypt), Validador, NegocioException
    ├── resources/db.properties
    └── webapp/
        ├── WEB-INF/web.xml  ·  WEB-INF/vistas/ (JSP: publico, cliente, admin, comun)
        ├── css/  js/
        └── lib/          Bootstrap 5, Bootstrap Icons y Chart.js (funciona sin internet)
```

---

## 8. Problemas comunes

| Error | Solución |
|---|---|
| “No fue posible conectarse a la base de datos” | MySQL apagado (en XAMPP: Start en MySQL) o contraseña incorrecta en `db.properties` |
| `Unknown database 'AlimentosSA'` | Falta ejecutar `database/AlimentosSA.sql` |
| 404 en todo / `ClassNotFoundException: jakarta.servlet…` | Está usando Tomcat 9 o el de XAMPP. Use **Tomcat 10.1** |
| Puerto 8080 ocupado | Cierre el otro Tomcat (o el de XAMPP) |
| MySQL de XAMPP no inicia | Otro MySQL ya usa el puerto 3306; detenga uno de los dos |
| Paquetes en rojo | Conéctese a internet > clic derecho > **Clean and Build** |
| Tildes raras (Ã¡) | Vuelva a ejecutar el script completo (incluye `set names utf8mb4`) |
| “Existencias insuficientes (sin contar lotes vencidos)” | Hay unidades en un lote vencido: dé de baja ese lote en Inventario (Salida, “Producto vencido”) |
