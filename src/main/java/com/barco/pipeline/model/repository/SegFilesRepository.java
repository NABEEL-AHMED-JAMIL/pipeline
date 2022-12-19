package com.barco.pipeline.model.repository;

import com.barco.pipeline.model.pojo.SegFiles;
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
public interface SegFilesRepository extends JpaRepository<SegFiles, Long> {

    @Modifying
    @Query(value = "delete from seg_files where source_job_id = ?1 and pipeline_id = ?2 and folder_id = ?3 ", nativeQuery = true)
    void deleteFileBySourceJobIdAndPipelineIdAndFolderId(Long sourceJobId, Long pipelineId, Long folderId);

    @Modifying
    @Query(value = "update seg_files set target_file_status = ?1 where folder_id in (select folder_id from seg_folder " +
        "where source_job_id = ?2 and pipeline_id = ?3 and target_folder_name in ?4)", nativeQuery = true)
    void updateFileBySourceJobIdAndPipelineIdAndTargetFolderNameIn(Integer fileStatus,
       Long sourceJobId, Long pipelineId, List<String> targetFolderNames);
    @Modifying
    @Query(value = "update seg_files set target_file_status = ?1 where folder_id in (select folder_id from seg_folder " +
        "where source_job_id = ?2 and pipeline_id = ?3 and target_folder_name = ?4)", nativeQuery = true)
    void updateFileBySourceJobIdAndPipelineIdAndTargetFolderName(Integer fileStatus,
        Long sourceJobId, Long pipelineId, String targetFolderName);
}
