package com.notisaver.main.log.core

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.notisaver.misc.createPackageHashcode
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter


class MessageAsset(context: Context) {
    private val file = File(context.filesDir, "message_json")

    private val shareRefers =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    private var messageJs: JSONObject? = null

    init {
        if (!file.exists()) {
            file.createNewFile()
        }

        try {
            if (!shareRefers.getBoolean("MDefault", false)) {
                writeDefaultInFile()
                shareRefers.edit(true) {
                    putBoolean("MDefault", true)
                }
            } else {
                readDefaultFromFile()
            }
        } catch (_: Exception) {

        }
    }


    @Throws
    private fun writeDefaultInFile() {
        val jsO = JSONObject().apply {
            put(createPackageHashcode("com.facebook.orca"), false)
            put(createPackageHashcode("com.whatsapp"), false)
            put(createPackageHashcode("com.twitter.android"), false)
            put(createPackageHashcode("com.discord"), false)
            put(createPackageHashcode("com.facebook.mlite"), false)
            put(createPackageHashcode("com.zing.zalo"), false)
            put(createPackageHashcode("jp.naver.line.android"), false)
            put(createPackageHashcode("com.tencent.mm"), false)
            put(createPackageHashcode("com.skype.raider"), false)
            put(createPackageHashcode("com.viber.voip"), false)
            put(createPackageHashcode("com.whatsapp.w4b"), false)
            put(createPackageHashcode("com.microsoft.teams"), false)
            put(createPackageHashcode("com.tencent.mm"), false)
            put(createPackageHashcode("org.thoughtcrime.securesms"), false)
            put(createPackageHashcode("com.kakao.talk"), false)
            put(createPackageHashcode("com.imo.android.imoim"), false)
            put(createPackageHashcode("com.google.android.apps.dynamite"), false)
            put(createPackageHashcode("com.google.android.apps.messaging"), false)
            put(createPackageHashcode("com.chating.messages.chat.fun"), false)
            put(createPackageHashcode("com.samsung.android.messaging"), false)
            put(createPackageHashcode("com.whatsapp.w4b"), false)
            put(createPackageHashcode("com.whatsapp.w4b"), false)
            put(createPackageHashcode("com.pinger.textfree"), false)
        }

        messageJs = jsO

        BufferedWriter(FileWriter(file)).also {
            it.write(jsO.toString())
        }.close()
    }

    @kotlin.jvm.Throws
    internal fun isPrepared(packageHashcode: String): Boolean {
        return messageJs?.getBoolean(packageHashcode) ?: false
    }

    internal fun updatePrepared(packageHashcode: String) {
        messageJs?.put(packageHashcode, true)
        if (messageJs != null) {
            BufferedWriter(FileWriter(file)).also {
                it.write(messageJs.toString())
            }.close()
        }
    }

    @Throws
    private fun readDefaultFromFile() {
        val fileReader = FileReader(file)
        val bufferedReader = BufferedReader(fileReader)
        val stringBuilder = StringBuilder()
        var line = bufferedReader.readLine()
        while (line != null) {
            stringBuilder.append(line).append("\n")
            line = bufferedReader.readLine()
        }
        bufferedReader.close()

        messageJs = JSONObject(stringBuilder.toString())
    }
}