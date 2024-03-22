package com.glen.stepcount.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glen.stepcount.domain.GetTodayStepsRecordFlowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getTodayStepsRecordFlowUseCase: GetTodayStepsRecordFlowUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainUiState())
    val state: StateFlow<MainUiState> = _state

    init {
        collectTodayStepsRecord()
    }

    private fun collectTodayStepsRecord() {
        viewModelScope.launch {
            getTodayStepsRecordFlowUseCase().collect { stepsRecord ->
                _state.update { it.copy(stepsRecord = stepsRecord) }
            }
        }
    }

    fun onUpdateHealthConnectStatus(isAvailable: Boolean) {
        _state.update {
            it.copy(
                isHealthConnectAvailable = isAvailable,
                hasReadStepsPermission = if (isAvailable) {
                    it.hasReadStepsPermission
                } else {
                    false
                }
            )
        }
    }

    fun onUpdateReadStepsPermission(hasPermission: Boolean) {
        _state.update { it.copy(hasReadStepsPermission = hasPermission) }
    }
}