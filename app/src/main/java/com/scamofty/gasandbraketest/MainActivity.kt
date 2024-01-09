package com.scamofty.gasandbraketest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.scamofty.gasandbraketest.ui.theme.GasAndBrakeTestTheme

class MainActivity : ComponentActivity() {
    private lateinit var gameControllerManager : GameControllerManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GasAndBrakeTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SendButtonInputsButton(gameControllerManager)
                }
            }
        }
        gameControllerManager = GameControllerManager(this)
    }
}

@Composable
fun SendButtonInputsButton(gameControllerManager: GameControllerManager) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Center,
        ) {
            Text(
                text = "Instructions: Start this service. Then minimize this and go to the game or app you want to use this with.",
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp)
            )
        var isActive by remember { mutableStateOf(false) }
        var color by remember { mutableStateOf(Color.Red) }
        var text by remember { mutableStateOf("Service Not Running") }
        Button(
            onClick = {
                isActive = !isActive
                when (color) {
                    Color.Blue -> {
                        color = Color.Red
                        text = "Service Not Running"
                        gameControllerManager.stop()
                    }
                    Color.Red -> {
                        color = Color.Blue
                        text = "Service Running"
                        gameControllerManager.start()
                    }
                }
            }, modifier = Modifier.padding(8.dp),
            colors= ButtonDefaults.buttonColors(containerColor = color)
        ) {
            Text(text,color=Color.White)
        }
    }
}