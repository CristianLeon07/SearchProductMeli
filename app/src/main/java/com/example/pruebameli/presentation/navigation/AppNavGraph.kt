package com.example.pruebameli.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.pruebameli.presentation.auth.AuthRoute
import com.example.pruebameli.presentation.detail.DetailProductScreen
import com.example.pruebameli.presentation.detail.DetailProductViewModel
import com.example.pruebameli.presentation.home.HomeRoute



@Composable
fun AppNavGraph(
    initialCode: String?,
    initialError: String?,
    onAuthArgsConsumed: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ScreenDest.Auth // si Auth es data object
    ) {
        composable<ScreenDest.Auth> {
            AuthRoute(
                navController = navController,
                initialCode = initialCode,
                initialError = initialError,
                onAuthArgsConsumed = onAuthArgsConsumed
            )
        }

        composable<ScreenDest.Home> {
            HomeRoute(navController = navController)
        }

        composable<ScreenDest.Detail> { entry ->
            val args = entry.toRoute<ScreenDest.Detail>()
            val vm: DetailProductViewModel = hiltViewModel()
            DetailProductScreen(
                productId = args.id,
                onBack = { navController.popBackStack() },
                detailProductViewModel = vm
            )
        }
    }
}

