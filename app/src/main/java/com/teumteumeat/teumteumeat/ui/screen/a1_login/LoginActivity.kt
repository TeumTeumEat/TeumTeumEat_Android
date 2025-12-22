package com.teumteumeat.teumteumeat.ui.screen.a1_login

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.teumteumeat.teumteumeat.R
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalAppContext
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity()  {
    private lateinit var googleClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        val launcher =
            registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            Log.d("GoogleLogin", "resultCode=${result.resultCode}")
            Log.d("GoogleLogin", "data=${result.data}")
            if (result.resultCode != Activity.RESULT_OK) {
                Log.e("GoogleLogin", "RESULT NOT OK")
                return@registerForActivityResult
            }

            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                Log.d("GoogleLogin", "task=$task")

                val account = task.getResult(ApiException::class.java)
                Log.d("GoogleLogin", "account=$account")

                val idToken = account.idToken
                Log.d("GoogleLogin", "idToken=$idToken")
                // todo: viewModel 에서 idToken 으로 레포지토리(api호출) 하는 로직 구현

            } catch (e: ApiException) {
                Log.e("GoogleLogin", "ApiException code=${e.statusCode}", e)
            }
        }

        setContent {
            TeumTeumEatTheme {
                CompositionLocalProvider(
                    LocalAppContext provides this.applicationContext,
                    LocalActivityContext provides this,
                ){
                    LoginScreen(
                        onGoogleClick = { launcher.launch(googleClient.signInIntent) }
                    )
                }
//                MainCompositionProvider(
//                    viewModel = viewModel,
//                    context = this.applicationContext,
//                    activity = this@MainActivity,
//                )
            }
        }


    }
}
