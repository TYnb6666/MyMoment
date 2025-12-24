package nuist.cn.mymoment.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import nuist.cn.mymoment.model.Diary
import nuist.cn.mymoment.viewmodel.AuthViewModel
import nuist.cn.mymoment.viewmodel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    diaryViewModel: DiaryViewModel,
    authViewModel: AuthViewModel,
    diaryCardColor: Color,
    onAddDiary: () -> Unit,
    onEditDiary: (Diary) -> Unit,
    onDeleteDiary: (Diary) -> Unit,
    onDiaryClick: (Diary) -> Unit,
    onLogout: () -> Unit,
    onNavigateToAllEntriesMap: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // ViewModel states
    val diaries by diaryViewModel.diaryListState
    val searchQuery by diaryViewModel.searchQuery
    val error by remember { diaryViewModel.errorState }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Load diaries on start
    LaunchedEffect(Unit) {
        diaryViewModel.startObserveDiaries()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Place, contentDescription = "See in Maps") },
                    label = { Text("See in Maps") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToAllEntriesMap()
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("MyMoment", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        // User account menu
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Account")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    expanded = false
                                    onLogout()
                                }
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddDiary) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        diaryViewModel.onSearchQueryChange(it)
                        diaryViewModel.performSearch() // Apply search filter
                    },
                    onSearch = { diaryViewModel.performSearch() },
                    active = false, // Simple search mode
                    onActiveChange = { },
                    placeholder = { Text("Search memories...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        // Clear search button
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                diaryViewModel.onSearchQueryChange("")
                                diaryViewModel.performSearch()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    content = {}
                )

                // Diary list content
                Box(modifier = Modifier.weight(1f)) {
                    if (diaries.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (searchQuery.isEmpty()) "No diary entries yet" else "No matches found",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        // Diary list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(diaries, key = { it.id }) { diary ->
                                DiaryItem(
                                    diary = diary,
                                    backgroundColor = diaryCardColor,
                                    onEdit = onEditDiary,
                                    onDelete = onDeleteDiary,
                                    onClick = { onDiaryClick(diary) }
                                )
                            }
                        }
                    }

                    // Error message display
                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryItem(
    diary: Diary,
    backgroundColor: Color,
    onEdit: (Diary) -> Unit,
    onDelete: (Diary) -> Unit,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val shape: Shape = RoundedCornerShape(12.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and menu row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = diary.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "menu")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = {
                            expanded = false
                            onEdit(diary)
                        })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = {
                            expanded = false
                            onDelete(diary)
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Diary content preview
            Text(
                text = diary.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: location and date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    diary.location?.let { geoPoint ->
                        Icon(
                            Icons.Default.Place,
                            contentDescription = "Location",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Lat: %.2f, Lon: %.2f".format(geoPoint.latitude, geoPoint.longitude),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Date display
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Text(
                    text = if (diary.timestamp > 0) sdf.format(Date(diary.timestamp)) else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}