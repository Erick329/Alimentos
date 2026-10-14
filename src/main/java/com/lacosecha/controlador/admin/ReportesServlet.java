package com.lacosecha.controlador.admin;

import com.lacosecha.controlador.BaseServlet;
import com.lacosecha.dao.LoteDAO;
import com.lacosecha.dao.PedidoDAO;
import com.lacosecha.dao.ProductoDAO;
import com.lacosecha.modelo.Lote;
import com.lacosecha.modelo.Pedido;
import java.math.BigDecimal;
import com.lacosecha.dao.ReporteDAO;
import com.lacosecha.modelo.FilaReporte;
import com.lacosecha.modelo.Producto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Reportes para la toma de decisiones, con gráficas y exportación a CSV (se abre en Excel). */
@WebServlet("/admin/reportes")
public class ReportesServlet extends BaseServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final LoteDAO loteDAO = new LoteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate hasta = fecha(req.getParameter("hasta"), LocalDate.now());
        LocalDate desde = fecha(req.getParameter("desde"), hasta.minusDays(89));
        if (desde.isAfter(hasta)) {
            LocalDate t = desde;
            desde = hasta;
            hasta = t;
        }
        try {
            String exportar = req.getParameter("exportar");
            if (exportar != null) {
                exportarCsv(resp, exportar, desde, hasta);
                return;
            }
            List<FilaReporte> ventas = reporteDAO.ventasPorPeriodo(desde, hasta);
            List<FilaReporte> top = reporteDAO.topProductos(desde, hasta);
            List<FilaReporte> categorias = reporteDAO.ventasPorCategoria(desde, hasta);
            List<FilaReporte> estados = reporteDAO.pedidosPorEstado(desde, hasta);

            req.setAttribute("desde", desde);
            req.setAttribute("hasta", hasta);
            req.setAttribute("resumen", reporteDAO.resumen(desde, hasta));
            req.setAttribute("ventas", ventas);
            req.setAttribute("top", top);
            req.setAttribute("categorias", categorias);
            req.setAttribute("estados", estados);
            req.setAttribute("clientes", reporteDAO.clientesFrecuentes(desde, hasta));
            req.setAttribute("bajaRotacion", reporteDAO.bajaRotacion(desde, hasta));
            req.setAttribute("bajoStock", productoDAO.bajoStock());

            // R8. Detalle de pedidos con TOTAL VENDIDO (no suma los cancelados)
            List<Pedido> pedidos = pedidoDAO.listarPorFechas(desde, hasta);
            BigDecimal totalVendido = BigDecimal.ZERO;
            for (Pedido p : pedidos) if (!"CANCELADO".equals(p.getEstado())) totalVendido = totalVendido.add(p.getTotal());
            req.setAttribute("pedidos", pedidos);
            req.setAttribute("totalVendido", totalVendido);

            // R9. Inventario actual con TOTAL EN STOCK (unidades y valor en quetzales)
            List<Producto> inventario = productoDAO.listar(0, null, true);
            int unidades = 0;
            BigDecimal valor = BigDecimal.ZERO;
            for (Producto p : inventario) {
                unidades += p.getStock();
                valor = valor.add(p.getPrecio().multiply(BigDecimal.valueOf(p.getStock())));
            }
            req.setAttribute("inventario", inventario);
            req.setAttribute("totalUnidades", unidades);
            req.setAttribute("valorInventario", valor);

            // R10. Lotes vencidos o que vencen en los próximos 30 días
            req.setAttribute("lotesPorVencer", loteDAO.proximosAVencer(30));
            // Datos para las gráficas (Chart.js) en formato JSON
            req.setAttribute("jsonGraficas", "{\"ventas\":" + json(ventas, true) + ",\"top\":" + json(top, false)
                    + ",\"categorias\":" + json(categorias, true) + ",\"estados\":" + json(estados, false) + "}");
            vista(req, resp, "admin/reportes", "Reportes");
        } catch (SQLException e) {
            errorBD(req, resp, e);
        }
    }

    private void exportarCsv(HttpServletResponse resp, String tipo, LocalDate desde, LocalDate hasta)
            throws SQLException, IOException {
        String titulo;
        String[] encabezado;
        List<FilaReporte> filas;
        switch (tipo) {
            case "top":
                titulo = "productos_mas_vendidos"; encabezado = new String[]{"Producto", "Ingresos (Q)", "Unidades"};
                filas = reporteDAO.topProductos(desde, hasta); break;
            case "categorias":
                titulo = "ventas_por_categoria"; encabezado = new String[]{"Categoría", "Ventas (Q)", "Unidades"};
                filas = reporteDAO.ventasPorCategoria(desde, hasta); break;
            case "clientes":
                titulo = "clientes_frecuentes"; encabezado = new String[]{"Cliente", "Total comprado (Q)", "Pedidos"};
                filas = reporteDAO.clientesFrecuentes(desde, hasta); break;
            case "estados":
                titulo = "pedidos_por_estado"; encabezado = new String[]{"Estado", "Monto (Q)", "Pedidos"};
                filas = reporteDAO.pedidosPorEstado(desde, hasta); break;
            case "pedidos": {
                resp.setContentType("text/csv; charset=UTF-8");
                resp.setHeader("Content-Disposition", "attachment; filename=\"detalle_pedidos_" + desde + "_" + hasta + ".csv\"");
                PrintWriter out = resp.getWriter();
                out.print('\uFEFF');
                out.println("Pedido,Fecha,Cliente,NIT,Método de pago,Estado,Total (Q)");
                BigDecimal total = BigDecimal.ZERO;
                for (Pedido p : pedidoDAO.listarPorFechas(desde, hasta)) {
                    out.println(p.getIdPedido() + "," + p.getFechaPedido() + "," + csv(p.getClienteNombre()) + "," + csv(p.getClienteNit()) + ","
                            + csv(p.getMetodoPago()) + "," + p.getEstado() + "," + p.getTotal());
                    if (!"CANCELADO".equals(p.getEstado())) total = total.add(p.getTotal());
                }
                out.println(",,,,,TOTAL VENDIDO," + total);
                return;
            }
            case "stock": {
                resp.setContentType("text/csv; charset=UTF-8");
                resp.setHeader("Content-Disposition", "attachment; filename=\"inventario_actual.csv\"");
                PrintWriter out = resp.getWriter();
                out.print('\uFEFF');
                out.println("Código,Producto,Categoría,Stock,Precio (Q),Valor (Q)");
                int unidades = 0;
                BigDecimal valor = BigDecimal.ZERO;
                for (Producto p : productoDAO.listar(0, null, true)) {
                    BigDecimal v = p.getPrecio().multiply(BigDecimal.valueOf(p.getStock()));
                    out.println(csv(p.getCodigo()) + "," + csv(p.getNombre()) + "," + csv(p.getCategoriaNombre()) + ","
                            + p.getStock() + "," + p.getPrecio() + "," + v);
                    unidades += p.getStock();
                    valor = valor.add(v);
                }
                out.println(",,TOTAL EN STOCK," + unidades + ",," + valor);
                return;
            }
            case "lotes": {
                resp.setContentType("text/csv; charset=UTF-8");
                resp.setHeader("Content-Disposition", "attachment; filename=\"lotes_por_vencer.csv\"");
                PrintWriter out = resp.getWriter();
                out.print('\uFEFF');
                out.println("Producto,Lote,Cantidad,Vence,Estado");
                for (Lote l : loteDAO.proximosAVencer(30)) {
                    out.println(csv(l.getProductoNombre()) + "," + csv(l.getNumeroLote()) + "," + l.getCantidad() + ","
                            + l.getFechaVencimiento() + "," + csv(l.getEstadoVencimiento()));
                }
                return;
            }
            case "inventario": {
                resp.setContentType("text/csv; charset=UTF-8");
                resp.setHeader("Content-Disposition", "attachment; filename=\"inventario_bajo_minimo.csv\"");
                PrintWriter out = resp.getWriter();
                out.print('﻿');
                out.println("Código,Producto,Categoría,Stock,Stock mínimo");
                for (Producto p : productoDAO.bajoStock()) {
                    out.println(csv(p.getCodigo()) + "," + csv(p.getNombre()) + "," + csv(p.getCategoriaNombre()) + ","
                            + p.getStock() + "," + p.getStockMinimo());
                }
                return;
            }
            default:
                titulo = "ventas_por_periodo"; encabezado = new String[]{"Período", "Ventas (Q)", "Pedidos"};
                filas = reporteDAO.ventasPorPeriodo(desde, hasta);
        }
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + titulo + "_" + desde + "_" + hasta + ".csv\"");
        PrintWriter out = resp.getWriter();
        out.print('﻿');   // BOM para que Excel reconozca las tildes
        out.println(String.join(",", encabezado));
        for (FilaReporte f : filas) out.println(csv(f.getEtiqueta()) + "," + f.getMonto() + "," + f.getCantidad());
    }

    private static String csv(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.matches("^[=+\\-@].*")) v = "'" + v;   // evita inyección de fórmulas en Excel
        return "\"" + v + "\"";
    }

    /** Convierte filas a JSON: {"etiquetas":[...],"valores":[...]} (monto o cantidad). */
    private static String json(List<FilaReporte> filas, boolean usarMonto) {
        StringBuilder e = new StringBuilder("[");
        StringBuilder v = new StringBuilder("[");
        for (int i = 0; i < filas.size(); i++) {
            if (i > 0) { e.append(','); v.append(','); }
            e.append('"').append(escaparJson(filas.get(i).getEtiqueta())).append('"');
            v.append(usarMonto ? filas.get(i).getMonto().toPlainString() : String.valueOf(filas.get(i).getCantidad()));
        }
        return "{\"etiquetas\":" + e.append(']') + ",\"valores\":" + v.append(']') + "}";
    }

    private static String escaparJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\') sb.append('\\').append(c);
            else if (c == '<') sb.append("\\u003c");   // evita cerrar la etiqueta <script>
            else if (c == '>') sb.append("\\u003e");
            else if (c == '&') sb.append("\\u0026");
            else if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
            else sb.append(c);
        }
        return sb.toString();
    }

    private static LocalDate fecha(String s, LocalDate porDefecto) {
        try {
            return s == null || s.isBlank() ? porDefecto : LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            return porDefecto;
        }
    }
}
