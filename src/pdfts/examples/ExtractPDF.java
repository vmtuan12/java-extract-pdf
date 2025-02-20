package pdfts.examples;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtractPDF {
    public static void main (String[] args) throws java.io.IOException {
        String filePath = "/home/mhtuan/work/rag/slide/all_pdf.txt";

        try {
            // Read all lines from the file
            Path path = Paths.get(filePath);
            List<String> lines = Files.readAllLines(path);

            // Print all lines
            for (String line : lines) {
                System.out.println(line);
                convert(line.trim(), line.trim().replaceAll(".pdf", ".md"));
                System.out.println("Successfully converted into " + line.replaceAll(".pdf", ".md"));
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static void convert(String pdfFilePath, String outputPath) throws java.io.IOException {
        PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(pdfFilePath));
        if (document.getNumberOfPages() >= 150) {
            return;
        }

        Map<Integer, String> payloads = new HashMap<>();

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        for (int i = 1; i <= document.getNumberOfPages(); i++) {
            stripper.setStartPage(i);
            stripper.setEndPage(i);
            String text = stripper.getText(document).trim();
            payloads.put(i - 1, text);
        }

        String url = "http://127.0.0.1:8000/upload/";
        File file = new File(pdfFilePath);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);


            // Convert the map to JSON string using Jackson
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(payloads);

            // Build multipart request
            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.STRICT)
                    .addPart("file", new FileBody(file))  // Attach file
                    .addPart("texts", new StringBody(jsonPayload, StandardCharsets.UTF_8)) // Attach JSON
                    .build();

            post.setEntity(entity);

            // Execute request
            CloseableHttpResponse response = client.execute(post);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String responseBody = EntityUtils.toString(response.getEntity());

                System.out.println(responseBody);
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                JsonNode textsNode = jsonResponse.get("content");

                // Convert "texts" field into a Map<Integer, String>
                Map<String, String> textsMap = objectMapper.convertValue(textsNode, Map.class);
                writeToFile(outputPath, textsMap);
            }

            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(String filename, Map<String, String> textsMap) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Map.Entry<String, String> entry : textsMap.entrySet()) {
                writer.write("# Page " + entry.getKey() + "\n" + entry.getValue() + "\n");
                writer.newLine(); // Add a new line after each value
            }
            System.out.println("Texts written to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}