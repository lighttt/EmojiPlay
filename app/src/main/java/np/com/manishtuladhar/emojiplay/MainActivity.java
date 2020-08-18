package np.com.manishtuladhar.emojiplay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_STORAGE_PERMISSION = 1;
    private final static int REQUEST_CAMERA_CODE = 1;
    private final static String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

    //views
    private ImageView mImageView;
    private Button mEmojiBtn;
    private FloatingActionButton mShareFab;
    private FloatingActionButton mSaveFab;
    private FloatingActionButton mClearFab;
    private TextView mTitleTv;

    //image vars
    private String mTempPhotoPath;
    private Bitmap mResultImageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.imageView);
        mEmojiBtn = findViewById(R.id.emoji_btn);
        mShareFab = findViewById(R.id.share_btn);
        mClearFab = findViewById(R.id.clear_btn);
        mSaveFab = findViewById(R.id.save_btn);
        mTitleTv = findViewById(R.id.title_tv);
    }

    /**
     * It helps to emoji you and launches camera
     */
    public void emojiPlay(View view) {
        //check for external storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            //launch camera
            launchCamera();
        }

    }

    // ====================================== PERMISSIONS ============================================

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //call external storage permission
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //launch camera
                    launchCamera();
                } else {
                    Toast.makeText(this, "Permission denied for using camera. Try Again!", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

// ====================================== CAMERA ============================================

    private void launchCamera() {
        //create capture intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check if camera allows
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                //creating temp file to store capture image
                photoFile = BitMapUtils.createTempImageFile(this);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //check if file exists
            if (photoFile != null) {
                //location
                mTempPhotoPath = photoFile.getAbsolutePath();
                //uri
                Uri photoUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, photoFile);
                //add uri to the camera so that it can stored the capture image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);

                //launch camera
                startActivityForResult(takePictureIntent,REQUEST_CAMERA_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            //process and set image
            processAndSetImage();
        } else {
            //delete cache image
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

// ====================================== IMAGE ============================================

    private void processAndSetImage()
    {
        //button visibility
        mTitleTv.setVisibility(View.GONE);
        mEmojiBtn.setVisibility(View.GONE);
        mClearFab.setVisibility(View.VISIBLE);
        mSaveFab.setVisibility(View.VISIBLE);
        mShareFab.setVisibility(View.VISIBLE);

        //resample to fit imageview
        mResultImageBitmap = BitMapUtils.resamplePic(this,mTempPhotoPath);

        //detect the faces
        EmojiPlayer.detectFaces(this,mResultImageBitmap);

        //set in iv
        mImageView.setImageBitmap(mResultImageBitmap);
    }

    /**
     * Saves the image clicked
     */
    public void saveMe(View view) {
        //delete the old temp file
        BitMapUtils.deleteImageFile(this,mTempPhotoPath);

        //save image file
        BitMapUtils.saveImage(this,mResultImageBitmap);
    }

    /**
     * Helps to share the image in bitmap format
     */
    public void shareMe(View view) {
        //delete the old temp file
        BitMapUtils.deleteImageFile(this,mTempPhotoPath);

        //save image file
        String savedPath = BitMapUtils.saveImage(this,mResultImageBitmap);

        //share image file
        BitMapUtils.shareImage(this,savedPath);

    }

    /**
     * Removes the image and reset
     */
    public void clearImage(View view) {
        //button visibility3.
        mImageView.setImageResource(0);
        mClearFab.setVisibility(View.GONE);
        mSaveFab.setVisibility(View.GONE);
        mShareFab.setVisibility(View.GONE);
        mTitleTv.setVisibility(View.VISIBLE);
        mEmojiBtn.setVisibility(View.VISIBLE);

        //delete
        BitMapUtils.deleteImageFile(this,mTempPhotoPath);

    }
}