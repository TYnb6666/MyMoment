package nuist.cn.mymoment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import nuist.cn.mymoment.model.Diary
import nuist.cn.mymoment.view.*
import nuist.cn.mymoment.viewmodel.AuthViewModel
import nuist.cn.mymoment.viewmodel.DiaryViewModel

/**
 * MainActivity is the main entry point of the MyMoment application.
 * 
 * This activity manages the app's navigation flow and UI state, including:
 * - User authentication (login/register)
 * - Diary management (create, read, update, delete)
 * - Theme customization (dark mode, font size, card colors)
 * - Location-based features (map view, location picker)
 * 
 * The UI is built using Jetpack Compose and follows Material 3 design guidelines.
 */
class MainActivity : ComponentActivity() {

    /** ViewModel responsible for user authentication state management */
    private val authViewModel: AuthViewModel by viewModels()
    
    /** ViewModel responsible for diary entries state management */
    private val diaryViewModel: DiaryViewModel by viewModels()

    /**
     * Called when the activity is starting.
     * Sets up the Compose UI with theme configuration and navigation logic.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Initialize theme and UI preferences
            // - Dark mode: follows system preference by default, can be toggled by user
            // - Font size: supports large font mode for better accessibility
            // - Card color: customizable diary card background color
            val systemInDark = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemInDark) }
            var isLargeFont by remember { mutableStateOf(false) }
            var diaryCardColor by remember { mutableStateOf(Color(0xFFF5F5F5)) }
            
            // Apply Material 3 color scheme based on dark mode setting
            val colorScheme = if (isDarkMode) darkColorScheme() else lightColorScheme()

            // Configure typography based on font size preference
            // When large font mode is enabled, all text sizes are increased for better readability
            val typography = if (isLargeFont) {
                Typography(
                    headlineLarge = Typography().headlineLarge.copy(fontSize = 38.sp),
                    headlineMedium = Typography().headlineMedium.copy(fontSize = 34.sp),
                    titleLarge = Typography().titleLarge.copy(fontSize = 28.sp),
                    titleMedium = Typography().titleMedium.copy(fontSize = 22.sp),
                    bodyLarge = Typography().bodyLarge.copy(fontSize = 22.sp),
                    bodyMedium = Typography().bodyMedium.copy(fontSize = 20.sp),
                    bodySmall = Typography().bodySmall.copy(fontSize = 18.sp),
                    labelLarge = Typography().labelLarge.copy(fontSize = 20.sp)
                )
            } else {
                Typography()
            }

            MaterialTheme(colorScheme = colorScheme, typography = typography) {
                // Navigation state management
                // These flags control which screen is currently displayed
                var showRegister by remember { mutableStateOf(false) }
                var isAddingDiary by remember { mutableStateOf(false) }
                var isPickingLocation by remember { mutableStateOf(false) }
                var isViewingAllEntriesMap by remember { mutableStateOf(false) }
                var isViewingSettings by remember { mutableStateOf(false) }
                var selectedDiary by remember { mutableStateOf<Diary?>(null) }

                // Monitor diary edit state to handle save completion
                val editState by diaryViewModel.editState
                LaunchedEffect(editState.saveComplete) {
                    if (editState.saveComplete) {
                        isAddingDiary = false
                        diaryViewModel.prepareNewDiary()
                    }
                }

                // Get current authentication state
                val authState = authViewModel.uiState.value

                // Authentication flow: show login or register screen when user is not logged in
                if (!authState.isLoggedIn) {
                    // Reset all navigation states when logged out
                    isAddingDiary = false
                    isPickingLocation = false
                    isViewingAllEntriesMap = false
                    isViewingSettings = false
                    selectedDiary = null

                    if (showRegister) {
                        RegisterScreen(
                            viewModel = authViewModel,
                            onBackToLogin = { showRegister = false },
                            onRegisterSuccess = { showRegister = false }
                        )
                    } else {
                        LoginScreen(
                            viewModel = authViewModel,
                            onGoToRegister = { showRegister = true }
                        )
                    }
                } else {
                    // Main navigation flow: determine which screen to display based on navigation state
                    when {
                        // Settings screen: manage app preferences
                        isViewingSettings -> {
                            SettingsScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeChange = { isDarkMode = it },
                                isLargeFont = isLargeFont,
                                onLargeFontChange = { isLargeFont = it },
                                selectedColor = diaryCardColor,
                                onColorChange = { diaryCardColor = it },
                                onBack = { isViewingSettings = false }
                            )
                        }
                        // Diary detail screen: view a specific diary entry
                        selectedDiary != null -> {
                            DiaryDetailScreen(
                                diary = selectedDiary!!,
                                onBack = { selectedDiary = null }
                            )
                        }
                        // Map view screen: display all diary entries on a map
                        isViewingAllEntriesMap -> {
                            AllEntriesMapScreen(
                                diaryViewModel = diaryViewModel,
                                onBack = { isViewingAllEntriesMap = false }
                            )
                        }
                        // Location picker screen: select location for a diary entry
                        isPickingLocation -> {
                            LocationPickerScreen(
                                diaryViewModel = diaryViewModel,
                                onLocationSelected = { isPickingLocation = false }
                            )
                        }
                        // Add/Edit diary screen: create new or edit existing diary entry
                        isAddingDiary -> {
                            AddDiaryScreen(
                                diaryViewModel = diaryViewModel,
                                onNavigateToLocationPicker = { isPickingLocation = true },
                                onBack = { isAddingDiary = false }
                            )
                        }
                        // Home screen: main diary list and management interface
                        else -> {
                            HomeScreen(
                                diaryViewModel = diaryViewModel,
                                authViewModel = authViewModel,
                                // Apply card color based on theme:
                                // - In dark mode: force black cards for better contrast
                                // - In light mode: use user-selected custom color
                                diaryCardColor = if (isDarkMode) Color.Black else diaryCardColor,
                                onAddDiary = {
                                    // Reset edit state if currently editing another diary
                                    if (diaryViewModel.editState.value.editingId != null) {
                                        diaryViewModel.prepareNewDiary()
                                    }
                                    isAddingDiary = true
                                },
                                onLogout = { authViewModel.logout() },
                                onNavigateToAllEntriesMap = { isViewingAllEntriesMap = true },
                                onNavigateToSettings = { isViewingSettings = true },
                                onEditDiary = { diary ->
                                    diaryViewModel.startEdit(diary)
                                    isAddingDiary = true
                                },
                                onDeleteDiary = { diary ->
                                    diaryViewModel.deleteDiary(diary.id)
                                },
                                onDiaryClick = { diary ->
                                    selectedDiary = diary
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
