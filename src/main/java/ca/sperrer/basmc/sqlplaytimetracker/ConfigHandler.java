package ca.sperrer.basmc.sqlplaytimetracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConfigHandler {
    public static Map<String, String> get_config(String filename) throws IOException {
        Map<String, String>  config_map = new HashMap<>();
        File f = new File(filename);
        if(f.exists() && !f.isDirectory()) {
            try {
                Scanner scan = new Scanner(f);
                String json = scan.nextLine();

                // Read Gson
                Gson gson = new Gson();
                config_map = gson.fromJson(json, Map.class);
            }
            catch (FileNotFoundException e) {
                System.out.println("File was empty.");
                e.printStackTrace();

                //Write
                FileWriter writer = new FileWriter(filename);

                //Write Gson
                config_map = new HashMap<>();
                config_map.put("host", "localhost:3306");
                config_map.put("database", "database");
                config_map.put("username", "root");
                config_map.put("password", "password");

                Gson gson = new GsonBuilder().create();
                String guid_json = gson.toJson(config_map);

                writer.write(guid_json);
                writer.close();
            }
        }
        else {
            try {
                //Write
                f.createNewFile();
                FileWriter writer = new FileWriter(filename);

                //Write Gson
                config_map = new HashMap<>();
                config_map.put("host", "localhost");
                config_map.put("database", "database");
                config_map.put("username", "root");
                config_map.put("password", "password");
                config_map.put("server_name", "AMinecraftServer");

                Gson gson = new GsonBuilder().create();
                String guid_json = gson.toJson(config_map);

                writer.write(guid_json);
                writer.close();
            }
            catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
        return config_map;
    }
}
