package com.notisaver.main.protection

import android.content.Context
import android.util.Base64
import androidx.annotation.StringRes
import androidx.core.content.edit
import com.notisaver.R
import timber.log.Timber

// avoid change this class
class LocalPasswordManager private constructor(context: Context) {

    companion object {
        internal const val SKIP_QUESTION = 0
        internal const val QUESTION_CREATED = 1

        internal const val MIN_PASSWORD_LENGTH = 6
        private const val MAX_PASSWORD_CONFIRM = 5

        private const val PASSWORD_KEY = "pw_key"
        private const val INIT_PASSWORD = "pw_init"
        private const val SECURITY_QUESTION_STATE = "init_question"
        private const val PASSWORD_STORE = "user_verification"
        private const val SECURITY_QUESTION = "security_question"

        @Volatile
        private var Instances: LocalPasswordManager? = null

        internal fun getInstance(context: Context): LocalPasswordManager {
            if (Instances == null) {
                synchronized(this) {
                    Instances = LocalPasswordManager(context)
                }
            }
            return Instances!!
        }
    }


    private val securityQuestionCode = arrayOf(
        0x0A1, 0x0A2, 0x0A3, 0x0A4, 0x0A5, 0x0A6, 0x0A7
    )

    private val applicationContext = context.applicationContext

    private val passwordStore = applicationContext.getSharedPreferences(
        encodeToString(PASSWORD_STORE), Context.MODE_PRIVATE
    )

    @Throws
    internal fun createPassword(password: String, confirmPassword: String) {
        validatePasswords(password, confirmPassword)
        val key = encodeToString(PASSWORD_KEY)
        val encode = encodeToString(password)
        passwordStore.edit(true) {
            putString(key, encode)
            putBoolean(INIT_PASSWORD, true)
        }
    }

    internal fun getPasswordEncoded(): String? {
        return passwordStore.getString(encodePasswordKey(), null)
    }

    @Throws(
        IntruderSecurityException::class,
        ConfirmWrongException::class,
        PasswordSetupException::class
    )
    fun confirmPassword(password: String) {
        val formatPassword: String = encodeToString(password)
        val securityPassword = getPasswordEncoded() ?: throw PasswordSetupException()

        if (formatPassword != securityPassword) {
            throw ConfirmWrongException("Password is wrong")
        }
    }

    internal fun isCreatedPassword(): Boolean {
        return passwordStore.getBoolean(INIT_PASSWORD, false)
    }

    internal fun clear() {
        passwordStore.edit(true) {
            clear()
        }
    }

    private fun encodePasswordKey(): String = encodeToString(PASSWORD_KEY)

    private fun encodeToString(string: String): String {
        return Base64.encodeToString(string.toByteArray(), Base64.NO_WRAP)
    }

//    private fun decodeToString(input: String): String {
//        return String(Base64.decode(input, Base64.NO_WRAP))
//    }

    @Throws
    private fun validatePasswords(password: String, confirmPassword: String) {
        if (!validatePasswordLength(password)) throw PasswordLengthException()
        if (password != confirmPassword) throw PasswordNotMatchException()
    }

    private fun validatePasswordLength(password: CharSequence?): Boolean {
        if (password == null) return false
        return password.trim().length >= MIN_PASSWORD_LENGTH
    }

    internal fun getSecurityQuestionIds() = arrayListOf(
        R.string.question_where_born,
        R.string.question_father_name,
        R.string.question_mother_name,
        R.string.question_date_birthday,
        R.string.question_favorite_color,
        R.string.question_favorite_number,
        R.string.question_hometown
    )

    internal fun getSecurityQuestionCode(questionRes: Int): Int {
        return when (questionRes) {
            R.string.question_where_born -> securityQuestionCode[0]
            R.string.question_father_name -> securityQuestionCode[1]
            R.string.question_mother_name -> securityQuestionCode[2]
            R.string.question_date_birthday -> securityQuestionCode[3]
            R.string.question_favorite_color -> securityQuestionCode[4]
            R.string.question_favorite_number -> securityQuestionCode[5]
            R.string.question_hometown -> securityQuestionCode[6]
            else -> throw IllegalArgumentException()
        }
    }

    @StringRes
    private fun questionCodeToStringId(questionCode: Int): Int {
        return when (questionCode) {
            securityQuestionCode[0] -> R.string.question_where_born
            securityQuestionCode[1] -> R.string.question_father_name
            securityQuestionCode[2] -> R.string.question_mother_name
            securityQuestionCode[3] -> R.string.question_date_birthday
            securityQuestionCode[4] -> R.string.question_favorite_color
            securityQuestionCode[5] -> R.string.question_favorite_number
            securityQuestionCode[6] -> R.string.question_hometown
            else -> throw IllegalArgumentException()
        }
    }

    internal fun saveSecurityAnswer(questionCode: Int, answer: String) {
        val encode = encodeToString(answer)
        val endText = "$encode.$questionCode"
        passwordStore.edit(true) {
            putString(encodeToString(SECURITY_QUESTION), endText)
            putInt(SECURITY_QUESTION_STATE, QUESTION_CREATED)
        }
    }

    internal fun skipSecurityQuestion() {
        passwordStore.edit(true) {
            putInt(SECURITY_QUESTION_STATE, SKIP_QUESTION)
        }
    }

    @StringRes
    fun getQuestionTextId(): Int? {
        val encode = getEncodeAnswer()

        return encode?.let {
            val questionCode = it.substring(it.lastIndexOf('.') + 1).also {adad ->
                Timber.d("QuestionCode: $adad")
            }
            questionCodeToStringId(questionCode.toInt())
        }
    }

    @Throws
    internal fun confirmAnswerSecurityQuestion(answer: String) {
        val answerOrigin = getEncodeAnswer()?.let {
            it.substring(0, it.lastIndexOf('.'))
        } ?: throw SecurityQuestionSetupException()
        val answerExpected = encodeToString(answer)

        if (answerOrigin != answerExpected) {
            throw ConfirmWrongException("The answer is wrong")
        }
    }

    private fun getEncodeAnswer() = passwordStore.getString(
        encodeToString(SECURITY_QUESTION), null
    )

    internal val isSecurityQuestionSetup
        get() = passwordStore.getInt(SECURITY_QUESTION_STATE, -1) == QUESTION_CREATED

    internal val isSecurityQuestionSkipped
        get() = passwordStore.getInt(SECURITY_QUESTION_STATE, -1) == SKIP_QUESTION

    class IntruderSecurityException : RuntimeException("Intruders!")
    class ConfirmWrongException(text: String): RuntimeException(text)
    class PasswordNotMatchException : RuntimeException("Password is not match")
    class PasswordLengthException : RuntimeException("Password is not enough length")
    class PasswordSetupException : RuntimeException("Password is not created")
    class SecurityQuestionSetupException : RuntimeException("Security question is not setup")
}