package app.zmono.font;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class FontRepository {
    public static final String FULL_JSON = "https://raw.githubusercontent.com/zFont/main/main/full.json";

    private FontRepository() {}

    public static List<FontItem> fetchFonts() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(FULL_JSON).openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("User-Agent", "ZMono/1.0");
        conn.connect();

        int code = conn.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("HTTP " + code);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        conn.disconnect();

        JSONObject root = new JSONObject(sb.toString());
        List<FontItem> out = new ArrayList<>();

        if (root.has("slider")) {
            JSONArray slider = root.optJSONArray("slider");
            if (slider != null) {
                for (int i = 0; i < slider.length(); i++) {
                    JSONObject o = slider.optJSONObject(i);
                    if (o != null) out.add(parseItem(o, "Slider"));
                }
            }
        }

        if (root.has("categories")) {
            JSONArray cats = root.optJSONArray("categories");
            if (cats != null) {
                for (int c = 0; c < cats.length(); c++) {
                    JSONObject cat = cats.optJSONObject(c);
                    if (cat == null) continue;
                    String name = cat.optString("name", "Fonts");
                    JSONArray items = cat.optJSONArray("items");
                    if (items == null) continue;
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject o = items.optJSONObject(i);
                        if (o != null) out.add(parseItem(o, name));
                    }
                }
            }
        }
        return out;
    }

    private static FontItem parseItem(JSONObject o, String category) {
        String n = o.optString("n", "Font");
        String s = o.optString("s", "0 KB");
        String u = o.optString("u", "");
        String t = o.optString("t", "");
        String p = o.optString("p", "");
        String c = o.optString("c", category);
        return new FontItem(n, s, u, c, t, p);
    }
}