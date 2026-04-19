import type {
  AttachmentTag,
  Generation,
  Slot,
  WeaponType,
} from "../shared/types";
import type { PageQuery } from "./query";

export interface WeaponListQuery extends PageQuery {
  keyword?: string;
  weaponType?: WeaponType | "ALL";
  generation?: Generation | "ALL";
  favorite?: boolean | null;
}

export interface AttachmentListQuery extends PageQuery {
  keyword?: string;
  slot?: Slot | "ALL";
  generation?: Generation | "ALL";
  tag?: AttachmentTag | "ALL";
  weaponId?: string;
}

export interface BuildListQuery extends PageQuery {
  keyword?: string;
  weaponId?: string;
  generation?: Generation | "ALL";
  favorite?: boolean | null;
}

export interface EffectDefinitionListQuery extends PageQuery {
  keyword?: string;
}

export interface OptionSearchQuery extends PageQuery {
  keyword?: string;
}

export interface WeaponAttachmentOptionQuery extends OptionSearchQuery {
  slot?: Slot | "ALL";
  generation?: Generation;
}

export interface AttachmentBindingListQuery extends AttachmentListQuery {
  bound?: boolean | null;
}
