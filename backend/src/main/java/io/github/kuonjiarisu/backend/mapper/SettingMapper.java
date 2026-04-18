package io.github.kuonjiarisu.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import io.github.kuonjiarisu.backend.model.AppSetting;

@Mapper
public interface SettingMapper {

    List<AppSetting> findAll();

    AppSetting findByKey(@Param("key") String key);

    void upsert(AppSetting setting);
}
