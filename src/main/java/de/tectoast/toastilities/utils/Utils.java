package de.tectoast.toastilities.utils;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

public class Utils {
    public static void save(JSONObject json, String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(json.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject load(String path) {
        File f = new File(path);
        if (!f.exists()) {
            try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
                w.write("{}");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new JSONObject();
        }
        JSONObject json = null;
        try {
            json = new JSONObject(new JSONTokener(new FileReader(f)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return json;
    }
}
