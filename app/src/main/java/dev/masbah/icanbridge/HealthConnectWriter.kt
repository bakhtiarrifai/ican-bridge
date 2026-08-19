package dev.masbah.icanbridge

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.BloodGlucose
import java.time.Instant
import java.time.ZoneId

class HealthConnectWriter(private val context: Context) {

    companion object {
        val PERMISSIONS = setOf(HealthPermission.getWritePermission(BloodGlucoseRecord::class))
    }

    fun availability(): Int = HealthConnectClient.getSdkStatus(context)

    private fun client(): HealthConnectClient = HealthConnectClient.getOrCreate(context)

    suspend fun hasPermission(): Boolean =
        client().permissionController.getGrantedPermissions().containsAll(PERMISSIONS)

    suspend fun write(mgdl: Int, atMs: Long) {
        val time = Instant.ofEpochMilli(atMs)
        val zoneOffset = ZoneId.systemDefault().rules.getOffset(time)
        val record = BloodGlucoseRecord(
            time = time,
            zoneOffset = zoneOffset,
            level = BloodGlucose.milligramsPerDeciliter(mgdl.toDouble()),
            specimenSource = BloodGlucoseRecord.SPECIMEN_SOURCE_WHOLE_BLOOD,
            mealType = MealType.MEAL_TYPE_UNKNOWN,
            relationToMeal = BloodGlucoseRecord.RELATION_TO_MEAL_GENERAL,
            metadata = Metadata(
                device = Device(manufacturer = "Sinocare", model = "iCan i3", type = Device.TYPE_UNKNOWN)
            )
        )
        client().insertRecords(listOf(record))
    }
}
