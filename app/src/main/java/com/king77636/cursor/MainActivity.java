package com.king77636.cursor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText inputPrompt;
    private TextView outputResponse;
    private Button sendButton;

    private static final String API_KEY = "ضع_مفتاح_الـ_api_هنا";
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // إنشاء الواجهة برمجياً لضمان عدم ظهور أخطاء مفقودة
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
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
                String prompt = inputPrompt.getText().toString().trim();
                if (!prompt.isEmpty()) {
                    callAI(prompt);
                }
            }
        });
    }

    private void callAI(String userPrompt) {
        outputResponse.setText("جاري توليد الكود والرد...");

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "deepseek-coder");
            
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", "أنت مساعد برمجي خبير بلغة Java. اكتب الكود المطلوب بدقة: " + userPrompt);
            messages.put(message);
            
            jsonBody.put("messages", messages);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> outputResponse.setText("خطأ في الاتصال: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String result = jsonResponse.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            runOnUiThread(() -> outputResponse.setText(result));
                        } catch (Exception e) {
                            runOnUiThread(() -> outputResponse.setText("خطأ في معالجة البيانات"));
                        }
                    } else {
                        runOnUiThread(() -> outputResponse.setText("فشل الطلب من الخادم"));
                    }
                }
            });

        } catch (Exception e) {
            outputResponse.setText("حدث خطأ: " + e.getMessage());
        }
    }
}
