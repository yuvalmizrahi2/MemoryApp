package memory.Memoryapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        findViewById(R.id.agreeNContinueTVBtn).setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        }
        else{
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            finish();
        }
    }



    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.agreeNContinueTVBtn:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

}
