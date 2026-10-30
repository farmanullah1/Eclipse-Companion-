# Dependency and License Inventory

The application uses exclusively open-source, permissive libraries bundled at compile-time:

| Library | Version | License | Purpose |
|---|---|---|---|
| `androidx.core:core-ktx` | 1.18.0 | Apache 2.0 | Android KTX extensions |
| `androidx.activity:activity-compose` | 1.10.1 | Apache 2.0 | Jetpack Compose Activity integration |
| `androidx.compose.material3:material3` | BOM 2024.09.00 | Apache 2.0 | Material Design 3 UI components |
| `androidx.compose.ui:ui` | BOM 2024.09.00 | Apache 2.0 | Compose UI toolkit |
| `androidx.compose.material:material-icons-extended` | BOM 2024.09.00 | Apache 2.0 | Material Symbols & Icons |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.8.7 | Apache 2.0 | MVVM lifecycle management |
| `androidx.lifecycle:lifecycle-runtime-compose` | 2.8.7 | Apache 2.0 | Flow collection with lifecycle safety |
| `androidx.datastore:datastore-preferences` | 1.1.7 | Apache 2.0 | Local asynchronous key-value persistence |
| `androidx.navigation:navigation-compose` | 2.8.9 | Apache 2.0 | In-app screen routing |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.10.2 | Apache 2.0 | Asynchronous concurrency & Flow |
| `junit:junit` | 4.13.2 | EPL 1.0 | Local unit testing |
| `org.robolectric:robolectric` | 4.16.1 | MIT | JVM Android framework unit testing |

### Network Audit
- No HTTP client (OkHttp/Retrofit) is engaged in runtime code.
- No analytics or remote tracking SDKs.
- `android.permission.INTERNET` is omitted from `AndroidManifest.xml`.
