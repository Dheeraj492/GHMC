package com.example.ghmc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ComplaintForm extends AppCompatActivity {

    private EditText firstName, lastName, phoneNumber, email, address, landmark, problemDescription;
    private ImageView uploadedPhoto;
    private Button submitButton, uploadPhotoButton;
    private Uri photoUri;

    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_form);

        // Initialize views
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        phoneNumber = findViewById(R.id.phoneNumber);
        email = findViewById(R.id.email);
        address = findViewById(R.id.address);
        landmark = findViewById(R.id.landmark);
        problemDescription = findViewById(R.id.problemDescription);
        uploadedPhoto = findViewById(R.id.uploadedPhoto);
        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        submitButton = findViewById(R.id.submitButton);

        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(this);

        // Handle Upload Photo Button
        uploadPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });

        // Handle Submit Button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSubmitForm();
            }
        });
    }

    // Method to select a photo from gallery
    private void selectPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_SELECT_IMAGE);
    }

    // Get the selected photo URI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            photoUri = data.getData();
            uploadedPhoto.setImageURI(photoUri); // Display the uploaded photo

            // Optional: Get the real path from the URI
            String realPath = getRealPathFromUri(photoUri);
            if (realPath != null) {
                // You can use the real path if needed
                // For example, log it or save it
                Toast.makeText(this, "Photo Path: " + realPath, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show(); // Optional
        }
    }

    // Method to validate form inputs and upload to Firebase
    private void validateAndSubmitForm() {
        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();
        String phone = phoneNumber.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userAddress = address.getText().toString().trim();
        String userLandmark = landmark.getText().toString().trim();
        String problemDesc = problemDescription.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(fName) || TextUtils.isEmpty(lName) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userAddress) || TextUtils.isEmpty(userLandmark)
                || TextUtils.isEmpty(problemDesc) || photoUri == null) {
            Toast.makeText(this, "Please fill all the fields and upload a photo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate phone number format
        if (phone.length() < 10 || phone.length() > 15) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Submitting Complaint...");
        progressDialog.show();

        // Upload the photo to Firebase Storage
        uploadPhoto(fName,lName,phone,userEmail,userAddress,userLandmark,problemDesc);
    }

    // Method to upload the photo to Firebase Storage
    private void uploadPhoto(String fName, String lName, String phone, String userEmail, String userAddress, String userLandmark, String problemDesc) {
        StorageReference photoRef = storageReference.child("complaints_photos/" + System.currentTimeMillis() + ".jpg");
        photoRef.putFile(photoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the photo URL
                        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String photoUrl = uri.toString();
                                // Submit the form data to Firestore
                                submitFormData(fName, lName, phone, userEmail, userAddress, userLandmark, problemDesc, photoUrl);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(ComplaintForm.this, "Failed to get photo URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ComplaintForm.this, "Failed to upload photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to submit form data to Firestore
    private void submitFormData(String fName, String lName, String phone, String userEmail, String userAddress, String userLandmark, String problemDesc, String photoUrl) {
        Map<String, Object> complaintData = new HashMap<>();
        complaintData.put("firstName", fName);
        complaintData.put("lastName", lName);
        complaintData.put("phoneNumber", phone);
        complaintData.put("email", userEmail);
        complaintData.put("address", userAddress);
        complaintData.put("landmark", userLandmark);
        complaintData.put("problemDescription", problemDesc);
        complaintData.put("photoUrl", photoUrl);

        firestore.collection("complaints").add(complaintData)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss(); // Dismiss the progress dialog
                    Toast.makeText(ComplaintForm.this, "Complaint Submitted Successfully", Toast.LENGTH_SHORT).show();
                    // Clear form and navigate to SuccessActivity
                    Intent intent = new Intent(ComplaintForm.this, SuccessActivity.class);
                    startActivity(intent);
                    clearForm(); // Clear the form fields after submission
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss(); // Dismiss the progress dialog
                    Toast.makeText(ComplaintForm.this, "Failed to submit complaint: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // Show error message
                });
    }

    // Method to clear the form after submission
    private void clearForm() {
        firstName.setText("");
        lastName.setText("");
        phoneNumber.setText("");
        email.setText("");
        address.setText("");
        landmark.setText("");
        problemDescription.setText("");
        uploadedPhoto.setImageResource(0); // Clear the uploaded photo preview
        photoUri = null; // Clear the photo URI
    }

    // Method to get the real path from URI
    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String realPath = cursor.getString(column_index);
            cursor.close();
            return realPath;
        }
        return null;
    }
}
