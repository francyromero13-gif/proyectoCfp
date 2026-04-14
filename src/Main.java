import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Producto {
    String id, nombre;
    double precio;
    int cantidadVendida = 0;

    Producto(String id, String n, double p) {
        this.id = id;
        this.nombre = n;
        this.precio = p;
    }

    public String getId() { return id; }
}

class Vendedor {
    String tipoDoc, numDoc, nombres, apellidos;
    double ventasTotales = 0.0;

    Vendedor(String td, String nd, String n, String a) {
        this.tipoDoc = td;
        this.numDoc = nd;
        this.nombres = n;
        this.apellidos = a;
    }

    public String getNumDoc() { return numDoc; }
}

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando...");

            Map<String, Producto> mapaProductos = cargarDatos(
                    "productos.csv",
                    linea -> {
                        String[] d = linea.split(";");
                        return new Producto(d[0], d[1], Double.parseDouble(d[2]));
                    },
                    Producto::getId
            );

            Map<String, Vendedor> mapaVendedores = cargarDatos(
                    "vendedores.csv",
                    linea -> {
                        String[] d = linea.split(";");
                        return new Vendedor(d[0], d[1], d[2], d[3]);
                    },
                    Vendedor::getNumDoc
            );

            try (Stream<Path> paths = Files.walk(Paths.get("."))) {
                paths.filter(path -> path.getFileName().toString().startsWith("vendedor_"))
                        .forEach(path -> procesarArchivoVenta(path, mapaProductos, mapaVendedores));
            }

            generarReportes(mapaVendedores, mapaProductos);

            // 🔥 MOSTRAR TABLAS EN CONSOLA
            mostrarTablaVendedores(mapaVendedores);
            mostrarTablaProductos(mapaProductos);

            System.out.println("\n¡Reportes generados!");

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    private static <T> Map<String, T> cargarDatos(
            String archivo,
            Function<String, T> constructor,
            Function<T, String> getKey) throws IOException {

        return Files.lines(Paths.get(archivo))
                .map(constructor)
                .collect(Collectors.toMap(getKey, item -> item));
    }

    private static void procesarArchivoVenta(
            Path archivo,
            Map<String, Producto> prods,
            Map<String, Vendedor> vends) {

        try {
            List<String> lineas = Files.readAllLines(archivo);

            if (lineas.isEmpty()) {
                System.out.println("Archivo vacío: " + archivo.getFileName());
                return;
            }

            String idVendedor = lineas.get(0).split(";")[1].trim();
            Vendedor vendedor = vends.get(idVendedor);

            if (vendedor == null) {
                System.out.println("Vendedor no encontrado: " + idVendedor);
                return;
            }

            System.out.println("Procesando archivo: " + archivo.getFileName());

            for (int i = 1; i < lineas.size(); i++) {

                String linea = lineas.get(i).trim();
                if (linea.isEmpty()) continue;

                String[] datos = linea.split(";");

                if (datos.length < 2) {
                    System.out.println("Formato inválido en línea " + i);
                    continue;
                }

                String idProducto = datos[0].trim();
                Producto producto = prods.get(idProducto);

                if (producto == null) {
                    System.out.println("Producto no existe: " + idProducto);
                    continue;
                }

                int cantidad;

                try {
                    cantidad = Integer.parseInt(datos[1].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Cantidad inválida en línea " + i);
                    continue;
                }

                if (cantidad <= 0) {
                    System.out.println("Cantidad negativa o cero en línea " + i);
                    continue;
                }

                vendedor.ventasTotales += producto.precio * cantidad;
                producto.cantidadVendida += cantidad;
            }

        } catch (Exception e) {
            System.out.println("Error procesando archivo: " + archivo.getFileName());
        }
    }

    private static void generarReportes(
            Map<String, Vendedor> mapaVendedores,
            Map<String, Producto> mapaProductos) throws IOException {

        List<Vendedor> vendedoresOrdenados = mapaVendedores.values().stream()
                .sorted(Comparator.comparingDouble((Vendedor v) -> v.ventasTotales).reversed())
                .collect(Collectors.toList());

        try (PrintWriter writer = new PrintWriter("reporte_vendedores.csv", "UTF-8")) {
            for (Vendedor v : vendedoresOrdenados) {
                writer.printf("%s %s;%.2f%n", v.nombres, v.apellidos, v.ventasTotales);
            }
        }

        List<Producto> productosOrdenados = mapaProductos.values().stream()
                .sorted(Comparator.comparingInt((Producto p) -> p.cantidadVendida).reversed())
                .collect(Collectors.toList());

        try (PrintWriter writer = new PrintWriter("reporte_productos.csv", "UTF-8")) {
            for (Producto p : productosOrdenados) {
                writer.printf("%s;%.2f%n", p.nombre, p.precio);
            }
        }
    }

    // 🔥 TABLA VENDEDORES
    private static void mostrarTablaVendedores(Map<String, Vendedor> mapaVendedores) {
        System.out.println("\n===== REPORTE DE VENDEDORES =====");

        List<Vendedor> lista = mapaVendedores.values().stream()
                .sorted(Comparator.comparingDouble((Vendedor v) -> v.ventasTotales).reversed())
                .collect(Collectors.toList());

        System.out.printf("%-20s %-20s %-15s%n", "Nombre", "Apellido", "Total Ventas");
        System.out.println("------------------------------------------------------------");

        for (Vendedor v : lista) {
            System.out.printf("%-20s %-20s %-15.2f%n",
                    v.nombres, v.apellidos, v.ventasTotales);
        }
    }

    // 🔥 TABLA PRODUCTOS
    private static void mostrarTablaProductos(Map<String, Producto> mapaProductos) {
        System.out.println("\n===== REPORTE DE PRODUCTOS =====");

        List<Producto> lista = mapaProductos.values().stream()
                .sorted(Comparator.comparingInt((Producto p) -> p.cantidadVendida).reversed())
                .collect(Collectors.toList());

        System.out.printf("%-25s %-15s %-10s%n", "Producto", "Precio", "Cantidad");
        System.out.println("------------------------------------------------------------");

        for (Producto p : lista) {
            System.out.printf("%-25s %-15.2f %-10d%n",
                    p.nombre, p.precio, p.cantidadVendida);
        }
    }
}
