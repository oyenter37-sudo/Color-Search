package com.example.data

import com.example.data.local.FoundColorDao
import com.example.data.local.FoundColorEntity
import com.example.model.HuntColor
import kotlinx.coroutines.flow.Flow

class ColorRepository(private val dao: FoundColorDao) {

    val allFound: Flow<List<FoundColorEntity>> = dao.getAllFound()
    val distinctCount: Flow<Int> = dao.getDistinctColorCount()
    val totalFoundCount: Flow<Int> = dao.getTotalFoundCount()

    suspend fun recordCapture(
        target: HuntColor,
        sampledR: Int,
        sampledG: Int,
        sampledB: Int,
        accuracy: Float
    ): Long {
        val hexFound = com.example.util.ColorUtils.toHex(sampledR, sampledG, sampledB)
        val entity = FoundColorEntity(
            colorId = target.id,
            colorName = target.nameRu,
            hexTarget = target.hex,
            hexFound = hexFound,
            category = target.category.displayNameRu,
            accuracy = accuracy
        )
        return dao.insertFound(entity)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
