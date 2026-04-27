import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum TipoItem {
    PRODUCTO, SERVICIO
}

class Item {
    String id, nombre;
    double precio;
    TipoItem tipo;
    int cantidadVendida = 0;

    Item(String id, String nombre, double precio, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.tipo = TipoItem.valueOf(tipo);
    }

    public String getId() { return id; }
}

class Cliente {
    String tipoDoc, numDoc, nombres, apellidos;
    double totalGastado = 0;

    Cliente(String td, String nd, String n, String a) {
        this.tipoDoc = td;
        this.numDoc = nd;
        this.nombres = n;
        this.apellidos = a;
    }

    public String getNumDoc() { return numDoc; }
}

public class Main {

    static int errores = 0;

    public static void main(String[] args) {
        try {
            System.out.println("Procesando información...");

            Map<String, Item> items = cargarDatos(
                    "items.csv",
                    l -> {
                        String[] d = l.split(";");
                        return new Item(d[0], d[1], Double.parseDouble(d[2]), d[3]);
                    },
                    Item::getId
            );

            Map<String, Cliente> clientes = cargarDatos(
                    "clientes.csv",
                    l -> {
                        String[] d = l.split(";");
                        return new Cliente(d[0], d[1], d[2], d[3]);
                    },
                    Cliente::getNumDoc
            );

            try (Stream<Path> paths = Files.walk(Paths.get("."))) {
                paths.filter(p -> p.getFileName().toString().startsWith("cliente_"))
                        .forEach(p -> procesarArchivo(p, items, clientes));
            }

            generarReportes(clientes, items);

            // TABLAS (ENTREGA 2)
            mostrarTablaClientes(clientes);
            mostrarTablaItems(items);

            System.out.println("\nErrores detectados: " + errores);
            System.out.println("¡Proceso finalizado!");

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static <T> Map<String, T> cargarDatos(
            String archivo,
            Function<String, T> constructor,
            Function<T, String> getKey) throws IOException {

        return Files.lines(Paths.get(archivo))
                .map(constructor)
                .collect(Collectors.toMap(getKey, x -> x));
    }

    private static void procesarArchivo(
            Path archivo,
            Map<String, Item> items,
            Map<String, Cliente> clientes) {

        try {
            List<String> lineas = Files.readAllLines(archivo);

            if (lineas.isEmpty()) return;

            String idCliente = lineas.get(0).split(";")[1];
            Cliente cliente = clientes.get(idCliente);

            if (cliente == null) {
                errores++;
                return;
            }

            for (int i = 1; i < lineas.size(); i++) {

                String[] d = lineas.get(i).split(";");

                if (d.length < 2) {
                    errores++;
                    continue;
                }

                Item item = items.get(d[0]);

                if (item == null) {
                    errores++;
                    continue;
                }

                int cantidad;

                try {
                    cantidad = Integer.parseInt(d[1]);
                } catch (Exception e) {
                    errores++;
                    continue;
                }

                if (cantidad <= 0) {
                    errores++;
                    continue;
                }

                cliente.totalGastado += item.precio * cantidad;
                item.cantidadVendida += cantidad;
            }

        } catch (Exception e) {
            errores++;
        }
    }

    private static void generarReportes(
            Map<String, Cliente> clientes,
            Map<String, Item> items) throws IOException {

        List<Cliente> clientesOrdenados = clientes.values().stream()
                .sorted(Comparator.comparingDouble((Cliente c) -> c.totalGastado).reversed())
                .collect(Collectors.toList());

        try (PrintWriter w = new PrintWriter("reporte_clientes.csv")) {
            for (Cliente c : clientesOrdenados) {
                w.println(String.format("%s %s;%.2f", c.nombres, c.apellidos, c.totalGastado));
            }
        }

        List<Item> itemsOrdenados = items.values().stream()
                .sorted(Comparator.comparingInt((Item i) -> i.cantidadVendida).reversed())
                .collect(Collectors.toList());

        try (PrintWriter w = new PrintWriter("reporte_items.csv")) {
            for (Item i : itemsOrdenados) {
                w.println(String.format("%s;%s;%.2f", i.nombre, i.tipo, i.precio));
            }
        }
    }

    // TABLA CLIENTES
    private static void mostrarTablaClientes(Map<String, Cliente> clientes) {

        System.out.println("\n===== REPORTE DE CLIENTES =====");

        List<Cliente> lista = clientes.values().stream()
                .sorted(Comparator.comparingDouble((Cliente c) -> c.totalGastado).reversed())
                .collect(Collectors.toList());

        System.out.printf("%-20s %-20s %-15s%n", "Nombre", "Apellido", "Total");
        System.out.println("------------------------------------------------------------");

        for (Cliente c : lista) {
            System.out.printf("%-20s %-20s %-15.2f%n",
                    c.nombres, c.apellidos, c.totalGastado);
        }
    }

    // TABLA ITEMS
    private static void mostrarTablaItems(Map<String, Item> items) {

        System.out.println("\n===== REPORTE DE ITEMS =====");

        List<Item> lista = items.values().stream()
                .sorted(Comparator.comparingInt((Item i) -> i.cantidadVendida).reversed())
                .collect(Collectors.toList());

        System.out.printf("%-25s %-15s %-10s%n", "Item", "Tipo", "Cantidad");
        System.out.println("------------------------------------------------------------");

        for (Item i : lista) {
            System.out.printf("%-25s %-15s %-10d%n",
                    i.nombre, i.tipo, i.cantidadVendida);
        }
    }
}
