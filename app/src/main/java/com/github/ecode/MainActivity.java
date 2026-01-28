package com.github.ecode;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.ecode.ui.CaptchaDialog;
import com.github.ecode.ui.ServerUrlDialog;
import com.github.ecode.util.MD5Utils;
import com.github.ecode.util.OkHttpUtils;
import com.github.ecode.util.PreferenceUtils;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnServer = findViewById(R.id.btn_server_config);

        // Server Config
        btnServer.setOnClickListener(v -> ServerUrlDialog.show(this));

        // Login Action
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String serverUrl = PreferenceUtils.getServerUrl(this);
            if (TextUtils.isEmpty(serverUrl)) {
                Toast.makeText(this, "请先配置服务器地址", Toast.LENGTH_SHORT).show();
                ServerUrlDialog.show(this);
                return;
            }

            // Show Captcha
            new CaptchaDialog(this, new CaptchaDialog.OnCaptchaListener() {
                @Override
                public void onSuccess(String token) {
                    performLogin(username, password, token);
                }

                @Override
                public void onFailed(String msg) {
                    Toast.makeText(MainActivity.this, "验证失败: " + msg, Toast.LENGTH_SHORT).show();
                }
            }).show();
        });
        
        // Check if token exists and jump? 
        // Develop.md says "preview version, so show user info after login". 
        // Maybe auto-login? Let's keep it manual for now as per "Login Page" requirement.
    }

    private void performLogin(String username, String password, String captchaToken) {
        String baseUrl = PreferenceUtils.getServerUrl(this);
        String url = baseUrl + "/api/user/login";

        OkHttpUtils.builder()
                .url(url)
                .addHeader("captcha-token", captchaToken)
                .addParam("loginType", "passwd")
                .addParam("username", username)
                .addParam("password", MD5Utils.encrypt(password))
                .post(true)
                .async(new OkHttpUtils.ICallBack() {
                    @Override
                    public void onSuccessful(Call call, String data) {
                        runOnUiThread(() -> {
                            try {
                                JSONObject json = JSON.parseObject(data);
                                if (json.getIntValue("code") == 200) {
                                    String token = json.getJSONObject("data").getString("token");
                                    PreferenceUtils.saveToken(MainActivity.this, token);
                                    Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                    
                                    Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this, "登录失败: " + json.getString("msg"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "解析错误", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call call, String errorMsg) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "网络错误: " + errorMsg, Toast.LENGTH_SHORT).show());
                    }
                });
    }
}
