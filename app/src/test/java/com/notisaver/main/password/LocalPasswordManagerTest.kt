package com.notisaver.main.password

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.notisaver.main.protection.LocalPasswordManager
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class LocalPasswordManagerTest {


    @Test
    fun test_create_password_length_failed() {
        val localPasswordManager = LocalPasswordManager.getInstance(
            ApplicationProvider.getApplicationContext()
        )

        try {
            localPasswordManager.createPassword("123", "123")
        } catch (e: Exception) {
            Assert.assertTrue(e is LocalPasswordManager.PasswordLengthException)
        }
    }

    @Test
    fun test_create_password_validate_failed() {
        val localPasswordManager = LocalPasswordManager.getInstance(
            ApplicationProvider.getApplicationContext()
        )

        try {
            localPasswordManager.createPassword("1234567", "123456")
        } catch (e: Exception) {
            Assert.assertTrue(e is LocalPasswordManager.PasswordNotMatchException)
        }
    }

    @Test
    fun test_password_is_created() {

        val localPasswordManager = LocalPasswordManager.getInstance(
            ApplicationProvider.getApplicationContext()
        )

        localPasswordManager.createPassword("123456", "123456")

        Assert.assertTrue(localPasswordManager.isCreatedPassword())

        val passwordEncoded = localPasswordManager.getPasswordEncoded()

        Assert.assertNotEquals(
            passwordEncoded, null
        )

        Assert.assertNotEquals(
            passwordEncoded, "123456"
        )
    }

    @Test
    fun test_clear_password() {
        val localPasswordManager = LocalPasswordManager.getInstance(
            ApplicationProvider.getApplicationContext()
        )

        localPasswordManager.clear()

        Assert.assertFalse(localPasswordManager.isCreatedPassword())

        Assert.assertEquals(
            localPasswordManager.getPasswordEncoded(), null
        )
    }

    @Test
    fun test_confirm_password_failed() {
        val localPasswordManager = LocalPasswordManager.getInstance(
            ApplicationProvider.getApplicationContext()
        )

        localPasswordManager.createPassword("123456", "123456")

        try {
            localPasswordManager.confirmPassword("1234567")
        } catch (e: Exception) {
            Assert.assertTrue(e is LocalPasswordManager.ConfirmWrongException)
        }
    }

    @Test
    fun test_security_question_skipped() {
        val localPasswordManager = LocalPasswordManager.getInstance(
            ApplicationProvider.getApplicationContext()
        )

        localPasswordManager.skipSecurityQuestion()

        Assert.assertTrue(
            localPasswordManager.isSecurityQuestionSkipped
        )
    }
}