package com.example.peggle.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.peggle.data.LevelData
import com.example.peggle.data.PegTemplate
import com.example.peggle.model.PeggleMaster

@Entity(tableName = "level_configs")
data class LevelConfigEntity(
    @PrimaryKey
    val id: String,
    val stageNumber: Int,
    val title: String,
    val subtitle: String,
    val masterName: String,
    val bgGradientStart: Long,
    val bgGradientEnd: Long,
    val bgDrawableRes: Int,
    val targetOrangePegs: Int = 25,
    val targetGreenPegs: Int = 2,
    val targetPurplePegs: Int = 1,
    val initialBallCount: Int = 10,
    val parScore: Int = 100_000,
    val pegLayoutJson: String
) {
    fun getMaster(): PeggleMaster {
        return try {
            PeggleMaster.valueOf(masterName)
        } catch (_: Exception) {
            PeggleMaster.BJORN
        }
    }

    fun getPegTemplates(): List<PegTemplate> {
        return PegLayoutSerializer.deserialize(pegLayoutJson)
    }

    fun toLevelData(): LevelData {
        return LevelData(
            id = id,
            stageNumber = stageNumber,
            title = title,
            subtitle = subtitle,
            master = getMaster(),
            backgroundGradient = Pair(bgGradientStart, bgGradientEnd),
            bgDrawableRes = bgDrawableRes,
            templates = getPegTemplates()
        )
    }

    companion object {
        fun fromLevelData(
            level: LevelData,
            targetOrangePegs: Int = 25,
            targetGreenPegs: Int = 2,
            targetPurplePegs: Int = 1,
            initialBallCount: Int = 10,
            parScore: Int = 100_000
        ): LevelConfigEntity {
            return LevelConfigEntity(
                id = level.id,
                stageNumber = level.stageNumber,
                title = level.title,
                subtitle = level.subtitle,
                masterName = level.master.name,
                bgGradientStart = level.backgroundGradient.first,
                bgGradientEnd = level.backgroundGradient.second,
                bgDrawableRes = level.bgDrawableRes,
                targetOrangePegs = targetOrangePegs,
                targetGreenPegs = targetGreenPegs,
                targetPurplePegs = targetPurplePegs,
                initialBallCount = initialBallCount,
                parScore = parScore,
                pegLayoutJson = PegLayoutSerializer.serialize(level.templates)
            )
        }
    }
}
