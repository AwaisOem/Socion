package com.example.socion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.PasswordCredential;
import androidx.credentials.PublicKeyCredential;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.concurrent.Executor;

public class RegisterActivity extends AppCompatActivity {
    private final String TAG="RegisterActivity";

    private CredentialManager credentialManager;
    private FirebaseAuth mAuth;
    private GetGoogleIdOption googleIdOption;
    private Context currContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        currContext = RegisterActivity.this;
        mAuth = FirebaseAuth.getInstance();
        initiateForCMLogin(false , currContext);


        // Button Event Listners
        findViewById(R.id.go_to_login).setOnClickListener(v -> {
            startActivityIfNeeded(new Intent(getApplicationContext() , LoginActivity.class) , 0);
        });
        findViewById(R.id.googleButtonForRegistration).setOnClickListener(v ->{
            registerWithGoogleUsingCM();
        });
        findViewById(R.id.registerButton).setOnClickListener(v ->{
            EditText rn =  findViewById(R.id.register_name);
            EditText re =  findViewById(R.id.register_email);
            EditText rp =  findViewById(R.id.register_password);
            EditText rc =  findViewById(R.id.register_confirm);
            String name = String.valueOf(rn.getText());
            String email = String.valueOf(re.getText());
            String password = String.valueOf(rp.getText());
            String confirmPassword = String.valueOf(rc.getText());
            if(password.isEmpty() || email.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
                Toast t= DynamicToast.makeWarning(getApplicationContext(), "Fields Should not be empty");
                        t.setGravity(Gravity.TOP,0 , 0 );
                        t.show();
                return;
            }
            if(!EmailValidator.getInstance().isValid(email)){
                Toast t= DynamicToast.makeWarning(getApplicationContext(), "Email is not valid");
                t.setGravity(Gravity.TOP,0 , 0 );
                t.show();
                return;
            }
            if(!password.equals(confirmPassword)){
                Toast t= DynamicToast.makeWarning(getApplicationContext(), "Passwords don't match");
                    t.setGravity(Gravity.TOP,0 , 0 );
                    t.show();
                return;
            }
            if(password.length() < 6){
                Toast t= DynamicToast.makeWarning(getApplicationContext(), "Minimum password length is 6");
                t.setGravity(Gravity.TOP,0 , 0 );
                t.show();
                return;
            }
            registerUsingPassword(name , email, password);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            goToMainActivity();
        }
    }

    private void goToMainActivity(){
        startActivity(new Intent(getApplicationContext() , MainActivity.class));
        finish();
    }
    private void registerUsingPassword(String name , String email ,String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task1 -> {
                    if (task1.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user==null){
                            Toast t= DynamicToast.makeError(getApplicationContext(), "Authentication failed");
                            t.setGravity(Gravity.TOP,0 , 0 );
                            t.show();
                            return;
                        }

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
//                                .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))

                        user.updateProfile(profileUpdates).addOnCompleteListener(task2 -> {
                            String message = "";
                            if (task2.isSuccessful()) {
                                message = "Register Successful: "+user.getEmail();
                            }else{
                                message = "Register Completed But with some Errors";
                            }
                            Toast t= DynamicToast.makeSuccess(getApplicationContext(), message);
                            t.setGravity(Gravity.TOP,0 , 0 );
                            t.show();
                            goToMainActivity();
                        });
                    } else {
                        Log.w(TAG, "registerUsingPassword:failure",task1.getException());
                        Toast t= DynamicToast.makeError(getApplicationContext(), "Authentication failed");
                        t.setGravity(Gravity.TOP,0 , 0 );
                        t.show();
                    }
                });

    }

    public void initiateForCMLogin(boolean onlyAuthAccounts ,Context context){
        credentialManager = CredentialManager.create(context);
        googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(onlyAuthAccounts)
                .setServerClientId(getString(R.string.default_web_client_id)).build();
    }


    public void registerWithGoogleUsingCM(){
        GetCredentialRequest request = new GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build();
        credentialManager.getCredentialAsync(
                currContext,
                request,
                new CancellationSignal(),
                Runnable::run,
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleCMRegister(result);
                    }
                    @Override
                    public void onError(GetCredentialException e) {
                        String message =  e.getMessage();
                        Log.e(TAG , (String) message);
                        Snackbar.make(findViewById(R.id.snackbar_coordinates) , (String) message.substring(message.indexOf(']')+1), 4000).show();
                    }
                }
);
    }
    public void handleCMRegister(GetCredentialResponse result) {
        // Handle the successfully returned credential.
        Credential credential = result.getCredential();

        if (credential instanceof PublicKeyCredential) {
            String responseJson = ((PublicKeyCredential) credential).getAuthenticationResponseJson();
            // Share responseJson i.e. a GetCredentialResponse on your server to validate and authenticate
        } else if (credential instanceof PasswordCredential) {
            String username = ((PasswordCredential) credential).getId();
            String password = ((PasswordCredential) credential).getPassword();
            // Use id and password to send to your server to validate and authenticate
        } else if (credential instanceof CustomCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());

                // Use googleIdTokenCredential and extract id to validate and
                googleAuthUsingFirebase(googleIdTokenCredential.getIdToken() , mAuth , TAG);

            } else {
                // Catch any unrecognized custom credential type here.
                Log.e(TAG, "Unexpected type of credential");
            }
        } else {
            // Catch any unrecognized credential type here.
            Log.e(TAG, "Unexpected type of credential");
        }
    }



    public void googleAuthUsingFirebase(String idToken ,FirebaseAuth localMAuth,String tag){
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        localMAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = localMAuth.getCurrentUser();
                        if(user != null){
                            Toast t= DynamicToast.makeSuccess(getApplicationContext(), "Register Successful:"+user.getEmail());
                            t.setGravity(Gravity.TOP,0 , 0 );
                            t.show();
                            goToMainActivity();
                        }else{
                            Log.d(tag, "signInWithCredential:success but user is null");
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(tag, "signInWithCredential:failure", task.getException());
                    }

                });

    }
}