package com.teumteumeat.teumteumeat.ui.screen.a1_login

import androidx.lifecycle.ViewModel
import com.teumteumeat.teumteumeat.domain.usecase.AutoLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val autoLoginUseCase: AutoLoginUseCase,
) : ViewModel() {
}
