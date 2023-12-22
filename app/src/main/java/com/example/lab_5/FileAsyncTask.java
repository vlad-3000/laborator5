package com.example.lab_5;

import android.os.AsyncTask;
import android.os.Environment;

import com.example.lab_5.service.CommonService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.impl.client.LaxRedirectStrategy;

public class FileAsyncTask extends AsyncTask<String, Long, String> {
    public interface ResultListener {
        void result(File resultFile);

        void progress(Long progress, Long max);
    }

    private final String url;
    private File resultFile;
    private ResultListener mListener;
    private String mJournalId;
    private Long progress = 0L;
    private Long maxProgress = 0L;

    public FileAsyncTask(String url, String journalId, ResultListener listener) {
        this.url = url;
        this.mListener = listener;
        this.mJournalId = journalId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            HttpGet requestGet = new HttpGet(url + mJournalId + ".pdf");
            CloseableHttpClient client = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build();
            CloseableHttpResponse response = client.execute(requestGet);

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                maxProgress = entity.getContentLength();
                BufferedInputStream bis = new BufferedInputStream(entity.getContent());
                String baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();


                String fileName = mJournalId + ".pdf";
                File file = new File(baseDir + File.separator + fileName);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                int inByte;
                while ((inByte = bis.read()) != -1) {
                    bos.write(inByte);
                    progress += 1L;
                    if (progress % 10000 == 0 || progress >= entity.getContentLength()) {
                        CommonService.getInstance().getHandler().post(() -> {
                            onProgressUpdate(progress);
                        });
                    }
                }
                bis.close();
                bos.close();
                resultFile = file;
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommonService.getInstance().showToast(e.getMessage());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);
        mListener.progress(progress, maxProgress);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mListener.result(resultFile);
    }
}
