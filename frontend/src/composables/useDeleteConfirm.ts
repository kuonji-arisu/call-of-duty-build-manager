interface UseDeleteConfirmOptions<T> {
  dialog: {
    error: (options: {
      title: string;
      content: string;
      positiveText: string;
      negativeText: string;
      positiveButtonProps: { type: "error" };
      onPositiveClick: () => Promise<void>;
    }) => void;
  };
  message: {
    success: (content: string) => void;
  };
  deletingId: { value: string };
  getId: (item: T) => string;
  title: string;
  content: (item: T) => string;
  successText: string;
  deleteAction: (item: T) => Promise<void>;
  onError: (message: string) => void;
}

export function useDeleteConfirm<T>(options: UseDeleteConfirmOptions<T>) {
  function confirmDelete(item: T) {
    options.dialog.error({
      title: options.title,
      content: options.content(item),
      positiveText: "删除",
      negativeText: "取消",
      positiveButtonProps: { type: "error" },
      onPositiveClick: async () => {
        options.deletingId.value = options.getId(item);
        options.onError("");
        try {
          await options.deleteAction(item);
          options.message.success(options.successText);
        } catch (error) {
          options.onError(error instanceof Error ? error.message : String(error));
        } finally {
          options.deletingId.value = "";
        }
      },
    });
  }

  return {
    confirmDelete,
  };
}
