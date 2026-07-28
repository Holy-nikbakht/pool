package com.polaki.expense.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromType(type: TransactionType): String = type.name

    @TypeConverter
    fun toType(value: String): TransactionType = TransactionType.valueOf(value)
}

@Database(
    entities = [Account::class, Category::class, Transaction::class, SmsSuggestion::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun smsSuggestionDao(): SmsSuggestionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pulaki.db"
                ).fallbackToDestructiveMigration() // fine pre-release; replace with real migrations before shipping
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seed(getInstance(context))
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seed(db: AppDatabase) {
            val cashAccountId = db.accountDao().insert(
                Account(name = "نقدی", initialBalance = 0, colorHex = "#1AD1A5", icon = "wallet")
            )
            db.accountDao().insert(
                Account(name = "کارت بانکی", initialBalance = 0, colorHex = "#4FD1FF", icon = "card")
            )

            val expenseDefaults = listOf(
                Triple("خوراک و رستوران", "#FF6B81", "food"),
                Triple("حمل و نقل", "#FFB86B", "car"),
                Triple("خرید", "#C792EA", "cart"),
                Triple("قبض و اجاره", "#FF5C8A", "bill"),
                Triple("سلامت", "#6BFFB8", "health"),
                Triple("سرگرمی", "#6BD4FF", "fun")
            )
            expenseDefaults.forEach { (name, color, icon) ->
                db.categoryDao().insert(
                    Category(name = name, type = TransactionType.EXPENSE, colorHex = color, icon = icon)
                )
            }

            val incomeDefaults = listOf(
                Triple("حقوق", "#1AD1A5", "salary"),
                Triple("درآمد آزاد", "#3DDC97", "freelance"),
                Triple("هدیه", "#8CFFDA", "gift")
            )
            incomeDefaults.forEach { (name, color, icon) ->
                db.categoryDao().insert(
                    Category(name = name, type = TransactionType.INCOME, colorHex = color, icon = icon)
                )
            }
        }
    }
}
