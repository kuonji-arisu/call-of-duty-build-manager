import type {
  NAlert,
  NButton,
  NCard,
  NCheckbox,
  NCheckboxGroup,
  NConfigProvider,
  NDescriptions,
  NDescriptionsItem,
  NDivider,
  NEmpty,
  NFlex,
  NForm,
  NFormItem,
  NImage,
  NInput,
  NInputNumber,
  NLayout,
  NLayoutContent,
  NLayoutHeader,
  NLayoutSider,
  NList,
  NListItem,
  NMessageProvider,
  NResult,
  NScrollbar,
  NSelect,
  NSpace,
  NSpin,
  NTag,
  NThing,
} from "naive-ui";

declare module "vue" {
  export interface GlobalComponents {
    NAlert: typeof NAlert;
    NButton: typeof NButton;
    NCard: typeof NCard;
    NCheckbox: typeof NCheckbox;
    NCheckboxGroup: typeof NCheckboxGroup;
    NConfigProvider: typeof NConfigProvider;
    NDescriptions: typeof NDescriptions;
    NDescriptionsItem: typeof NDescriptionsItem;
    NDivider: typeof NDivider;
    NEmpty: typeof NEmpty;
    NFlex: typeof NFlex;
    NForm: typeof NForm;
    NFormItem: typeof NFormItem;
    NImage: typeof NImage;
    NInput: typeof NInput;
    NInputNumber: typeof NInputNumber;
    NLayout: typeof NLayout;
    NLayoutContent: typeof NLayoutContent;
    NLayoutHeader: typeof NLayoutHeader;
    NLayoutSider: typeof NLayoutSider;
    NList: typeof NList;
    NListItem: typeof NListItem;
    NMessageProvider: typeof NMessageProvider;
    NResult: typeof NResult;
    NScrollbar: typeof NScrollbar;
    NSelect: typeof NSelect;
    NSpace: typeof NSpace;
    NSpin: typeof NSpin;
    NTag: typeof NTag;
    NThing: typeof NThing;
  }
}

export {};
