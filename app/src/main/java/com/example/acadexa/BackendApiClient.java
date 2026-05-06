package com.example.acadexa;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackendApiClient {
    private final String baseUrl;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public BackendApiClient(String baseUrl) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
    }

    public void getUserData(int userId, String token, JsonCallback callback) {
        try {
            postJson("get_user_data", new JSONObject().put("user_id", userId), token, callback);
        } catch (Exception e) {
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    public void getTasks(int userId, String token, JsonCallback callback) {
        try {
            postJson("get_tasks", new JSONObject().put("user_id", userId), token, callback);
        } catch (Exception e) {
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    public void addTask(JSONObject taskPayload, String token, JsonCallback callback) {
        postJson("add_task", taskPayload, token, callback);
    }

    public void updateTask(JSONObject taskPayload, String token, JsonCallback callback) {
        postJson("update_task", taskPayload, token, callback);
    }

    public void deleteTask(int taskId, String token, JsonCallback callback) {
        try {
            postJson("delete_task", new JSONObject().put("task_id", taskId), token, callback);
        } catch (Exception e) {
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    public void getSchedule(int userId, String token, JsonCallback callback) {
        try {
            postJson("get_schedule", new JSONObject().put("user_id", userId), token, callback);
        } catch (Exception e) {
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    public void addSchedule(JSONObject schedulePayload, String token, JsonCallback callback) {
        postJson("add_schedule", schedulePayload, token, callback);
    }

    public void getNotifications(int userId, String token, JsonCallback callback) {
        try {
            postJson("get_notifications", new JSONObject().put("user_id", userId), token, callback);
        } catch (Exception e) {
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    private void postJson(String endpoint, JSONObject body, String token, JsonCallback callback) {
        if (baseUrl.isEmpty()) {
            if (callback != null) {
                callback.onError("Backend base URL is not configured");
            }
            return;
        }

        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(baseUrl.endsWith("/") ? baseUrl + endpoint : baseUrl + "/" + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                if (token != null && !token.isEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }

                byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(payload.length);
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(payload);
                }

                int statusCode = connection.getResponseCode();
                InputStream inputStream = statusCode >= 200 && statusCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();
                String response = readStream(inputStream);
                if (statusCode >= 200 && statusCode < 300) {
                    if (callback != null) {
                        callback.onSuccess(new JSONObject(response.isEmpty() ? "{}" : response));
                    }
                } else if (callback != null) {
                    callback.onError(response.isEmpty() ? ("Request failed with status " + statusCode) : response);
                }
            } catch (Exception exception) {
                if (callback != null) {
                    callback.onError(exception.getMessage());
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String readStream(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    public interface JsonCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }
}
