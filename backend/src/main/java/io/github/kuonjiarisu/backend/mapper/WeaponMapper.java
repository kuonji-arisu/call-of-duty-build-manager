package io.github.kuonjiarisu.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import io.github.kuonjiarisu.backend.model.OwnedStringValue;
import io.github.kuonjiarisu.backend.model.Weapon;
import io.github.kuonjiarisu.backend.model.WeaponRow;

@Mapper
public interface WeaponMapper {

    List<WeaponRow> findAllRows();

    List<WeaponRow> findRows(
        @Param("keyword") String keyword,
        @Param("weaponType") String weaponType,
        @Param("generation") String generation,
        @Param("favorite") Boolean favorite,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    long countRows(
        @Param("keyword") String keyword,
        @Param("weaponType") String weaponType,
        @Param("generation") String generation,
        @Param("favorite") Boolean favorite
    );

    WeaponRow findRowById(@Param("id") String id);

    List<WeaponRow> findRowsByIds(@Param("ids") List<String> ids);

    List<OwnedStringValue> findAllGenerations();

    List<OwnedStringValue> findGenerationsByWeaponIds(@Param("ids") List<String> ids);

    List<OwnedStringValue> findAllSlots();

    List<OwnedStringValue> findSlotsByWeaponIds(@Param("ids") List<String> ids);

    int countById(@Param("id") String id);

    int countBuildsByWeaponId(@Param("weaponId") String weaponId);

    int countAttachmentBindingsByWeaponId(@Param("weaponId") String weaponId);

    int countBuildItemsByWeaponIdAndSlots(
        @Param("weaponId") String weaponId,
        @Param("slots") List<String> slots
    );

    int countAttachmentBindingsByWeaponIdAndSlots(
        @Param("weaponId") String weaponId,
        @Param("slots") List<String> slots
    );

    int countBuildsByWeaponIdAndGenerations(
        @Param("weaponId") String weaponId,
        @Param("generations") List<String> generations
    );

    int countInvalidAttachmentBindingsByWeaponIdAndGenerations(
        @Param("weaponId") String weaponId,
        @Param("generations") List<String> generations
    );

    void upsertRow(WeaponRow weapon);

    int deleteGenerationsByWeaponId(@Param("weaponId") String weaponId);

    int deleteSlotsByWeaponId(@Param("weaponId") String weaponId);

    void insertGenerations(@Param("weaponId") String weaponId, @Param("values") List<String> values);

    void insertSlots(@Param("weaponId") String weaponId, @Param("values") List<String> values);

    int deleteById(@Param("id") String id);
}
