package org.thethunderdownunder.tba_team_sustainability;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class Main {
    public static void main(String... args) {
        if (args.length < 2) System.exit(1);
        String year1 = args[0];
        String year2 = args[1];
        String years[] = {year1, year2};

        /*Comparison Data Variables*/
        ArrayList<Integer> rookiesY1 = new ArrayList<>();
        ArrayList<Integer> rookiesOfY1InY2 = new ArrayList<>();

        yearLoop:
        for (String year : years) {
            System.out.println(year);
            File teamFile = new File("teams/" + year);
            if (teamFile.isDirectory()) {
                String[] files = teamFile.list();
                if (files == null) ;
                else for (String file : files) new File(teamFile.getPath(), file).delete();
                teamFile.delete();
            }
            for (int fileCount = 0;;fileCount++) {
                String file = null;
                try {
                    URL site = new URL("https://www.thebluealliance.com/api/v3/teams/" + year + "/" + fileCount + "?X-TBA-Auth-Key=" + Constants.TBA_AUTH_KEY);
                    HttpURLConnection connection = (HttpURLConnection) site.openConnection();
                    connection.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36");
                    DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
                    String line;
                    //BufferedWriter bw = new BufferedWriter(new FileWriter("teams/" + year + "/data-" + fileCount));
                    StringBuilder sb = new StringBuilder();
                    while ((line = dataInputStream.readLine()) != null && !Objects.equals(line, "[]") && !Objects.equals(line, "")) {
                        //bw.write(line);
                        sb.append(line);
                    }
                    file = sb.toString();
                    //bw.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Objects.equals(file, "[]") || Objects.equals(file, "") || file == null) {
                    continue yearLoop;
                }

                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(file).getAsJsonArray();

                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    if (obj.get("rookie_year").isJsonNull()) continue;
                    if (obj.get("rookie_year").getAsInt() == Integer.parseInt(year1)){
                        if (Objects.equals(year, year1)) rookiesY1.add(obj.get("rookie_year").getAsInt());
                        else rookiesOfY1InY2.add(obj.get("rookie_year").getAsInt());
                    }
                }
            }
        }

        System.out.println(rookiesY1.size());
        System.out.println(rookiesOfY1InY2.size());

        int deadCount = 0;

        for (int rookie : rookiesY1) {
            if (!rookiesOfY1InY2.contains(rookie)) deadCount++;
        }

        System.out.println("Year 1: " + year1 + "; Year 2: " + year2 + "; Dead Count: " + deadCount + ";");
    }
}
