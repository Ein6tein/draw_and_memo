package lv.chernishenko.igor.drawmemo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.theartofdev.edmodo.cropper.CropImageView;

import lv.chernishenko.igor.drawmemo.utils.Utils;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class CropImageActivity extends ActionBarActivity {

    private static final String TAG = CropImageActivity.class.getCanonicalName();

    public static final String BITMAP_KEY = "bitmap_key";

    private CropImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.crop_image_activity_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        imageView = (CropImageView) findViewById(R.id.crop_image_view);
        Cursor cursor = getContentResolver().query(getIntent().getData(),
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
                null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int rotate = cursor.getInt(0);
            if (rotate != -1) {
                imageView.rotateImage(rotate);
            }
        }
        imageView.setImageUri(getIntent().getData());

        findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedImage = imageView.getCroppedImage();
                String key = Utils.getInstance().getUniqueImageFilename(CropImageActivity.this);
                Utils.getInstance().storeBitmap(key, croppedImage);
                closeCropper(key);
            }
        });
        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeCropper(String key) {
        Intent data = new Intent();
        data.putExtra(BITMAP_KEY, key);
        setResult(RESULT_OK, data);
        finish();
    }
}