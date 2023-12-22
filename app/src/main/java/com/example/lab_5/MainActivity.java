package com.example.lab_5;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lab_5.databinding.ActivityMainBinding;
import com.example.lab_5.service.CommonService;
import com.example.lab_5.service.Service;

import java.io.File;

public class MainActivity extends AppCompatActivity implements FileAsyncTask.ResultListener {
    private ActivityMainBinding binding;
    private File mCurrentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.teId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.btnDownload.setEnabled(count > 0);
                if (count > 0) {
                    boolean fileExists = Service.getInstance().checkFileExists(s.toString());
                    binding.btnDelete.setEnabled(fileExists);
                    binding.btnWatch.setEnabled(fileExists);
                    binding.btnDownload.setEnabled(!fileExists);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.progressHorizontal.setMax(100);
        binding.btnWatch.setOnClickListener(this::onWatchClick);
        binding.btnDelete.setOnClickListener(this::onDeleteClick);
        binding.btnDownload.setOnClickListener(this::onDownloadClick);
        requestPermissions();
        CommonService.getInstance().getHandler().postDelayed(() -> {
            initPopup();
        }, 250);
    }

    private void initPopup() {
        if (Service.getInstance().isDontShowAgain(this)) {
            return;
        }
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup_tutorial, null);

        int width = 200;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(binding.btnDelete, Gravity.CENTER, 0, 0);
        Button ok = (Button) popupView.findViewById(R.id.btn_ok);
        CheckBox cb = (CheckBox) popupView.findViewById(R.id.cb_dont_show_again);
        cb.setOnCheckedChangeListener((compoundButton, checked) -> Service.getInstance().setDontShowAgain(checked, this));
        ok.setOnClickListener((v) -> popupWindow.dismiss());
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });
    }

    private void onDownloadClick(View v) {
        Service.getInstance().startPolling(binding.teId.getText().toString(), this);
    }

    private void onWatchClick(View v) {
        Service.getInstance().watchFile(mCurrentFile, this);
    }

    private void onDeleteClick(View v) {
        if (Service.getInstance().deleteFile(binding.teId.getText().toString())) {
            mCurrentFile = null;
            binding.btnDelete.setEnabled(false);
            binding.btnWatch.setEnabled(false);
            binding.teId.setText("");
        } else {
            CommonService.getInstance().showToast("Не удалось удалить файл: " + mCurrentFile.getAbsolutePath());
        }
    }

    private void requestPermissions() {
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 0);
                return;
            }
        }
    }

    @Override
    public void result(File file) {
        binding.btnWatch.setEnabled(file != null);
        binding.btnDelete.setEnabled(file != null);
        if (file == null) {
            CommonService.getInstance().showToast("Не удалось загрузить файл по ID: " + binding.teId.getText().toString());
            binding.teId.setText("");
            return;
        }
        mCurrentFile = file;
    }

    @Override
    public void progress(Long progress, Long max) {
        binding.progressHorizontal.setProgress((int) ((progress / max) * 100), false);
    }
}