package thesis.dobrechwile;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import model.Chwile;
import util.ChwileApi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Publikacja extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Publikacja";
    private Button saveButton;
    private Button editButton;
    private EditText datePicker;
    private ProgressBar progressBar;
    private ImageView addPhoto;
    private EditText titleEditText;
    private EditText descEditText;
    private TextView currentUserTextView;
    private ImageView imageView;
    private Button locationButton;
    private TextView locationTextView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String[] chooseIntent = {"Galeria", "Kamera"};
    final Calendar myCalendar= Calendar.getInstance();

    private String currentUserId;

    private String currentPhotoPath;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("Chwile");
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publikacja);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.postProgressBar);
        titleEditText = findViewById(R.id.postTitleEditText);
        datePicker = findViewById(R.id.datePicker);
        descEditText = findViewById(R.id.postDescEditText);
        imageView = findViewById(R.id.postImageView);
        saveButton = findViewById(R.id.postButton);
        editButton = findViewById(R.id.editButton);
        locationButton = findViewById(R.id.locationButton);
        locationTextView = findViewById(R.id.locationTextView);
        saveButton.setOnClickListener(this);
        locationButton.setOnClickListener(this);
        addPhoto = findViewById(R.id.postCameraButton);
        addPhoto.setOnClickListener(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        DatePickerDialog.OnDateSetListener date =new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateLabel();
            }
        };
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(Publikacja.this,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        progressBar.setVisibility(View.INVISIBLE);

        if (ChwileApi.getInstance() != null) {
            currentUserId = ChwileApi.getInstance().getUserId();
        }
        String value = getIntent().getStringExtra("wyswietlanie");
        if (value != null) {
            titleEditText.setText(getIntent().getStringExtra("title"));
            descEditText.setText(getIntent().getStringExtra("description"));
            datePicker.setText(getIntent().getStringExtra("date"));
            datePicker.setEnabled(false);
            String imageUrl = getIntent().getStringExtra("imageUrl");
            Picasso.get().load(imageUrl).placeholder(R.drawable.niebo1).fit().into(imageView);
            String documentUid = getIntent().getStringExtra("documentUid");
            locationTextView.setText(getIntent().getStringExtra("location"));
            titleEditText.setEnabled(false);
            datePicker.setEnabled(false);
            addPhoto.setVisibility(View.INVISIBLE);
            saveButton.setVisibility(View.INVISIBLE);
            locationButton.setVisibility(View.INVISIBLE);
            editButton.setVisibility(View.VISIBLE);
            descEditText.setEnabled(false);
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    datePicker.setEnabled(true);
                    titleEditText.setEnabled(true);
                    descEditText.setEnabled(true);
                    locationButton.setVisibility(View.VISIBLE);
                    addPhoto.setVisibility(View.VISIBLE);
                    editButton.setVisibility(View.VISIBLE);
                    editButton.setText("Zapisz zmiany");
                    editButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DocumentReference docRef = collectionReference.document(documentUid);
                            docRef.update("title", titleEditText.getText().toString());
                            docRef.update("description", descEditText.getText().toString());
                            docRef.update("location", locationTextView.getText().toString());
                            docRef.update("timeAdded", datePicker.getText().toString());

                            if (imageUri != null) {
                                final StorageReference filepath = storageReference.child("chwile_images").child("image_" + Timestamp.now().getSeconds());
                                filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                progressBar.setVisibility(View.VISIBLE);
                                                String imageUrl = uri.toString();
                                                docRef.update("imageUrl", imageUrl);
                                                startActivity(new Intent(Publikacja.this, ListaChwil.class));
                                                finish();
                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            } else {
                                startActivity(new Intent(Publikacja.this, ListaChwil.class));
                                finish();
                            }
                        }
                    });

                }
            });
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {

                } else {

                }
            }
        };

    }

    private void updateLabel() {
        String myFormat="yyyy/MM/dd HH:mm:ss";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        datePicker.setText(dateFormat.format(myCalendar.getTime()));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.postButton:
                //save
                saveMoment();
                break;
            case R.id.postCameraButton:
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {

                    }
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(this,
                                "com.example.android.fileprovider",
                                photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            }

                }
               Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                AlertDialog.Builder builder = new AlertDialog.Builder(Publikacja.this);
                builder.setTitle("Wybierz zdjęcie");
                builder.setItems(chooseIntent, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                startActivityForResult(pickPhoto , 1);
                                break;
                            case 1:
                                startActivityForResult(takePictureIntent, 0);
                                break;

                        }
                    }
                });
                builder.show();

                break;
            case R.id.locationButton:
                if (ActivityCompat.checkSelfPermission(Publikacja.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    ActivityCompat.requestPermissions(Publikacja.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                }
                break;
        }

    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener < Location > () {
            @Override
            public void onComplete(@NonNull Task < Location > task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(Publikacja.this, Locale.getDefault());
                        List < Address > addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        locationTextView.setText(addresses.get(0).getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void saveMoment() {
        final String title = titleEditText.getText().toString().trim();
        final String descripiton = descEditText.getText().toString().trim();
        final String location = locationTextView.getText().toString().trim();
        final String timeAdded = datePicker.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);


        if(!TextUtils.isEmpty(title)
        && !TextUtils.isEmpty(descripiton)
        && imageUri != null){
            final StorageReference filepath = storageReference.child("chwile_images").child("image_" + Timestamp.now().getSeconds());
            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            Chwile chwile = new Chwile();
                            chwile.setTitle(title);
                            chwile.setDescription(descripiton);
                            chwile.setImageUrl(imageUrl);
                            chwile.setTimeAdded(timeAdded);
                            chwile.setUserId(currentUserId);
                            chwile.setLocation(location);


                            collectionReference.add(chwile).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(Publikacja.this, ListaChwil.class));
                                    finish();


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure" + e.getMessage());

                                }
                            });

                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });


        }else{
            progressBar.setVisibility(View.INVISIBLE);


        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                galleryAddPic();
                if(resultCode == RESULT_OK){
                    galleryAddPic();
                }

                break;
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    imageUri = selectedImage;
                    imageView.setImageURI(imageUri);
                }
                break;
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        imageUri = contentUri;
        imageView.setImageURI(imageUri);
    }

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if(data != null){
                            imageUri = data.getData();
                            imageView.setImageURI(imageUri);
                        }
                    }
                }
            });

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}