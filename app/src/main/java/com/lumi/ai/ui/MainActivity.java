package com.lumi.ai.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lumi.ai.BuildConfig;
import com.lumi.ai.R;
import com.lumi.ai.api.ApiClient;
import com.lumi.ai.database.AppDatabase;
import com.lumi.ai.database.ChatEntity;
import com.lumi.ai.database.MessageEntity;
import com.lumi.ai.model.GeminiRequest;
import com.lumi.ai.model.GeminiResponse;
import com.lumi.ai.model.VideoOperationResponse;
import com.lumi.ai.ui.adapter.ChatAdapter;
import com.lumi.ai.ui.adapter.HistoryAdapter;
import com.lumi.ai.utils.AnimationHelper;
import com.lumi.ai.utils.MediaStorageHelper;
import com.lumi.ai.utils.VoiceInputHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerMessages, recyclerHistory;
    private EditText editMessage;
    private ImageButton btnSend, btnVoice, btnAttachPhoto, btnMenu, btnNewChatTop, btnRemoveAttachment;
    private Button btnNewChatDrawer;
    private ChipGroup chipGroupModes;
    private FrameLayout previewContainer;
    private ImageView imgAttachmentPreview;

    private ChatAdapter chatAdapter;
    private HistoryAdapter historyAdapter;
    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private VoiceInputHelper voiceHelper;
    private long currentChatId = -1;
    private Uri attachedImageUri = null;
    private Runnable thinkingMoreRunnable;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    attachedImageUri = uri;
                    imgAttachmentPreview.setImageURI(uri);
                    previewContainer.setVisibility(View.VISIBLE);
                    updateSendButtonVisibility();
                }
            });

    private final ActivityResultLauncher<String> requestMicPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startVoiceRecognition();
                } else {
                    Toast.makeText(this, "Нужен доступ к микрофону для голосового ввода", Toast.LENGTH_SHORT).show();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String err = Log.getStackTraceString(throwable);
            mainHandler.post(() -> new AlertDialog.Builder(this)
                    .setTitle("Ошибка LUMI AI")
                    .setMessage(err.length() > 600 ? err.substring(0, 600) : err)
                    .setPositiveButton("Закрыть", (d, w) -> finish())
                    .show());
        });

        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

        db = AppDatabase.getInstance(this);
        buildJavaUI();
        setupAnimations();
        setupVoiceInput();
        loadHistoryList();
        startNewChat();
    }

    private void buildJavaUI() {
        int bgCharcoal = Color.parseColor("#141414");
        int surfaceDark = Color.parseColor("#1F1F1F");
        int surfaceElevated = Color.parseColor("#2A2A2A");
        int crimsonRed = Color.parseColor("#E51A1A");

        drawerLayout = new DrawerLayout(this);
        drawerLayout.setBackgroundColor(bgCharcoal);

        LinearLayout mainCol = new LinearLayout(this);
        mainCol.setOrientation(LinearLayout.VERTICAL);
        mainCol.setBackgroundColor(bgCharcoal);
        mainCol.setFitsSystemWindows(true);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(32, 24, 32, 24);

        btnMenu = new ImageButton(this);
        btnMenu.setImageResource(android.R.drawable.ic_menu_sort_by_size);
        btnMenu.setColorFilter(Color.WHITE);
        btnMenu.setBackgroundColor(Color.TRANSPARENT);

        TextView title = new TextView(this);
        title.setText("  LUMI AI");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        btnNewChatTop = new ImageButton(this);
        btnNewChatTop.setImageResource(android.R.drawable.ic_input_add);
        btnNewChatTop.setColorFilter(crimsonRed);
        btnNewChatTop.setBackgroundColor(Color.TRANSPARENT);

        topBar.addView(btnMenu);
        topBar.addView(title);
        topBar.addView(btnNewChatTop);
        mainCol.addView(topBar);

        recyclerMessages = new RecyclerView(this);
        recyclerMessages.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        recyclerMessages.setPadding(24, 8, 24, 8);
        mainCol.addView(recyclerMessages);
        chipGroupModes = new ChipGroup(this);
        chipGroupModes.setSingleSelection(true);
        chipGroupModes.setSelectionRequired(true);
        chipGroupModes.setPadding(28, 8, 28, 8);

        Chip chipChat = new Chip(this);
        chipChat.setId(R.id.chipModeChat);
        chipChat.setText("Умный чат");
        chipChat.setCheckable(true);
        chipChat.setChecked(true);
        chipChat.setTextColor(Color.WHITE);

        Chip chipImg = new Chip(this);
        chipImg.setId(R.id.chipModeImage);
        chipImg.setText("Создать фото");
        chipImg.setCheckable(true);
        chipImg.setTextColor(Color.WHITE);

        Chip chipVid = new Chip(this);
        chipVid.setId(R.id.chipModeVideo);
        chipVid.setText("Создать видео");
        chipVid.setCheckable(true);
        chipVid.setTextColor(Color.WHITE);

        chipGroupModes.addView(chipChat);
        chipGroupModes.addView(chipImg);
        chipGroupModes.addView(chipVid);
        mainCol.addView(chipGroupModes);

        previewContainer = new FrameLayout(this);
        previewContainer.setVisibility(View.GONE);
        previewContainer.setPadding(32, 0, 0, 12);
        imgAttachmentPreview = new ImageView(this);
        imgAttachmentPreview.setLayoutParams(new FrameLayout.LayoutParams(180, 180));
        imgAttachmentPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        btnRemoveAttachment = new ImageButton(this);
        btnRemoveAttachment.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnRemoveAttachment.setBackgroundColor(crimsonRed);
        btnRemoveAttachment.setColorFilter(Color.WHITE);
        previewContainer.addView(imgAttachmentPreview);
        previewContainer.addView(btnRemoveAttachment);
        mainCol.addView(previewContainer);

        LinearLayout inputBar = new LinearLayout(this);
        inputBar.setOrientation(LinearLayout.HORIZONTAL);
        inputBar.setGravity(Gravity.CENTER_VERTICAL);
        inputBar.setBackgroundColor(surfaceElevated);
        inputBar.setPadding(16, 12, 16, 12);

        btnAttachPhoto = new ImageButton(this);
        btnAttachPhoto.setImageResource(android.R.drawable.ic_menu_camera);
        btnAttachPhoto.setColorFilter(Color.LTGRAY);
        btnAttachPhoto.setBackgroundColor(Color.TRANSPARENT);

        editMessage = new EditText(this);
        editMessage.setHint("Спроси Lumi или опиши задачу…");
        editMessage.setTextColor(Color.WHITE);
        editMessage.setHintTextColor(Color.GRAY);
        editMessage.setBackgroundColor(Color.TRANSPARENT);
        editMessage.setMaxLines(4);
        editMessage.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        btnVoice = new ImageButton(this);
        btnVoice.setImageResource(android.R.drawable.ic_btn_speak_now);
        btnVoice.setColorFilter(crimsonRed);
        btnVoice.setBackgroundColor(Color.TRANSPARENT);

        btnSend = new ImageButton(this);
        btnSend.setImageResource(android.R.drawable.ic_menu_send);
        btnSend.setColorFilter(Color.WHITE);
        btnSend.setBackgroundColor(crimsonRed);
        btnSend.setVisibility(View.GONE);

        inputBar.addView(btnAttachPhoto);
        inputBar.addView(editMessage);
        inputBar.addView(btnVoice);
        inputBar.addView(btnSend);
        mainCol.addView(inputBar);

        drawerLayout.addView(mainCol);

        LinearLayout drawerCol = new LinearLayout(this);
        drawerCol.setOrientation(LinearLayout.VERTICAL);
        drawerCol.setBackgroundColor(surfaceDark);
        drawerCol.setPadding(32, 48, 32, 32);
        DrawerLayout.LayoutParams drawerLp = new DrawerLayout.LayoutParams(720, ViewGroup.LayoutParams.MATCH_PARENT);
        drawerLp.gravity = GravityCompat.START;
        drawerCol.setLayoutParams(drawerLp);

        btnNewChatDrawer = new Button(this);
        btnNewChatDrawer.setText("+ Новый чат");
        btnNewChatDrawer.setBackgroundColor(crimsonRed);
        btnNewChatDrawer.setTextColor(Color.WHITE);
        drawerCol.addView(btnNewChatDrawer);

        recyclerHistory = new RecyclerView(this);
        recyclerHistory.setPadding(0, 24, 0, 0);
        drawerCol.addView(recyclerHistory);

        drawerLayout.addView(drawerCol);
        setContentView(drawerLayout);
        chatAdapter = new ChatAdapter(this);
        LinearLayoutManager chatLm = new LinearLayoutManager(this);
        chatLm.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(chatLm);
        recyclerMessages.setAdapter(chatAdapter);

        historyAdapter = new HistoryAdapter(new HistoryAdapter.OnChatActionListener() {
            @Override
            public void onChatSelected(ChatEntity chat) {
                drawerLayout.closeDrawer(GravityCompat.START);
                loadChatMessages(chat.id);
            }

            @Override
            public void onChatDeleted(ChatEntity chat) {
                executor.execute(() -> {
                    db.chatDao().deleteChat(chat);
                    loadHistoryList();
                    if (currentChatId == chat.id) {
                        mainHandler.post(() -> startNewChat());
                    }
                });
            }
        });
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setAdapter(historyAdapter);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btnNewChatTop.setOnClickListener(v -> startNewChat());
        btnNewChatDrawer.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startNewChat();
        });

        btnAttachPhoto.setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );

        btnRemoveAttachment.setOnClickListener(v -> {
            attachedImageUri = null;
            previewContainer.setVisibility(View.GONE);
            updateSendButtonVisibility();
        });

        editMessage.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonVisibility();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSend.setOnClickListener(v -> handleSendMessage());

        btnVoice.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition();
            } else {
                requestMicPermission.launch(Manifest.permission.RECORD_AUDIO);
            }
        });
    }

    private void setupAnimations() {
        AnimationHelper.setupSpringButton(btnSend);
        AnimationHelper.setupSpringButton(btnVoice);
        AnimationHelper.setupSpringButton(btnAttachPhoto);
        AnimationHelper.setupSpringButton(btnMenu);
        AnimationHelper.setupSpringButton(btnNewChatTop);
        AnimationHelper.setupSpringButton(btnNewChatDrawer);
    }

    private void updateSendButtonVisibility() {
        boolean hasText = editMessage.getText().toString().trim().length() > 0;
        boolean hasImage = attachedImageUri != null;
        if (hasText || hasImage) {
            btnVoice.setVisibility(View.GONE);
            btnSend.setVisibility(View.VISIBLE);
        } else {
            btnSend.setVisibility(View.GONE);
            btnVoice.setVisibility(View.VISIBLE);
        }
    }

    private void setupVoiceInput() {
        voiceHelper = new VoiceInputHelper(this, new VoiceInputHelper.VoiceCallback() {
            @Override
            public void onListeningStarted() {
                editMessage.setHint("Слушаю вас…");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                float scale = 1.0f + Math.max(0f, rmsdB) * 0.03f;
                btnVoice.setScaleX(Math.min(scale, 1.35f));
                btnVoice.setScaleY(Math.min(scale, 1.35f));
            }

            @Override
            public void onResult(String recognizedText) {
                btnVoice.setScaleX(1f);
                btnVoice.setScaleY(1f);
                editMessage.setHint("Спроси Lumi или опиши задачу…");
                editMessage.setText(recognizedText);
                editMessage.setSelection(recognizedText.length());
            }

            @Override
            public void onError(String errorMessage) {
                btnVoice.setScaleX(1f);
                btnVoice.setScaleY(1f);
                editMessage.setHint("Спроси Lumi или опиши задачу…");
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void startVoiceRecognition() {
        if (voiceHelper != null) {
            voiceHelper.startListening();
        }
    }

    private void startNewChat() {
        currentChatId = -1;
        List<MessageEntity> welcome = new ArrayList<>();
        welcome.add(new MessageEntity(-1, "Привет! Я **LUMI AI**. Готов решить любую задачу по урокам, разобрать фото тетради, нарисовать картинку или создать видео.", false, null, "TEXT", System.currentTimeMillis()));
        chatAdapter.setMessages(welcome);
    }

    private void loadHistoryList() {
        executor.execute(() -> {
            List<ChatEntity> chats = db.chatDao().getAllChats();
            mainHandler.post(() -> historyAdapter.setChats(chats));
        });
    }

    private void loadChatMessages(long chatId) {
        currentChatId = chatId;
        executor.execute(() -> {
            List<MessageEntity> msgs = db.chatDao().getMessagesForChat(chatId);
            mainHandler.post(() -> {
                chatAdapter.setMessages(msgs);
                if (!msgs.isEmpty()) {
                    recyclerMessages.scrollToPosition(msgs.size() - 1);
                }
            });
        });
    }

    private void handleSendMessage() {
        String text = editMessage.getText().toString().trim();
        Uri imageToSend = attachedImageUri;
        if (text.isEmpty() && imageToSend == null) return;

        editMessage.setText("");
        attachedImageUri = null;
        previewContainer.setVisibility(View.GONE);
        updateSendButtonVisibility();

        int selectedModeId = chipGroupModes.getCheckedChipId();
        String mediaUriString = imageToSend != null ? imageToSend.toString() : null;

        executor.execute(() -> {
            if (currentChatId == -1) {
                String title = text.isEmpty() ? "Задача по фото" : (text.length() > 35 ? text.substring(0, 35) + "…" : text);
                currentChatId = db.chatDao().insertChat(new ChatEntity(title, System.currentTimeMillis()));
                loadHistoryList();
            }

            MessageEntity userMsg = new MessageEntity(currentChatId, text, true, mediaUriString, imageToSend != null ? "IMAGE" : "TEXT", System.currentTimeMillis());
            db.chatDao().insertMessage(userMsg);

            mainHandler.post(() -> {
                chatAdapter.addMessage(userMsg);
                scrollToBottom();
                showThinkingIndicator(selectedModeId);
            });

            if (selectedModeId == R.id.chipModeImage) {
                requestImageGeneration(text);
            } else if (selectedModeId == R.id.chipModeVideo) {
                requestVideoGeneration(text);
            } else {
                requestSmartChat(text, imageToSend);
            }
        });
    }

    private void showThinkingIndicator(int modeId) {
        String initialStatus = "Думает…";
        if (modeId == R.id.chipModeImage) initialStatus = "Рисует…";
        if (modeId == R.id.chipModeVideo) initialStatus = "Создает видео…";

        chatAdapter.addMessage(new MessageEntity(currentChatId, initialStatus, false, null, "THINKING", System.currentTimeMillis()));
        scrollToBottom();

        thinkingMoreRunnable = () -> {
            String secondStatus = "Думает еще…";
            if (modeId == R.id.chipModeImage) secondStatus = "Добавляет детали…";
            if (modeId == R.id.chipModeVideo) secondStatus = "Рендерит кадры…";
            chatAdapter.updateThinkingStatus(secondStatus);
        };
        mainHandler.postDelayed(thinkingMoreRunnable, 5500);
    }

    private void hideThinkingIndicator() {
        if (thinkingMoreRunnable != null) {
            mainHandler.removeCallbacks(thinkingMoreRunnable);
        }
        chatAdapter.removeThinkingIndicator();
    }

    private void requestSmartChat(String prompt, Uri imageUri) {
        List<MessageEntity> history = db.chatDao().getMessagesForChat(currentChatId);
        List<GeminiRequest.Content> contents = new ArrayList<>();

        int start = Math.max(0, history.size() - 8);
        for (int i = start; i < history.size() - 1; i++) {
            MessageEntity m = history.get(i);
            if (m.text != null && !m.text.isEmpty()) {
                contents.add(m.isUser ? GeminiRequest.Content.fromUserText(m.text) : GeminiRequest.Content.fromModelText(m.text));
            }
        }

        if (imageUri != null) {
            String base64 = MediaStorageHelper.uriToBase64(this, imageUri);
            String finalPrompt = prompt.isEmpty() ? "Реши задание с этого фото подробно и до конца." : prompt;
            contents.add(GeminiRequest.Content.fromUserTextAndImage(finalPrompt, base64, "image/jpeg"));
        } else {
            contents.add(GeminiRequest.Content.fromUserText(prompt));
        }

        GeminiRequest request = new GeminiRequest(contents, false);
        ApiClient.getService().generateSmartContent(BuildConfig.GEMINI_API_KEY, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                String answer = (response.isSuccessful() && response.body() != null && response.body().getText() != null)
                        ? response.body().getText()
                        : "Ошибка сервера Google AI (код " + response.code() + "). Проверьте API-ключ.";
                saveAndShowLumiResponse(answer, null, "TEXT");
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                saveAndShowLumiResponse("Ошибка сети: " + t.getMessage(), null, "TEXT");
            }
        });
    }
    private void requestImageGeneration(String prompt) {
        List<GeminiRequest.Content> contents = new ArrayList<>();
        contents.add(GeminiRequest.Content.fromUserText(prompt));
        GeminiRequest request = new GeminiRequest(contents, true);

        ApiClient.getService().generateImage(BuildConfig.GEMINI_API_KEY, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getBase64Image() != null) {
                    executor.execute(() -> {
                        String path = MediaStorageHelper.saveBase64ImageToInternal(MainActivity.this, response.body().getBase64Image());
                        saveAndShowLumiResponse("Готово!", path, "IMAGE");
                    });
                } else {
                    saveAndShowLumiResponse("Не удалось сгенерировать изображение (код " + response.code() + ").", null, "TEXT");
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                saveAndShowLumiResponse("Ошибка сети при создании фото: " + t.getMessage(), null, "TEXT");
            }
        });
    }

    private void requestVideoGeneration(String prompt) {
        JsonObject instance = new JsonObject();
        instance.addProperty("prompt", prompt);
        JsonArray instances = new JsonArray();
        instances.add(instance);

        JsonObject body = new JsonObject();
        body.add("instances", instances);

        ApiClient.getService().startVideoGeneration(BuildConfig.GEMINI_API_KEY, body).enqueue(new Callback<VideoOperationResponse>() {
            @Override
            public void onResponse(Call<VideoOperationResponse> call, Response<VideoOperationResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().name != null) {
                    pollVideoOperation(response.body().name);
                } else {
                    saveAndShowLumiResponse("Генерация видео недоступна на текущем тарифе ключа (код " + response.code() + ").", null, "TEXT");
                }
            }

            @Override
            public void onFailure(Call<VideoOperationResponse> call, Throwable t) {
                saveAndShowLumiResponse("Ошибка запуска генерации видео: " + t.getMessage(), null, "TEXT");
            }
        });
    }

    private void pollVideoOperation(String operationName) {
        mainHandler.postDelayed(() -> {
            ApiClient.getService().checkVideoOperation(operationName, BuildConfig.GEMINI_API_KEY).enqueue(new Callback<VideoOperationResponse>() {
                @Override
                public void onResponse(Call<VideoOperationResponse> call, Response<VideoOperationResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        VideoOperationResponse op = response.body();
                        if (Boolean.TRUE.equals(op.done)) {
                            String uri = op.getVideoUri();
                            if (uri != null) {
                                downloadGeneratedVideo(uri);
                            } else {
                                saveAndShowLumiResponse("Рендеринг видео завершился с ошибкой.", null, "TEXT");
                            }
                        } else {
                            pollVideoOperation(operationName);
                        }
                    } else {
                        saveAndShowLumiResponse("Ошибка проверки статуса видео.", null, "TEXT");
                    }
                }

                @Override
                public void onFailure(Call<VideoOperationResponse> call, Throwable t) {
                    saveAndShowLumiResponse("Сбой сети при ожидании видео.", null, "TEXT");
                }
            });
        }, 10000);
    }

    private void downloadGeneratedVideo(String videoUrl) {
        ApiClient.getService().downloadVideoFile(videoUrl, BuildConfig.GEMINI_API_KEY).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executor.execute(() -> {
                        String path = MediaStorageHelper.saveVideoStreamToInternal(MainActivity.this, response.body());
                        saveAndShowLumiResponse("Видео готово!", path, "VIDEO");
                    });
                } else {
                    saveAndShowLumiResponse("Не удалось скачать готовое видео.", null, "TEXT");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                saveAndShowLumiResponse("Ошибка загрузки видеофайла.", null, "TEXT");
            }
        });
    }

    private void saveAndShowLumiResponse(String text, String mediaPath, String mediaType) {
        executor.execute(() -> {
            MessageEntity lumiMsg = new MessageEntity(currentChatId, text, false, mediaPath, mediaType, System.currentTimeMillis());
            db.chatDao().insertMessage(lumiMsg);
            mainHandler.post(() -> {
                hideThinkingIndicator();
                chatAdapter.addMessage(lumiMsg);
                scrollToBottom();
            });
        });
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            recyclerMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceHelper != null) {
            voiceHelper.destroy();
        }
    }
}
