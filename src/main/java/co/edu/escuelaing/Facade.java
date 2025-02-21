package co.edu.escuelaing;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static co.edu.escuelaing.Calculator_Reflexiva.responseJSON;

public class Facade {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(35000);
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        System.out.println("Listo para recibir ...");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Nueva conexión aceptada");
            threadPool.execute(() -> handleClient(clientSocket));
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String[] requestLine = in.readLine().split(" ");
            URI uri = new URI(requestLine[1]);
            String path = uri.getPath();
            String query = uri.getQuery();



            if (path.equals("/calculadora")) {
                String[] command = query.split("=");
                String result = FacadeConnection.setComando(command[1]);
                out.println(responseJSON(result));
                return;
            }
            if (path.equals("/computar")) {
                String resourcePath = "src/main/resources/public/index.html";
                File file = new File(resourcePath);
                byte[] fileContent = Files.readAllBytes(file.toPath());

                OutputStream outputStream = clientSocket.getOutputStream();
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);

                PrintWriter headerWriter = new PrintWriter(bufferedOutputStream, true);
                headerWriter.println("HTTP/1.1 200 OK");
                headerWriter.println("Content-Type: text/html");
                headerWriter.println("Content-Length: " + fileContent.length);
                headerWriter.println();
                headerWriter.flush();
                bufferedOutputStream.write(fileContent);
                bufferedOutputStream.flush();
            }



            if (query == null) {
                out.println(responseJSON("Error: Query o Path no encontrado"));
                return;
            }

            out.println(responseJSON("Ruta no válida"));
        } catch (Exception e) {
            System.err.println("Error: " + e);
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                System.err.println("Error: " + e);
            }
        }
    }
}
