package com.xupp.storage.dao;

import com.xupp.storage.model.po.ErsMaterialEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MaterialDao extends JpaRepository<ErsMaterialEntity, String>, JpaSpecificationExecutor<ErsMaterialEntity> {
    List<ErsMaterialEntity> findByRefId(String refId);

    List<ErsMaterialEntity> findByRefId(String refId, Sort sort);

    List<ErsMaterialEntity> findByGuidIn(List<String> guids);

    List<ErsMaterialEntity> findByParentGuidIn(List<String> guids);
    List<ErsMaterialEntity> findByParentGuid(String parentGuid);

    //通过 guid
    List<ErsMaterialEntity> findByParentGuidAndSpaceAndRefId(String parent, String space, String refid);


    List<ErsMaterialEntity> findByParentGuidAndSpaceAndRefIdAndState(String parent, String space, String refid, Integer state);



}
