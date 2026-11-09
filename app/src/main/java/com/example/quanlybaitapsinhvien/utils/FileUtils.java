package com.example.quanlybaitapsinhvien.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            Toast.makeText(context, "Đã tải file về thư mục Downloads: " + outFile.getName(), Toast.LENGTH_LONG).show();
            return outFile;
        } catch (IOException e) {
            Log.e(TAG, "Asset missing or write error: " + e.getMessage());
            try (OutputStream out = new FileOutputStream(outFile)) {
                String defaultContent = "Câu 1: Thủ đô của Việt Nam là gì?\nA. TP. Hồ Chí Minh\nB. Hà Nội\nC. Đà Nẵng\nD. Cần Thơ\nĐáp án: B\n";
                out.write(defaultContent.getBytes());
                out.flush();
                Toast.makeText(context, "Đã tải file về thư mục Downloads: " + outFile.getName(), Toast.LENGTH_LONG).show();
                return outFile;
            } catch (IOException ex) {
                Log.e(TAG, "Error writing default content: " + ex.getMessage());
                Toast.makeText(context, "Lỗi khi lưu file: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
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
            context.startActivity(intent);
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
