package nuist.cn.mymoment.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import nuist.cn.mymoment.R
import nuist.cn.mymoment.data.diary.Diary
import nuist.cn.mymoment.data.diary.DiaryRepository
import nuist.cn.mymoment.ui.diary.editor.CreateDiaryActivity
import nuist.cn.mymoment.ui.diary.list.DiaryActionListener
import nuist.cn.mymoment.ui.diary.list.DiaryAdapter
import nuist.cn.mymoment.ui.diary.list.DiaryListViewModel
import nuist.cn.mymoment.ui.diary.list.DiaryListViewModelFactory
import nuist.cn.mymoment.ui.login.LoginActivity
import nuist.cn.mymoment.ui.map.DiaryMapActivity
import nuist.cn.mymoment.data.preferences.AppPreferences

class MainActivity : AppCompatActivity(), DiaryActionListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DiaryAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyState: TextView
    private lateinit var preferences: AppPreferences
    private lateinit var viewModel: DiaryListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences(this)
        if (!preferences.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        preferences.setNightMode(preferences.isNightMode())
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupViews()
        setupViewModel()
    }

    private fun setupViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.app_name,
            R.string.app_name
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_all_diaries -> drawerLayout.closeDrawer(GravityCompat.START)
                R.id.nav_map -> openMap()
                R.id.nav_toggle_theme -> toggleTheme()
                R.id.nav_toggle_font -> toggleFontSize()
                R.id.nav_logout -> logout()
            }
            true
        }

        recyclerView = findViewById(R.id.diaryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DiaryAdapter(this)
        adapter.setLargeFont(preferences.isLargeFont())
        recyclerView.adapter = adapter

        emptyState = findViewById(R.id.emptyState)

        swipeRefreshLayout = findViewById(R.id.swipeRefresh)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        findViewById<FloatingActionButton>(R.id.fabAddDiary).setOnClickListener {
            val intent = Intent(this, CreateDiaryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupViewModel() {
        val factory = DiaryListViewModelFactory(DiaryRepository)
        viewModel = ViewModelProvider(this, factory)[DiaryListViewModel::class.java]
        viewModel.diaries.observe(this) { diaries ->
            swipeRefreshLayout.isRefreshing = false
            adapter.submitList(diaries)
            emptyState.visibility = if (diaries.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.isLoading.observe(this) { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.action_search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.search(query.orEmpty())
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText.orEmpty())
                return true
            }
        })
        searchView.setOnCloseListener {
            viewModel.refresh()
            false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openMap() {
        startActivity(Intent(this, DiaryMapActivity::class.java))
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun toggleTheme() {
        val enable = !preferences.isNightMode()
        preferences.setNightMode(enable)
        drawerLayout.closeDrawer(GravityCompat.START)
        recreate()
    }

    private fun toggleFontSize() {
        val enable = !preferences.isLargeFont()
        preferences.setLargeFont(enable)
        adapter.setLargeFont(enable)
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun logout() {
        preferences.setLoggedIn(false)
        drawerLayout.closeDrawer(GravityCompat.START)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onEditDiary(diary: Diary) {
        val intent = Intent(this, CreateDiaryActivity::class.java).apply {
            putExtra(CreateDiaryActivity.EXTRA_DIARY, diary)
        }
        startActivity(intent)
    }

    override fun onDeleteDiary(diary: Diary) {
        if (diary.id.isBlank()) {
            Toast.makeText(this, R.string.diary_delete_failed, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.deleteDiary(diary.id) { success ->
            val message = if (success) R.string.diary_delete_success else R.string.diary_delete_failed
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}

