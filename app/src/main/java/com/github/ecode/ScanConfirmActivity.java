package com.github.ecode;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.github.ecode.model.User;
import com.github.ecode.util.OkHttpUtils;
import com.github.ecode.util.PreferenceUtils;

import okhttp3.Call;

public class ScanConfirmActivity extends AppCompatActivity {

    private String sceneId;
    private String ip;
    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_confirm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sceneId = getIntent().getStringExtra("sceneId");
        ip = getIntent().getStringExtra("ip");

        initViews();
        loadUserInfo();
        loadIpLocation();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        TextView tvIp = findViewById(R.id.tv_ip);
        tvLocation = findViewById(R.id.tv_location);
        Button btnConfirm = findViewById(R.id.btn_confirm);
        Button btnCancel = findViewById(R.id.btn_cancel);

        tvIp.setText("IP: " + (ip != null ? ip : "--"));
        tvLocation.setText("正在获取...");

        btnConfirm.setOnClickListener(v -> confirmLogin(true));
        btnCancel.setOnClickListener(v -> confirmLogin(false));
    }

    private void loadUserInfo() {
        String baseUrl = PreferenceUtils.getServerUrl(this);
        String token = PreferenceUtils.getToken(this);
        String url = baseUrl + "/api/user";

        OkHttpUtils.builder()
                .url(url)
                .addHeader("token", token)
                .get()
                .async(new OkHttpUtils.ICallBack() {
                    @Override
                    public void onSuccessful(Call call, String data) {
                        runOnUiThread(() -> {
                            try {
                                JSONObject json = JSON.parseObject(data);
                                if (json.getIntValue("code") == 200) {
                                    User user = json.getObject("data", User.class);
                                    if (user != null) {
                                        tvUsername.setText(user.getUsername());
                                        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                                            Glide.with(ScanConfirmActivity.this)
                                                    .load(user.getProfilePicture())
                                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                                    .placeholder(R.mipmap.ic_launcher)
                                                    .error(R.mipmap.ic_launcher)
                                                    .into(ivAvatar);
                                        } else {
                                            Glide.with(ScanConfirmActivity.this)
                                                    .load(R.mipmap.ic_launcher)
                                                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                                                    .into(ivAvatar);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call call, String errorMsg) {
                        // Ignore user info fetch failure
                    }
                });
    }

    private void loadIpLocation() {
        if (ip == null || ip.isEmpty()) {
            tvLocation.setText("未知归属地");
            return;
        }
        
        String url = "https://opendata.baidu.com/api.php?co=&resource_id=6006&oe=utf8&query=" + ip;

        OkHttpUtils.builder()
                .url(url)
                .get()
                .async(new OkHttpUtils.ICallBack() {
                    @Override
                    public void onSuccessful(Call call, String data) {
                        runOnUiThread(() -> {
                            try {
                                JSONObject json = JSON.parseObject(data);
                                JSONArray dataArray = json.getJSONArray("data");
                                if (dataArray != null && !dataArray.isEmpty()) {
                                    JSONObject info = dataArray.getJSONObject(0);
                                    String location = info.getString("location");
                                    if (location != null && !location.isEmpty()) {
                                        tvLocation.setText(location);
                                    } else {
                                        tvLocation.setText("未知归属地");
                                    }
                                } else {
                                    tvLocation.setText("未知归属地");
                                }
                            } catch (Exception e) {
                                tvLocation.setText("获取失败");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call call, String errorMsg) {
                        runOnUiThread(() -> tvLocation.setText("获取失败"));
                    }
                });
    }

    private void confirmLogin(boolean isConfirm) {
        String baseUrl = PreferenceUtils.getServerUrl(this);
        String token = PreferenceUtils.getToken(this);
        String url = baseUrl + "/api/user/scan/confirm";

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
                            try {
                                JSONObject json = JSON.parseObject(data);
                                if (isConfirm && json.getIntValue("code") == 200) {
                                    Toast.makeText(ScanConfirmActivity.this, "已确认登录", Toast.LENGTH_SHORT).show();
                                } else if (!isConfirm) {
                                    Toast.makeText(ScanConfirmActivity.this, "已取消登录", Toast.LENGTH_SHORT).show();
                                } else {
                                     String msg = json.getString("msg");
                                     if(msg != null) Toast.makeText(ScanConfirmActivity.this, "操作: " + msg, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                // ignore
                            }
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(Call call, String errorMsg) {
                        runOnUiThread(() -> {
                            Toast.makeText(ScanConfirmActivity.this, "操作失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                            if (!isConfirm) finish(); // Cancel should finish anyway
                        });
                    }
                });
    }
}