import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class PythonTruthTableServer {

    public static Vector<String> vectorization(String expr) {
        StringBuilder response = new StringBuilder();
        try {
            // Define the server URL with the parameter
            String serverUrl = "http://localhost:8000/vectorization?param=" + expr;

            // Create a URL object
            URL url = new URL(serverUrl);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to GET
            connection.setRequestMethod("GET");

            // Get the response from the server
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Print the server's response
            } else {
                System.err.println("HTTP Request failed with response code: " + responseCode);
            }

            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Vector<String> vectorizeResponse = HelloSelenium.arrToVec(response.toString().split("[ \\[\\]]"));
        vectorizeResponse.removeIf(String::isBlank);

        return vectorizeResponse;
    }
}

