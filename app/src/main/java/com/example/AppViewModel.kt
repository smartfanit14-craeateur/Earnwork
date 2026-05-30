package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application) {
    
    private val prefs = PreferencesManager(application)

    // Reactive states
    private val _coins = MutableStateFlow(prefs.coins)
    val coins: StateFlow<Int> = _coins.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(prefs.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.email)
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.language)
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _appCurrency = MutableStateFlow(prefs.currency)
    val appCurrency: StateFlow<String> = _appCurrency.asStateFlow()

    private val _currentTab = MutableStateFlow("dashboard")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Activity log list for display inside Dashboard and Wallet
    private val _activityLogs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val activityLogs: StateFlow<List<ActivityLog>> = _activityLogs.asStateFlow()

    // Ad playback overlay state
    private val _adOverlay = MutableStateFlow<AdOverlayState?>(null)
    val adOverlay: StateFlow<AdOverlayState?> = _adOverlay.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private var adTimerJob: Job? = null

    init {
        // Initialize AdMob SDK safely when AppViewModel is created
        AdMediationManager.initialize(application)
        
        // Populate standard initial activity logs
        generateInitialLogs()
    }

    private fun generateInitialLogs() {
        val list = mutableListOf<ActivityLog>()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        
        // Add 2 mock history entries to make the app feel alive on first launch
        list.add(
            ActivityLog(
                descriptionEn = "Daily Check-in Reward credited",
                descriptionAr = "تم إضافة مكافأة تسجيل الدخول اليومي",
                coinsAdded = 50,
                timestamp = sdf.format(Date(System.currentTimeMillis() - 3600000 * 2))
            )
        )
        list.add(
            ActivityLog(
                descriptionEn = "Welcome Bonus credited",
                descriptionAr = "تم إضافة مكافأة الترحيب بالدخول",
                coinsAdded = 100,
                timestamp = sdf.format(Date(System.currentTimeMillis() - 3600000 * 5))
            )
        )
        // If they have existing coins of larger scale, let's backfill
        if (_coins.value == 0) {
            _coins.value = 150
            prefs.coins = 150
        }
        _activityLogs.value = list
    }

    fun login(emailInput: String, passwordInput: String): Boolean {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            _loginError.value = "empty_error"
            return false
        }
        if (emailInput.contains("@") && passwordInput.length >= 6) {
            prefs.isLoggedIn = true
            prefs.email = emailInput
            _isLoggedIn.value = true
            _userEmail.value = emailInput
            _loginError.value = null
            return true
        } else {
            _loginError.value = "invalid_credentials"
            return false
        }
    }

    fun loginAsGuest() {
        prefs.isLoggedIn = true
        prefs.email = "guest_user@paidwork.test"
        _isLoggedIn.value = true
        _userEmail.value = "guest_user@paidwork.test"
        _loginError.value = null
    }

    fun logout() {
        prefs.reset()
        _isLoggedIn.value = false
        _userEmail.value = "guest@company.com"
        _coins.value = 150
        prefs.coins = 150
        _currentTab.value = "dashboard"
        generateInitialLogs()
    }

    fun changeLanguage(lang: String) {
        prefs.language = lang
        _appLanguage.value = lang
    }

    fun changeCurrency(currencyValue: String) {
        prefs.currency = currencyValue
        _appCurrency.value = currencyValue
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    // Trigger full-screen ad simulation or load real ad if AdMob
    fun watchRewardedAd(networkName: String, testId: String) {
        adTimerJob?.cancel()
        
        _adOverlay.value = AdOverlayState(
            networkName = networkName,
            remainingSeconds = 15,
            testId = testId,
            isCompleted = false
        )

        // Launch absolute 15 second timer simulation to guarantee coins reward state
        adTimerJob = viewModelScope.launch {
            for (i in 14 downTo 0) {
                delay(1000)
                _adOverlay.value = _adOverlay.value?.copy(remainingSeconds = i)
            }
            // Finished! Provide coins
            _adOverlay.value = _adOverlay.value?.copy(isCompleted = true, remainingSeconds = 0)
            
            // Add 100 coins
            val currentCoins = _coins.value
            val newCoins = currentCoins + 100
            prefs.coins = newCoins
            _coins.value = newCoins

            // Put a real activity log entry
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val newLog = ActivityLog(
                descriptionEn = "Watched $networkName rewarded ad video",
                descriptionAr = "شاهدت إعلان فيديو بمكافأة من $networkName",
                coinsAdded = 100,
                timestamp = sdf.format(Date())
            )
            val updatedLogs = listOf(newLog) + _activityLogs.value
            _activityLogs.value = updatedLogs

            // Reload ad unit in the background using AdMob unit reference
            AdMediationManager.loadAdMobRewardedAd(getApplication())
        }
    }

    fun dismissAdOverlay() {
        adTimerJob?.cancel()
        _adOverlay.value = null
    }

    fun claimRewards() {
        _adOverlay.value = null
    }

    // Dynamic helper to calculate currency value of the coins
    // 100 coins = $1.00 USD, or EGP / SAR equivalents
    fun getCoinsValue(coinsRaw: Int, currencyMode: String): String {
        val usdVal = coinsRaw * 0.01 // 100 coins = 1 USD
        return when (currencyMode) {
            "EGP" -> {
                val egpVal = usdVal * 47.50 // Mock exchange rate EGP
                String.format(Locale.US, "%.2f EGP", egpVal)
            }
            "SAR" -> {
                val sarVal = usdVal * 3.75 // Mock exchange rate SAR
                String.format(Locale.US, "%.2f SAR", sarVal)
            }
            else -> {
                String.format(Locale.US, "$%.2f USD", usdVal)
            }
        }
    }

    // Single trigger for manually doing check-ins
    fun dailyCheckIn(): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = sdf.format(Date())
        
        // Simple logic allowing them to check in to make application feel great
        val newCoins = _coins.value + 50
        prefs.coins = newCoins
        _coins.value = newCoins

        val dateTimeSdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val newLog = ActivityLog(
            descriptionEn = "Daily Reward Checked-in (+50 Coins)",
            descriptionAr = "تم تسجيل الحضور اليومي للمكافأة (+50 عملة)",
            coinsAdded = 50,
            timestamp = dateTimeSdf.format(Date())
        )
        _activityLogs.value = listOf(newLog) + _activityLogs.value
        return true
    }
}

data class ActivityLog(
    val descriptionEn: String,
    val descriptionAr: String,
    val coinsAdded: Int,
    val timestamp: String
)

data class AdOverlayState(
    val networkName: String,
    val remainingSeconds: Int,
    val testId: String,
    val isCompleted: Boolean
)
