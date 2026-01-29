package com.github.ecode;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import com.github.ecode.model.User;
import com.github.ecode.util.OkHttpUtils;
import com.github.ecode.util.PreferenceUtils;

import okhttp3.Call;

/**
 * 用户信息 Activity
 * 展示用户详情，提供退出登录、扫码等功能
 */
public class UserInfoActivity extends AppCompatActivity {

    private TextView tvName;
    private TextView tvUsername;
    private TextView tvRole;
    private TextView tvScore;
    private TextView tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 确保 OkHttpUtils 初始化（虽然 MainActivity 已经初始化，但为了健壮性再次调用是安全的）
        OkHttpUtils.init(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvName = findViewById(R.id.tv_name);
        tvUsername = findViewById(R.id.tv_username);
        tvRole = findViewById(R.id.tv_role);
        tvScore = findViewById(R.id.tv_score);
        tvEmail = findViewById(R.id.tv_email);

        Button btnScan = findViewById(R.id.btn_scan);
        Button btnLogout = findViewById(R.id.btn_logout);
        Button btnNightMode = findViewById(R.id.btn_night_mode);

        // 扫码按钮点击事件
        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
        });

        // 退出登录按钮点击事件
        btnLogout.setOnClickListener(v -> {
            // 清除本地存储的 Token
            PreferenceUtils.clearToken(this);
            // 跳转到登录页面，并清空任务栈
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // 夜间模式切换按钮点击事件
        btnNightMode.setOnClickListener(v -> {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });

        // 加载用户信息
        loadUserInfo();
    }

    /**
     * 从服务器加载用户信息
     */
    private void loadUserInfo() {
        String baseUrl = PreferenceUtils.getServerUrl(this);
        String token = PreferenceUtils.getToken(this);
        String url = baseUrl + "/api/user";

        OkHttpUtils.builder()
                .url(url)
                // 在请求头中携带 Token
                .addHeader("token", token)
                .get()
                .async(new OkHttpUtils.ICallBack() {
                    @Override
                    public void onSuccessful(Call call, String data) {
                        runOnUiThread(() -> {
                            try {
                                JSONObject json = JSON.parseObject(data);
                                // 状态码 200 表示成功
                                if (json.getIntValue("code") == 200) {
                                    User user = json.getObject("data", User.class);
                                    updateUI(user);
                                } else {
                                    // 其他错误（401 已被 OkHttpUtils 拦截处理）
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

    /**
     * 更新 UI 显示
     *
     * @param user 用户对象
     */
    private void updateUI(User user) {
        if (user == null) return;
        tvName.setText("昵称: " + (user.getName() == null ? "未设置" : user.getName()));
        tvUsername.setText("用户名: " + user.getUsername());
        tvRole.setText("角色: " + user.getRole());
        tvScore.setText("积分: " + user.getScore());
        tvEmail.setText("邮箱: " + (user.getEmail() == null ? "未绑定" : user.getEmail()));
    }
}
