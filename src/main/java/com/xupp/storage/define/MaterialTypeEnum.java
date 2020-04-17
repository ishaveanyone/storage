
package com.xupp.storage.define;

public enum MaterialTypeEnum {
    DIRECTORY(0), FILE(1);

    Integer type;

    MaterialTypeEnum(Integer type){
        this.type = type;
    }

   public Integer getType(){
        return this.type;
   }



}
