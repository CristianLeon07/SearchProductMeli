package com.example.pruebameli

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.pruebameli.presentation.auth.AuthRoute
import com.example.pruebameli.presentation.navigation.AppNavGraph
import com.example.pruebameli.ui.theme.PruebaMeliTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingCode by mutableStateOf<String?>(null)
    private var pendingError by mutableStateOf<String?>(null)

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        readAuthExtras(intent)

        setContent {
            PruebaMeliTheme {
                Scaffold {
                    AppNavGraph(
                        initialCode = pendingCode,
                        initialError = pendingError,
                        onAuthArgsConsumed = {
                            pendingCode = null
                            pendingError = null
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readAuthExtras(intent)
    }

    private fun readAuthExtras(intent: Intent?) {
        pendingCode = intent?.getStringExtra("code")
        pendingError = intent?.getStringExtra("error")

        // si esto viene por deep link real, lo ideal es leerlo desde intent.data
        // pero por ahora lo dejamos así como lo tienes.
    }
}
