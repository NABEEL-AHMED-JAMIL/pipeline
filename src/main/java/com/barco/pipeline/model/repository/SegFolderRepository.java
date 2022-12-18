package com.barco.pipeline.model.repository;

import com.barco.pipeline.model.pojo.SegFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author Nabeel Ahmed
 */
@Repository
@Transactional
public interface SegFolderRepository extends JpaRepository<SegFolder, Long> {

    List<SegFolder> findAllSegFolderBySourceJobIdAndPipelineId(Long sourceJobId, Long pipelineId);

    @Modifying
    @Query(value = "update seg_folder set target_folder_status = ?1 where source_job_id = ?2 and pipeline_id = ?3 and target_folder_name in ?4", nativeQuery = true)
    void updateFolderBySourceJobIdAndPipelineIdAndTargetFolderNameIn(Integer fileStatus, Long sourceJobId, Long pipelineId, List<String> targetFolderNames);

}
