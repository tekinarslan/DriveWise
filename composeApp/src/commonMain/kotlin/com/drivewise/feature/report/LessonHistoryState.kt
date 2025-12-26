package com.drivewise.feature.report

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.drivewise.core.OnboardingStore
import com.drivewise.data.LessonHistoryMapper
import com.drivewise.data.TrackPointRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LessonHistoryState(
    val loading: Boolean = false,
    val error: String? = null,
    val lessons: List<LessonHistoryUiItem> = emptyList()
)

class LessonHistoryModel(
    private val repo: TrackPointRepository,
    private val onboardingStore: OnboardingStore
) : ScreenModel {

    private val _state = MutableStateFlow(LessonHistoryState())
    val state: StateFlow<LessonHistoryState> = _state

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(loading = true, error = null)
                repo.lessonSummaries()
            }.onSuccess { summaries ->
                val uiItems = summaries
                    .filter { it.startedAtMs > 0L && it.endedAtMs > 0L }
                    .map { LessonHistoryMapper.map(it, onboardingStore.getLanguage().code) }

                _state.value = LessonHistoryState(
                    loading = false,
                    lessons = uiItems,
                    error = null
                )
            }.onFailure { e ->
                _state.value = LessonHistoryState(
                    loading = false,
                    lessons = emptyList(),
                    error = (e.message ?: "Unknown error")
                )
            }
        }
    }
}
