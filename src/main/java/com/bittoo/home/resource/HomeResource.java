package com.bittoo.home.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Path("/v1/home")
public class HomeResource {

  private String readFromInputStream(InputStream inputStream) throws IOException {
    StringBuilder resultStringBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        resultStringBuilder.append(line).append("\n");
      }
    }
    return resultStringBuilder.toString();
  }

  @GET
  public String get(String id) {
    InputStream inputStream = null;
    ClassLoader classLoader = getClass().getClassLoader();
    try {
      File file = new File(classLoader.getResource("home_page.json").getFile());
      inputStream = new FileInputStream(file);
      return readFromInputStream(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return "{\n"
        + "  \"sections\": [\n"
        + "    {\n"
        + "      \"name\": \"section1\",\n"
        + "      \"order\": 1,\n"
        + "      \"type\": \"carousel\",\n"
        + "      \"elements\": [\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"image1\",\n"
        + "          \"link\": \"\",\n"
        + "          \"type\": \"deal\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"image2\",\n"
        + "          \"link\": \"\",\n"
        + "          \"type\": \"deal\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"image3\",\n"
        + "          \"link\": \"\",\n"
        + "          \"type\": \"deal\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"image4\",\n"
        + "          \"link\": \"5\",\n"
        + "          \"type\": \"deal\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"image5\",\n"
        + "          \"link\": \"/deals?dealId=5\",\n"
        + "          \"type\": \"deal\"\n"
        + "        }\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"section2\",\n"
        + "      \"order\": 2,\n"
        + "      \"type\": \"round\",\n"
        + "      \"elements\": [\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"\",\n"
        + "          \"link\": \"filter=dog\",\n"
        + "          \"type\": \"search\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"\",\n"
        + "          \"link\": \"filter=cat\",\n"
        + "          \"type\": \"search\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"\",\n"
        + "          \"link\": \"filter=birds\",\n"
        + "          \"type\": \"search\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"\",\n"
        + "          \"link\": \"filter=animals\",\n"
        + "          \"type\": \"search\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"\",\n"
        + "          \"link\": \"filter=all\",\n"
        + "          \"type\": \"search\"\n"
        + "        }\n"
        + "      ]\n"
        + "    },\n"
        + "    {\n"
        + "      \"name\": \"section3\",\n"
        + "      \"order\": 3,\n"
        + "      \"type\": \"categories\",\n"
        + "      \"elements\": [\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"food\",\n"
        + "          \"link\": \"filter=dog\",\n"
        + "          \"type\": \"category\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"toys\",\n"
        + "          \"link\": \"filter=cat\",\n"
        + "          \"type\": \"category\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"pharmacy\",\n"
        + "          \"link\": \"filter=birds\",\n"
        + "          \"type\": \"category\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"celebration\",\n"
        + "          \"link\": \"filter=animals\",\n"
        + "          \"type\": \"category\"\n"
        + "        },\n"
        + "        {\n"
        + "          \"imgsrc\": \"\",\n"
        + "          \"text\": \"giftcards\",\n"
        + "          \"link\": \"filter=all\",\n"
        + "          \"type\": \"category\"\n"
        + "        }\n"
        + "      ]\n"
        + "    }\n"
        + "  ]\n"
        + "}";
  }
}
