package com.udacity.project4.authentication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAuthenticationBinding

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result: FirebaseAuthUIAuthenticationResult ->
        onSignInResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
//          TODO: If the user was authenticated, send him to RemindersActivity

        binding.btnLogin.setOnClickListener {
            startSignIn()
        }
//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    private fun startSignIn() {
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.AppTheme)
            .setLogo(R.drawable.map)
            .setAvailableProviders(
                listOf(
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                    AuthUI.IdpConfig.EmailBuilder().build()
                )
            )
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            startActivity(RemindersActivity.createIntent(this))
            finish()
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                Toast.makeText(this, R.string.login_canceled, Toast.LENGTH_LONG).show()
                return
            }
            if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                Toast.makeText(this, R.string.login_network_error, Toast.LENGTH_LONG).show()
                return
            }
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show()

        }
    }
}
