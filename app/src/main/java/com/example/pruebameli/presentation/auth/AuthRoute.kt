package com.example.pruebameli.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.pruebameli.presentation.navigation.ScreenDest



@Composable
fun AuthRoute(
    navController: NavController,
    initialCode: String? = null,
    initialError: String? = null,
    onAuthArgsConsumed: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = hiltViewModel()

    val state by viewModel.state.collectAsState()
    val hasSession by viewModel.hasSession.collectAsState()

    LaunchedEffect(initialCode, initialError) {
        if (!initialError.isNullOrBlank()) {
            onAuthArgsConsumed()
            return@LaunchedEffect
        }

        if (!initialCode.isNullOrBlank()) {
            viewModel.onAuthCodeReceived(initialCode)
            onAuthArgsConsumed() // ✅ importantísimo
        }
    }

    LaunchedEffect(Unit) {
        viewModel.openAuthPage.collect { url ->
            AuthLoginLauncher.open(context, url)
        }
    }

    LaunchedEffect(hasSession) {
        if (hasSession) {
            navController.navigate(ScreenDest.Home) {
                popUpTo(ScreenDest.Auth) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    AuthScreen(
        state = state,
        hasSession = hasSession,
        onLoginClick = viewModel::onLoginClick
    )
}
