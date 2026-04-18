package io.github.kuonjiarisu.backend.support;

public final class PageSupport {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE = 1_000_000;
    public static final int MAX_PAGE_SIZE = 100;

    private PageSupport() {
    }

    public static int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return DEFAULT_PAGE;
        }
        return Math.min(page, MAX_PAGE);
    }

    public static int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public static int offset(int page, int pageSize) {
        return (page - 1) * pageSize;
    }

    public static String normalizeText(String value) {
        if (value == null || value.isBlank() || "ALL".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }
}
