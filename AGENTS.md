# Codebase Guide - EcodeForAndroid

## 1. Project Overview
- **Type**: Android Application
- **Language**: Java 1.8 (Core), Gradle (Build)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35
- **Architecture**: Minimal/Bootstrap state (Currently Single Activity)
- **Key Libraries**:
    - `okhttp3` (Networking)
    - `fastjson2` (JSON Parsing)
    - `androidx.appcompat` (UI/Compat)

## 2. Build & Test Commands

### Basic Build
- **Clean**: `./gradlew clean`
- **Build**: `./gradlew build` (Assembles and tests)
- **Assemble Debug**: `./gradlew assembleDebug`
- **Assemble Release**: `./gradlew assembleRelease`

### Testing
- **Run Unit Tests**: `./gradlew test`
- **Run Debug Unit Tests**: `./gradlew testDebugUnitTest`
- **Run Instrumented Tests**: `./gradlew connectedAndroidTest`
- **Run Single Test Class**: `./gradlew testDebugUnitTest --tests "com.github.ecode.ExampleUnitTest"`
- **Run Single Test Method**: `./gradlew testDebugUnitTest --tests "com.github.ecode.ExampleUnitTest.addition_isCorrect"`

### Linting & Verification
- **Lint Check**: `./gradlew lint`
- **Lint Fix**: `./gradlew lintFix` (Safe auto-fixes)
- **Check All**: `./gradlew check`

## 3. Code Style & Conventions

### Language Standards
- **Java Version**: Java 8 (Streams, Lambdas allowed)
- **Encoding**: UTF-8

### Naming Conventions
- **Classes**: PascalCase (e.g., `MainActivity`, `OkHttpUtils`)
- **Methods/Variables**: camelCase (e.g., `onCreate`, `paramMap`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `TAG`)
- **Layout Files**: snake_case (e.g., `activity_main.xml`)
- **Packages**: lowercase (e.g., `com.github.ecode.util`)

### Structure & Organization
- **Packages**:
    - `com.github.ecode`: Root package, Activities
    - `com.github.ecode.util`: Utility classes (e.g., Networking)
- **Imports**: No wildcard imports (`import java.util.*`). Explicit imports only.
- **Ordering**:
    1. Android/AndroidX imports
    2. Third-party libraries
    3. Java standard libraries
    4. Internal project classes

### Implementation Patterns
- **Activity**: Extend `AppCompatActivity`.
- **UI Binding**: Currently using `findViewById`. Consider ViewBinding for new features.
- **Networking**:
    - Use `OkHttpUtils` wrapper.
    - **Do NOT** create raw `OkHttpClient` instances in UI code.
    - **Async Requests**: Prefer `async(ICallBack)` for non-blocking UI operations.
    - **SSL/HTTPS**: `OkHttpUtils` handles SSL configuration (currently trusts all certs - *Dev Mode*).

### Error Handling
- **Network**: Handle `IOException` in callbacks.
- **UI**: Ensure network callbacks run on Main Thread if updating UI (Note: `OkHttpUtils` callbacks run on background threads; use `runOnUiThread`).
- **Logging**: Use `android.util.Log` with `TAG` constant.

## 4. Development Workflow

### Adding Dependencies
- Add to `app/build.gradle` in `dependencies` block.
- Use version catalog `libs.*` if available, or strict version strings if not defined in catalog.

### New Features
1. **Model**: Create data classes (POJOs) for JSON parsing.
2. **Network**: Add methods to `OkHttpUtils` or create Repository classes if logic grows.
3. **UI**: Create layout XML first, then Activity logic.

### Documentation
- **JavaDoc**: Required for public utility methods (see `OkHttpUtils`).
- **Comments**: Inline comments for complex logic (e.g., SSL setup).

---

## 5. Specific Rules for Agents

- **File Creation**: Always verify parent directory existence before creation.
- **Editing**: Read file content first. Use unique context for `edit` tool.
- **UI**: Prioritize `Material Design 3` components as requested in `Develop.md`.
- **Features**: Follow requirements in `Develop.md` (Login, Scan, User Info).
- **Security**:
    - Passwords must be MD5 hashed before sending (per `Develop.md`).
    - Store Tokens securely (SharedPrefs/EncryptedSharedPreferences).
