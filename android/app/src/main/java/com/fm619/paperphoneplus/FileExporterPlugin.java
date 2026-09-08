package com.fm619.paperphoneplus;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CapacitorPlugin(name = "FileExporter")
public class FileExporterPlugin extends Plugin {
    @PluginMethod
    public void exportFiles(PluginCall call) {
        JSArray files = call.getArray("files");
        if (files == null || files.length() == 0) {
            call.reject("No files to export");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(call, intent, "directorySelected");
    }

    @ActivityCallback
    private void directorySelected(PluginCall call, ActivityResult activityResult) {
        if (call == null) return;
        Intent data = activityResult.getData();
        if (activityResult.getResultCode() != Activity.RESULT_OK || data == null || data.getData() == null) {
            JSObject result = new JSObject();
            result.put("saved", 0);
            result.put("failed", 0);
            result.put("cancelled", true);
            call.resolve(result);
            return;
        }

        Uri treeUri = data.getData();
        try {
            getContext().getContentResolver().takePersistableUriPermission(
                    treeUri,
                    data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
        } catch (SecurityException ignored) {
            // The one-time grant is still valid for this export.
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            int saved = 0;
            int failed = 0;
            Set<String> usedNames = new HashSet<>();
            JSArray files = call.getArray("files");
            String authorization = call.getString("authorization");

            for (int index = 0; files != null && index < files.length(); index++) {
                HttpURLConnection connection = null;
                Uri destination = null;
                try {
                    JSONObject file = files.getJSONObject(index);
                    String source = file.optString("url", "");
                    if (source.isEmpty()) throw new IllegalArgumentException("Missing URL");

                    String requestedName = file.optString("fileName", "file_" + (index + 1));
                    String fileName = uniqueFileName(sanitize(requestedName, "file_" + (index + 1)), usedNames);
                    String mimeType = URLConnection.guessContentTypeFromName(fileName);
                    if (mimeType == null) mimeType = "application/octet-stream";

                    connection = (HttpURLConnection) new URL(source).openConnection();
                    connection.setConnectTimeout(20_000);
                    connection.setReadTimeout(60_000);
                    connection.setInstanceFollowRedirects(true);
                    if (authorization != null && !authorization.isEmpty()) {
                        connection.setRequestProperty("Authorization", authorization);
                    }
                    int status = connection.getResponseCode();
                    if (status < 200 || status >= 300) throw new IllegalStateException("HTTP " + status);

                    String treeDocumentId = DocumentsContract.getTreeDocumentId(treeUri);
                    Uri parent = DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocumentId);
                    destination = DocumentsContract.createDocument(getContext().getContentResolver(), parent, mimeType, fileName);
                    if (destination == null) throw new IllegalStateException("Unable to create destination file");

                    try (InputStream input = connection.getInputStream();
                         OutputStream output = getContext().getContentResolver().openOutputStream(destination, "w")) {
                        if (output == null) throw new IllegalStateException("Unable to open destination file");
                        byte[] buffer = new byte[64 * 1024];
                        int count;
                        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                    }
                    saved++;
                } catch (Exception error) {
                    failed++;
                    if (destination != null) {
                        try { DocumentsContract.deleteDocument(getContext().getContentResolver(), destination); }
                        catch (Exception ignored) {}
                    }
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }

            JSObject result = new JSObject();
            result.put("saved", saved);
            result.put("failed", failed);
            result.put("cancelled", false);
            call.resolve(result);
            executor.shutdown();
        });
    }

    private String sanitize(String name, String fallback) {
        String cleaned = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        return cleaned.isEmpty() || cleaned.equals(".") || cleaned.equals("..") ? fallback : cleaned;
    }

    private String uniqueFileName(String name, Set<String> used) {
        if (used.add(name)) return name;
        int dot = name.lastIndexOf('.');
        String stem = dot > 0 ? name.substring(0, dot) : name;
        String extension = dot > 0 ? name.substring(dot) : "";
        int suffix = 2;
        while (!used.add(stem + "_" + suffix + extension)) suffix++;
        return stem + "_" + suffix + extension;
    }
}
