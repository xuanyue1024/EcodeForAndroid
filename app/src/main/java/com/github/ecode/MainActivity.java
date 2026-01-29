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

/**
 * 主 Activity，处理用户登录逻辑
 */
public class MainActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化 OkHttpUtils 上下文，确保 401 跳转能正常工作
        OkHttpUtils.init(this);

        // 检查是否存在有效的 Token，如果存在则直接跳转到 UserInfoActivity
        String token = PreferenceUtils.getToken(this);
        if (!TextUtils.isEmpty(token)) {
            Intent intent = new Intent(this, UserInfoActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化视图组件
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnServer = findViewById(R.id.btn_server_config);

        // 服务器配置按钮点击事件
        btnServer.setOnClickListener(v -> ServerUrlDialog.show(this));

        // 登录按钮点击事件
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // 校验用户名和密码是否为空
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 校验是否配置了服务器地址
            String serverUrl = PreferenceUtils.getServerUrl(this);
            if (TextUtils.isEmpty(serverUrl)) {
                Toast.makeText(this, "请先配置服务器地址", Toast.LENGTH_SHORT).show();
                ServerUrlDialog.show(this);
                return;
            }

            // 弹出验证码对话框，验证成功后执行登录
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
    }

    /**
     * 执行登录请求
     *
     * @param username     用户名
     * @param password     密码
     * @param captchaToken 验证码 Token
     */
    private void performLogin(String username, String password, String captchaToken) {
        String baseUrl = PreferenceUtils.getServerUrl(this);
        String url = baseUrl + "/api/user/login";

        OkHttpUtils.builder()
                .url(url)
                .addHeader("captcha-token", captchaToken)
                .addParam("loginType", "passwd")
                .addParam("username", username)
                // 密码使用 MD5 加密传输
                .addParam("password", MD5Utils.encrypt(password))
                .post(true)
                .async(new OkHttpUtils.ICallBack() {
                    @Override
                    public void onSuccessful(Call call, String data) {
                        runOnUiThread(() -> {
                            try {
                                JSONObject json = JSON.parseObject(data);
                                // 状态码 200 表示成功
                                if (json.getIntValue("code") == 200) {
                                    String token = json.getJSONObject("data").getString("token");
                                    // 持久化存储 Token
                                    PreferenceUtils.saveToken(MainActivity.this, token);
                                    Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                    
                                    // 跳转到 UserInfoActivity
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
