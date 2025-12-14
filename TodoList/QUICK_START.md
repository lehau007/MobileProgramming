# 🚀 Quick Start Guide

## Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with API 30-36
- Android device or emulator (API 30+, Android 11+)

## Getting Started

### 1. Open Project
```bash
# Open Android Studio
# File → Open → Select TodoList folder
```

### 2. Sync Gradle
Android Studio will automatically sync Gradle dependencies. If not:
```
File → Sync Project with Gradle Files
```
Or run:
```bash
./gradlew build
```

### 3. Build & Run

#### Option A: Using Android Studio
1. Click the "Run" button (green triangle)
2. Select your device/emulator
3. App will build and launch

#### Option B: Using Command Line
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Or build and install in one step
./gradlew build
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 4. Grant Permissions
On first launch (Android 13+):
- App will request notification permission
- Tap "Allow" to enable task reminders

## Using the App

### Create a Task
1. Tap the **+** (FAB) button
2. Enter task details:
   - **Title** (required)
   - **Description** (optional)
   - **Due Date** - tap to select
   - **Due Time** - tap to select
   - **Priority** - Low/Medium/High
   - **Reminder** - Choose when to be notified
3. Tap **Save**

### View Tasks
- All tasks displayed on main screen
- Checkbox shows completion status
- Color bar indicates priority:
  - 🟢 Green = Low
  - 🟡 Yellow = Medium
  - 🔴 Red = High

### Edit a Task
1. Tap on any task
2. Modify details
3. Tap **Save**

### Complete a Task
- Tap the checkbox next to the task
- Task will be marked as complete
- Notification will be canceled

### Delete a Task
1. Tap the trash icon on a task
2. Confirm deletion
3. Task and notification will be removed

### Sort & Filter
- Tap the **sort icon** (top bar) to sort by:
  - Date
  - Priority
- Tap the **filter icon** (top bar) to filter:
  - All tasks
  - Pending only
  - Completed only

## Testing Notifications

### Method 1: Quick Test
1. Create a task due in 2 minutes
2. Set reminder to "5 minutes before"
3. Wait (notification won't show as due time hasn't passed)

### Method 2: Proper Test
1. Create a task due in 6 minutes
2. Set reminder to "5 minutes before"
3. In 1 minute, you'll receive a notification

### Method 3: Past Due Test
1. Create a task with past due date
2. Notification won't schedule (expected behavior)

## Running Tests

### Unit Tests
```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests TaskRepositoryImplTest

# Run with coverage
./gradlew testDebugUnitTestCoverage
```

### View Test Results
```bash
# Open in browser
open app/build/reports/tests/testDebugUnitTest/index.html
```

## Troubleshooting

### Gradle Sync Issues
```bash
# Clean and rebuild
./gradlew clean build

# Clear caches
rm -rf ~/.gradle/caches/
./gradlew build --refresh-dependencies
```

### Hilt/KSP Issues
```bash
# Invalidate caches in Android Studio
File → Invalidate Caches → Invalidate and Restart
```

### Build Errors
1. Ensure you have the correct JDK (11+)
2. Check Android SDK is installed (API 30-36)
3. Sync Gradle files
4. Clean and rebuild

### Notifications Not Showing
1. Check notification permission granted
2. Verify task due time is in future
3. Check reminder time calculation
4. Look for notifications in system tray
5. Check notification settings in device Settings

### App Crashes
1. Check Logcat in Android Studio
2. Look for stack traces
3. Verify all dependencies synced
4. Clean and rebuild project

## Project Structure Overview

```
TodoList/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lehau007/todolist/
│   │   │   │   ├── data/           # Data layer
│   │   │   │   ├── domain/         # Business logic
│   │   │   │   ├── ui/             # UI screens
│   │   │   │   ├── di/             # Dependency injection
│   │   │   │   ├── navigation/     # Navigation
│   │   │   │   └── notification/   # Notifications
│   │   │   ├── res/                # Resources
│   │   │   └── AndroidManifest.xml
│   │   └── test/                   # Unit tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml          # Dependency versions
└── README files                     # Documentation
```

## Key Features to Test

### ✅ Task Management
- [ ] Create task
- [ ] Edit task
- [ ] Delete task
- [ ] Mark complete/incomplete
- [ ] View task details

### ✅ Sorting
- [ ] Sort by date
- [ ] Sort by priority

### ✅ Filtering
- [ ] View all tasks
- [ ] View pending tasks
- [ ] View completed tasks

### ✅ Notifications
- [ ] Schedule notification
- [ ] Receive notification
- [ ] Tap notification (deep link)
- [ ] Cancel on completion
- [ ] Cancel on deletion

### ✅ UI/UX
- [ ] Material Design 3
- [ ] Empty state
- [ ] Priority colors
- [ ] Overdue highlighting
- [ ] Smooth animations
- [ ] Responsive layout

## Development Workflow

### Making Changes

1. **Create Feature Branch**
```bash
git checkout -b feature/new-feature
```

2. **Make Changes**
- Edit code
- Add tests
- Update documentation

3. **Run Tests**
```bash
./gradlew test
```

4. **Check for Errors**
- Build project
- Check Lint warnings
- Fix any issues

5. **Commit Changes**
```bash
git add .
git commit -m "feat: Add new feature"
```

### Code Style
- Follow Kotlin conventions
- Use meaningful names
- Add comments for complex logic
- Keep functions small
- Use sealed classes for states

### Adding New Features

1. **Data Layer**: Add entity, DAO methods
2. **Domain Layer**: Add model, use case
3. **Repository**: Implement data access
4. **ViewModel**: Add business logic
5. **UI**: Create/update screens
6. **Tests**: Add unit tests
7. **Documentation**: Update docs

## Documentation Files

- **README.md** - Original requirements
- **IMPLEMENTATION.md** - Comprehensive technical documentation
- **PROJECT_SUMMARY.md** - Implementation checklist and overview
- **ARCHITECTURE.md** - Architecture diagrams and patterns
- **QUICK_START.md** - This file

## Useful Commands

```bash
# Build
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK

# Install
./gradlew installDebug           # Install debug on device

# Test
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests

# Clean
./gradlew clean                  # Clean build

# Lint
./gradlew lint                   # Run lint checks

# Dependencies
./gradlew dependencies           # Show dependency tree

# Tasks
./gradlew tasks                  # Show all available tasks
```

## Performance Tips

### For Development
- Use Android Emulator with hardware acceleration
- Disable animations in emulator for faster testing
- Use Instant Run (if available)

### For Testing
- Use physical device for accurate performance
- Test on multiple API levels
- Test with real-world data volume

## Next Steps

1. **Explore the Code**: Start with `MainActivity.kt` and `NavGraph.kt`
2. **Run Tests**: Verify everything works
3. **Create Tasks**: Test the full workflow
4. **Read Docs**: Review IMPLEMENTATION.md for details
5. **Customize**: Add your own features!

## Support Resources

- **Documentation**: See IMPLEMENTATION.md
- **Architecture**: See ARCHITECTURE.md  
- **Issues**: Check Logcat for errors
- **Code Comments**: Most files have detailed comments

## Common Tasks

### Change App Name
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">Your App Name</string>
```

### Change Package Name
1. Right-click package in Android Studio
2. Refactor → Rename
3. Update `applicationId` in `build.gradle.kts`

### Add New Dependency
Edit `gradle/libs.versions.toml`:
```toml
[versions]
newLib = "1.0.0"

[libraries]
new-library = { group = "com.example", name = "library", version.ref = "newLib" }
```

Then add to `app/build.gradle.kts`:
```kotlin
dependencies {
    implementation(libs.new.library)
}
```

### Change Theme Colors
Edit `app/src/main/java/com/lehau007/todolist/ui/theme/Color.kt`

## Success Checklist

- [x] Project builds without errors
- [x] All tests pass
- [x] App runs on device/emulator
- [x] Can create, edit, delete tasks
- [x] Notifications work
- [x] UI is responsive
- [x] No hard-coded strings
- [x] Clean architecture followed
- [x] Documentation complete

## 🎉 You're Ready!

The app is fully functional and ready for development. Start by running the app and exploring the features. Happy coding!

---

**Need Help?** Check IMPLEMENTATION.md for detailed information.
