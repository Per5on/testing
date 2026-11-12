package bg.prices

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "basket_items",
    indices = [Index(value = ["categoryCode"]), Index(value = ["createdAt"])]
)
data class BasketItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryCode: String,
    val quantity: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface BasketDao {
    @Query("SELECT * FROM basket_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BasketItemEntity>>

    @Insert
    suspend fun insert(item: BasketItemEntity)

    @Query("UPDATE basket_items SET quantity = MIN(99, quantity + :delta) WHERE id = :id AND quantity + :delta >= 1")
    suspend fun changeQuantity(id: Long, delta: Int)

    @Query("DELETE FROM basket_items WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM basket_items")
    suspend fun clear()
}

@Database(entities = [BasketItemEntity::class], version = 1, exportSchema = false)
abstract class BgPricesDatabase : RoomDatabase() {
    abstract fun basketDao(): BasketDao

    companion object {
        @Volatile private var INSTANCE: BgPricesDatabase? = null

        fun get(context: Context): BgPricesDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BgPricesDatabase::class.java,
                    "bg_prices_local.db"
                ).build().also { INSTANCE = it }
            }
    }
}
