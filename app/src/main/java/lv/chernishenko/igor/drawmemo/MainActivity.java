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
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_SELECT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, CropImageActivity.class);
                boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
                }
                Uri selectedImageUri;
                if (isCamera) {
                    if (data != null) {
                        selectedImageUri = data.getData();
                    } else {
                        selectedImageUri = outputFileUri;
                    }
                } else {
                    selectedImageUri = data.getData();
                }
                intent.setData(selectedImageUri);
                startActivityForResult(intent, CROP_IMAGE_REQUEST);
            }
        }
        if (requestCode == CROP_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                String key = data.getStringExtra(CropImageActivity.BITMAP_KEY);
                Bitmap bitmap = Utils.getInstance().getBitmapFromStorage(key);
                backgroundView.setImageBitmap(bitmap);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void displayColorChooserDialog(boolean colorForBrush) {
        this.colorForBrush = colorForBrush;
        if (colorChooserDialog == null) {
            colorChooserDialog = new Dialog.Builder()
                    .title(getString(R.string.select_color))
                    .contentView(R.layout.dialog_color_chooser)
                    .positiveAction(getString(R.string.ok))
                    .negativeAction(getString(R.string.cancel))
                    .build(this);
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
                    if (MainActivity.this.colorForBrush) {
                        drawArea.setDrawingColor(lineColor.getColor());
                    } else {
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
        int color;
        if (this.colorForBrush) {
            color = lineColor.getColor();
        } else {
            color = backgroundColor.getColor();
        }
        colorView.setBackgroundColor(color);
        redSlider.setValue((color >> 16) & 0xFF, false);
        greenSlider.setValue((color >> 8) & 0xFF, false);
        blueSlider.setValue((color >> 0) & 0xFF, false);
        colorChooserDialog.show();
    }

    private int getColor(Slider redSlider, Slider greenSlider, Slider blueSlider) {
        if (colorForBrush) {
            lineColor.setARGB(255, redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
            return lineColor.getColor();
        } else {
            backgroundColor.setARGB(255, redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue());
            return backgroundColor.getColor();
        }
    }

    private void displayWidthChooserDialog() {
        if (lineWidthChooserDialog == null) {
            lineWidthChooserDialog = new Dialog.Builder()
                    .title(getString(R.string.select_width))
                    .contentView(R.layout.dialog_width_chooser)
                    .positiveAction(getString(R.string.ok))
                    .negativeAction(getString(R.string.cancel))
                    .build(this);
            lineWidthChooserDialog.cancelable(true);
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

    private void showLineWidth(Bitmap bitmap, Canvas canvas) {
        lineColor.setStrokeWidth(widthSlider.getValue());
        bitmap.eraseColor(getResources().getColor(android.R.color.transparent));
        canvas.drawLine(30, 50, 370, 50, lineColor);
        widthView.setImageBitmap(bitmap);
    }

    private void displayBgSourceChooserDialog() {
        if (bgSourceChooserDialog == null) {
            bgSourceChooserDialog = (SimpleDialog) new SimpleDialog.Builder()
                    .title(getString(R.string.select_bg_source))
                    .positiveAction(getString(R.string.ok))
                    .negativeAction(getString(R.string.cancel))
                    .build(this);
            bgSourceChooserDialog.setCancelable(true);
            bgSourceChooserDialog.items(new String[]{
                    getString(R.string.bg_source_color),
                    getString(R.string.bg_source_image)
            }, 0);
            bgSourceChooserDialog.positiveActionClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bgSourceChooserDialog.getSelectedIndex() == 0) {
                        displayColorChooserDialog(false);
                        bgSourceChooserDialog.dismiss();
                    } else {
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

    private void startImageSelection() {
        File root = new File(Utils.getInstance().getImagesFolder());
        root.mkdirs();
        String fileName = Utils.getInstance().getUniqueImageFilename(this) + ".jpg";
        File sdImageMainDirectory = new File(root, fileName);
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
        Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, IMAGE_SELECT_REQUEST);
    }
}