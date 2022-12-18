package com.barco.pipeline.model.repository;

import com.barco.pipeline.model.pojo.LookupData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Nabeel Ahmed
 */
@Repository
public interface LookupDataRepository extends CrudRepository<LookupData, Long> {

    /**
     * Method use to get the LookupData by lookupType if present in db
     * @param lookupType
     * @return LookupData
     * */
    public LookupData findByLookupType(String lookupType);

    public List<LookupData> findByParentLookupIdIsNull();
}