package com.daiyan.dataencrypt;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import android.webkit.MimeTypeMap;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;



public class MainActivity extends AppCompatActivity {



    // Constants
    private static final int NO_FILE_SELECTED = 0;
    private static final int FILE_TYPE_IMAGE = 1;
    private static final int FILE_TYPE_AUDIO = 2;
    private static final int FILE_TYPE_VIDEO = 3;
    private static final int FILE_SELECT_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 123;

    // UI Elements
    private EditText EnterText;
    private TextView OutputText;
    private FloatingActionButton Encrypt;
    private ImageView imageView;

    // File Handling
    private Uri selectedFileUri;
    private Bitmap selectedBitmap;
    private int currentFileType = NO_FILE_SELECTED;
    String text;
    TextView Password;
    ImageView passwordOnOff;

    boolean changeToggle = false; // false = encryption | true = Decyption
    Button firstTextButton, secondTextButton;
    String firstText = "Normal Text", secondText = "Encrypted File", fixpassword;
    boolean passwordOnOffFlag = false;
    EditText password, confirm_password;
    ScrollView homeScrollView, starredScrollView, fixPassScrollView, filesScrollView;
    ImageView homeLogo, starredLogo, fixPassLogo, filesLogo;
    ImageView homeBackground, starredBackground, fixPassBackground, filesBackground;
    String page = "home";
    ListView listView, starredListView;
    ArrayList<String> fileNames = new ArrayList<>();
    ArrayList<String> starredFileNames = new ArrayList<>();
    File folder;
    String clickFileName;
    Button saveToStarredbtn, decodeRealTextbtn;
    ImageView closeFilebtn;
    ImageView closeFilebtn2;
    ImageView changeToggleButton;
    View home, starred, fixPass, files, toggleChange, decodeRealText;
    Button decodeRealTextbtn3, decodeRealTextSTR;
    String selectedFleRealName, selectedFleRealNameOlder, fileType;
    ImageView select, selectFile;
    ImageView homeTransparent, starredTransparant, fixpassTransparant, filesTransparant;
    TextView file_name;
    ImageView edit_output_text, close_image_button;
    File currentFile = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.status_bar_color));

        checkPermissions();
        initialization();
        onClickListener();

    }
    private void checkPermissions() {
        List<String> requiredPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            addPermissionIfNotGranted(requiredPermissions, Manifest.permission.READ_MEDIA_IMAGES);
            addPermissionIfNotGranted(requiredPermissions, Manifest.permission.READ_MEDIA_VIDEO);
            addPermissionIfNotGranted(requiredPermissions, Manifest.permission.READ_MEDIA_AUDIO);
            addPermissionIfNotGranted(requiredPermissions, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            addPermissionIfNotGranted(requiredPermissions, Manifest.permission.READ_MEDIA_IMAGES);
            addPermissionIfNotGranted(requiredPermissions, Manifest.permission.READ_MEDIA_VIDEO);
            addPermissionIfNotGranted(requiredPermissions, Manifest.permission.READ_MEDIA_AUDIO);
        } else { // Android 12 and below versions
            addPermissionIfNotGranted(requiredPermissions, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                addPermissionIfNotGranted(requiredPermissions, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!requiredPermissions.isEmpty()) {
            requestPermissions(requiredPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }

        // Android 11+ MANAGE_EXTERNAL_STORAGE request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
    }
        private void addPermissionIfNotGranted(List<String> list, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            list.add(permission);
        }
    }
    public void initialization(){
        // Initialize UI
        EnterText = findViewById(R.id.EnterText);
        OutputText = findViewById(R.id.OutputText);
        edit_output_text = findViewById(R.id.edit_output_text);
        Encrypt = findViewById(R.id.next);
        imageView = findViewById(R.id.imageView);
        Password = findViewById(R.id.password);
        firstTextButton = findViewById(R.id.firstTextButton);
        secondTextButton = findViewById(R.id.secondTextButton);
        firstTextButton.setText(firstText);
        secondTextButton.setText(secondText);
        OutputText.setVisibility(View.GONE);
        edit_output_text.setVisibility(View.GONE);
        passwordOnOff = findViewById(R.id.passwordOnOff);
        homeScrollView = findViewById(R.id.homeScrollView);
        starredScrollView = findViewById(R.id.starredScrollView);
        fixPassScrollView = findViewById(R.id.fixPassScrollView);
        filesScrollView = findViewById(R.id.filesScrollView);
        homeScrollView.setVisibility(View.VISIBLE);
        starredScrollView.setVisibility(View.GONE);
        fixPassScrollView.setVisibility(View.GONE);
        filesScrollView.setVisibility(View.GONE);
        password = findViewById(R.id.passwordbox);
        confirm_password = findViewById(R.id.confirmpasswordbox);
        homeLogo = findViewById(R.id.homeLogo);
        starredLogo = findViewById(R.id.starredLogo);
        fixPassLogo = findViewById(R.id.fixPassLogo);
        filesLogo = findViewById(R.id.filesLogo);
        homeBackground = findViewById(R.id.homeBackground);
        starredBackground = findViewById(R.id.starredBackground);
        fixPassBackground = findViewById(R.id.fixPassBackground);
        filesBackground = findViewById(R.id.filesBackground);
        saveToStarredbtn = findViewById(R.id.saveToStarredbtn);
        decodeRealTextbtn = findViewById(R.id.decodeRealTextbtn);
        firstTextButton = findViewById(R.id.firstTextButton);
        secondTextButton = findViewById(R.id.secondTextButton);
        decodeRealTextbtn3 = findViewById(R.id.decodeRealTextbtn3);
        listView = findViewById(R.id.listView);
        starredListView = findViewById(R.id.starredListView);
        closeFilebtn = findViewById(R.id.closeFilebtn);
        closeFilebtn2 = findViewById(R.id.closeFilebtn2);
        decodeRealTextSTR = findViewById(R.id.decodeRealTextbtn3);
        changeToggleButton = findViewById(R.id.changeToggle);
        homeTransparent = findViewById(R.id.homeTransparent);
        starredTransparant = findViewById(R.id.starredTransparant);
        fixpassTransparant = findViewById(R.id.fixpassTransparant);
        filesTransparant = findViewById(R.id.filesTransparant);
        selectFile = findViewById(R.id.Select);
        file_name = findViewById(R.id.file_name);
        file_name.setVisibility(View.GONE);
        close_image_button = findViewById(R.id.imageView3);
        close_image_button.setVisibility(View.GONE);


        folder = new File(Environment.getExternalStorageDirectory(), "StegX Encryption");
        if (!folder.exists()) {folder.mkdirs();}
    }
    public void onClickListener(){

        // FOR File page file OnClick
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String fileName = fileNames.get(position);
            fileClicked(fileName);
            clickFileName = fileName;
            closeFilebtn.setVisibility(View.VISIBLE);
            saveToStarredbtn.setVisibility(View.VISIBLE);
            decodeRealTextbtn.setVisibility(View.VISIBLE);
            select = findViewById(R.id.Select);
        });
        // FOR Starred page file OnClick
        starredListView.setOnItemClickListener((parent, view, position, id) -> {
            String fileName = starredFileNames.get(position);
            showToast(fileName);
            clickFileName = fileName;
            decodeRealTextbtn3.setVisibility(View.VISIBLE);
            closeFilebtn2.setVisibility(View.VISIBLE);
        });

        // home
        passwordOnOff.setOnClickListener(v->passwordOnOff(v));
        changeToggleButton.setOnClickListener(v->changeToggle(v));
        selectFile.setOnClickListener(v->selectFile(v));
        edit_output_text.setOnClickListener(v->edit_output_text(v));
        close_image_button.setOnClickListener(v->close_image_button(v));

        //starred
        decodeRealTextbtn3.setOnClickListener(v->decodeRealText(v));
        closeFilebtn2.setOnClickListener(v->closeFile2(v));

        //files
        closeFilebtn.setOnClickListener(v->closeFile(v));
        decodeRealTextbtn.setOnClickListener(v->decodeRealText(v));
        saveToStarredbtn.setOnClickListener(v->saveToStarred(v));

        //comman
        Encrypt.setOnClickListener(v->next(v));
        homeTransparent.setOnClickListener(v->home(v));
        starredTransparant.setOnClickListener(v->starred(v));
        fixpassTransparant.setOnClickListener(v->fixPassword(v));
        filesTransparant.setOnClickListener(v->files(v));

    }
        public void fileClicked(String fileName) {


    }
    public void passwordOnOff(View view) {
        fixpassword = fileHandler.readFromFile(view.getContext(), "password.txt");
        if (fixpassword == null) {
            Password.setVisibility(View.VISIBLE);
            showToast("No Password saved Yet");
            passwordOnOffFlag = false;
            return;
        }

        if (passwordOnOffFlag == false) {
            passwordOnOff.setImageResource(R.drawable.lock_icon);
            Password.setText(fixpassword);
            Password.setVisibility(View.GONE);
            showToast("Fix Password is Applied on Your Entered Text");
            passwordOnOffFlag = true;
        } else {
            passwordOnOff.setImageResource(R.drawable.unlock_icon);
            Password.setText("");
            Password.setVisibility(View.VISIBLE);
            passwordOnOffFlag = false;
        }
    }
    public void changeToggle(View view) {
        toggleChange = view;
        if (changeToggle == false) {
            EnterText.setHint("Select a Image Or Audio File");
            EnterText.setEnabled(false);
            OutputText.setHint("Normal text will appear here");
            firstTextButton.setText(secondText);
            secondTextButton.setText(firstText);
            changeToggle = true;
        } else {
            EnterText.setHint("Enter text");

            file_name.setText("");
            file_name.setVisibility(View.GONE);
            imageView.setImageBitmap(null);
            imageView.setVisibility(View.GONE);
            close_image_button.setVisibility(View.GONE);

            EnterText.setEnabled(true);
            OutputText.setHint("Encrypted output will appear here");
            firstTextButton.setText(firstText);
            secondTextButton.setText(secondText);
            changeToggle = false;
        }
    }
    public void selectFile(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "audio/*",
//                "video/*"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, FILE_SELECT_CODE);
    }
        public void visibleselect(View view) {
        if (select.getVisibility() == View.VISIBLE) {
            return;
        } else if (select.getVisibility() == View.GONE) {
            select.setVisibility(View.VISIBLE);
        } else if (select.getVisibility() == View.INVISIBLE) {
            return;
        }
    }
    public String getMimeType(File file) {
        if (file == null || !file.exists()) {
            return null;
        }

        String path = file.getAbsolutePath();
        // Path se extension nikalne ka sabse desi aur effective tareeka
        int lastDot = path.lastIndexOf('.');

        if (lastDot != -1 && lastDot < path.length() - 1) {
            String extension = path.substring(lastDot + 1).toLowerCase();

            // Android ka standard MimeTypeMap use karein
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            // Agar MimeTypeMap fail ho jaye (kuch naye formats ke liye),
            // toh hum khud return kar sakte hain
            if (mime != null) {
                return mime;
            }
        }

        return null;
    }
    public void setCurrentFile(File file){
        currentFile = file;
    }
    public File getCurrentFile(){
        return currentFile;
    }
    public void decodeRealText(View view) {
        showToast(clickFileName);
        decodeRealText = view;
        File file = new File(folder, clickFileName);
        setCurrentFile(file);
        fileType = getFileExtension(clickFileName);



        String contentType = getMimeType(file);
        if (contentType != null) {
            if (contentType.startsWith("image")) {

                if (page.equals("decodeRealTextFlag")) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    selectedBitmap = bitmap;
                    decodeImage();
                    OutputText.setVisibility(View.VISIBLE);
                    edit_output_text.setVisibility(View.VISIBLE);
//                    imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));

                    return;
                }
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                selectedBitmap = bitmap;
                imageView.setImageBitmap(selectedBitmap);

            } else if (contentType.startsWith("audio")) {

                if (page.equals("decodeRealTextFlag")) {
                    //passing name of the file
                    selectedFileUri = Uri.fromFile(file);
                    selectedFleRealName = getFileNameFromUri(selectedFileUri);
                    showToast("i was in audio and in if ");
                    AudioUtils.setSelectedFileRealName(selectedFleRealName);
                    decodeAudio();
                    OutputText.setVisibility(View.VISIBLE);
                    edit_output_text.setVisibility(View.VISIBLE);

                    return;
                }
                imageView.setImageResource(R.drawable.ic_file_preview);


            }



            home(home);
            if(changeToggle==false){changeToggle(toggleChange);}
            file_name.setText(clickFileName);
            imageView.setVisibility(View.VISIBLE);
            file_name.setVisibility(View.VISIBLE);
            close_image_button.setVisibility(View.VISIBLE);

            EnterText.setHint("Select a Image Or Audio File");
            EnterText.setEnabled(false);
            EnterText.setText("");
            page = "decodeRealTextFlag";
        }


//        if (fileType.equals("png") || fileType.equals("jpg") || fileType.equals("jpeg") || fileType.equals("webp") || fileType.equals("gif") || fileType.equals("bmp")) {
//            }
    }
        private String getFileExtension(String fileName) {
        if (fileName.lastIndexOf('.') > 0) {
            return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        }
        return "png";
    }
    public void closeFile2(View view) {
        decodeRealTextbtn3.setVisibility(View.GONE);
        closeFilebtn2.setVisibility(View.GONE);
    }
    public void closeFile(View view) {
        closeFilebtn.setVisibility(View.GONE);
        saveToStarredbtn.setVisibility(View.GONE);
        decodeRealTextbtn.setVisibility(View.GONE);
    }
    public void saveToStarred(View view) {
        for (String element : starredFileNames) {
            if (element.equals(clickFileName)) {
                showToast("File Already Saved");
                return;
            }
        }
        showToast(clickFileName);
        starredFileNames.add(clickFileName);
        closeFilebtn.setVisibility(View.GONE);
        saveToStarredbtn.setVisibility(View.GONE);
        decodeRealTextbtn.setVisibility(View.GONE);
    }
    public void next(View view) {
        switch (page) {
            case "decodeRealTextFlag":
                decodeRealText(decodeRealText);
                break;
            case "home":
                OutputText.setVisibility(View.VISIBLE);
                edit_output_text.setVisibility(View.VISIBLE);

                if (changeToggle == true) {
                    decodeFile();
                } else {
                    encodeFile();
                }
                break;
            case "fixPass":
                if (password.getText().toString().equals(confirm_password.getText().toString())) {
                    fileHandler.saveToFile(view.getContext(), "password.txt", password.getText().toString());
                    Toast.makeText(view.getContext(), "Password Saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(view.getContext(), "Password Mismatch", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    private void decodeFile() {
        if (Password.getText().toString().trim().isEmpty()) {
            showToast("Please Enter Password");
            return;
        }
        if(currentFileType == NO_FILE_SELECTED){showToast("Please Select a Image/Audio File to Dencrypt Data from it");}

        new Thread(() -> {
            try {
                switch (currentFileType) {
                    case NO_FILE_SELECTED:
//                        if (EnterText.getText().toString().trim().isEmpty()) {
//                            showToast("Please Enter Text");
//                            return;
//                        }
//                        TextEncrypt textStegX = new TextEncrypt(EnterText.getText().toString(), Password.getText().toString());
//                        text = textStegX.decrypt();
//                        runOnUiThread(() -> OutputText.setText(text));
//                        break;
                    case FILE_TYPE_IMAGE:
                        decodeImage();
                        break;
                    case FILE_TYPE_AUDIO:
                        decodeAudio();
                        break;
//                    case FILE_TYPE_VIDEO:
//                        decodeVideo();
//                        break;
                }
            } catch (Exception e) {
                showToast("Decoding error: " + e.getMessage());
            }
        }).start();
    }
        private void decodeImage() {
        if (selectedBitmap == null) {
            showToast("No image selected");
            return;
        }
        try {
            String decodedMessage = ImageEncrypt.decodeMessage(selectedBitmap);
            TextEncrypt textStegX = new TextEncrypt(decodedMessage, Password.getText().toString());
            text = textStegX.decrypt();
            runOnUiThread(() -> OutputText.setText(text));
        } catch (Exception e) {
            showToast("Error decoding image: " + e.getMessage());
        }
    }
        private void decodeAudio() {
        if (selectedFileUri == null) {
            showToast("No audio file selected");
            return;
        }
        try {
            byte[] audioData = AudioUtils.loadAudio(this, selectedFileUri);
            String decodedMessage = AudioEncrypt.decodeAudio(audioData);
            TextEncrypt textStegX = new TextEncrypt(decodedMessage, Password.getText().toString());
            text = textStegX.decrypt();
            runOnUiThread(() -> OutputText.setText(text));
        } catch (IOException e) {
            showToast("Error decoding audio: " + e.getMessage());
        }
    }
        private void decodeVideo() {
        if (selectedFileUri == null) {
            showToast("No video file selected");
            return;
        }
        try {
            List<Bitmap> frames = VideoUtils.loadVideoFrames(this, selectedFileUri);
            String decodedMessage = VideoEncrypt.decodeVideoFrames(frames);
            TextEncrypt textStegX = new TextEncrypt(decodedMessage, Password.getText().toString());
            text = textStegX.decrypt();
            runOnUiThread(() -> OutputText.setText(text));
        } catch (Exception e) {
            showToast("Error decoding video: " + e.getMessage());
        }
    }
    private void encodeFile() {
        if(currentFileType == NO_FILE_SELECTED){showToast("Please Select a Image/Audio File to Encrypt Data in it");}
        if (!EnterText.getText().toString().trim().isEmpty() && !Password.getText().toString().trim().isEmpty()) {
            new Thread(() -> {
                try {
                    TextEncrypt textStegX = new TextEncrypt(EnterText.getText().toString(), Password.getText().toString());
                    text = textStegX.encrypt();
                    switch (currentFileType) {
//                        case NO_FILE_SELECTED:
//                            runOnUiThread(() -> OutputText.setText(text));
//                            break;
                        case FILE_TYPE_IMAGE:
                            encodeImage(text);
                            break;
                        case FILE_TYPE_AUDIO:
                            encodeAudio(text);
                            break;
//                        case FILE_TYPE_VIDEO:
//                            encodeVideo(text);
//                            break;
                    }
                } catch (Exception e) {
                    showToast("Encoding error: " + e.getMessage());
                }
            }).start();
        } else {
            showToast("Please Enter Text & Password");
        }
    }
        private void encodeImage(String message) throws IOException {

        if (selectedBitmap == null) {
            showToast("No image selected");
            return;
        }
        Bitmap encodedBitmap = ImageEncrypt.encodeMessage(selectedBitmap, message);
        saveEncodedImage(encodedBitmap);
        showToast("Image encoded successfully");
    }
            private void saveEncodedImage(Bitmap bitmap) throws IOException {
        // CORRECTED: Saving directly to Internal Root folder
        String fileName = "stegx_" + System.currentTimeMillis() + ".png";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // All Files Access granted: Save directly to root
                File file = new File(folder, fileName);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    showToast("Saved to Internal Root: StegX Encryption/" + fileName);
                }
            } else {
                // Fallback for Android 11+ if MANAGE_EXTERNAL_STORAGE not enabled
                showToast("All Files Access NOT enabled. Check Settings.");
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } else {
            // Android 9 and 10 behavior
            File file = new File(folder, (selectedFleRealNameOlder != null ? selectedFleRealNameOlder : fileName));
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                showToast("Image saved to StegX Encryption");
            }
        }
    }
    private void encodeAudio(String message) {
        if (selectedFileUri == null) {
            showToast("No audio file selected");
            return;
        }
        try {
            // UI Update hamesha Main Thread par hona chahiye!
            runOnUiThread(() -> {
                imageView.setImageResource(R.drawable.ic_file_preview);
            });

            byte[] audioData = AudioUtils.loadAudio(this, selectedFileUri);
            byte[] encodedAudio = AudioEncrypt.encodeAudio(audioData, message);
            String outputPath = AudioUtils.saveAudio(this, encodedAudio);

            // Success message UI thread par run karne ke liye showToast already handled hai
            showToast("Audio encoded successfully");

        } catch (Exception e) {
            showToast("Error encoding audio: " + e.getMessage());
        }
    }
        private void encodeVideo(String message) {
        if (selectedFileUri == null) {
            showToast("No video file selected");
            return;
        }
        try {
            List<Bitmap> frames = VideoUtils.loadVideoFrames(this, selectedFileUri);
            List<Bitmap> encodedFrames = VideoEncrypt.encodeVideoFrames(frames, message);
            String outputPath = VideoUtils.saveVideo(this, encodedFrames);
            showToast("Video encoded successfully");
        } catch (Exception e) {
            showToast("Error encoding video: " + e.getMessage());
        }
    }
    public void home(View view) {
        home = view;
        page = "home";
        homeScrollView.setVisibility(View.VISIBLE);
        starredScrollView.setVisibility(View.GONE);
        fixPassScrollView.setVisibility(View.GONE);
        filesScrollView.setVisibility(View.GONE);

        homeLogo.setImageResource(R.drawable.clicked_home);
        starredLogo.setImageResource(R.drawable.starred);
        fixPassLogo.setImageResource(R.drawable.fix_password);
        filesLogo.setImageResource(R.drawable.files);

        homeBackground.setImageResource(R.drawable.edittext_padding);
        starredBackground.setImageResource(R.drawable.transparent);
        fixPassBackground.setImageResource(R.drawable.transparent);
        filesBackground.setImageResource(R.drawable.transparent);
        Encrypt.setVisibility(View.VISIBLE);
        EnterText.setVisibility(View.VISIBLE);

        if(changeToggle == true){EnterText.setEnabled(false);}
        else{EnterText.setEnabled(true);}

    }
    public void starred(View view) {
        starred = view;
        page = "starred";
        homeScrollView.setVisibility(View.GONE);
        starredScrollView.setVisibility(View.VISIBLE);
        fixPassScrollView.setVisibility(View.GONE);
        filesScrollView.setVisibility(View.GONE);

        homeLogo.setImageResource(R.drawable.home);
        starredLogo.setImageResource(R.drawable.clicked_starred);
        fixPassLogo.setImageResource(R.drawable.fix_password);
        filesLogo.setImageResource(R.drawable.files);

        homeBackground.setImageResource(R.drawable.transparent);
        starredBackground.setImageResource(R.drawable.edittext_padding);
        fixPassBackground.setImageResource(R.drawable.transparent);
        filesBackground.setImageResource(R.drawable.transparent);
        Encrypt.setVisibility(View.GONE);

        decodeRealTextbtn3.setVisibility(View.GONE);
        closeFilebtn2.setVisibility(View.GONE);

        ArrayAdapter<String> starredAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, starredFileNames);
        starredListView.setAdapter(starredAdapter);
    }
    public void fixPassword(View view) {
        fixPass = view;
        page = "fixPass";
        homeScrollView.setVisibility(View.GONE);
        starredScrollView.setVisibility(View.GONE);
        fixPassScrollView.setVisibility(View.VISIBLE);
        filesScrollView.setVisibility(View.GONE);

        homeLogo.setImageResource(R.drawable.home);
        starredLogo.setImageResource(R.drawable.starred);
        fixPassLogo.setImageResource(R.drawable.clicked_fix_password);
        filesLogo.setImageResource(R.drawable.files);

        homeBackground.setImageResource(R.drawable.transparent);
        starredBackground.setImageResource(R.drawable.transparent);
        fixPassBackground.setImageResource(R.drawable.edittext_padding);
        filesBackground.setImageResource(R.drawable.transparent);
        Encrypt.setVisibility(View.VISIBLE);
    }
    public void files(View view) {
        files = view;
        page = "files";
        homeScrollView.setVisibility(View.GONE);
        starredScrollView.setVisibility(View.GONE);
        fixPassScrollView.setVisibility(View.GONE);
        filesScrollView.setVisibility(View.VISIBLE);

        homeLogo.setImageResource(R.drawable.home);
        starredLogo.setImageResource(R.drawable.starred);
        fixPassLogo.setImageResource(R.drawable.fix_password);
        filesLogo.setImageResource(R.drawable.clicked_files);

        homeBackground.setImageResource(R.drawable.transparent);
        starredBackground.setImageResource(R.drawable.transparent);
        fixPassBackground.setImageResource(R.drawable.transparent);
        filesBackground.setImageResource(R.drawable.edittext_padding);

        closeFilebtn.setVisibility(View.GONE);
        saveToStarredbtn.setVisibility(View.GONE);
        decodeRealTextbtn.setVisibility(View.GONE);

        Encrypt.setVisibility(View.GONE);

        fileNames = new ArrayList<>();
        if (folder.exists() && folder.isDirectory()) {
            File[] fileList = folder.listFiles();
            if (fileList != null) {
                for (File f : fileList) {
                    if (f.isFile()) fileNames.add(f.getName());
                }
            } else {
                showToast("No files found.");
            }
        } else {
            showToast("Folder does not exist.");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNames);
        listView.setAdapter(adapter);
    }

    public void edit_output_text(View view){
        if(!(OutputText.getText().toString().equals("WRONG PASSWORD") )){
            EnterText.setText(OutputText.getText().toString());
            EnterText.setEnabled(true);
            OutputText.setText("");
            OutputText.setVisibility(View.GONE);
            edit_output_text.setVisibility(View.GONE);
            if(changeToggle==true){changeToggle(toggleChange);}
            page = "home";


            String contentType = getMimeType(getCurrentFile());
            if (contentType != null) {
                if (contentType.startsWith("image")) {
                    currentFileType = FILE_TYPE_IMAGE;

                } else if (contentType.startsWith("audio"))
                    currentFileType = FILE_TYPE_AUDIO;

            }

            }
    }
    public void close_image_button(View view){
        imageView.setImageBitmap(null);
        imageView.setVisibility(View.GONE);
        file_name.setVisibility(View.GONE);
        close_image_button.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null || data.getData() == null) {
                showToast("Error selecting file");
                return;
            }
            selectedFileUri = data.getData();

            // Yahan se hamara smart helper function call hoga
            selectedFleRealName = getFileNameFromUri(selectedFileUri);
            AudioUtils.setSelectedFileRealName(selectedFleRealName);
            selectedFleRealNameOlder = selectedFleRealName;
            handleFileSelection();


            file_name.setText(getFileNameFromUri((selectedFileUri)));
            file_name.setVisibility(View.VISIBLE);
            close_image_button.setVisibility(View.VISIBLE);

            ContentResolver resolver = getContentResolver();
            String mimeType = resolver.getType(selectedFileUri);
            if (mimeType == null) {
                showToast("Unsupported file type");
                return;
            }
            if (mimeType.startsWith("image/")) {
                imageView.setImageBitmap(selectedBitmap);

            } else if (mimeType.startsWith("audio/")) {
                imageView.setImageResource(R.drawable.ic_file_preview);
            }
            imageView.setVisibility(View.VISIBLE);
        }
    }
        private String getFileNameFromUri(Uri uri) {
        String result = null;

        // Step 1: Tumhari null-safe scheme checking aur cursor reading
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = this.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                // Exception ko handle karna zaroori hai taaki IO block hone par app crash na ho
                e.printStackTrace();
            }
        }

        // Step 2: Fallback agar content provider ne naam nahi diya
        if (result == null) {
            String path = uri.getPath();
            int lastSlash = path != null ? path.lastIndexOf('/') : -1;
            result = (lastSlash != -1) ? path.substring(lastSlash + 1) : "unknown_file";
        }

        // Step 3: Innovation layer (Dynamic Extension Fallback)
        // Agar existing naam mein dot (.) nahi hai, toh system se actual extension nikal kar jodo
        if (result != null && !result.contains(".")) {
            String mimeType = this.getContentResolver().getType(uri);
            if (mimeType != null) {
                String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                if (extension != null) {
                    result += "." + extension;
                }
            }
        }

        return result;
    }
        private void handleFileSelection() {
        ContentResolver resolver = getContentResolver();
        String mimeType = resolver.getType(selectedFileUri);
        if (mimeType == null) {
            showToast("Unsupported file type");
            return;
        }
        if (mimeType.startsWith("image/")) {
            currentFileType = FILE_TYPE_IMAGE;
            loadImageFile();
        } else if (mimeType.startsWith("audio/")) {
            currentFileType = FILE_TYPE_AUDIO;
            showFilePreview("Audio file selected");
        }
//        else if (mimeType.startsWith("video/")) {
//            currentFileType = FILE_TYPE_VIDEO;
//            showFilePreview("Video file selected");
//        }
        else {
            showToast("Unsupported file format");
        }
    }


    public String getFileNameFromUri(Context context, Uri uri) {
        String fileName = null;

        // Check if it is a content URI (used in modern Android)
        if ("content".equals(uri.getScheme())) {
            // try-with-resources ensures the cursor is closed automatically
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        fileName = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Hamesha proper logging use karo production mein
            }
        }

        // Fallback logic for normal file:// URIs ya agar content resolver fail ho jaye
        if (fileName == null) {
            fileName = uri.getPath();
            if (fileName != null) {
                int cut = fileName.lastIndexOf('/');
                if (cut != -1) {
                    fileName = fileName.substring(cut + 1);
                }
            }
        }

        return fileName;
    }

            private void loadImageFile() {
        new Thread(() -> {
            try (InputStream inputStream = getContentResolver().openInputStream(selectedFileUri)) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                options.inSampleSize = calculateInSampleSize(options, 800, 600);
                options.inJustDecodeBounds = false;
                try (InputStream stream = getContentResolver().openInputStream(selectedFileUri)) {
                    selectedBitmap = BitmapFactory.decodeStream(stream, null, options);
                    runOnUiThread(() -> {
                        if (selectedBitmap != null) {
                            imageView.setImageBitmap(selectedBitmap);
                            imageView.setVisibility(View.VISIBLE);
                            String actualFileName = getFileNameFromUri(this, selectedFileUri);
                            file_name.setText(actualFileName);
                            file_name.setVisibility(View.VISIBLE);
                            close_image_button.setVisibility(View.VISIBLE);
                        } else {
                            showToast("Failed to load image");
                        }
                    });
                }
            } catch (IOException e) {
                showToast("Error loading file: " + e.getMessage());
            }
        }).start();
    }
                private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
            private void showFilePreview(String message) {
        runOnUiThread(() -> {
            imageView.setImageResource(R.drawable.ic_file_preview);
            showToast(message);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    showToast("Permission required: " + permissions[i]);
                }
            }
        }
    }
    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
    }



}