package io.github.kuonjiarisu.backend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import io.github.kuonjiarisu.backend.model.AttachmentEffectRow;
import io.github.kuonjiarisu.backend.model.AttachmentRow;
import io.github.kuonjiarisu.backend.model.OwnedStringValue;
import io.github.kuonjiarisu.backend.model.command.AttachmentEffectSaveCommand;

@Mapper
public interface AttachmentMapper {

    List<AttachmentRow> findAllRows();

    List<AttachmentRow> findRows(
        @Param("keyword") String keyword,
        @Param("slot") String slot,
        @Param("generation") String generation,
        @Param("tag") String tag,
        @Param("weaponId") String weaponId,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    long countRows(
        @Param("keyword") String keyword,
        @Param("slot") String slot,
        @Param("generation") String generation,
        @Param("tag") String tag,
        @Param("weaponId") String weaponId
    );

    List<AttachmentRow> findBindingCandidateRows(
        @Param("weaponId") String weaponId,
        @Param("weaponSlots") List<String> weaponSlots,
        @Param("weaponGenerations") List<String> weaponGenerations,
        @Param("keyword") String keyword,
        @Param("slot") String slot,
        @Param("generation") String generation,
        @Param("tag") String tag,
        @Param("bound") Boolean bound,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    long countBindingCandidateRows(
        @Param("weaponId") String weaponId,
        @Param("weaponSlots") List<String> weaponSlots,
        @Param("weaponGenerations") List<String> weaponGenerations,
        @Param("keyword") String keyword,
        @Param("slot") String slot,
        @Param("generation") String generation,
        @Param("tag") String tag,
        @Param("bound") Boolean bound
    );

    List<AttachmentRow> findAvailableRowsForWeapon(
        @Param("weaponId") String weaponId,
        @Param("weaponSlots") List<String> weaponSlots,
        @Param("weaponGenerations") List<String> weaponGenerations,
        @Param("candidateGenerations") List<String> candidateGenerations,
        @Param("keyword") String keyword,
        @Param("slot") String slot,
        @Param("generation") String generation,
        @Param("tag") String tag,
        @Param("limit") int limit,
        @Param("offset") int offset
    );

    long countAvailableRowsForWeapon(
        @Param("weaponId") String weaponId,
        @Param("weaponSlots") List<String> weaponSlots,
        @Param("weaponGenerations") List<String> weaponGenerations,
        @Param("candidateGenerations") List<String> candidateGenerations,
        @Param("keyword") String keyword,
        @Param("slot") String slot,
        @Param("generation") String generation,
        @Param("tag") String tag
    );

    List<AttachmentRow> findAvailableOptionRowsForWeapon(
        @Param("weaponId") String weaponId,
        @Param("weaponSlots") List<String> weaponSlots,
        @Param("weaponGenerations") List<String> weaponGenerations,
        @Param("slot") String slot
    );

    AttachmentRow findRowById(@Param("id") String id);

    List<AttachmentRow> findRowsByIds(@Param("ids") List<String> ids);

    List<OwnedStringValue> findWeaponIdsByAttachmentIds(@Param("ids") List<String> ids);

    List<String> findAttachmentIdsBoundToWeapon(
        @Param("weaponId") String weaponId,
        @Param("attachmentIds") List<String> attachmentIds
    );

    List<OwnedStringValue> findGenerationsByAttachmentIds(@Param("ids") List<String> ids);

    List<AttachmentEffectRow> findAllEffects();

    List<AttachmentEffectRow> findEffectsByAttachmentIds(@Param("ids") List<String> ids);

    int countById(@Param("id") String id);

    int countBuildItemsByAttachmentId(@Param("attachmentId") String attachmentId);

    int countBuildItemsByWeaponIdAndAttachmentIds(
        @Param("weaponId") String weaponId,
        @Param("attachmentIds") List<String> attachmentIds
    );

    void upsertRow(AttachmentRow attachment);

    int deleteWeaponIdsByAttachmentId(@Param("attachmentId") String attachmentId);

    int deleteGenerationsByAttachmentId(@Param("attachmentId") String attachmentId);

    int deleteEffectsByAttachmentId(@Param("attachmentId") String attachmentId);

    void insertWeaponBindingIfAbsent(
        @Param("attachmentId") String attachmentId,
        @Param("weaponId") String weaponId
    );

    int deleteWeaponBindingsByWeaponIdAndAttachmentIds(
        @Param("weaponId") String weaponId,
        @Param("attachmentIds") List<String> attachmentIds
    );

    void insertGenerations(@Param("attachmentId") String attachmentId, @Param("values") List<String> values);

    void insertEffects(
        @Param("attachmentId") String attachmentId,
        @Param("effects") List<AttachmentEffectSaveCommand> effects
    );

    int deleteById(@Param("id") String id);
}
