package com.example.quanlybaitapsinhvien.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.quanlybaitapsinhvien.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.media.MediaScannerConnection;

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static File copyAssetToDownloads(Context context, String assetFileName, String outputFileName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) {
            boolean created = downloadsDir.mkdirs();
            if (!created) {
                Log.w(TAG, "Could not create downloads directory");
            }
        }

        File outFile;
        if (downloadsDir.exists() && downloadsDir.canWrite()) {
            outFile = new File(downloadsDir, outputFileName);
        } else {
            outFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), outputFileName);
        }

        try (InputStream in = context.getAssets().open(assetFileName);
             OutputStream out = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            Log.e(TAG, "Asset missing or write error: " + e.getMessage());
            try (OutputStream out = new FileOutputStream(outFile)) {
                String defaultContent = "MẪU BÀI TẬP / CÂU HỎI TRẮC NGHIỆM\n\n" +
                        "Câu 1: Thủ đô của Việt Nam là gì?\n" +
                        "A. TP. Hồ Chí Minh\n" +
                        "B. Hà Nội\n" +
                        "C. Đà Nẵng\n" +
                        "D. Cần Thơ\n" +
                        "Đáp án: B\n\n" +
                        "Câu 2: Hệ điều hành Android thuộc sở hữu của tập đoàn nào?\n" +
                        "A. Apple\n" +
                        "B. Google\n" +
                        "C. Microsoft\n" +
                        "D. Samsung\n" +
                        "Đáp án: B\n";
                out.write(defaultContent.getBytes());
                out.flush();
            } catch (IOException ex) {
                Log.e(TAG, "Error writing default content: " + ex.getMessage());
                Toast.makeText(context, "Lỗi khi lưu file: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        if (outFile.exists()) {
            MediaScannerConnection.scanFile(
                    context,
                    new String[]{outFile.getAbsolutePath()},
                    null,
                    (path, uri) -> Log.d(TAG, "Scanned " + path + ": uri=" + uri)
            );
            Toast.makeText(context, "✅ Đã tải file mẫu về điện thoại: Downloads/" + outFile.getName(), Toast.LENGTH_LONG).show();
            openFile(context, outFile);
        }

        return outFile;
    }

    public static String getFileNameFromUri(Context context, Uri uri) {
        if (uri == null) return "file_dinh_kem";
        String result = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Throwable ignored) {
                // Ignore security or query exceptions
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result != null ? result : "file_dinh_kem";
    }

    public static String copyUriToInternalStorage(Context context, Uri uri) {
        if (uri == null) return null;
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.toString();
        }
        try {
            String fileName = getFileNameFromUri(context, uri);
            File dir = new File(context.getFilesDir(), "attachments");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File destFile = new File(dir, System.currentTimeMillis() + "_" + fileName);
            try (InputStream in = context.getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(destFile)) {
                if (in != null) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    out.flush();
                    return Uri.fromFile(destFile).toString();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy URI to internal storage: " + e.getMessage(), e);
        }
        return uri.toString();
    }

    public static File getOrCopyFileFromUri(Context context, String pathOrUriStr) {
        if (pathOrUriStr == null || pathOrUriStr.trim().isEmpty()) return null;
        try {
            Uri uri = Uri.parse(pathOrUriStr);
            String scheme = uri.getScheme();

            if (scheme == null || "file".equalsIgnoreCase(scheme)) {
                File file = new File(uri.getPath() != null ? uri.getPath() : pathOrUriStr);
                if (file.exists()) return file;
            }

            if ("content".equalsIgnoreCase(scheme)) {
                String fileName = getFileNameFromUri(context, uri);
                File dir = new File(context.getFilesDir(), "attachments");
                if (!dir.exists()) dir.mkdirs();

                File destFile = new File(dir, "cache_" + fileName);
                if (destFile.exists() && destFile.length() > 0) {
                    return destFile;
                }

                try (InputStream in = context.getContentResolver().openInputStream(uri);
                     OutputStream out = new FileOutputStream(destFile)) {
                    if (in != null) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        out.flush();
                        return destFile;
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Could not copy content stream: " + t.getMessage());
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Error in getOrCopyFileFromUri: " + t.getMessage());
        }
        return null;
    }

    @SuppressWarnings("ResourceType")
    public static File copySampleImageToCache(Context context) {
        try {
            File cacheDir = context.getCacheDir();
            File outFile = new File(cacheDir, "sample_attachment.jpg");
            if (outFile.exists() && outFile.length() > 0) {
                return outFile;
            }

            try (InputStream in = context.getResources().openRawResource(R.drawable.images);
                 OutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
                return outFile;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error copying sample image to cache: " + e.getMessage());
            return null;
        }
    }

    public static void openUriOrFile(Context context, String pathOrUriStr) {
        if (pathOrUriStr == null || pathOrUriStr.trim().isEmpty()) {
            Toast.makeText(context, "Không tìm thấy file đính kèm!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File file = getOrCopyFileFromUri(context, pathOrUriStr);

            Uri targetUri;
            String mimeType = null;

            if (file != null && file.exists()) {
                targetUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".fileprovider",
                        file
                );
                mimeType = getMimeType(file.getAbsolutePath());
            } else {
                targetUri = Uri.parse(pathOrUriStr);
                String scheme = targetUri.getScheme();

                if ("content".equalsIgnoreCase(scheme)) {
                    try {
                        mimeType = context.getContentResolver().getType(targetUri);
                    } catch (Throwable ignored) {
                        // Catch SecurityException on getType safely
                    }
                }

                if (mimeType == null || mimeType.isEmpty() || "*/*".equals(mimeType)) {
                    mimeType = getMimeType(pathOrUriStr);
                }
            }

            // Fallback for image URIs
            if (mimeType == null || "*/*".equals(mimeType)) {
                String lower = pathOrUriStr.toLowerCase();
                if (lower.contains("image") || lower.contains("img") || lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg") || lower.endsWith(".webp")) {
                    mimeType = "image/*";
                }
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(targetUri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Intent chooser = Intent.createChooser(intent, "Mở file đính kèm");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);

        } catch (SecurityException se) {
            Log.w(TAG, "SecurityException opening URI: " + pathOrUriStr, se);
            File fallbackFile = copySampleImageToCache(context);
            if (fallbackFile != null && fallbackFile.exists()) {
                openFile(context, fallbackFile);
            } else {
                Toast.makeText(context, "Không thể truy cập file đính kèm này!", Toast.LENGTH_SHORT).show();
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Không có ứng dụng hỗ trợ mở loại file này!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error opening file/URI: " + e.getMessage(), e);
            Toast.makeText(context, "Lỗi mở file: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void openFile(Context context, File file) {
        if (file == null || !file.exists()) {
            Toast.makeText(context, "File không tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
        );

        String mimeType = getMimeType(file.getAbsolutePath());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            Intent chooser = Intent.createChooser(intent, "Mở file đính kèm");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Không có ứng dụng nào hỗ trợ mở file này!", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null && !extension.isEmpty()) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        if (type == null) {
            if (url.endsWith(".txt")) {
                type = "text/plain";
            } else if (url.endsWith(".csv")) {
                type = "text/csv";
            } else if (url.endsWith(".pdf")) {
                type = "application/pdf";
            } else if (url.endsWith(".doc")) {
                type = "application/msword";
            } else if (url.endsWith(".docx")) {
                type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else {
                type = "*/*";
            }
        }
        return type;
    }
}
