package sk.fiit.dp.dpintellijplugin.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

@Service(Service.Level.PROJECT)
class ProjectCoroutineScopeService : Disposable, CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job

    override fun dispose() {
        job.cancel()
    }
}