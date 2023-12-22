package com.example.lab_5.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.webkit.MimeTypeMap;

import com.example.lab_5.FileAsyncTask;

import java.io.File;
import java.util.logging.Logger;

public class Service {
    private Logger log = Logger.getLogger(Service.class.getSimpleName());
    private static final String DONT_SHOW_AGAIN = "dont_show_again";
    private static Service instance;
    private final static String BASE_URI = "https://ntv.ifmo.ru/file/journal/";

    public static Service getInstance() {
        return instance;
    }

    public static Service createInstance(Context context) {
        if (instance == null) instance = new Service(context);
        return instance;
    }

    public Service(Context context) {
    }

    public void startPolling(String id, FileAsyncTask.ResultListener listener) {
        FileAsyncTask task = new FileAsyncTask(BASE_URI, id, listener);
        task.execute();
    }

    public void watchFile(File file, Context context) {
        // Get URI and MIME type of file
        try {
            Uri uri = Uri.fromFile(file).normalizeScheme();
            String mime = getMimeType(uri.toString());
            // Open file with user selected app
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setType(mime);
            context.startActivity(Intent.createChooser(intent, "Open file with"));
        } catch (Exception e) {
            CommonService.getInstance().showToast(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getMimeType(String url) {
        try {
            String ext = MimeTypeMap.getFileExtensionFromUrl(url);
            String mime = null;
            if (ext != null) {
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            }
            return mime;
        } catch (Exception e) {
            CommonService.getInstance().showToast(e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    public boolean checkFileExists(String id) {
        try {
            String baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            String fileName = id + ".pdf";
            File file = new File(baseDir + File.separator + fileName);
            return file.exists();
        } catch (Exception e) {
            CommonService.getInstance().showToast(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFile(String id) {
        try {
            String fileName = id + ".pdf";
            String baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            String filePath = baseDir + File.separator + fileName;
            File file = new File(filePath);

            if (!file.exists()) {
                CommonService.getInstance().showToast("Файла: " + filePath + " не существует!");
                return false;
            }
            return file.delete();
        } catch (Exception e) {
            CommonService.getInstance().showToast(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void setDontShowAgain(boolean value, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(DONT_SHOW_AGAIN, value).apply();
    }

    public boolean isDontShowAgain(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(DONT_SHOW_AGAIN, false);
    }
}