package org.example.project.no

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import kotlinx.coroutines.suspendCancellableCoroutine
import org.example.project.ev.FlowSignal
import org.example.project.ev.LoadingFlowContext
import org.example.project.ev.LoadingNode
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

class ReferrerNode : LoadingNode {
    override val name: String = "ReferrerNode"

    override suspend fun run(ctx: LoadingFlowContext): Pair<LoadingFlowContext, FlowSignal> {

        val ref = getRef(ctx.context)

        return ctx.put("referrer", ref) to FlowSignal.Continue
    }

    suspend fun getRef(context: Context): String = suspendCancellableCoroutine { continuation ->

        val client = InstallReferrerClient.newBuilder(context).build()
        val isResumed = AtomicBoolean(false)

        continuation.invokeOnCancellation {
            client.endConnection()
        }

        client.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    if (!isResumed.compareAndSet(false, true)) return

                    val result = if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                        client.installReferrer.installReferrer
                    } else {
                        "null"
                    }

                    continuation.resume(result)
//                continuation.resume("cmpgn=aaaa_TEST-Deeplink_bbbb_cccc_dddd")
                    //                continuation.resume("cmpgn=test1_MA-TEST_22_33_sub5_sub6")
//                    continuation.resume("cmpgn=test1_CA-TEST_22_33_sub5_sub6")

                } catch (_: Exception) {
                    if (isResumed.compareAndSet(false, true)) {
                        continuation.resume("null")
                    }
                } finally {
                    client.endConnection()
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                if (isResumed.compareAndSet(false, true)) {
                    continuation.resume("null")
                }
            }
        })
    }
}