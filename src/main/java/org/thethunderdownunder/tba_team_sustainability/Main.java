package org.thethunderdownunder.tba_team_sustainability;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public class Main {
    private static int fileCount = 0;

    public static void main(String... args) {
        System.out.println(Arrays.toString(args));
        if (args.length < 1) System.exit(1);
        String year = args[0];
        System.out.println("year is " + year);



        URL site = null;
        File teamFile = new File("teams/" +     year);
        if (teamFile.isDirectory()) {
            String[] files = teamFile.list();
            for (String file : files) {
                new File(teamFile.getPath(), file).delete();
            }
            teamFile.delete();
        }
        teamFile.mkdirs();
        for (fileCount = 0;;fileCount++) {
            String file = null;
            System.out.println("fileCount: " + fileCount);
            HttpURLConnection connection = null;
            /* For dodgy schools*/
            SSLUtilities.trustAllHostnames();
            SSLUtilities.trustAllHttpsCertificates();
            try {
                site = new URL("https://www.thebluealliance.com/api/v3/teams/" + year + "/" + fileCount + "?X-TBA-Auth-Key=" + Constants.TBA_AUTH_KEY);
                connection = (HttpURLConnection) site.openConnection();
                connection.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36");
                //connection.setDoOutput(true);
                DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
                String line;
                BufferedWriter bw = new BufferedWriter(new FileWriter("teams/" + year + "/data-" + fileCount));


                StringBuilder sb = new StringBuilder();
                while ((line = dataInputStream.readLine()) != null) {
                    bw.write(line);
                    sb.append(line);
                }
                file = sb.toString();
                bw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String data = "";
            try {
                data = new String(Files.readAllBytes(Paths.get("teams/" + year + "/data-" + fileCount)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (Objects.equals(data, "[]") || Objects.equals(data, "")) break;
            JsonParser parser = new JsonParser();
            JsonArray array = parser.parse(file).getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                System.out.println(obj.get("nickname").getAsString());
            }
        }

        System.exit(0);

    }
}