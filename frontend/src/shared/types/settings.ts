export interface AppSetting {
  key: string;
  value: string;
  updatedAt: string;
}

export interface AppInfo {
  language: string;
}

export interface InitialData {
  appInfo: AppInfo;
  settings: AppSetting[];
}
