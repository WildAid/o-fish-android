package org.wildaid.ofish.ui.search.simple

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import io.realm.RealmList
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.wildaid.ofish.data.Repository
import org.wildaid.ofish.data.report.MenuData
import org.wildaid.ofish.ui.search.base.BaseSearchType

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SimpleSearchViewModelTest {

    private lateinit var searchViewModel: SimpleSearchViewModel
    private val wholeList = listOf("CA", "UA", "UK", "MX", "US", "DE")
    private val filteredList = listOf("UA", "UK", "US","Other")

    @MockK
    private lateinit var mockedRepository: Repository

    @MockK
    private lateinit var searchData: SimpleSearchViewModel.SimpleSearchDataSource

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        searchViewModel =
            SimpleSearchViewModel(mockedRepository, ApplicationProvider.getApplicationContext())

        searchData = SimpleSearchViewModel(
            mockedRepository,
            ApplicationProvider.getApplicationContext()
        ).SimpleSearchDataSource(wholeList)

    }

    @Test
    fun getSourceFlagState() {
        every { mockedRepository.getFlagStates() } returns emptyList()

        val searchDataSource =
            searchViewModel.getDataSource(SimpleSearchFragment.SearchFlagState, null)

        assert(searchDataSource is SimpleSearchViewModel.SimpleSearchDataSource)

        verify { mockedRepository.getFlagStates() }
    }

    @Test
    fun getSourceEms() {
        every { mockedRepository.getMenuData() } returns MenuData().apply {
            emsTypes = RealmList()
        }

        val searchDataSource =
            searchViewModel.getDataSource(SimpleSearchFragment.SearchEms, null)

        assert(searchDataSource is SimpleSearchViewModel.SimpleSearchDataSource)

        verify { mockedRepository.getMenuData() }
    }

    @Test
    fun getSourceFishery() {
        every { mockedRepository.getMenuData() } returns MenuData().apply {
            fisheries = RealmList()
        }

        val searchDataSource =
            searchViewModel.getDataSource(SimpleSearchFragment.SearchFishery, null)

        assert(searchDataSource is SimpleSearchViewModel.SimpleSearchDataSource)

        verify { mockedRepository.getMenuData() }
    }

    @Test
    fun getSourceGear() {
        every { mockedRepository.getMenuData() } returns MenuData().apply {
            gear = RealmList()
        }

        val searchDataSource =
            searchViewModel.getDataSource(SimpleSearchFragment.SearchGear, null)

        assert(searchDataSource is SimpleSearchViewModel.SimpleSearchDataSource)

        verify { mockedRepository.getMenuData() }
    }

    @Test
    fun getSourceActivity() {
        every { mockedRepository.getMenuData() } returns MenuData().apply {
            activities = RealmList()
        }

        val searchDataSource =
            searchViewModel.getDataSource(SimpleSearchFragment.SearchActivity, null)

        assert(searchDataSource is SimpleSearchViewModel.SimpleSearchDataSource)

        verify { mockedRepository.getMenuData() }
    }

    @Test
    fun getSourceSpecies() {
        every { mockedRepository.getMenuData() } returns MenuData().apply {
            species = RealmList()
        }

        val searchDataSource =
            searchViewModel.getDataSource(SimpleSearchFragment.SearchSpecies, null)

        assert(searchDataSource is SimpleSearchViewModel.SimpleSearchDataSource)

        verify { mockedRepository.getMenuData() }
    }

    @Test(expected = IllegalArgumentException::class)
    fun getSourceIllegalArgument() {
        val invalidSearchEntity = BaseSearchType()
        every {
            searchViewModel.getDataSource(
                invalidSearchEntity,
                null
            )
        } throws IllegalArgumentException("Unsupported search type $invalidSearchEntity")

        val searchDataSource =
            searchViewModel.getDataSource(invalidSearchEntity, null)

        assert(searchDataSource is SimpleSearchViewModel.SimpleSearchDataSource)

        verify { mockedRepository.getMenuData() }
    }

    @Test
    fun initiateSimpleSearchDataSource() {
        assert(searchData.initiateDataBlocking() == wholeList)
    }

    @Test
    fun filterSimpleSearchDataSource() {
        assert(searchData.applyFilter("U") == filteredList)
    }
}