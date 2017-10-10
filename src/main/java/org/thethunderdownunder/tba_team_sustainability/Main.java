package org.thethunderdownunder.tba_team_sustainability;

import com.google.gson.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class Main {
    private static int fileCount = 0;

    public static void main(String... args) {
        System.out.println(Arrays.toString(args));
        if (args.length < 1) System.exit(1);
        String year1 = args[0];
        String year2 = args[1];
        String[] years = {year1, year2};
        System.out.println("year is " + year1);

        URL site;
        File teamFile = new File("teams/" + year1);
        if (teamFile.isDirectory()) {
            String[] files = teamFile.list();
            for (String file : files) {
                new File(teamFile.getPath(), file).delete();
            }
            teamFile.delete();
        }
        teamFile.mkdirs();
        File teamFile2 = new File("teams/" + year2);
        if (teamFile2.isDirectory()) {
            String[] files = teamFile2.list();
            for (String file : files) {
                new File(teamFile2.getPath(), file).delete();
            }
            teamFile2.delete();
        }
        teamFile2.mkdirs();
        StringBuilder allData = new StringBuilder();
        ArrayList<String> teamsInYear1 = new ArrayList<>();
        ArrayList<Integer> year1RookieYears = new ArrayList<>();
        ArrayList<String> teamsInYear2 = new ArrayList<>();
        fileLoop:
        for (fileCount = 0;;fileCount++) {
            String file = null;
            //System.out.println("fileCount: " + fileCount);
            HttpURLConnection connection = null;
            /* For dodgy schools*/
            SSLUtilities.trustAllHostnames();
            SSLUtilities.trustAllHttpsCertificates();
            for (int i = 0; i < 2; i++) {
                file = null;
                try {
                    site = new URL("https://www.thebluealliance.com/api/v3/teams/" + years[i] + "/" + fileCount + "?X-TBA-Auth-Key=" + Constants.TBA_AUTH_KEY);
                    connection = (HttpURLConnection) site.openConnection();
                    connection.addRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36");
                    DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
                    String line;
                    BufferedWriter bw = new BufferedWriter(new FileWriter("teams/" + years[i] + "/data-" + fileCount));
                    StringBuilder sb = new StringBuilder();
                    while ((line = dataInputStream.readLine()) != null && !Objects.equals(line, "[]") && !Objects.equals(line, "")) {
                        bw.write(line);
                        sb.append(line);
                    }
                    file = sb.toString();
                    bw.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (Objects.equals(file, "[]") || Objects.equals(file, "") || file == null) {
                    if (i == 1) break fileLoop;
                    else continue;
                }

                allData.append(file);
                JsonParser parser = new JsonParser();
                JsonArray array = parser.parse(file).getAsJsonArray();
                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    //System.out.println(obj.get("nickname").getAsString());
                    if (i == 0) {
                        String rookieYear = !obj.get("rookie_year").isJsonNull() ? obj.get("rookie_year").getAsString() : year1;
                        teamsInYear1.add(obj.get("team_number").getAsString());
                        year1RookieYears.add(Integer.parseInt(rookieYear));
                    } else {
                        teamsInYear2.add(obj.get("team_number").getAsString());
                    }
                }
            }
        }

        int deadTeams = 0;
        ArrayList<Integer> teamAges = new ArrayList<>();

        for (String team : teamsInYear1) {
            if (!teamsInYear2.contains(team))  {
                System.out.println(team);
                teamAges.add(Integer.parseInt(year2) - year1RookieYears.get(teamsInYear1.indexOf(team)));
                deadTeams++;
            }
        }
        System.out.println("Rookie Dropouts: " + Collections.frequency(teamAges, 1));
        while (teamAges.contains(1)) teamAges.remove(teamAges.indexOf(1));
        System.out.println("2nd year Dropouts: " + Collections.frequency(teamAges, 2));
        while (teamAges.contains(2)) teamAges.remove(teamAges.indexOf(2));
        System.out.println("3rd year Dropouts: " + Collections.frequency(teamAges, 3));
        while (teamAges.contains(3)) teamAges.remove(teamAges.indexOf(3));
        System.out.println("4th Dropouts: " + Collections.frequency(teamAges, 4));
        while (teamAges.contains(4)) teamAges.remove(teamAges.indexOf(4));
        System.out.println("5th Dropouts: " + Collections.frequency(teamAges, 5));
        while (teamAges.contains(5)) teamAges.remove(teamAges.indexOf(5));

        System.out.println("Other Dropouts: " + teamAges.size());


        System.exit(0);
    }
}
