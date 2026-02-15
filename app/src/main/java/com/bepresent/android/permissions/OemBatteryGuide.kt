package com.bepresent.android.permissions

import android.os.Build

data class OemBatteryInstruction(
    val manufacturer: String,
    val steps: String
)

object OemBatteryGuide {

    fun getInstructions(): OemBatteryInstruction? {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer in listOf("xiaomi", "redmi", "poco") -> OemBatteryInstruction(
                manufacturer = "Xiaomi",
                steps = "Settings > Apps > Manage Apps > BePresent > Battery Saver > No restrictions"
            )
            manufacturer in listOf("huawei", "honor") -> OemBatteryInstruction(
                manufacturer = "Huawei",
                steps = "Settings > Apps > BePresent > Battery > App Launch > Manual (all toggles ON)"
            )
            manufacturer == "samsung" -> OemBatteryInstruction(
                manufacturer = "Samsung",
                steps = "Settings > Battery > Background usage limits > Never sleeping apps > Add BePresent"
            )
            manufacturer in listOf("oppo", "realme") -> OemBatteryInstruction(
                manufacturer = "Oppo",
                steps = "Settings > Battery > App Quick Freeze > disable for BePresent"
            )
            manufacturer == "oneplus" -> OemBatteryInstruction(
                manufacturer = "OnePlus",
                steps = "Settings > Battery > Battery Optimization > BePresent > Don't optimize"
            )
            manufacturer == "vivo" -> OemBatteryInstruction(
                manufacturer = "Vivo",
                steps = "Settings > Battery > Background Power Consumption > BePresent > Off"
            )
            else -> null
        }
    }
}
