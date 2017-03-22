package com.example.ocrtesttesseract;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.R.attr.data;

public class MainActivity extends AppCompatActivity {

    Bitmap image; //our image
    private TessBaseAPI mTess; //Tess API reference
    String datapath = ""; //path to folder containing language data file
    private ImageView imageView;
    private static final int CAMERA_REQUEST = 0000;
    private static final int IMAGE_GALLERY_REQUEST = 0001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //for user image from camera activity
        this.imageView = (ImageView) this.findViewById(R.id.imageView);

        //init image
        image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image2);

        //get files
        datapath = getFilesDir()+ "/tesseract/";

        //make sure training data has been copied
        checkFile(new File(datapath + "tessdata/"));

        //init Tesseract API
        String language = "eng";
        mTess = new TessBaseAPI();
        mTess.init(datapath, language);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private void copyFile() {
        try {
            //location we want the file to be at
            String filepath = datapath + "/tessdata/eng.traineddata";

            //get access to AssetManager
            AssetManager assetManager = getAssets();

            //open byte streams for reading/writing
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFile(File dir) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists()&& dir.mkdirs()){
            copyFile();
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFile();
            }
        }
    }

    // when camera button is clicked
    public void onCameraClick(View view) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    //when gallery is clicked
    public void onGalleryClick(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_GALLERY_REQUEST);
    }

    //processing data
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK){
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(photo);
                image = photo;
        }
        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            InputStream inputStream;
            try {
                inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap photo = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(photo);
                image = photo;
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
                Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
            }

        }
    }


    //the actual nitty-gritty
    public void processImage(View view){
        mTess.setImage(image);
        String OCRresult = mTess.getUTF8Text();
        TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
        OCRTextView.setText(OCRresult);
    }


}
