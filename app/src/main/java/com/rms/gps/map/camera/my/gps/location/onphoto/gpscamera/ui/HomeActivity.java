package com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.ui;

import static android.content.Intent.createChooser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.BuildConfig;
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.databinding.ActivityHomeBinding;
import com.rms.gps.map.camera.my.gps.location.onphoto.gpscamera.model.FileUtils;

import java.io.File;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityHomeBinding binding;
    private View clickedView;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImage;
    private ActivityResultLauncher<Intent> forResultLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    for (String permission : result.keySet()) {
                        if (Boolean.FALSE.equals(result.get(permission))) {
                            return;
                        }
                    }
                    if (clickedView != null)
                        clickedView.performClick();
                }
        );

        forResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
            if(result.getResultCode() == RESULT_OK){
                var intent = result.getData();
                if(intent!=null) {
                    var data = intent.getData();
                    processUri(data);
                }
            }
        });

        pickImage = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri->{
            getContentResolver().takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION );
            processUri(uri);
        });

        binding.gpsCam.setOnClickListener(this);
        binding.gpsVideo.setOnClickListener(this);
        binding.setTemplate.setOnClickListener(this);
        binding.editLocation.setOnClickListener(this);
        binding.savedStuff.setOnClickListener(this);
        binding.showOnMap.setOnClickListener(this);



    }

    private void processUri(Uri data) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                data = MediaStore.getMediaUri(this, data);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        String path = FileUtils.getPath(this,data);
        if(path!=null) {
            File file = new File(path);
            if (file.exists())
                data = FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                );
            else Log.d("HomeActivity", "onCreate: file at path does not exist "+path);
        }else Log.d("HomeActivity", "onCreate: path is null ");
        if (clickedView != null) {
            if (clickedView.getId() == binding.showOnMap.getId())
                startActivity(new Intent(this, ShowOnMapActivity.class)
                        .putExtra("image_selected", data.toString())
                        .putExtra("path", path));
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        clickedView = v;

        if (Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (v.getId() == binding.gpsCam.getId()) {
                startActivity(new Intent(this, CamActivity.class));
            } else if (v.getId() == binding.showOnMap.getId()) {
                Toast.makeText(this, "Pick an Image to get it's location", Toast.LENGTH_SHORT).show();
                /*pickImage.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                );*/
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                //intent.setDataAndType( android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                forResultLauncher.launch(createChooser(intent, "My Title"));
            } else if (v.getId() == binding.gpsVideo.getId()) {
                startActivity(new Intent(this, CamcorderActivity.class));
            }
        } else {
            var permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
            };
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permissions = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.ACCESS_MEDIA_LOCATION
                };
            } else {
                permissions = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.ACCESS_MEDIA_LOCATION
                };
            }
            permissionLauncher.launch(
                    permissions
            );
        }
    }
}
