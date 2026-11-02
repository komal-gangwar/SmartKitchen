package com.example.smartkitchen;

import android.os.Handler;
import android.os.Looper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.android.gms.tasks.Tasks;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Shared recipe lookup: Firestore first, Gemini on a cache miss. */
public final class RecipeRepository {
    public interface Callback {
        void onResult(Recipe recipe);
        void onError(Exception error);
    }

    private static final FirebaseFirestore DB = FirebaseFirestore.getInstance();
    private static final ExecutorService IO = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final ConcurrentHashMap<String, Object> IN_FLIGHT = new ConcurrentHashMap<>();

    private RecipeRepository() {}

    public static void getById(String id, Callback callback) {
        if (id == null || id.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Recipe was not specified."));
            return;
        }
        DB.collection("recipes").document(id).get()
                .addOnSuccessListener(snapshot -> {
                    Recipe cached = fromDocument(snapshot);
                    if (cached != null) {
                        callback.onResult(cached);
                    } else {
                        Recipe bundled = RecipeData.getRecipeById(id);
                        if (bundled != null) callback.onResult(bundled);
                        else callback.onError(new IllegalStateException("Recipe not found."));
                    }
                })
                .addOnFailureListener(error -> {
                    Recipe bundled = RecipeData.getRecipeById(id);
                    if (bundled != null) callback.onResult(bundled);
                    else callback.onError(error);
                });
    }

    public static void findOrGenerate(String request, Callback callback) {
        String normalized = request == null ? "" : request.trim().replaceAll("\\s+", " ");
        if (normalized.length() < 2) {
            callback.onError(new IllegalArgumentException("Enter a recipe name or describe what you want to cook."));
            return;
        }
        final String id = "recipe_" + sha256(normalized.toLowerCase(Locale.ROOT));
        DB.collection("recipes").document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Recipe cached = fromDocument(task.getResult());
                if (cached != null) { callback.onResult(cached); return; }
            }
            generateOnce(normalized, id, callback);
        });
    }

    private static void generateOnce(String request, String id, Callback callback) {
        Object existing = IN_FLIGHT.putIfAbsent(id, new Object());
        if (existing != null) {
            // A second request re-reads the shared document after the first write completes.
            IO.execute(() -> {
                try {
                    for (int i = 0; i < 60; i++) {
                        Thread.sleep(500);
                        DocumentSnapshot snapshot = Tasks.await(DB.collection("recipes").document(id).get());
                        Recipe recipe = fromDocument(snapshot);
                        if (recipe != null) { deliver(callback, recipe, null); return; }
                    }
                    deliver(callback, null, new IllegalStateException("Recipe generation is taking too long. Please try again."));
                } catch (Exception error) { deliver(callback, null, error); }
            });
            return;
        }
        IO.execute(() -> {
            try {
                Recipe recipe = callGemini(request, id);
                Tasks.await(DB.collection("recipes").document(id).set(toMap(recipe), SetOptions.merge()));
                deliver(callback, recipe, null);
            } catch (Exception error) {
                deliver(callback, null, error);
            } finally {
                IN_FLIGHT.remove(id);
            }
        });
    }

    private static Recipe callGemini(String request, String id) throws Exception {
        if (BuildConfig.GEMINI_API_KEY.trim().isEmpty()) {
            throw new IllegalStateException("Gemini API key is not configured. Set GEMINI_API_KEY in your Gradle user properties or environment.");
        }
        String prompt = "Create a practical recipe for this request: " + request + ". Return only a JSON object with string fields "
                + "name, emoji, rating, time, difficulty, description, nutrition, ingredients, step1Title, step1Description, "
                + "step2Title, step2Description, step3Title, step3Description, step4Title, step4Description, step5Title, "
                + "step5Description, chefTips. ingredients and chefTips should be readable multiline text. Use safe, clear cooking instructions.";
        JSONObject body = new JSONObject();
        JSONArray parts = new JSONArray().put(new JSONObject().put("text", prompt));
        body.put("contents", new JSONArray().put(new JSONObject().put("parts", parts)));
        body.put("generationConfig", new JSONObject().put("responseMimeType", "application/json"));

        HttpURLConnection connection = (HttpURLConnection) new URL(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent").openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(45000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("x-goog-api-key", BuildConfig.GEMINI_API_KEY);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int status = connection.getResponseCode();
        InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
        } finally { connection.disconnect(); }
        if (status < 200 || status >= 300) throw new IllegalStateException("Gemini request failed (HTTP " + status + "). Check API access and quota.");
        JSONObject root = new JSONObject(response.toString());
        String text = root.getJSONArray("candidates").getJSONObject(0).getJSONObject("content")
                .getJSONArray("parts").getJSONObject(0).getString("text").trim();
        JSONObject data = new JSONObject(text);
        return new Recipe(id, required(data, "name"), value(data, "emoji", "🍽️"), value(data, "rating", "4.8"),
                required(data, "time"), value(data, "difficulty", "Medium"), required(data, "description"),
                value(data, "nutrition", "Nutrition information unavailable"), required(data, "ingredients"),
                required(data, "step1Title"), required(data, "step1Description"), required(data, "step2Title"), required(data, "step2Description"),
                required(data, "step3Title"), required(data, "step3Description"), required(data, "step4Title"), required(data, "step4Description"),
                required(data, "step5Title"), required(data, "step5Description"), value(data, "chefTips", "Use clean utensils and check food is fully cooked."));
    }

    private static String required(JSONObject data, String key) throws Exception {
        String result = data.optString(key, "").trim();
        if (result.isEmpty()) throw new IllegalStateException("Gemini returned an incomplete recipe.");
        return result;
    }

    private static String value(JSONObject data, String key, String fallback) {
        String result = data.optString(key, "").trim();
        return result.isEmpty() ? fallback : result;
    }

    private static Map<String, Object> toMap(Recipe r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", r.id); map.put("name", r.name); map.put("emoji", r.emoji); map.put("rating", r.rating);
        map.put("time", r.time); map.put("difficulty", r.difficulty); map.put("description", r.description);
        map.put("nutrition", r.nutrition); map.put("ingredients", r.ingredients);
        map.put("step1Title", r.step1Title); map.put("step1Description", r.step1Description);
        map.put("step2Title", r.step2Title); map.put("step2Description", r.step2Description);
        map.put("step3Title", r.step3Title); map.put("step3Description", r.step3Description);
        map.put("step4Title", r.step4Title); map.put("step4Description", r.step4Description);
        map.put("step5Title", r.step5Title); map.put("step5Description", r.step5Description); map.put("chefTips", r.chefTips);
        return map;
    }

    private static Recipe fromDocument(DocumentSnapshot d) {
        if (d == null || !d.exists()) return null;
        try {
            return new Recipe(d.getId(), d.getString("name"), d.getString("emoji"), d.getString("rating"), d.getString("time"),
                    d.getString("difficulty"), d.getString("description"), d.getString("nutrition"), d.getString("ingredients"),
                    d.getString("step1Title"), d.getString("step1Description"), d.getString("step2Title"), d.getString("step2Description"),
                    d.getString("step3Title"), d.getString("step3Description"), d.getString("step4Title"), d.getString("step4Description"),
                    d.getString("step5Title"), d.getString("step5Description"), d.getString("chefTips"));
        } catch (Exception ignored) { return null; }
    }

    private static void deliver(Callback callback, Recipe recipe, Exception error) {
        MAIN.post(() -> { if (error != null) callback.onError(error); else callback.onResult(recipe); });
    }

    private static String sha256(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : digest) out.append(String.format(Locale.ROOT, "%02x", b));
            return out.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
