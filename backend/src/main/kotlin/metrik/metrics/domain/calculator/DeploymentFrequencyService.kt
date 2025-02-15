package metrik.metrics.domain.calculator

import metrik.project.domain.model.Build
import metrik.project.domain.model.Status
import metrik.project.domain.repository.BuildRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DeploymentFrequencyService {
    @Autowired
    private lateinit var buildRepository: BuildRepository

    fun getDeploymentCount(pipelineId: String, targetStage: String, startTimestamp: Long, endTimestamp: Long): Int {
        return buildRepository.getAllBuilds(pipelineId)
                .sortedBy { it.timestamp }
                .filterNot { isInvalidBuild(it, startTimestamp, endTimestamp, targetStage) }
                .count()
    }

    private fun isInvalidBuild(currentBuild: Build, startTimestamp: Long,
                               endTimestamp: Long, targetStage: String): Boolean {
        return !isTargetStageWithinTimeRange(currentBuild, startTimestamp, endTimestamp, targetStage) ||
                !isTargetStageSuccess(currentBuild, targetStage)
    }

    private fun isTargetStageSuccess(build: Build, targetStage: String) =
            build.stages.find { stage -> stage.name == targetStage }?.status == Status.SUCCESS

    private fun isTargetStageWithinTimeRange(build: Build, startTimestamp: Long,
                                             endTimestamp: Long, targetStage: String): Boolean {
        val stage = build.stages.find { it.name == targetStage }
        val deploymentFinishTimestamp = stage?.getStageDoneTime()

        return deploymentFinishTimestamp in startTimestamp..endTimestamp
    }
}
