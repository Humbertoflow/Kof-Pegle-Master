package com.example.peggle.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {

    @Query("SELECT * FROM level_configs ORDER BY stageNumber ASC")
    fun getAllLevelsFlow(): Flow<List<LevelConfigEntity>>

    @Query("SELECT * FROM level_configs ORDER BY stageNumber ASC")
    suspend fun getAllLevels(): List<LevelConfigEntity>

    @Query("SELECT * FROM level_configs WHERE id = :id LIMIT 1")
    fun observeLevelById(id: String): Flow<LevelConfigEntity?>

    @Query("SELECT * FROM level_configs WHERE id = :id LIMIT 1")
    suspend fun getLevelById(id: String): LevelConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: LevelConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevels(levels: List<LevelConfigEntity>)

    @Update
    suspend fun updateLevel(level: LevelConfigEntity)

    @Query("DELETE FROM level_configs WHERE id = :id")
    suspend fun deleteLevelById(id: String)

    @Query("DELETE FROM level_configs")
    suspend fun deleteAllLevels()

    @Query("SELECT COUNT(*) FROM level_configs")
    suspend fun getLevelCount(): Int
}
