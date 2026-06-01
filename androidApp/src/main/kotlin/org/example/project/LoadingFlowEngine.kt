package org.example.project

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.withContext
import org.example.project.ev.FlowSignal
import org.example.project.ev.LoadingDecision
import org.example.project.ev.LoadingFlowContext
import org.example.project.ev.LoadingNode
import org.example.project.storage.ScoreStorage
import java.util.concurrent.atomic.AtomicReference

class LoadingFlowEngine(
    private val nodes: List<LoadingNode>
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun execute(
        context: Context,
        storage: ScoreStorage
    ): Flow<LoadingDecision> {

        val contextRef = AtomicReference(
            LoadingFlowContext(
                context = context.applicationContext,
                storage = storage
            )
        )

        return nodes
            .asFlow()

            .onStart {
                log("LoadingFlowEngine: started")
            }

            .flatMapConcat { node ->

                flow {

                    val currentContext = contextRef.get()

//                    log("LoadingFlowEngine: running ${node.name}")

                    val result = withContext(Dispatchers.IO) {
                        node.run(currentContext)
                    }

                    val updatedContext = result.first
                    val signal = result.second

                    contextRef.set(updatedContext)

                    emit(
                        NodeExecution(
                            nodeName = node.name,
                            signal = signal
                        )
                    )
                }
            }

            .onEach { execution ->

                when (val signal = execution.signal) {

                    FlowSignal.Continue -> {
                        log("LoadingFlowEngine: ${execution.nodeName} -> continue")
                    }

                    is FlowSignal.Finish -> {

                        log("LoadingFlowEngine: ${execution.nodeName} -> finish")

                        log("LoadingFlowEngine: decision = ${signal.decision}")
                    }
                }
            }

            .transformWhile { execution ->

                when (val signal = execution.signal) {

                    FlowSignal.Continue -> {
                        true
                    }

                    is FlowSignal.Finish -> {

                        emit(signal.decision)

                        false
                    }
                }
            }

            .catch { throwable ->

                log("LoadingFlowEngine: error = ${throwable.message}")

                emit(LoadingDecision.OpenOtherScreen(reason = "flow_error"))
            }

            .flowOn(Dispatchers.IO)
    }
}

private data class NodeExecution(
    val nodeName: String,
    val signal: FlowSignal
)