package com.example.inventory;

import com.example.inventory.model.Product;
import com.example.inventory.repository.InMemoryProductRepository;
import com.example.inventory.service.InventoryService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class WebApp {

    private static final int LOW_STOCK = 5;

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9090;

        InventoryService service = new InventoryService(new InMemoryProductRepository());
        service.addProduct(new Product("LAP-001", "Laptop", new BigDecimal("899.99"), 10));
        service.addProduct(new Product("MOU-001", "Mouse", new BigDecimal("19.50"), 50));
        service.addProduct(new Product("KEY-001", "Keyboard", new BigDecimal("45.00"), 3));
        service.addProduct(new Product("MON-001", "Monitor", new BigDecimal("249.00"), 8));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", ex -> {
            if (!ex.getRequestURI().getPath().equals("/")) {
                send(ex, 404, "text/plain", "Not found");
                return;
            }
            send(ex, 200, "text/html", page(service));
        });

        server.createContext("/api/products", ex ->
                send(ex, 200, "application/json", toJson(service.findLowStock(Integer.MAX_VALUE))));

        server.createContext("/api/low-stock", ex ->
                send(ex, 200, "application/json", toJson(service.findLowStock(LOW_STOCK))));

        server.createContext("/sell", ex -> action(ex, service, true));
        server.createContext("/restock", ex -> action(ex, service, false));

        server.start();
        System.out.println("Inventory dashboard running on http://localhost:" + port);
    }

    private static void action(HttpExchange ex, InventoryService service, boolean sell) throws IOException {
        try {
            String query = ex.getRequestURI().getRawQuery();
            String sku = param(query, "sku");
            int qty = Integer.parseInt(param(query, "qty"));
            if (sell) service.sell(sku, qty); else service.restock(sku, qty);
            ex.getResponseHeaders().set("Location", "/");
            ex.sendResponseHeaders(303, -1);
            ex.close();
        } catch (RuntimeException e) {
            send(ex, 400, "text/plain", "Error: " + e.getMessage());
        }
    }

    private static String page(InventoryService service) {
        List<Product> products = service.findLowStock(Integer.MAX_VALUE);
        StringBuilder rows = new StringBuilder();
        for (Product p : products) {
            boolean low = p.quantity() <= LOW_STOCK;
            rows.append("<tr class='").append(low ? "low" : "").append("'>")
                .append("<td>").append(esc(p.sku())).append("</td>")
                .append("<td>").append(esc(p.name())).append("</td>")
                .append("<td>$").append(p.price()).append("</td>")
                .append("<td>").append(p.quantity()).append(low ? " (low)" : "").append("</td>")
                .append("<td>$").append(p.stockValue()).append("</td>")
                .append("<td><a href='/sell?sku=").append(p.sku()).append("&qty=1'>Sell 1</a> | ")
                .append("<a href='/restock?sku=").append(p.sku()).append("&qty=5'>Restock 5</a></td>")
                .append("</tr>");
        }
        return """
            <!DOCTYPE html>
            <html><head><meta charset="utf-8"><title>Inventory Dashboard</title>
            <style>
              body { font-family: system-ui, sans-serif; margin: 2rem auto; max-width: 860px; padding: 0 1rem; }
              table { border-collapse: collapse; width: 100%%; }
              th, td { padding: .6rem .8rem; border-bottom: 1px solid #ddd; text-align: left; }
              th { background: #f3f3f3; }
              tr.low td { background: #fff3cd; }
              .total { margin: 1rem 0; font-size: 1.2rem; }
            </style></head><body>
            <h1>Inventory Dashboard</h1>
            <div class="total">Total inventory value: <b>$%s</b></div>
            <table>
              <tr><th>SKU</th><th>Name</th><th>Price</th><th>Quantity</th><th>Stock value</th><th>Actions</th></tr>
              %s
            </table>
            <p>JSON: <a href="/api/products">/api/products</a> | <a href="/api/low-stock">/api/low-stock</a></p>
            </body></html>
            """.formatted(service.totalInventoryValue(), rows);
    }

    private static String toJson(List<Product> products) {
        return products.stream()
                .map(p -> "{\"sku\":\"%s\",\"name\":\"%s\",\"price\":%s,\"quantity\":%d}"
                        .formatted(esc(p.sku()), esc(p.name()), p.price(), p.quantity()))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String param(String query, String key) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static void send(HttpExchange ex, int status, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (var os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
