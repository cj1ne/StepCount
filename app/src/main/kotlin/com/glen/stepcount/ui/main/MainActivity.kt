package com.glen.stepcount.ui.main

import android.Manifest
import android.content.Intent
import android.health.connect.HealthConnectManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.glen.stepcount.R
import com.glen.stepcount.databinding.ActivityMainBinding
import com.glen.stepcount.ui.ReadStepsWorker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
    }

    @Inject
    lateinit var healthConnectClient: Lazy<HealthConnectClient>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpStepCountView()
        setUpSettingsView()
        setUpPermissionView()
        requestNotificationPermission()
    }

    private fun setUpStepCountView() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.stepCount }.distinctUntilChanged().collect {
                    binding.stepCount.text = it
                }
            }
        }
    }

    private fun setUpSettingsView() {
        binding.settingsButton.setOnClickListener { showHealthConnectMarket() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.settingsStatus }.distinctUntilChanged().collect {
                    binding.settingsButton.text = it.toString(this@MainActivity)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.settingsEnabled }.distinctUntilChanged().collect {
                    binding.settingsButton.isEnabled = it
                }
            }
        }
    }

    private fun setUpPermissionView() {
        binding.permissionButton.setOnClickListener { showHealthConnectPermissionSettings() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.permissionStatus }.distinctUntilChanged().collect {
                    binding.permissionButton.text = it.toString(this@MainActivity)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.map { it.permissionEnabled }.distinctUntilChanged().collect {
                    binding.permissionButton.isEnabled = it
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkHealthConnect()
    }

    /**
     * 헬스 커넥트 SDK 사용 가능 여부 확인.
     *
     * [HealthConnectClient] 인스턴스는 [HealthConnectClient.SDK_AVAILABLE]인 경우에만 생성할 수 있으므로
     * Status가 [HealthConnectClient.SDK_AVAILABLE]인 경우에만 권한 여부 확인.
     *
     * Status가 [HealthConnectClient.SDK_UNAVAILABLE]인 경우 앱의 기능을 사용할 수 없으므로 미지원 다이얼로그 노출.
     *
     */
    private fun checkHealthConnect() {
        val status = HealthConnectClient.getSdkStatus(this)
        viewModel.onUpdateHealthConnectStatus(status == HealthConnectClient.SDK_AVAILABLE)
        when (status) {
            HealthConnectClient.SDK_AVAILABLE -> checkPermission()
            HealthConnectClient.SDK_UNAVAILABLE -> showUnavailableDialog()
        }
    }

    /**
     * 걸음 수 읽기 권한을 확인하여 권한이 허용된 경우 실시간 걸음 수 데이터 연동을 위해 [ReadStepsWorker]를 실행.
     *
     */
    private fun checkPermission() {
        lifecycleScope.launch {
            val granted = healthConnectClient.get().permissionController.getGrantedPermissions()
            val hasPermission = granted.contains(HealthPermission.getReadPermission(StepsRecord::class))
            viewModel.onUpdateReadStepsPermission(hasPermission)
            if (hasPermission) {
                enqueueReadStepsWork()
            }
        }
    }

    private fun enqueueReadStepsWork() {
        val readStepsWork = OneTimeWorkRequestBuilder<ReadStepsWorker>().build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "readSteps",
                ExistingWorkPolicy.KEEP,
                readStepsWork
            )
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) return
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /**
     * 걸음 수 읽기 권한을 변경하기 위해 헬스 커넥트 권한 설정 화면 실행.
     *
     * 헬스 커넥트 SDK에서 Permission launcher를 제공하고 있지만 해당 launcher를 실행하여 진입한 화면에서
     * 취소 버튼을 여러 번 클릭한 경우 launcher 재실행 시 아무런 동작을 하지 않아 권한 설정 화면으로 이동하도록 처리.
     */
    private fun showHealthConnectPermissionSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Intent(HealthConnectManager.ACTION_MANAGE_HEALTH_PERMISSIONS)
                .putExtra(Intent.EXTRA_PACKAGE_NAME, packageName)
        } else {
            Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
        }
        startActivity(intent)
    }

    private fun showUnavailableDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.app_unavailable_message)
            .setPositiveButton(R.string.confirm) { _, _ -> Process.killProcess(Process.myPid()) }
            .show()
    }

    private fun showHealthConnectMarket() {
        val uri = Uri.Builder().scheme("market")
            .authority("details")
            .appendQueryParameter("id", "com.google.android.apps.healthdata")
            .build()

        runCatching {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
                    data = uri
                    putExtra("overlay", true)
                    putExtra("callerId", packageName)
                }
            )
        }.onFailure {
            Toast.makeText(this, R.string.activity_not_found_error_message, Toast.LENGTH_SHORT).show()
        }
    }
}