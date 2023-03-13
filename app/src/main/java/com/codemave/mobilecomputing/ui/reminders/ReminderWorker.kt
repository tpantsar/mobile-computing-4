package com.codemave.mobilecomputing.ui.reminders

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    userParameters: WorkerParameters
) : Worker(context, userParameters) {

    override fun doWork(): Result {
        return try {
            println("Reminder fired")
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

}