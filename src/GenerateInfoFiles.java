import java.io.PrintWriter;
import java.util.Random;

public class GenerateInfoFiles {

    private static final String[] NOMBRES = {"Carlos", "Ana", "Luis", "Maria", "Juan"};
    private static final String[] APELLIDOS = {"Gomez", "Perez", "Rodriguez", "Martinez"};
    private static final String[] TIPOS_DOC = {"CC", "CE", "TI"};

// (Productos y servicios)

    private static final String[] ITEMS_NOMBRES = {
            "ConcentradoPerro", "ConcentradoGato", "Juguete",
            "Vacuna", "Baño", "Consulta"
    };

    private static final double[] ITEMS_PRECIOS = {
            120000, 110000, 25000,
            80000, 30000, 50000
    };

    private static final String[] ITEMS_TIPOS = {
            "PRODUCTO", "PRODUCTO", "PRODUCTO",
            "SERVICIO", "SERVICIO", "SERVICIO"
    };

    public static void main(String[] args) {
        try {
            System.out.println("Generando archivos...");

            createItemsFile(ITEMS_NOMBRES.length);
            createClientesFile(3);

            System.out.println("¡Archivos generados!");

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    public static void createItemsFile(int count) throws Exception {
        try (PrintWriter writer = new PrintWriter("items.csv", "UTF-8")) {

            for (int i = 0; i < count; i++) {
                writer.println((i + 1) + ";" +
                        ITEMS_NOMBRES[i] + ";" +
                        ITEMS_PRECIOS[i] + ";" +
                        ITEMS_TIPOS[i]);
            }
        }
    }

    public static void createClientesFile(int count) throws Exception {

        Random rand = new Random();

        try (PrintWriter writer = new PrintWriter("clientes.csv", "UTF-8")) {

            for (int i = 0; i < count; i++) {

                long id = 100000000 + rand.nextInt(900000000);
                String nombre = NOMBRES[rand.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[rand.nextInt(APELLIDOS.length)];
                String tipoDoc = TIPOS_DOC[rand.nextInt(TIPOS_DOC.length)];

                writer.println(tipoDoc + ";" + id + ";" + nombre + ";" + apellido);

                int archivos = rand.nextInt(2) + 2;

                for (int j = 0; j < archivos; j++) {
                    createClienteFile(rand.nextInt(4) + 2, id, j);
                }
            }
        }
    }

    public static void createClienteFile(int registros, long id, int index) throws Exception {

        Random rand = new Random();
        String fileName = "cliente_" + id + "_" + index + ".csv";

        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {

            writer.println("CC;" + id);

            for (int i = 0; i < registros; i++) {

                int itemId = rand.nextInt(ITEMS_NOMBRES.length) + 1;
                int cantidad = rand.nextInt(5) + 1;

                writer.println(itemId + ";" + cantidad + ";");
            }
        }
    }
}
