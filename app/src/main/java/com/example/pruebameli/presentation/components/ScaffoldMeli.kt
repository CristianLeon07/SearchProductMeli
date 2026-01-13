package com.example.pruebameli.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable


@Composable
fun MeliScaffold(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            MeliTopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        content = content
    )
}
