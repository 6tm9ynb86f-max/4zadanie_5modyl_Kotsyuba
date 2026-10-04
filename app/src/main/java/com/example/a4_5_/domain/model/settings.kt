package com.example.a4_5_.domain.model

data class TodoSettings(
    val highlightCompleted: Boolean = true,
    val completedColor: Long = DEFAULT_COMPLETED_COLOR
) {
    companion object {
        const val DEFAULT_COMPLETED_COLOR: Long = 0xFFE2F0D9L
    }
}