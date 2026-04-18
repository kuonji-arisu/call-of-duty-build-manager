package io.github.kuonjiarisu.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import io.github.kuonjiarisu.backend.model.AttachmentEffectDefinition;
import io.github.kuonjiarisu.backend.model.AttachmentEffectDefinitionOption;

@Mapper
public interface AttachmentEffectDefinitionMapper {

    List<AttachmentEffectDefinition> findAll();

    List<AttachmentEffectDefinitionOption> findOptions();

    List<AttachmentEffectDefinition> findPage(
        @Param("keyword") String keyword,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    long countPage(@Param("keyword") String keyword);

    AttachmentEffectDefinition findById(@Param("id") String id);

    int countById(@Param("id") String id);

    int countEffectsByDefinitionId(@Param("definitionId") String definitionId);

    void upsert(AttachmentEffectDefinition definition);

    int deleteById(@Param("id") String id);
}
