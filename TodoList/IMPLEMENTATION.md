# Todo List Android App - Implementation Documentation

## Overview

This is a production-ready To-Do List Android application built with Kotlin following modern Android development best practices. The app implements clean architecture, MVVM pattern, and uses Jetpack libraries for a robust, maintainable codebase.

## ✨ Features

### Core Functionality
- **Task Management**: Create, edit, delete, and mark tasks as complete
- **Task Properties**:
  - Unique ID
  - Title (required)
  - Description (optional)
  - Due date & time
  - Priority levels (Low, Medium, High)
  - Completion status
  - Creation & update timestamps
  - Configurable reminder time

### Sorting & Filtering
- Sort by: Date, Priority
- Filter by: All, Pending, Completed

### Notifications
- Scheduled reminders before task due time (5min, 30min, 1hr, 1 day)
- Survives device reboot (WorkManager)
- Works when app is closed
- Auto-cancels when task deleted/completed
- Notification channels (Android 8.0+)
- Android 13+ notification permission handling
- Deep linking to task details

### UI/UX
- Material Design 3
- Jetpack Compose UI
- Responsive layouts
- Empty states
- Visual priority indicators
- Smooth animations
- Task completion checkboxes
- FAB for quick task addition
- Date & time pickers

## 🏗️ Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  (UI, ViewModels, Compose Screens)  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│          Domain Layer               │
│  (Use Cases, Models, Repository     │
│   Interfaces)                       │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│           Data Layer                │
│  (Room Database, Repository Impl,   │
│   Data Sources, Mappers)            │
└─────────────────────────────────────┘
```

### MVVM Pattern

- **Model**: Domain models and data entities
- **View**: Compose UI screens and components
- **ViewModel**: State management and business logic orchestration

### Key Design Decisions

1. **Separation of Concerns**: Each layer has a single responsibility
2. **Dependency Inversion**: Domain layer doesn't depend on data layer
3. **Repository Pattern**: Abstracts data sources
4. **Use Cases**: Encapsulate business logic
5. **Reactive UI**: Flow and StateFlow for reactive updates
6. **Dependency Injection**: Hilt for testability and maintainability

## 📦 Technical Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| Min SDK | API 30 (Android 11) |
| Target SDK | API 36 |
| UI Framework | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| Database | Room |
| Async | Coroutines + Flow |
| DI | Hilt |
| Notifications | WorkManager |
| Navigation | Navigation Compose |
| State | StateFlow / LiveData |

## 📁 Project Structure

```
com.lehau007.todolist/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   └── TaskDao.kt
│   │   ├── entity/
│   │   │   └── TaskEntity.kt
│   │   └── TodoDatabase.kt
│   ├── mapper/
│   │   └── TaskMapper.kt
│   └── repository/
│       └── TaskRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Task.kt
│   │   └── TaskSortOrder.kt
│   ├── repository/
│   │   └── TaskRepository.kt
│   └── usecase/
│       ├── GetTasksUseCase.kt
│       ├── GetTaskByIdUseCase.kt
│       ├── UpsertTaskUseCase.kt
│       ├── DeleteTaskUseCase.kt
│       └── ToggleTaskCompletionUseCase.kt
│
├── ui/
│   ├── components/
│   │   └── TaskItem.kt
│   ├── screens/
│   │   ├── TaskListScreen.kt
│   │   └── AddEditTaskScreen.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel/
│       ├── TaskListViewModel.kt
│       └── AddEditTaskViewModel.kt
│
├── navigation/
│   └── NavGraph.kt
│
├── notification/
│   ├── TaskReminderWorker.kt
│   └── TaskNotificationManager.kt
│
├── di/
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
│
├── MainActivity.kt
└── TodoApplication.kt
```

## 🗃️ Database Schema

### TaskEntity Table

```kotlin
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val dueDateTime: Long,
    val priority: Int, // 0=Low, 1=Medium, 2=High
    val isCompleted: Boolean,
    val reminderMinutesBefore: Int,
    val createdAt: Long,
    val updatedAt: Long
)
```

### Room Configuration

- Database name: `todo_database`
- Version: 1
- Export schema: true
- Fallback to destructive migration (development only)

### Migration Strategy

Currently using fallback to destructive migration for development. For production:

1. Create migration classes for schema changes
2. Remove `fallbackToDestructiveMigration()`
3. Add proper migration paths:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Migration logic
    }
}
```

## 🔔 Notification System

### Implementation

**WorkManager** is used for reliable notification scheduling:

1. **TaskNotificationManager**: Schedules/cancels notifications
2. **TaskReminderWorker**: Displays notifications
3. **Notification Channel**: "Task Reminders" channel

### Features

- Survives device reboot (WorkManager persistence)
- Works when app is closed
- Auto-cancels on task completion/deletion
- Deep links to task details
- Android 13+ permission handling

### Notification Flow

```
Task Created/Updated
    ↓
UpsertTaskUseCase
    ↓
TaskNotificationManager.scheduleNotification()
    ↓
WorkManager enqueues TaskReminderWorker
    ↓
Worker executes at reminder time
    ↓
Notification displayed
    ↓
User taps notification
    ↓
Deep link to task details
```

## 🧪 Testing

### Unit Tests

#### TaskRepositoryImplTest
- Tests data layer transformations
- Mocks DAO interactions
- Validates sorting and filtering

#### TaskListViewModelTest
- Tests ViewModel logic
- Validates state management
- Tests user interactions

### Test Structure

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Setup mocks
    }
    
    @Test
    fun `test case`() = runTest {
        // Test logic
    }
}
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests TaskRepositoryImplTest

# Run tests with coverage
./gradlew testDebugUnitTestCoverage
```

## 🚀 Building & Running

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK with API 30-36

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on device
./gradlew installDebug

# Run tests
./gradlew test

# Clean build
./gradlew clean build
```

### Gradle Sync

After cloning, Android Studio will automatically sync Gradle dependencies. If not:

1. File → Sync Project with Gradle Files
2. Or run: `./gradlew build`

## 🔧 Configuration

### Dependencies (libs.versions.toml)

Key dependencies and their purposes:

```toml
[versions]
room = "2.6.1"              # Database
hilt = "2.51.1"             # Dependency Injection
workManager = "2.9.0"       # Background work
navigationCompose = "2.8.5" # Navigation
coroutines = "1.9.0"        # Async operations
```

### Build Configuration

- Min SDK: 30 (Android 11, covers 90%+ of devices)
- Target SDK: 36 (latest)
- Compile SDK: 36
- Java Version: 11
- Kotlin JVM Target: 11

## 🎨 UI Components

### TaskListScreen

Main screen displaying task list with:
- Top bar with sort/filter options
- LazyColumn for task list
- Empty state message
- FAB for adding tasks
- Delete confirmation dialogs

### AddEditTaskScreen

Form for creating/editing tasks:
- Text fields for title/description
- Date/time pickers for due date
- Priority selection chips
- Reminder time radio buttons
- Save button with loading state

### TaskItem

Reusable card component showing:
- Completion checkbox
- Visual priority indicator
- Task title and description
- Due date/time
- Priority badge
- Delete button

## 🔐 Permissions

### Required Permissions

1. **POST_NOTIFICATIONS** (Android 13+)
   - Runtime permission
   - Requested on app launch
   - Required for showing notifications

2. **SCHEDULE_EXACT_ALARM**
   - Normal permission
   - For precise notification timing

3. **RECEIVE_BOOT_COMPLETED**
   - Normal permission
   - For rescheduling after reboot

### Permission Handling

```kotlin
private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted: Boolean ->
    // Handle permission result
}

private fun requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        requestPermissionLauncher.launch(
            Manifest.permission.POST_NOTIFICATIONS
        )
    }
}
```

## 🐛 Common Issues & Solutions

### Issue: Hilt compilation errors

**Solution**: Ensure KSP plugin is applied and rebuild:
```bash
./gradlew clean build
```

### Issue: Room schema export errors

**Solution**: Check `ksp` block in build.gradle.kts:
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

### Issue: Notification not showing

**Checklist**:
1. Check notification permission granted
2. Verify WorkManager is initialized
3. Check notification channel created
4. Ensure task is not completed
5. Verify reminder time is in future

### Issue: Navigation not working

**Solution**: Ensure Hilt is setup in MainActivity:
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

## 📱 Future Enhancements

### Potential Features

1. **Cloud Sync**: Firebase or custom backend
2. **Task Categories**: Organize tasks by category
3. **Recurring Tasks**: Daily, weekly, monthly repeats
4. **Task Attachments**: Photos, files, voice notes
5. **Collaboration**: Share tasks with others
6. **Task History**: View completed task history
7. **Statistics**: Completion rates, charts
8. **Themes**: Dark mode, custom colors
9. **Widgets**: Home screen widgets
10. **Export/Import**: Backup and restore

### Technical Improvements

1. **Pagination**: For large task lists
2. **Search**: Full-text search
3. **Offline-first**: Better offline support
4. **Analytics**: Track user behavior
5. **Crash Reporting**: Firebase Crashlytics
6. **Performance**: Profiling and optimization
7. **Accessibility**: TalkBack, large text
8. **Localization**: Multiple languages
9. **CI/CD**: Automated testing and deployment
10. **Code Coverage**: Aim for 80%+

## 👨‍💻 Development Guidelines

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Add KDoc comments for public APIs
- Keep functions small and focused
- Use extension functions for utilities

### Git Workflow

1. Create feature branch from `main`
2. Implement feature with tests
3. Run tests and lint
4. Submit pull request
5. Code review
6. Merge to `main`

### Commit Messages

```
feat: Add task filtering by priority
fix: Resolve notification crash on Android 13
refactor: Extract task item to reusable component
test: Add unit tests for TaskRepository
docs: Update README with setup instructions
```

## 📄 License

This project is created as a demonstration of modern Android development practices.

## 🙏 Acknowledgments

- Android Jetpack libraries
- Material Design 3
- Kotlin Coroutines
- Dagger Hilt

---

**Built with ❤️ using Kotlin and Jetpack Compose**
