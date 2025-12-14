package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiStateLibrary>(UiStateLibrary())
    val uiState = _uiState.asStateFlow()

    // Flow 값으로 currentPage 읽기
    private val currentPage get() = uiState.value.currentPage
    private val totalPage get() = uiState.value.totalPage


    fun nextPage() {
        if (currentPage < totalPage) {
            Log.d("1증가 전", "증가함, ${currentPage}/${totalPage}")
            viewModelScope.launch {
                _uiState.update { currentState ->
                    currentState.copy(
                        currentPage = currentPage + 1
                    )
                }
            }
            Log.d("1증가 후", "증가함, ${currentPage}/${totalPage}")
        }
    }

    fun prevPage() {
        if (currentPage > 0) {
            Log.d("1감소 전", "감소함, ${currentPage}/${totalPage}")
            viewModelScope.launch {
                _uiState.update { currentState ->
                    currentState.copy(
                        currentPage = currentPage - 1
                    )
                }
            }
            Log.d("1감소 후", "감소함, ${currentPage}/${totalPage}")
        }
    }
}