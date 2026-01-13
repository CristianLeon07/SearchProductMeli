package com.example.pruebameli.presentation.home.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.pruebameli.ui.theme.PruebaMeliTheme


@Composable
fun SearchBarMeli(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = "Buscar")

            Spacer(Modifier.width(8.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "Campo de búsqueda de productos"
                    },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        onSearch()
                    }
                ),
                decorationBox = { inner ->
                    if (query.isBlank()) {
                        Text(
                            "Buscar en Mercado Libre",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                    inner()
                }
            )

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSearch()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.semantics {
                    contentDescription = "Botón buscar productos"
                }
            ) { Text("Buscar") }
        }
    }
}

//PREVIEWS

@PreviewLightDark
@Composable
private fun SearchBarMeliEmptyPreview() {
    PruebaMeliTheme {
        SearchBarMeli(
            query = "",
            onQueryChange = {},
            onSearch = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun SearchBarMeliWithTextPreview() {
    PruebaMeliTheme {
        var query by remember { mutableStateOf("iPhone 15 Pro Max") }
        SearchBarMeli(
            query = query,
            onQueryChange = { query = it },
            onSearch = {}
        )
    }
}
