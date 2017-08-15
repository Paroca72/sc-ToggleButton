package com.sccomponents.buttons.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sccomponents.buttons.ScPlayerButton;
import com.sccomponents.buttons.ScSwitch;
import com.sccomponents.buttons.ScToggleButton;

public class MainActivity extends AppCompatActivity {

    private ScPlayerButton player = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permission
        this.checkRunTimePermission();

        // Get the components
        this.player = (ScPlayerButton) this.findViewById(R.id.player);
        assert this.player != null;

        Button open = (Button) this.findViewById(R.id.open);
        assert open != null;

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });
    }

    private void checkRunTimePermission() {
        String[] permissionArrays = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionArrays, 0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK &&
                requestCode == 0) {
            Uri uri = data.getData();
            this.player.setSource(uri.toString());
        }
    }

}
