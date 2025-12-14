# Architecture Visualization

## App Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────┐         ┌──────────────────┐              │
│  │  TaskListScreen │         │ AddEditTaskScreen│              │
│  └────────┬────────┘         └────────┬─────────┘              │
│           │                           │                          │
│           ▼                           ▼                          │
│  ┌────────────────┐         ┌──────────────────┐              │
│  │ TaskListViewModel│       │AddEditTaskViewModel│            │
│  └────────┬───────┘         └────────┬──────────┘              │
│           │                           │                          │
│           │  ┌──────────────────┐    │                          │
│           └─►│   Navigation     │◄───┘                          │
│              │    (NavGraph)    │                               │
│              └──────────────────┘                               │
│                                                                   │
└───────────────────────────┬───────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌────────────────────┐                                         │
│  │   Task (Model)     │  ◄───────────────────┐                 │
│  │   - id             │                       │                 │
│  │   - title          │                       │                 │
│  │   - priority       │                       │                 │
│  │   - dueDateTime    │                       │                 │
│  └────────────────────┘                       │                 │
│                                                │                 │
│  ┌────────────────────────────────┐           │                 │
│  │      Use Cases                 │           │                 │
│  ├────────────────────────────────┤           │                 │
│  │ • GetTasksUseCase              │───────────┤                 │
│  │ • GetTaskByIdUseCase           │           │                 │
│  │ • UpsertTaskUseCase            │           │                 │
│  │ • DeleteTaskUseCase            │           │                 │
│  │ • ToggleTaskCompletionUseCase  │           │                 │
│  └───────────┬────────────────────┘           │                 │
│              │                                 │                 │
│              ▼                                 │                 │
│  ┌────────────────────────────────┐           │                 │
│  │   TaskRepository Interface     │───────────┘                 │
│  └────────────────────────────────┘                             │
│                                                                   │
└───────────────────────────┬───────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                         DATA LAYER                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌────────────────────────────────┐                             │
│  │ TaskRepositoryImpl             │                             │
│  │  (implements TaskRepository)   │                             │
│  └───────────┬────────────────────┘                             │
│              │                                                    │
│              ▼                                                    │
│  ┌────────────────────────────────┐                             │
│  │      TaskMapper                │                             │
│  │  • toEntity()                  │                             │
│  │  • toDomain()                  │                             │
│  └───────────┬────────────────────┘                             │
│              │                                                    │
│              ▼                                                    │
│  ┌────────────────────────────────┐                             │
│  │      Room Database             │                             │
│  ├────────────────────────────────┤                             │
│  │  TaskDao                       │                             │
│  │  • getAllTasks()               │                             │
│  │  • getTaskById()               │                             │
│  │  • insertTask()                │                             │
│  │  • updateTask()                │                             │
│  │  • deleteTask()                │                             │
│  └───────────┬────────────────────┘                             │
│              │                                                    │
│              ▼                                                    │
│  ┌────────────────────────────────┐                             │
│  │    TaskEntity (Table)          │                             │
│  │  @PrimaryKey id                │                             │
│  │  title: String                 │                             │
│  │  dueDateTime: Long             │                             │
│  │  priority: Int                 │                             │
│  │  isCompleted: Boolean          │                             │
│  └────────────────────────────────┘                             │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## Dependency Injection Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                       TodoApplication                            │
│                   @HiltAndroidApp                                │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Hilt Components                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  DatabaseModule (@InstallIn SingletonComponent)          │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  provideTodoDatabase() → TodoDatabase                    │   │
│  │  provideTaskDao() → TaskDao                              │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  RepositoryModule (@InstallIn SingletonComponent)        │   │
│  ├─────────────────────────────────────────────────────────┤   │
│  │  @Binds bindTaskRepository()                             │   │
│  │  TaskRepositoryImpl → TaskRepository                     │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                   │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Injected Classes                              │
├─────────────────────────────────────────────────────────────────┤
│  @Inject constructor                                             │
│  • Use Cases (GetTasksUseCase, etc.)                            │
│  • Repository (TaskRepositoryImpl)                              │
│  • ViewModels (@HiltViewModel)                                  │
│  • TaskNotificationManager                                      │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow: Creating a Task

```
User Input (AddEditTaskScreen)
    │
    │ updateTitle(), updatePriority(), etc.
    ▼
AddEditTaskViewModel
    │
    │ saveTask()
    ▼
UpsertTaskUseCase
    │
    ├─► TaskRepository.upsertTask(task)
    │       │
    │       ▼
    │   TaskRepositoryImpl
    │       │
    │       │ task.toEntity()
    │       ▼
    │   TaskDao.insertTask(entity)
    │       │
    │       ▼
    │   Room Database (SQLite)
    │
    └─► TaskNotificationManager.scheduleNotification(task)
            │
            ▼
        WorkManager.enqueue(TaskReminderWorker)
            │
            │ At reminder time
            ▼
        Notification displayed
```

## Data Flow: Displaying Tasks

```
TaskListScreen (UI)
    │
    │ observes StateFlow
    ▼
TaskListViewModel
    │
    │ tasks: StateFlow<List<Task>>
    ▼
GetTasksUseCase(sortOrder, filter)
    │
    ▼
TaskRepository.getTasks()
    │
    ▼
TaskRepositoryImpl
    │
    ├─► TaskDao.getAllTasks() / getPendingTasks() / etc.
    │       │
    │       ▼
    │   Flow<List<TaskEntity>> from Room
    │
    └─► map { entities -> entities.map { it.toDomain() } }
            │
            ▼
        Flow<List<Task>> to ViewModel
            │
            ▼
        StateFlow<List<Task>> to UI
            │
            ▼
        UI automatically updates (Compose)
```

## Notification Flow

```
Task Created/Updated
    │
    ▼
UpsertTaskUseCase
    │
    ▼
TaskNotificationManager.scheduleNotification()
    │
    │ Calculate reminder time
    │ reminderTime = dueDateTime - reminderMinutesBefore
    │
    ▼
WorkManager.enqueueUniqueWork()
    │
    │ OneTimeWorkRequest with delay
    │
    ▼
WorkManager Database (persisted)
    │
    │ At reminder time
    ▼
TaskReminderWorker.doWork()
    │
    ├─► Get TaskDao via EntryPoint
    │
    ├─► Load task from database
    │
    ├─► Create notification channel
    │
    ├─► Build notification
    │       │
    │       ├─► Title: task.title
    │       ├─► Content: due time + description
    │       └─► PendingIntent: deep link to task
    │
    └─► NotificationManagerCompat.notify()
            │
            ▼
        Notification displayed to user
            │
            │ User taps
            ▼
        MainActivity opens with taskId
            │
            ▼
        Navigate to EditTaskScreen
```

## Component Lifecycle

```
App Launch
    │
    ▼
TodoApplication created
    │
    │ Hilt initializes
    │ Singleton components created
    ▼
MainActivity created
    │
    │ @AndroidEntryPoint
    │ Request permissions
    │
    ▼
Compose UI initialized
    │
    ▼
NavHost created
    │
    ▼
TaskListScreen displayed
    │
    │ hiltViewModel() creates
    ▼
TaskListViewModel
    │
    │ @Inject constructor
    │ Dependencies injected:
    │ - GetTasksUseCase
    │ - ToggleTaskCompletionUseCase
    │ - DeleteTaskUseCase
    │
    ▼
observeTasks() in init {}
    │
    │ Collect from Flow
    │ Update StateFlow
    │
    ▼
UI observes StateFlow
    │
    │ collectAsStateWithLifecycle()
    │
    ▼
LazyColumn displays tasks
    │
    │ User interaction
    │
    ▼
ViewModel handles action
    │
    ▼
Use Case executes
    │
    ▼
Repository updates database
    │
    ▼
Flow emits new data
    │
    ▼
StateFlow updated
    │
    ▼
UI recomposes automatically
```

## Testing Architecture

```
Production Code              Test Code
─────────────────           ──────────────

TaskRepositoryImpl  ◄───── TaskRepositoryImplTest
    │                           │
    │ Uses TaskDao              │ Uses MockK
    │                           │ Mock TaskDao
    │                           │
    ▼                           ▼
TaskDao (Real)              TaskDao (Mock)


TaskListViewModel   ◄───── TaskListViewModelTest
    │                           │
    │ Uses Use Cases            │ Uses MockK
    │                           │ Mock Use Cases
    │                           │
    ▼                           ▼
Use Cases (Real)            Use Cases (Mock)
```

## Key Architectural Patterns

### 1. Repository Pattern
```
ViewModel → Repository Interface → Repository Implementation → Data Source
(Presentation)  (Domain)           (Data)                     (Database)
```

### 2. Use Case Pattern
```
ViewModel → Use Case → Repository
(UI Logic)  (Business Logic)  (Data Access)
```

### 3. Mapper Pattern
```
TaskEntity (Database) ←→ Task (Domain)
        toEntity()    ←  Mapper
        toDomain()    →
```

### 4. MVVM Pattern
```
View (Compose) → ViewModel → Model (Domain)
    │               │            │
    └─ Observes ────┘            │
                                 │
                    Repository ──┘
```

### 5. Dependency Inversion
```
High Level (Domain) depends on abstractions
    │
    │ TaskRepository interface
    │
Low Level (Data) implements abstractions
    │
    │ TaskRepositoryImpl
```

## Thread Model

```
Main Thread (UI)
    │
    │ Compose recomposition
    │ User interactions
    │
    ▼
ViewModel (Main)
    │
    │ viewModelScope.launch
    ▼
Coroutines (IO Dispatcher)
    │
    │ suspend functions
    │ Database operations
    │ Use cases
    │
    ▼
Room Database (Background)
    │
    │ SQL queries
    │
    ▼
Flow emissions → StateFlow (Main)
    │
    ▼
UI updates (Main Thread)
```
