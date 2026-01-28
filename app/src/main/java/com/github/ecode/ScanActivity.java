package com.github.ecode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.ecode.util.OkHttpUtils;
import com.github.ecode.util.PreferenceUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = "ScanActivity";
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private boolean isProcessing = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "需要相机权限来扫描二维码", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        previewView = findViewById(R.id.view_finder);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider>cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "相机启动失败", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new BarcodeAnalyzer());

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "绑定相机用例失败", e);
        }
    }

    private class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
        private final BarcodeScanner scanner = BarcodeScanning.getClient();

        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            if (isProcessing) {
                imageProxy.close();
                return;
            }

            @androidx.camera.core.ExperimentalGetImage
            android.media.Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

                scanner.process(image)
                        .addOnSuccessListener(barcodes -> {
                            if (!barcodes.isEmpty()) {
                                for (Barcode barcode : barcodes) {
                                    if (barcode.getFormat() == Barcode.FORMAT_QR_CODE) {
                                        String rawValue = barcode.getRawValue();
                                        if (!TextUtils.isEmpty(rawValue)) {
                                            isProcessing = true;
                                            playBeep();
                                            notifyScanned(rawValue);
                                            break; 
                                        }
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "扫描失败", e))
                        .addOnCompleteListener(task -> imageProxy.close());
            } else {
                imageProxy.close();
            }
        }
    }

    private void playBeep() {
        runOnUiThread(() -> {
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void notifyScanned(String sceneId) {
        String baseUrl = PreferenceUtils.getServerUrl(this);
        String token = PreferenceUtils.getToken(this);
        String url = baseUrl + "/user/scan/scanned?sceneId=" + sceneId;

        OkHttpUtils.builder()
                .url(url)
                .addHeader("token", token)
                .post(false)
                .async(new OkHttpUtils.ICallBack() {
                    @Override
                    public void onSuccessful(Call call, String data) {
                        runOnUiThread(() -> {
                            try {
                                JSONObject json = JSON.parseObject(data);
                                if (json.getIntValue("code") == 200) {
                                    showConfirmDialog(sceneId);
                                } else {
                                    Toast.makeText(ScanActivity.this, "扫码处理失败: " + json.getString("msg"), Toast.LENGTH_SHORT).show();
                                    isProcessing = false; // Reset flag to allow rescanning
                                }
                            } catch (Exception e) {
                                Toast.makeText(ScanActivity.this, "解析错误", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call call, String errorMsg) {
                        runOnUiThread(() -> {
                            Toast.makeText(ScanActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                            isProcessing = false; // Reset flag
                        });
                    }
                });
    }

    private void showConfirmDialog(String sceneId) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("确认登录")
                .setMessage("是否确认在电脑端登录？")
                .setPositiveButton("确认", (dialog, which) -> confirmLogin(sceneId, true))
                .setNegativeButton("取消", (dialog, which) -> {
                    confirmLogin(sceneId, false);
                    finish(); // Or isProcessing = false if we want to allow continuous scanning
                })
                .setOnDismissListener(dialog -> {
                    // Handle dismiss if needed (e.g. back button)
                    if (!isFinishing()) {
                         finish(); 
                    }
                })
                .show();
    }

    private void confirmLogin(String sceneId, boolean isConfirm) {
        String baseUrl = PreferenceUtils.getServerUrl(this);
        String token = PreferenceUtils.getToken(this);
        String url = baseUrl + "/user/scan/confirm";

        OkHttpUtils.builder()
                .url(url)
                .addHeader("token", token)
                .addParam("sceneId", sceneId)
                .addParam("isConfirm", String.valueOf(isConfirm))
                .post(true)
                .async(new OkHttpUtils.ICallBack() {
                    @Override
                    public void onSuccessful(Call call, String data) {
                        runOnUiThread(() -> {
                            if (isConfirm) {
                                Toast.makeText(ScanActivity.this, "已确认登录", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ScanActivity.this, "已取消登录", Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(Call call, String errorMsg) {
                        runOnUiThread(() -> {
                            Toast.makeText(ScanActivity.this, "操作失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
