export interface AppSetting {
  key: string;
  value: string;
  updatedAt: string;
}

export interface AppInfo {
  language: string;
}

export interface GenerationOption {
  value: string;
  label: string;
}

export interface CatalogData {
  generations: GenerationOption[];
}

export interface InitialData {
  appInfo: AppInfo;
  catalog: CatalogData;
  settings: AppSetting[];
}
