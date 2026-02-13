package com.descope.testapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.descope.testapp.ui.screens.magiclink.MagicLinkDeliveryMethod
import com.descope.testapp.ui.screens.otp.OtpDeliveryMethod

/**
 * Manages persistence of last used authentication data.
 * This allows the app to pre-fill fields with previously used values.
 */
class LastUsedPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // ==================== OTP ====================

    /**
     * Save the last used OTP data.
     * @param deliveryMethod The delivery method used (EMAIL or PHONE)
     * @param value The email address or phone number used
     */
    fun saveLastUsedOtp(deliveryMethod: OtpDeliveryMethod, value: String) {
        prefs.edit {
            putString(KEY_OTP_DELIVERY_METHOD, deliveryMethod.name)
            putString(KEY_OTP_VALUE, value)
        }
    }

    /**
     * Get the last used OTP delivery method.
     * @return The last used delivery method, or null if none saved
     */
    fun getLastUsedOtpDeliveryMethod(): OtpDeliveryMethod? {
        val methodName = prefs.getString(KEY_OTP_DELIVERY_METHOD, null) ?: return null
        return try {
            OtpDeliveryMethod.valueOf(methodName)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    /**
     * Get the last used OTP value (email or phone).
     * @return The last used value, or null if none saved
     */
    fun getLastUsedOtpValue(): String? {
        return prefs.getString(KEY_OTP_VALUE, null)
    }

    // ==================== Magic Link ====================

    /**
     * Save the last used Magic Link data.
     * @param deliveryMethod The delivery method used (EMAIL or PHONE)
     * @param value The email address or phone number used
     */
    fun saveLastUsedMagicLink(deliveryMethod: MagicLinkDeliveryMethod, value: String) {
        prefs.edit {
            putString(KEY_MAGIC_LINK_DELIVERY_METHOD, deliveryMethod.name)
            putString(KEY_MAGIC_LINK_VALUE, value)
        }
    }

    /**
     * Get the last used Magic Link delivery method.
     * @return The last used delivery method, or null if none saved
     */
    fun getLastUsedMagicLinkDeliveryMethod(): MagicLinkDeliveryMethod? {
        val methodName = prefs.getString(KEY_MAGIC_LINK_DELIVERY_METHOD, null) ?: return null
        return try {
            MagicLinkDeliveryMethod.valueOf(methodName)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    /**
     * Get the last used Magic Link value (email or phone).
     * @return The last used value, or null if none saved
     */
    fun getLastUsedMagicLinkValue(): String? {
        return prefs.getString(KEY_MAGIC_LINK_VALUE, null)
    }

    // ==================== Enchanted Link ====================

    /**
     * Save the last used Enchanted Link email.
     * @param email The email address used
     */
    fun saveLastUsedEnchantedLinkEmail(email: String) {
        prefs.edit {
            putString(KEY_ENCHANTED_LINK_EMAIL, email)
        }
    }

    /**
     * Get the last used Enchanted Link email.
     * @return The last used email, or null if none saved
     */
    fun getLastUsedEnchantedLinkEmail(): String? {
        return prefs.getString(KEY_ENCHANTED_LINK_EMAIL, null)
    }

    // ==================== Password ====================

    /**
     * Save the last used Password email.
     * @param email The email address used
     */
    fun saveLastUsedPasswordEmail(email: String) {
        prefs.edit {
            putString(KEY_PASSWORD_EMAIL, email)
        }
    }

    /**
     * Get the last used Password email.
     * @return The last used email, or null if none saved
     */
    fun getLastUsedPasswordEmail(): String? {
        return prefs.getString(KEY_PASSWORD_EMAIL, null)
    }

    // ==================== Passkey ====================

    /**
     * Save the last used Passkey Login ID.
     * @param loginId The login ID used
     */
    fun saveLastUsedPasskeyLoginId(loginId: String) {
        prefs.edit {
            putString(KEY_PASSKEY_LOGIN_ID, loginId)
        }
    }

    /**
     * Get the last used Passkey Login ID.
     * @return The last used login ID, or null if none saved
     */
    fun getLastUsedPasskeyLoginId(): String? {
        return prefs.getString(KEY_PASSKEY_LOGIN_ID, null)
    }

    // ==================== TOTP ====================

    /**
     * Save the last used TOTP Login ID.
     * @param loginId The login ID used
     */
    fun saveLastUsedTotpLoginId(loginId: String) {
        prefs.edit {
            putString(KEY_TOTP_LOGIN_ID, loginId)
        }
    }

    /**
     * Get the last used TOTP Login ID.
     * @return The last used login ID, or null if none saved
     */
    fun getLastUsedTotpLoginId(): String? {
        return prefs.getString(KEY_TOTP_LOGIN_ID, null)
    }

    // ==================== General ====================

    /**
     * Clear all last used data.
     */
    fun clearAll() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "last_used_prefs"
        private const val KEY_OTP_DELIVERY_METHOD = "otp_delivery_method"
        private const val KEY_OTP_VALUE = "otp_value"
        private const val KEY_MAGIC_LINK_DELIVERY_METHOD = "magic_link_delivery_method"
        private const val KEY_MAGIC_LINK_VALUE = "magic_link_value"
        private const val KEY_ENCHANTED_LINK_EMAIL = "enchanted_link_email"
        private const val KEY_PASSWORD_EMAIL = "password_email"
        private const val KEY_PASSKEY_LOGIN_ID = "passkey_login_id"
        private const val KEY_TOTP_LOGIN_ID = "totp_login_id"
    }
}
