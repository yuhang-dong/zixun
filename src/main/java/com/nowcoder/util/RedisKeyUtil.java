package com.nowcoder.util;

public class RedisKeyUtil {
    private static String SPLIT = ":";
    private static String BIZ_LIKE = "LIKE";
    private static String BIZ_DISLIKE = "DISLIKE";
    private static String BIZ_EVENTQUEUE = "BIZQUEUE";


    public static String getEventQueueKey() {
        return BIZ_EVENTQUEUE;
    }

    public static String getLikeKey(int entityId,int entityType) {
        return BIZ_LIKE+SPLIT+String.format("%d%s%d",entityType,SPLIT,entityId);
    }


    public static String getDisLikeKey(int entityId,int entityType) {
        return BIZ_DISLIKE+SPLIT+String.format("%d%s%d",entityType,SPLIT,entityId);
    }
}
