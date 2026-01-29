package com.github.ecode;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.github.ecode.model.User;
import com.github.ecode.util.OkHttpUtils;
import com.github.ecode.util.PreferenceUtils;

import okhttp3.Call;

/**
 * 用户信息 Activity
 * 展示用户详情，提供退出登录、扫码等功能
 */
public class UserInfoActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvName;
    private TextView tvId;
    private TextView tvRole;
    private TextView tvStatus;
    private TextView tvSex;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvAddress;
    private TextView tvScore;
    private TextView tvBirthDate;
    private TextView tvCreateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 确保 OkHttpUtils 初始化
        OkHttpUtils.init(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        loadUserInfo();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        tvName = findViewById(R.id.tv_name);
        tvId = findViewById(R.id.tv_id);
        tvRole = findViewById(R.id.tv_role);
        tvStatus = findViewById(R.id.tv_status);
        tvSex = findViewById(R.id.tv_sex);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvAddress = findViewById(R.id.tv_address);
        tvScore = findViewById(R.id.tv_score);
        tvBirthDate = findViewById(R.id.tv_birth_date);
        tvCreateTime = findViewById(R.id.tv_create_time);
    }

    private void setupListeners() {
        Button btnScan = findViewById(R.id.btn_scan);
        Button btnLogout = findViewById(R.id.btn_logout);
        Button btnNightMode = findViewById(R.id.btn_night_mode);

        // Update button text based on current night mode
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            btnNightMode.setText("切换日间模式");
        } else {
            btnNightMode.setText("切换夜间模式");
        }

        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            PreferenceUtils.clearToken(this);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        btnNightMode.setOnClickListener(v -> {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
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
                                    updateUI(user);
                                } else {
                                    Toast.makeText(UserInfoActivity.this, "获取信息失败: " + json.getString("msg"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call call, String errorMsg) {
                        runOnUiThread(() -> Toast.makeText(UserInfoActivity.this, "网络错误", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void updateUI(User user) {
        if (user == null) return;

        // 加载头像
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            Glide.with(this)
                 .load(user.getProfilePicture())
                 .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                 .placeholder(R.mipmap.ic_launcher)
                 .error(R.mipmap.ic_launcher)
                 .into(ivAvatar);
        } else {
            Glide.with(this)
                 .load(R.mipmap.ic_launcher)
                 .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                 .into(ivAvatar);
        }

        tvUsername.setText(user.getUsername());
        tvName.setText(user.getName() != null ? user.getName() : "未设置昵称");
        
        tvId.setText("ID: " + user.getId());
        tvRole.setText("角色: " + getRoleText(user.getRole()));
        tvStatus.setText("状态: " + getStatusText(user.getStatus()));
        tvSex.setText("性别: " + getSexText(user.getSex()));
        
        tvEmail.setText("邮箱: " + (user.getEmail() != null ? user.getEmail() : "未绑定"));
        tvPhone.setText("手机: " + (user.getPhone() != null ? user.getPhone() : "未绑定"));
        tvAddress.setText("地址: " + (user.getAddress() != null ? user.getAddress() : "未设置"));
        
        tvScore.setText("积分: " + user.getScore());
        tvBirthDate.setText("生日: " + (user.getBirthDate() != null ? user.getBirthDate() : "--"));
        tvCreateTime.setText("注册时间: " + (user.getCreateTime() != null ? user.getCreateTime() : "--"));
    }

    private String getRoleText(String role) {
        if (role == null) return "--";
        switch (role) {
            case "ADMIN": return "管理员";
            case "TEACHER": return "教师";
            case "STUDENT": return "学生";
            default: return role;
        }
    }

    private String getStatusText(String status) {
        if (status == null) return "--";
        switch (status) {
            case "ENABLE": return "正常";
            case "DISABLE": return "禁用";
            default: return status;
        }
    }

    private String getSexText(String sex) {
        if (sex == null) return "--";
        switch (sex) {
            case "MALE": return "男";
            case "FEMALE": return "女";
            default: return sex;
        }
    }
}