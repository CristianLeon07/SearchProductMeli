package com.example.pruebameli.core.utils

import com.example.pruebameli.domain.common.ResourceData

fun <T> ResourceData<T>.toUiState(): ResourceUiState<T> = when (this) {
    is ResourceData.Success -> ResourceUiState.Success(data)
    is ResourceData.Error -> ResourceUiState.Error(message)
    ResourceData.Loading -> ResourceUiState.Loading
}
