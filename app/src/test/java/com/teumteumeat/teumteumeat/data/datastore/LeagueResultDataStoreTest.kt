package com.teumteumeat.teumteumeat.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.time.LocalDate

/** 실제 Preferences DataStore(임시 파일)로 계정별 기록이 서로 섞이지 않는지 검증한다. */
class LeagueResultDataStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val week1 = LocalDate.of(2026, 9, 21)
    private val week2 = LocalDate.of(2026, 9, 28)

    private fun newStore(file: File = tempFolder.newFile("league.preferences_pb").also { it.delete() }) =
        LeagueResultDataStore(PreferenceDataStoreFactory.create { file })

    @Test
    fun `계정마다 확인한 주가 따로 기록된다`() = runTest {
        val store = newStore()

        store.markSeen("A", week2)

        assertEquals(week2, store.getLastSeenWeekStart("A"))
        assertNull(store.getLastSeenWeekStart("B")) // 다른 계정은 아직 확인하지 않은 상태
    }

    @Test
    fun `계정을 바꿨다가 돌아와도 원래 계정의 기록이 유지된다`() = runTest {
        val store = newStore()
        store.markSeen("A", week2)
        store.markSeen("B", week1)

        assertEquals(week2, store.getLastSeenWeekStart("A"))
        assertEquals(week1, store.getLastSeenWeekStart("B"))
    }

    @Test
    fun `더 오래된 주로는 덮어쓰지 않는다`() = runTest {
        val store = newStore()
        store.markSeen("A", week2)

        store.markSeen("A", week1)

        assertEquals(week2, store.getLastSeenWeekStart("A"))
    }

    @Test
    fun `계정 id 를 모르면 unknown 한 곳에 기록하고 다른 계정과 섞이지 않는다`() = runTest {
        val store = newStore()

        store.markSeen(null, week2)

        assertEquals(week2, store.getLastSeenWeekStart(null))
        assertNull(store.getLastSeenWeekStart("A"))
    }

    @Test
    fun `clear 는 해당 계정 기록만 지운다`() = runTest {
        val store = newStore()
        store.markSeen("A", week2)
        store.markSeen("B", week2)

        store.clear("A")

        assertNull(store.getLastSeenWeekStart("A"))
        assertEquals(week2, store.getLastSeenWeekStart("B"))
    }

    @Test
    fun `쓰기에 성공하면 true 를 돌려준다`() = runTest {
        val store = newStore()

        assertTrue(store.markSeen("A", week2))
        assertTrue(store.clear("A"))
    }

    // ---- 디스크 오류(IOException) ----

    /** 읽기/쓰기가 모두 [IOException] 으로 실패하는 DataStore 대역. */
    private class FailingDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw IOException("disk error") }
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            throw IOException("disk error")
    }

    @Test
    fun `쓰기가 IOException 으로 실패하면 예외 대신 false 를 돌려준다`() = runTest {
        val store = LeagueResultDataStore(FailingDataStore())

        assertFalse(store.markSeen("A", week2))
        assertFalse(store.clear("A"))
    }

    @Test
    fun `읽기가 IOException 으로 실패하면 기록 없음으로 본다`() = runTest {
        val store = LeagueResultDataStore(FailingDataStore())

        assertNull(store.getLastSeenWeekStart("A"))
    }

    @Test
    fun `IOException 이 아닌 오류는 삼키지 않고 그대로 전파한다`() = runTest {
        val broken = object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flow { emit(emptyPreferences()) }
            override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
                throw IllegalStateException("bug")
        }

        val result = runCatching { LeagueResultDataStore(broken).markSeen("A", week2) }

        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
}
