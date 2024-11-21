package com.librarysdk.sdk.workers

import android.content.Context
import androidx.work.*
import java.util.Date


class UploadWorker(context: Context, workerParams: WorkerParameters):
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val date = Date()
        println("Uploading $date")
        return Result.success()
    }

}
