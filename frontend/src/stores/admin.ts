import { defineStore } from "pinia";

export const useAdminStore = defineStore("admin", {
  state: () => ({
    selectedWeaponId: "",
    selectedAttachmentId: "",
    selectedBuildId: "",
  }),
  actions: {
    setSelectedWeaponId(value: string) {
      this.selectedWeaponId = value;
    },
    setSelectedAttachmentId(value: string) {
      this.selectedAttachmentId = value;
    },
    setSelectedBuildId(value: string) {
      this.selectedBuildId = value;
    },
  },
});
