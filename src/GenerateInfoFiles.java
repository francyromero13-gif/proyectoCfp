import java.io.PrintWriter;
import java.util.Random;

public class GenerateInfoFiles {

    private static final String[] NOMBRES = {"Carlos", "Ana", "Luis", "Maria", "Juan"};
    private static final String[] APELLIDOS = {"Gomez", "Perez", "Rodriguez", "Martinez"};
    private static final String[] TIPOS_DOC = {"CC", "CE", "TI"};

    private static final String[] PRODUCTOS_NOMBRES = {
            "ConcentradoPerro", "ConcentradoGato", "ArenaGato",
            "Juguete", "Collar", "Correa", "Shampoo", "Cama"
    };

    private static final double[] PRODUCTOS_PRECIOS = {
            120000, 110000, 50000, 25000, 30000, 45000, 38000, 150000
    };

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando generación de archivos...");

            createProductsFile(PRODUCTOS_NOMBRES.length);
            createSalesManInfoFile(3);

            System.out.println("¡Archivos generados exitosamente!");

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    public static void createProductsFile(int productsCount) throws Exception {
        try (PrintWriter writer = new PrintWriter("productos.csv", "UTF-8")) {

            for (int i = 0; i < productsCount; i++) {
                writer.println((i + 1) + ";" + PRODUCTOS_NOMBRES[i] + ";" + PRODUCTOS_PRECIOS[i]);
            }
        }
    }

    public static void createSalesManInfoFile(int salesmanCount) throws Exception {

        Random rand = new Random();

        try (PrintWriter writer = new PrintWriter("vendedores.csv", "UTF-8")) {

            for (int i = 0; i < salesmanCount; i++) {

                long id = 100000000 + rand.nextInt(900000000);
                String nombre = NOMBRES[rand.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[rand.nextInt(APELLIDOS.length)];
                String tipoDoc = TIPOS_DOC[rand.nextInt(TIPOS_DOC.length)];

                writer.println(tipoDoc + ";" + id + ";" + nombre + ";" + apellido);

                // 🔥 Generar múltiples archivos por vendedor
                int cantidadArchivos = rand.nextInt(2) + 2; // entre 2 y 3 archivos

                for (int j = 0; j < cantidadArchivos; j++) {
                    createSalesMenFile(rand.nextInt(4) + 2, nombre, id, j);
                }
            }
        }
    }

    public static void createSalesMenFile(int randomSalesCount, String name, long id, int index) throws Exception {

        Random rand = new Random();

        String fileName = "vendedor_" + id + "_" + index + ".csv";

        try (PrintWriter writer = new PrintWriter(fileName, "UTF-8")) {

            writer.println("CC;" + id);

            for (int i = 0; i < randomSalesCount; i++) {
                int productoId = rand.nextInt(PRODUCTOS_NOMBRES.length) + 1;
                int cantidad = rand.nextInt(10) + 1;

                writer.println(productoId + ";" + cantidad + ";");
            }
        }
    }
}
