package com.rve.rvkernelmanager.ui.ViewModel

import android.content.Context
import android.content.BroadcastReceiver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.rve.rvkernelmanager.utils.Utils
import com.rve.rvkernelmanager.utils.BatteryUtils

class BatteryViewModel : ViewModel() {
    private val _battTech = MutableStateFlow("")
    val battTech: StateFlow<String> = _battTech

    private val _battHealth = MutableStateFlow("")
    val battHealth: StateFlow<String> = _battHealth

    private val _battTemp = MutableStateFlow("")
    val battTemp: StateFlow<String> = _battTemp

    private val _battVoltage = MutableStateFlow("")
    val battVoltage: StateFlow<String> = _battVoltage

    private val _battDesignCapacity = MutableStateFlow("")
    val battDesignCapacity: StateFlow<String> = _battDesignCapacity

    private val _battMaximumCapacity = MutableStateFlow("")
    val battMaximumCapacity: StateFlow<String> = _battMaximumCapacity

    private val _isEnableChargingChecked = MutableStateFlow(false)
    val isEnableChargingChecked: StateFlow<Boolean> = _isEnableChargingChecked

    private val _isFastChargingChecked = MutableStateFlow(false)
    val isFastChargingChecked: StateFlow<Boolean> = _isFastChargingChecked

    private val _hasEnableCharging = MutableStateFlow(false)
    val hasEnableCharging: StateFlow<Boolean> = _hasEnableCharging

    private val _hasFastCharging = MutableStateFlow(false)
    val hasFastCharging: StateFlow<Boolean> = _hasFastCharging

    private var tempReceiver: BroadcastReceiver? = null
    private var voltageReceiver: BroadcastReceiver? = null
    private var maxCapacityReceiver: BroadcastReceiver? = null

    fun loadBatteryInfo(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _battTech.value = BatteryUtils.getBatteryTechnology(context)
                _battHealth.value = BatteryUtils.getBatteryHealth(context)
                _battDesignCapacity.value = BatteryUtils.getBatteryDesignCapacity(context)
            }
        }
    }

    fun registerBatteryListeners(context: Context) {
        viewModelScope.launch {
            tempReceiver = BatteryUtils.registerBatteryTemperatureListener(context) { temp ->
                _battTemp.value = temp
            }
            voltageReceiver = BatteryUtils.registerBatteryVoltageListener(context) { voltage ->
                _battVoltage.value = voltage
            }
            maxCapacityReceiver = BatteryUtils.registerBatteryCapacityListener(context) { maxCapacity ->
                _battMaximumCapacity.value = maxCapacity
            }
        }
    }

    fun unregisterBatteryListeners(context: Context) {
        viewModelScope.launch {
            tempReceiver?.let { context.unregisterReceiver(it) }
            voltageReceiver?.let { context.unregisterReceiver(it) }
            maxCapacityReceiver?.let { context.unregisterReceiver(it) }
        }
    }

    fun checkChargingFiles() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _hasEnableCharging.value = Utils.testFile(BatteryUtils.ENABLE_CHARGING_PATH)
                _hasFastCharging.value = Utils.testFile(BatteryUtils.FAST_CHARGING_PATH)
                _isEnableChargingChecked.value = Utils.readFile(BatteryUtils.ENABLE_CHARGING_PATH) == "1"
                _isFastChargingChecked.value = Utils.readFile(BatteryUtils.FAST_CHARGING_PATH) == "1"
            }
        }
    }

    fun toggleEnableCharging(checked: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val success = Utils.writeFile(BatteryUtils.ENABLE_CHARGING_PATH, if (checked) "1" else "0")
                if (success) {
                    _isEnableChargingChecked.value = checked
                }
            }
        }
    }

    fun toggleFastCharging(checked: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val success = Utils.writeFile(BatteryUtils.FAST_CHARGING_PATH, if (checked) "1" else "0")
                if (success) {
                    _isFastChargingChecked.value = checked
                }
            }
        }
    }
}
