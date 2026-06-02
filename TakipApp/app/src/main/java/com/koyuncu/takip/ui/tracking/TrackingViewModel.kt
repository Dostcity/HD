package com.koyuncu.takip.ui.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koyuncu.takip.data.local.TrackedProductEntity
import com.koyuncu.takip.data.repo.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrackingViewModel(private val repo: ProductRepository) : ViewModel() {

    val products: StateFlow<List<TrackedProductEntity>> =
        repo.products.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _checking = MutableStateFlow(false)
    val checking: StateFlow<Boolean> = _checking.asStateFlow()

    /** Son "Kontrol et" teşhis raporu (her ürün için durum). */
    private val _report = MutableStateFlow<String?>(null)
    val report: StateFlow<String?> = _report.asStateFlow()

    fun add(name: String, url: String, targetPrice: Double?) =
        viewModelScope.launch {
            if (name.isNotBlank()) repo.add(name, url, targetPrice)
        }

    fun delete(product: TrackedProductEntity) =
        viewModelScope.launch { repo.delete(product) }

    fun checkNow() = viewModelScope.launch {
        _checking.value = true
        try {
            val results = repo.checkAll()
            _report.value = if (results.isEmpty()) {
                "Takip edilen ürün yok."
            } else {
                results.joinToString("\n") { "• ${it.product.name}: ${it.note}" }
            }
        } catch (t: Throwable) {
            _report.value = "Hata: ${t.message}"
        } finally {
            _checking.value = false
        }
    }
}
