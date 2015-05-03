package lv.chernishenko.igor.drawmemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rey.material.app.Dialog;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.Slider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lv.chernishenko.igor.drawmemo.custom.DrawAreaView;
import lv.chernishenko.igor.drawmemo.utils.Utils;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();

    private static final int IMAGE_SELECT_REQUEST = 1234;
    private static final int CROP_IMAGE_REQUEST = 9876;

    private DrawAreaView drawArea;
    private ImageView backgroundView;

    private Paint lineColor;
    private Paint backgroundColor;

    private Dialog colorChooserDialog;
    private Dialog lineWidthChooserDialog;
    private SimpleDialog bgSourceChooserDialog;

    private View colorView;
    private Slider redSlider;
    private Slider greenSlider;
    private Slider blueSlider;

    private ImageView widthView;
    private Slider widthSlider;

    private boolean isEraserEnabled = false;
    private boolean colorForBrush = true;
    private Uri outputFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);

        int drawAreaSize = displaySize.x;

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawArea = (DrawAreaView) findViewById(R.id.draw_area);
        backgroundView = (ImageView) findViewById(R.id.bg_view);

        ViewGroup.LayoutParams lp = drawArea.getLayoutParams();
        lp.width = drawAreaSize;
        lp.height = drawAreaSize;
        drawArea.setLayoutParams(lp);
        backgroundView.setLayoutParams(lp);

        lineColor = new Paint();
        lineColor.setStrokeCap(Paint.Cap.ROUND);
        lineColor.setARGB(255, 0, 0, 0);

        backgroundColor = new Paint();
        backgroundColor.setARGB(255, 255, 255, 255);
        backgroundView.setBackgroundColor(backgroundColor.getColor());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.change_color) {
            displayColorChooserDialog(true);
            return true;
        } else if (id == R.id.change_size) {
            displayWidthChooserDialog();
            return true;
        } else if (id == R.id.eraser) {
            // We 'toggle' eraser.
            if (isEraserEnabled) {
                isEraserEnabled = false;
                item.setIcon(R.drawable.menu_erase);
            } else {
                isEraserEnabled = true;
                item.setIcon(R.drawable.menu_erase_active);
            }
            drawArea.setEraserMode(isEraserEnabled);
            return true;
        } else if (id == R.id.change_bg) {
            displayBgSourceChooserDialog();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // TODO: show warning about progress loss.
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_SELECT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, CropImageActivity.class);
                // We assume, that data comes from Camera
                boolean isCamera = true;
                if (data != null) {
                    // If we have data, we check for sure, did data come from Camera
                    isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
                }
                Uri selectedImageUri;
                // If data came from Camera
                if (isCamera) {
                    // We check, do we have it
                    if (data != null) {
                        // If yes, we use path from data
                        selectedImageUri = data.getData();
                    } else {
                        // If no, we use path created beforehand
                        selectedImageUri = outputFileUri;
                    }
                } else {
                    // Else we just get path from data
                    selectedImageUri = data.getData();
                }
                intent.setData(selectedImageUri);
                startActivityForResult(intent, CROP_IMAGE_REQUEST);
            }
        }
        if (requestCode == CROP_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                // If image was cropped successfully, we get it from storage
                // using key, that we receive in data
                String key = data.getStringExtra(CropImageActivity.BITMAP_KEY);
                Bitmap bitmap = Utils.getInstance().getBitmapFromStorage(key);
                backgroundView.setImageBitmap(bitmap);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Displays color chooser dialog.
     *
     * @param colorForBrush - if true, color will be set for brush. Else - for background.
     */
    private void displayColorChooserDialog(boolean colorForBrush) {
        this.colorForBrush = colorForBrush;
        // We check, if we already have instance of dialog
        if (colorChooserDialog == null) {
            // If no, we build it by using Dialog.Builder
            colorChooserDialog = new Dialog.Builder()
                    .title(getString(R.string.select_color))
                    .contentView(R.layout.dialog_color_chooser)
                    .positiveAction(getString(R.string.ok))
                    .negativeAction(getString(R.string.cancel))
                    .build(this);
            // By setting it cancelable we can close it by tapping outside area
            colorChooserDialog.cancelable(true);
            colorView = colorChooserDialog.findViewById(R.id.color_view);
            redSlider = (Slider) colorChooserDialog.findViewById(R.id.color_red_slider);
            greenSlider = (Slider) colorChooserDialog.findViewById(R.id.color_green_slider);
            blueSlider = (Slider) colorChooserDialog.findViewById(R.id.color_blue_slider);
            redSlider.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                @Override
                public void onPositionChanged(Slider slider, float v, float v2, int i, int i2) {
                    colorView.setBackgroundColor(getColor(redSlider, greenSlider, blueSlider));
                }
            });
            greenSlider.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                @Override
                public void onPositionChanged(Slider slider, float v, float v2, int i, int i2) {
                    colorView.setBackgroundColor(getColor(redSlider, greenSlider, blueSlider));
                }
            });
            blueSlider.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                @Override
                public void onPositionChanged(Slider slider, float v, float v2, int i, int i2) {
                    colorView.setBackgroundColor(getColor(redSlider, greenSlider, blueSlider));
                }
            });
            colorChooserDialog.positiveActionClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If color was set for brush
                    if (MainActivity.this.colorForBrush) {
                        // We change color of brush
                        drawArea.setDrawingColor(lineColor.getColor());
                    } else {
                        // Else we change color of background
                        backgroundView.setImageBitmap(null);
                        backgroundView.setBackgroundColor(backgroundColor.getColor());
                    }
                    colorChooserDialog.dismiss();
                }
            });
            colorChooserDialog.negativeActionClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    colorChooserDialog.dismiss();
                }
            });
        }
        // Before showing dialog we adjust color of demonstration view
        // and positions of sliders
        int color;
        if (this.colorForBrush) {
            color = lineColor.getColor();
        } else {
            color = backgroundColor.getColor();
        }
        colorView.setBackgroundColor(color);
        // Because we get color as single number, we need to use
        // binary shifts, to get corresponding value for each Slider
        redSlider.setValue((color >> 16) & 0xFF, false);
        greenSlider.setValue((color >> 8) & 0xFF, false);
        blueSlider.setValue((color >> 0) & 0xFF, false);
        colorChooserDialog.show();
    }

    /**
     * Creates color from Sliders' values.
     *
     * @param redSlider - slider for red color.
     * @param greenSlider - slider for green color.
     * @param blueSlider - slider for blue color.
     * @return int color. According to colorForBrush value, readjusts color
     * of brush or background.
     */
    private int getColor(Slider redSlider, Slider greenSlider, Slider blueSlider) {
        if (colorForBrush) {
            lineColor.setARGB(255, redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
            return lineColor.getColor();
        } else {
            backgroundColor.setARGB(255, redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
            return backgroundColor.getColor();
        }
    }

    /**
     * Displays brush/eraser width chooser dialog.
     */
    private void displayWidthChooserDialog() {
        // We check, if we already have instance of dialog
        if (lineWidthChooserDialog == null) {
            // If no, we build it by using Dialog.Builder
            lineWidthChooserDialog = new Dialog.Builder()
                    .title(getString(R.string.select_width))
                    .contentView(R.layout.dialog_width_chooser)
                    .positiveAction(getString(R.string.ok))
                    .negativeAction(getString(R.string.cancel))
                    .build(this);
            // By setting it cancelable we can close it by tapping outside area
            lineWidthChooserDialog.cancelable(true);
            // We create bitmap and canvas for brush width demonstration.
            final Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);
            widthView = (ImageView) lineWidthChooserDialog.findViewById(R.id.width_view);
            widthSlider = (Slider) lineWidthChooserDialog.findViewById(R.id.width_slider);
            showLineWidth(bitmap, canvas);
            widthSlider.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                @Override
                public void onPositionChanged(Slider slider, float v, float v1, int i, int i1) {
                    showLineWidth(bitmap, canvas);
                }
            });
            lineWidthChooserDialog.positiveActionClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawArea.setLineWidth(widthSlider.getValue());
                    lineWidthChooserDialog.dismiss();
                }
            });
            lineWidthChooserDialog.negativeActionClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lineWidthChooserDialog.dismiss();
                }
            });
        }
        lineWidthChooserDialog.show();
    }

    /**
     * Displays demonstration of brush/eraser width.
     *
     * @param bitmap
     * @param canvas
     */
    private void showLineWidth(Bitmap bitmap, Canvas canvas) {
        lineColor.setStrokeWidth(widthSlider.getValue());
        bitmap.eraseColor(getResources().getColor(android.R.color.transparent));
        canvas.drawLine(30, 50, 370, 50, lineColor);
        widthView.setImageBitmap(bitmap);
    }

    /**
     * Displays dialog with two options:
     *
     * (*) Set background color.
     * (*) Set background image.
     */
    private void displayBgSourceChooserDialog() {
        if (bgSourceChooserDialog == null) {
            bgSourceChooserDialog = (SimpleDialog) new SimpleDialog.Builder()
                    .title(getString(R.string.select_bg_source))
                    .positiveAction(getString(R.string.ok))
                    .negativeAction(getString(R.string.cancel))
                    .build(this);
            bgSourceChooserDialog.setCancelable(true);
            // We add choices and starting position as 0.
            bgSourceChooserDialog.items(new String[]{
                    getString(R.string.bg_source_color),
                    getString(R.string.bg_source_image)
            }, 0);
            bgSourceChooserDialog.positiveActionClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // We check if any of two options was selected.
                    // If yes, we react accordingly. Else we do nothing.
                    if (bgSourceChooserDialog.getSelectedIndex() == 0) {
                        displayColorChooserDialog(false);
                        bgSourceChooserDialog.dismiss();
                    } else if (bgSourceChooserDialog.getSelectedIndex() == 1) {
                        startImageSelection();
                        bgSourceChooserDialog.dismiss();
                    }
                }
            });
            bgSourceChooserDialog.negativeActionClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bgSourceChooserDialog.dismiss();
                }
            });
        }
        bgSourceChooserDialog.show();
    }

    /**
     * Starts image selection either from Camera, or from gallery, or any other app,
     * that can provide image selection functionality.
     */
    private void startImageSelection() {
        File root = new File(Utils.getInstance().getImagesFolder());
        root.mkdirs();
        String fileName = Utils.getInstance().getUniqueImageFilename(this) + ".jpg";
        File sdImageMainDirectory = new File(root, fileName);
        // We create default path beforehand in case, if camera won't return data to us.
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        List<Intent> cameraIntents = new ArrayList<Intent>();
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            String packageName = res.activityInfo.packageName;
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.select_source));

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, IMAGE_SELECT_REQUEST);
    }
}