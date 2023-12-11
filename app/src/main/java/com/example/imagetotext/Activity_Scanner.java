package com.example.imagetotext;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


public class Activity_Scanner extends AppCompatActivity {

    private ImageView capture;
    private EditText resultv;

    private Button snapbtn ,detectbtn;

    private Bitmap imagebitmap;

    static  final  int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        snapbtn = findViewById(R.id.buttonSnap);
        capture = findViewById(R.id.captureimage);
        snapbtn = findViewById(R.id.buttonSnap);
        detectbtn = findViewById(R.id.buttondetect);
        resultv  =  findViewById(R.id.textv);

        detectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DetectText();

            }
        });

        snapbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckPermission()){
                    CaptureImage();
                }
                else {
                    RequestPermission();
                }
            }
        });

    }

    private boolean  CheckPermission(){
        int camerapermission = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA_SERVICE);
        return camerapermission== getPackageManager().PERMISSION_GRANTED;
    }


    private void RequestPermission() {
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{
               Manifest.permission.CAMERA,
        }, PERMISSION_CODE);
    }
    private  void CaptureImage(){
        Intent takepicture =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takepicture.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takepicture,REQUEST_IMAGE_CAPTURE);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            boolean camerapermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(camerapermission){
                Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show();
                CaptureImage();
            }
            else {
                Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK)
        {
            Bundle extras  = data.getExtras();
            imagebitmap =  (Bitmap)extras.get("data");
            capture.setImageBitmap(imagebitmap);

        }
    }


    private void DetectText() {
  //InputImage: InputImage is a class provided by ML Kit for Firebase. It represents an image that can be processed by various ML Kit APIs, such as image labeling, text recognition, etc
        InputImage image = InputImage.fromBitmap(imagebitmap,0);
        //So, in summary, this line of code creates a TextRecognizer instance with default options. This TextRecognizer can be used to process InputImage instances (representing images) and extract text content from them. The ML Kit Text Recognition API can be used for tasks such as reading text from images, which is useful in scenarios like scanning documents, extracting information from images, and more.
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task <Text>result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder result = new StringBuilder();
                for(Text.TextBlock block :text.getTextBlocks()){
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockframe = block.getBoundingBox();
                    for(Text.Line line : block.getLines()){
                        String linetext  = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                         for(Text.Element element :line.getElements()){
                             String elementtext = element.getText();
                             result.append(elementtext);
                         }

                    resultv.setText(blockText);

                    }
                }


            }
        }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Failed to Detect Text from Image",Toast.LENGTH_SHORT).show();
            }
        });
    }
}