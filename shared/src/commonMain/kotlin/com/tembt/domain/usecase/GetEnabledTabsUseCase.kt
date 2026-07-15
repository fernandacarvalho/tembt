package com.tembt.domain.usecase

import com.tembt.domain.model.AppTab
import com.tembt.domain.model.FeatureFlag
import com.tembt.domain.repository.FeatureFlagRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class GetEnabledTabsUseCase(
    private val repository: FeatureFlagRepository,
    private val fetchTimeoutMillis: Long = DEFAULT_FETCH_TIMEOUT_MILLIS,
) {

    suspend operator fun invoke(): List<AppTab> {
        val fetched = try {
            withTimeout(fetchTimeoutMillis) { repository.fetchAndActivate() }
            true
        } catch (e: TimeoutCancellationException) {
            false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }

        if (!fetched) {
            try {
                repository.activateLastFetched()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // No previously fetched config — in-app defaults apply
            }
        }

        return buildList {
            add(AppTab.MAP)
            if (repository.isEnabled(FeatureFlag.TAB_SCHEDULE)) add(AppTab.SCHEDULE)
            if (repository.isEnabled(FeatureFlag.TAB_TOURNAMENTS)) add(AppTab.TOURNAMENTS)
        }
    }

    companion object {
        const val DEFAULT_FETCH_TIMEOUT_MILLIS = 3_000L
    }
}
