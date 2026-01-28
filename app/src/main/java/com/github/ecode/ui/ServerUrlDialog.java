package com.github.ecode.ui;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.ecode.util.PreferenceUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ServerUrlDialog {

    public static void show(Context context) {
        final EditText input = new EditText(context);
        input.setHint("http://192.168.1.10:8080");
        String savedUrl = PreferenceUtils.getServerUrl(context);
        if (!TextUtils.isEmpty(savedUrl)) {
            input.setText(savedUrl);
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);

        new MaterialAlertDialogBuilder(context)
                .setTitle("设置服务器地址")
                .setMessage("请输入服务器地址(包含端口)")
                .setView(input)
                .setPositiveButton("保存", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (TextUtils.isEmpty(url)) {
                        Toast.makeText(context, "地址不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!url.startsWith("http")) {
                        url = "http://" + url;
                    }
                    PreferenceUtils.saveServerUrl(context, url);
                    Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
