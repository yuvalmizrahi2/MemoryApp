package memory.Memoryapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity implements Validator.ValidationListener {

    @NotEmpty
    private EditText groupName;
    private Validator validator;
    private static boolean valIsDone;
    private CircleImageView groupProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mData;
    private StorageReference groupImageRef;
    private ProgressDialog loadingBar;
    private static final int GalleryPick = 1;
    private String key;
    private Uri imageUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        initFields();
        initValidator();
        initFireBase();
    }


    private void initFields(){
        findViewById(R.id.create_group_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnCreateGroupButton();
            }
        });
        groupName = findViewById(R.id.set_group_name);
        groupProfileImage = findViewById(R.id.set_group_image);
        groupProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickOnset_group_image();
            }
        });
        loadingBar = new ProgressDialog(this);
    }


    private void initValidator(){
        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    private void initFireBase(){
        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference();
        groupImageRef = FirebaseStorage.getInstance().getReference().child("Group Images");
        key = mData.child("Groups").push().getKey();

    }

    private void clickOnCreateGroupButton(){
        validator.validate();
        if(valIsDone){
            loadingBar.setTitle("Create Group");
            loadingBar.setMessage("Please wait, while we are create your new group for you...");
            loadingBar.show();
            Group group;
            if(imageUrl == null)
                group= new Group(groupName.getText().toString(), "", key, mAuth.getCurrentUser().getUid());
            else
                group= new Group(groupName.getText().toString(), imageUrl.toString(), key, mAuth.getCurrentUser().getUid());
            mData.child("Groups").child(key).setValue(group)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                UserDataHolder.getUserDataHolder().getUser().getGroupId().add(key);
                                mData.child("Users").child(mAuth.getCurrentUser().getUid())
                                        .setValue(UserDataHolder.getUserDataHolder().getUser())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    if (imageUrl != null) {
                                                        StorageReference filePath = groupImageRef.child(key + ".jpg");
                                                        filePath.putFile(imageUrl).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    loadingBar.dismiss();
                                                                    finish();
                                                                } else {
                                                                    loadingBar.dismiss();
                                                                    Toast.makeText(CreateGroupActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                    else{
                                                        loadingBar.dismiss();
                                                        finish();
                                                    }
                                                }
                                                else {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(CreateGroupActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                            else {
                                loadingBar.dismiss();
                                Toast.makeText(CreateGroupActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void clickOnset_group_image() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GalleryPick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Set Group Image");
                loadingBar.setMessage("Please wait, while your Group image is uploading...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                imageUrl = result.getUri();
                Picasso.get().load(imageUrl.toString()).into(groupProfileImage);

            }
        }
    }

    @Override
    public void onValidationSucceeded() {
        valIsDone = true;
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        valIsDone = false;
        for(ValidationError error: errors){
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            if(view instanceof EditText){
                ((EditText)view).setError(message);
            }
            else{
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
