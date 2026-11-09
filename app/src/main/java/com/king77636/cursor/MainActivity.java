package com.king77636.cursor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private EditText inputPrompt;
    private TextView outputResponse;
    private Button sendButton;

    private static final String API_KEY = "ضع_مفتاح_الـ_api_هنا";
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        inputPrompt = new EditText(this);
        inputPrompt.setHint("اكتب طلبك هنا (مثلاً: كود Java لتحريك سيارة)");
        layout.addView(inputPrompt);

        sendButton = new Button(this);
        sendButton.setText("إرسال للذكاء الاصطناعي");
        layout.addView(sendButton);

        outputResponse = new TextView(this);
        outputResponse.setText("النتيجة ستظهر هنا...");
        outputResponse.setPadding(0, 32, 0, 0);
        layout.addView(outputResponse);

        setContentView(layout);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String prompt = inputPrompt.getText().toString().trim();
                if (!prompt.isEmpty()) {
                    outputResponse.setText("جاري إرسال الطلب...");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            callAIAPI(prompt);
                        }
                    }).start();
                }
            }
        });
    }

    private void callAIAPI(String prompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);

            String jsonInputString = "{\"model\":\"deepseek-coder\",\"messages\":[{\"role\":\"user\",\"content\":\"أنت مساعد برمجي خبير بلغة Java. اكتب الكود المطلوب بدقة: " + prompt.replace("\"", "\\\"") + "\"}]}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            BufferedReader br;
            if (code == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            }

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            final String resultText = response.toString();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    outputResponse.setText(resultText);
                }
            });

        } catch (final Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    outputResponse.setText("خطأ: " + e.getMessage());
                }
            });
        }
    }
}
