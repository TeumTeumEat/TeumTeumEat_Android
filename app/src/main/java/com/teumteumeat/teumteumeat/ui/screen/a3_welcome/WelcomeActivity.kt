package com.teumteumeat.teumteumeat.ui.screen.a3_welcome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.BaseFillButton
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeumTeumEatTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    DefaultMonoBg(
        color = MaterialTheme.colorScheme.surface,
        content = {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(top = 193.dp)
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(27.dp))

                    Text(
                        text = "틈틈잇 지식 한입",
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 25.2.sp,
                            fontWeight = FontWeight(500),
                            color = Color(0xFF333333),
                        )
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(bottom = 84.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    BaseFillButton(
                        text = "카카오 로그인",
                        onClick = {
                            // Utils.UxUtils.moveActivity(context, LoginActivity::class.java, false)
                        }
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    BaseFillButton(text = "일반 로그인?",
                        onClick = {
                            // Utils.UxUtils.moveActivity(context, SignUpActivity::class.java, false)
                        }
                    )

                }

            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TeumTeumEatTheme {
        Greeting("Android")
    }
}