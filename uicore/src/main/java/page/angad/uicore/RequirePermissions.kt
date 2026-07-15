package page.angad.uicore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequirePermission(
    permission: String,
    onSuccess: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val state = rememberPermissionState(permission)

    LaunchedEffect(state.status) {
        if (!state.status.isGranted) state.launchPermissionRequest()
        else onSuccess()
    }

    if (state.status.isGranted) {
        content()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequirePermissions(
    permissions: List<String>,
    onSuccess: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val state = rememberMultiplePermissionsState(permissions)

    LaunchedEffect(state.allPermissionsGranted) {
        if (!state.allPermissionsGranted) state.launchMultiplePermissionRequest()
        else onSuccess()
    }

    if (state.allPermissionsGranted) {
        content()
    }
}