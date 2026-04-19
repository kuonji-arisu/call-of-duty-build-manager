package io.github.kuonjiarisu.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import io.github.kuonjiarisu.backend.model.Build;
import io.github.kuonjiarisu.backend.model.BuildItem;
import io.github.kuonjiarisu.backend.model.BuildRow;
import io.github.kuonjiarisu.backend.model.OwnedStringValue;

@Mapper
public interface BuildMapper {

    List<BuildRow> findBuildRows(
        @Param("keyword") String keyword,
        @Param("weaponId") String weaponId,
        @Param("generation") String generation,
        @Param("favorite") Boolean favorite,
        @Param("hideMissingWeapon") boolean hideMissingWeapon,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    long countBuildRows(
        @Param("keyword") String keyword,
        @Param("weaponId") String weaponId,
        @Param("generation") String generation,
        @Param("favorite") Boolean favorite,
        @Param("hideMissingWeapon") boolean hideMissingWeapon
    );

    BuildRow findBuildRowById(@Param("id") String id);

    List<BuildRow> findBuildRowsByIds(@Param("ids") List<String> ids);

    List<OwnedStringValue> findGenerationsByBuildIds(@Param("ids") List<String> ids);

    List<BuildItem> findBuildItemsByAttachmentId(@Param("attachmentId") String attachmentId);

    List<BuildItem> findBuildItemsByBuildId(@Param("buildId") String buildId);

    List<BuildItem> findBuildItemsByBuildIds(@Param("ids") List<String> ids);

    int countById(@Param("id") String id);

    void upsertBuildRow(BuildRow build);

    int deleteGenerationsByBuildId(@Param("buildId") String buildId);

    void insertGenerations(@Param("buildId") String buildId, @Param("values") List<String> values);

    int deleteBuildById(@Param("id") String id);

    int deleteItemsByBuildId(@Param("buildId") String buildId);

    void insertItems(@Param("items") List<BuildItem> items);
}
