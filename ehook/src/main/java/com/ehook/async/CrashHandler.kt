package com.ehook.async

import com.ehook.helper.defaultFormat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.concurrent.Executors

class CrashHandler: Thread.UncaughtExceptionHandler {

    interface Callback {
        fun handException(e: Throwable)
    }

    companion object {

        private var instance: CrashHandler? = null

        fun newInstance(): CrashHandler {
            if (CrashHandler.Companion.instance == null) {
                CrashHandler.Companion.instance =
                    CrashHandler()
            }
            return CrashHandler.Companion.instance!!
        }
    }

    var callback: CrashHandler.Callback? = null

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        var tmpThrowable: Throwable? = null
        if (e.message == null) {
            val builder = StringBuilder(256)
            builder.append("\n")
            for (element in e.getStackTrace()) {
                builder.append(element.toString()).append("\n")
            }
            tmpThrowable = Throwable(builder.toString(), e)
        }
        if (handleException(tmpThrowable ?: e)) callback?.handException(tmpThrowable ?: e)
    }

    private fun handleException(e: Throwable?): Boolean {
        return if (e == null) false else try {
            Executors.newSingleThreadExecutor().submit {
                try {
                    val file = File(com.ehook.cache.LRUCache.cachePath("crash", "crash_" + Date().defaultFormat() + ".log"))
                    file.createNewFile()
                    val fos = FileOutputStream(file)
                    fos.write(e.message?.toByteArray(Charsets.UTF_8) ?: byteArrayOf())
                    fos.close()
                } catch (e: IOException) {
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

}