package com.meetingroom.common;

/**
 * 分页参数规范化（B-15）：
 * page 最小 1；size 限定在 1~100 之间，越界自动收敛，行为可预期。
 */
public final class PageUtils {

    public static final int MAX_SIZE = 100;

    private PageUtils() {
    }

    public static int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    public static int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, MAX_SIZE);
    }
}
