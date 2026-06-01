package org.example.project

import org.example.project.no.BrokenScoreNode
import org.example.project.no.CachedScoreNode
import org.example.project.no.DeviceNode
import org.example.project.no.FirebaseIdNode
import org.example.project.no.FirstInstNode
import org.example.project.no.GadidNode
import org.example.project.no.ProbeNode
import org.example.project.no.ReferrerNode
import org.example.project.no.ScoreAssembleNode

object LoadingFlowFactory {

    fun create(baseUrl: String): LoadingFlowEngine {
        return LoadingFlowEngine(
            nodes = listOf(
                CachedScoreNode(),
                BrokenScoreNode(),
                ReferrerNode(),
                GadidNode(),
                DeviceNode(),
                ProbeNode(),
                FirstInstNode(),
                FirebaseIdNode(),
                ScoreAssembleNode(
                    scoreBuilder = ScoreBuilder(baseUrl)
                )
            )
        )
    }
}