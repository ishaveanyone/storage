
package com.xupp.storage.service;


import com.alibaba.fastjson.JSONObject;
import com.xupp.storage.dao.MaterialDao;
import com.xupp.storage.define.*;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import com.alibaba.fastjson.JSON;


import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MaterialService  {
    @Autowired
    private MaterialDao materialDao;

    @Autowired
    private MapperFacade mapper;

    public MaterialTreeVO addMaterial(MaterialRequest addRequest, MaterialTypeEnum type) {
        //检查是否存在父级目录
        if(!String.valueOf(Constants.Material.MATERIAL_ROOT_DIR_PID).
                equals(addRequest.getParentGuid()))
        {
            String parentGud = addRequest.getParentGuid();
            if (StringUtils.isNotBlank(parentGud)) {
                ErsMaterialEntity checkParentMaterial = this.materialDao.findById(parentGud).get();
                if (null == checkParentMaterial) {
//                    throw new MaterialException.NotFound(parentGud);
                }
            }
        }

        Map<String,String> tags=addRequest.getTags();
        List<ErsMaterialEntity> adata= materialDao.findByParentGuid(addRequest.getParentGuid());
        //筛选是不是有 "相同"
        Optional<ErsMaterialEntity> filterdata= adata.stream().filter(o->{
            Map<String,String> ftags= JSONObject.parseObject(o.getTags(),Map.class);
            boolean flag=true;
            for(Map.Entry<String,String> entry:tags.entrySet()){
                if(!ftags.containsKey(entry.getKey())){
                    continue;
                }else{
                    String tvalue= ftags.get(entry.getKey());
                    flag=flag&&tvalue.equals(entry.getValue());
                }
            }
            flag=flag&&addRequest.getRefId().equals(o.getRefId());
            flag=flag&&addRequest.getName().equals(o.getName());
            return flag;
        }).findFirst();
        //检索同一层是不是已经存在同名文件了
       if(filterdata.isPresent()){
            //如果存在的话 就直接 返回一个数据
            return this.map(filterdata.get());
        }
        //找出这一层级最大的 如果是空 那么就指定postion 是 0
        ErsMaterialEntity ersMaterialEntity = this.mapper.map(addRequest, ErsMaterialEntity.class);
        ersMaterialEntity.setNodeType(type.getType());
        ersMaterialEntity.setCreator("");
        ersMaterialEntity.setTags(JSON.toJSONString(addRequest.getTags()));
        if(type.equals(MaterialTypeEnum.FILE)){
            ersMaterialEntity.setState(Constants.Material.UPLOADING);
        }else{
            ersMaterialEntity.setState(Constants.Material.NORMAL);
        }
        this.materialDao.save(ersMaterialEntity);
        return this.map(ersMaterialEntity);
    }

    public Boolean deleteMaterial(String materialGuid) {
        ErsMaterialEntity delEntity = this.materialDao.findById(materialGuid).get();
        if(null == delEntity){
//            throw new MaterialException.NotFound(materialGuid);
        }
        this.materialDao.delete(delEntity);
        return true;
    }


    @Transactional
    public Boolean mutiDeleteMaterial(List<String> materialGuids) {
        List<ErsMaterialEntity> rdelEntity = this.materialDao.findByGuidIn(materialGuids);
        materialDao.deleteAll(rdelEntity); // 如果删除失败就会回滚
        List<ErsMaterialEntity> drdelEntity= this.materialDao.findByGuidIn(materialGuids);
        if(drdelEntity.isEmpty()){
            return true;
        }
        return false;
    }

    //todo 获取列表接口
    public Object getMaterialTree(String refGuid) {
       return null;
    }

    public MaterialTreeVO getMaterial(String materialGuid) {
        ErsMaterialEntity ersMaterialEntity=materialDao.findById(materialGuid).get();
        if(ersMaterialEntity!=null){
            return this.map(ersMaterialEntity);
        }

        throw new RuntimeException("");
    }

    private MaterialTreeVO map(ErsMaterialEntity ersMaterialEntity){
        MaterialTreeVO materialTreeVO = new MaterialTreeVO();
        materialTreeVO.setNodeCode(ersMaterialEntity.getGuid());
        materialTreeVO.setNodeName(ersMaterialEntity.getName());
        materialTreeVO.setCreateTime(ersMaterialEntity.getCreateTime());
        materialTreeVO.setNodeType(ersMaterialEntity.getNodeType());
        materialTreeVO.setParentCode(ersMaterialEntity.getParentGuid());
        materialTreeVO.setCreator(ersMaterialEntity.getCreator());
        materialTreeVO.setTags(ersMaterialEntity.getTags());
        materialTreeVO.setState(ersMaterialEntity.getState());
        materialTreeVO.setSpace(ersMaterialEntity.getSpace());
        return materialTreeVO;
    }

    public List<MaterialTreeVO> getMaterialByLevel(MaterialQueryDTO materialQueryDTO) {
        List<ErsMaterialEntity> odata = this.materialDao.findAll(
                (Root<ErsMaterialEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    String fileName = materialQueryDTO.getFileName();
                    String parentId = materialQueryDTO.getParentid();
                    String refId = materialQueryDTO.getRefid();
                    String space = materialQueryDTO.getSpace();
                    if(StringUtils.isNotBlank(fileName)){
                        predicates.add(criteriaBuilder.like(root.get("name"), "%"+fileName+"%"));
                    }
                    if(StringUtils.isNotBlank(parentId)){
                        predicates.add(criteriaBuilder.equal(root.get("parentGuid"), parentId));
                    }
                    if(refId!=null){
                        predicates.add(criteriaBuilder.equal(root.get("refId"), refId));
                    }
                    if(StringUtils.isNotBlank(space)){
                        predicates.add(criteriaBuilder.equal(root.get("space"), space));
                    }

                    Predicate[] predicateArray = new Predicate[predicates.size()];
                    return criteriaBuilder.and(predicates.toArray(predicateArray));
        });

        //进行所有的数据的过滤
        Map<String,String> tags=materialQueryDTO.getTags();

        return odata.stream().filter(o->{
           if(tags==null||tags.isEmpty()){
               return true;
           }
           Map<String,String> ftags= JSONObject.parseObject(o.getTags(),Map.class);
           boolean flag=true;
           for(Map.Entry<String,String> entry:tags.entrySet()){
               if(!ftags.containsKey(entry.getKey())){
                  continue;
               }else{
                   String tvalue= ftags.get(entry.getKey());
                   flag=flag&&tvalue.equals(entry.getValue());
               }
           }
           return flag;
        }).sorted((o1,o2)->{
            Date d1=o1.getCreateTime();
            Date d2=o2.getCreateTime();
            return d1.compareTo(d2); //进行排序后进行返回
        }).map(o->this.map(o)).collect(Collectors.toList());
    }


    public Boolean updateMaterialState(String nodeGuid, Integer state) {
        ErsMaterialEntity ersMaterialEntity= materialDao.findById(nodeGuid).get();
        Assert.notNull(ersMaterialEntity,"参数错误 查询不到文件");
        ersMaterialEntity.setState(state);
        materialDao.save(ersMaterialEntity);
        return true;
    }

    public ErsMaterialEntity getMaterialInfo(String guid) {
        return materialDao.findById(guid).get();
    }


}
