package com.dranithix.fishackathon;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thebluealliance.spectrum.SpectrumDialog;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class AddItemActivity extends AppCompatActivity {
    @Bind(R.id.colorPicker)
    TextView colorPicker;

    @Bind(R.id.firstCategory)
    Spinner firstCategory;

    @Bind(R.id.secondCategory)
    Spinner secondCategory;

    @Bind(R.id.gearId)
    TextView gearId;

    @Bind(R.id.coverPhoto)
    ImageView coverPhoto;

    String[][] categoryTypes;

    private int currentColor = android.R.color.white;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        ButterKnife.bind(this);

        categoryTypes = new String[][]{getResources().getStringArray(R.array.net_types), getResources().getStringArray(R.array.trap_types),
                getResources().getStringArray(R.array.hook_types), getResources().getStringArray(R.array.grappling_types)};
        ArrayAdapter<String> firstCategoriesAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, getResources().getStringArray(R.array.gear_types));
        firstCategory.setAdapter(firstCategoriesAdapter);
        setSecondCategoryOptions(0);
    }

    @OnClick(R.id.qrButton)
    public void clickQrButton(View view) {
        Intent qrIntent = new Intent(this, QrActivity.class);
        startActivityForResult(qrIntent, QrActivity.QR_CODE_FOUND);
    }

    @OnClick({R.id.uploadPhoto, R.id.uploadPhotoText})
    public void selectPhoto() {
        PermissionListener dialogPermissionListener =
                DialogOnDeniedPermissionListener.Builder
                        .withContext(this)
                        .withTitle("Camera permission")
                        .withMessage("Camera permission is needed to upload a cover photo.")
                        .withButtonText(android.R.string.ok)
                        .build();

        Dexter.checkPermission(dialogPermissionListener, Manifest.permission.CAMERA);
        EasyImage.openChooserWithGallery(this, "Choose a photo", 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == QrActivity.QR_CODE_FOUND) {
            System.out.println("Founded QR Code! Data is " + data.getStringExtra("result"));
            Toast.makeText(this, "Detected gear!", Toast.LENGTH_LONG).show();

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
            gearId.setText(data.getStringExtra("result"));
        } else {
            EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
                @Override
                public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                    //Some error handling
                }

                @Override
                public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                    //Handle the image
                    onPhotoReturned(imageFile);
                }

                @Override
                public void onCanceled(EasyImage.ImageSource source, int type) {
                    //Cancel handling, you might wanna remove taken photo if it was canceled
                    if (source == EasyImage.ImageSource.CAMERA) {
                        File photoFile = EasyImage.lastlyTakenButCanceledPhoto(AddItemActivity.this);
                        if (photoFile != null) photoFile.delete();
                    }
                }
            });
        }
    }

    private void onPhotoReturned(File photoFile) {
        Picasso.with(this)
                .load(photoFile)
                .fit().centerCrop()
                .into(coverPhoto);

    }


    @OnItemSelected(R.id.firstCategory)
    public void setSecondCategoryOptions(int position) {
        ArrayAdapter<String> secondCategoryAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, categoryTypes[position]);
        secondCategory.setAdapter(secondCategoryAdapter);
    }

    @OnClick(R.id.colorPicker)
    public void chooseColor(View view) {
        new SpectrumDialog.Builder(this)
                .setSelectedColor(currentColor)
                .setColors(R.array.net_colors)
                .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                        if (positiveResult) {
                            ((GradientDrawable) colorPicker.getBackground()).setColor(currentColor = color);
                            colorPicker.setHintTextColor(getContrastColor(color));
                        }
                    }
                }).build().show(getSupportFragmentManager(), "Color Dialog");
    }

    public static int getContrastColor(int color) {
        double y = (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color)) / 1000;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }
}
