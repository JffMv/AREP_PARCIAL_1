package co.edu.escuelaing;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Calculator_Reflexiva {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(45000);
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
            System.out.println(query + " esta es la query");

            if (query == null) {
                out.println(responseJSON("Error: Query no encontrado"));
                return;
            }

            if (path.equals("/compreflex")) {
                String[] command = query.split("=");

                Object result = command[0].equals("comando")?invokeMethodFromString(command[1]): "Error no se encontro lo solicitado";
                out.println(responseJSON(result.toString()));
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

    public static String responseJSON(String message) {
        String jsonResponse = String.format("{ \"message\": \"%s\" }", message);
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json\r\n" +
                "Content-Length: " + jsonResponse.length() + "\r\n" +
                "Connection: close\r\n\r\n" +
                jsonResponse;
    }
    public static String bbl(String[] parts){
        ArrayList<Integer> numeros = new ArrayList<>();

        for (String part : parts){
            Integer numero = Integer.parseInt(part);
            numeros.add(numero);
        }
        return burburle(numeros).toString();

    }
    private static ArrayList<Integer> burburle(ArrayList<Integer> numeros){
        for (int i = 0; i < numeros.size()-1; i++){
            if (numeros.get(i)<numeros.get(i+1)){
                continue;
            }
            else {
                int number1= numeros.get(i);
                int number2 = numeros.get(i+1);
                numeros.set(i, number2);
                numeros.set(i+1, number1);
                return burburle(numeros);
            }
        }
        return numeros;
    }


    public static Object invokeMethodFromString(String methodString) {
        try {
            String content = methodString.substring(methodString.indexOf('(') + 1, methodString.lastIndexOf(')'));
            String methodName = methodString.substring(methodString.indexOf('=') + 1, methodString.lastIndexOf('('));


            String[] parts = content.contains(",") ? content.split(",\\s*"): new String[]{content};

            if (methodName.equals("bbl")){
                return bbl(parts);
            }
            String className = "java.lang.Math";

            Class<?> targetClass = Class.forName(className);


            int paramCount = (parts.length);
            Object[] params = new Object[paramCount];
            Class<?>[] paramTypes = new Class<?>[paramCount];

            for (int i = 0; i < paramCount; i++) {
                String paramType = "double";
                String paramValue = parts[i].trim();

                paramTypes[i] = getTypeClass(paramType);
                params[i] = parseValue(paramType, paramValue);
            }

            Method method = targetClass.getMethod(methodName, paramTypes);

            Object instance = Modifier.isStatic(method.getModifiers()) ? null : targetClass.getDeclaredConstructor().newInstance();
            return method.invoke(instance, params);

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    private static Class<?> getTypeClass(String type) {
        switch (type.toLowerCase()) {
            case "int": return int.class;
            case "double": return double.class;
            case "string": return String.class;
            case "boolean": return boolean.class;
            default: return String.class; // Si no reconoce, lo trata como String
        }
    }


    public static String clase(String className) {
        try {
            Class<?> targetClass = Class.forName(className);
            StringBuilder result = new StringBuilder();

            result.append("Campos declarados:\n");
            for (Field field : targetClass.getDeclaredFields()) {
                result.append("  ").append(field).append("\n");
            }

            result.append("\nMétodos declarados:\n");
            for (Method method : targetClass.getDeclaredMethods()) {
                result.append("  ").append(method).append("\n");
            }

            return result.toString();
        } catch (ClassNotFoundException e) {
            return "Error: Clase no encontrada - " + e.getMessage();
        }
    }

    private static Object parseValue(String type, String value) {
        switch (type.toLowerCase()) {
            case "int": return Integer.parseInt(value);
            case "double": return Double.parseDouble(value);
            case "string": return value;
            default: return null;
        }
    }
}