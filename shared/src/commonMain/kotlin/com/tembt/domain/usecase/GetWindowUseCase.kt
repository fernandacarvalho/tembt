package com.tembt.domain.usecase

import com.tembt.domain.model.ScheduleWindow
import com.tembt.domain.repository.WindowRepository

class GetWindowUseCase(private val windowRepository: WindowRepository) {
    suspend operator fun invoke(): Result<ScheduleWindow> = windowRepository.getWindow()
}
