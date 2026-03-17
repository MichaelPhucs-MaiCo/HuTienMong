package com.vanphuc.utils;

import java.util.ArrayList;
import java.util.List;

public class FriendManager {
    private static final List<String> friends = new ArrayList<>();

    public static List<String> getFriends() {
        return friends;
    }

    public static void setFriends(List<String> newFriends) {
        friends.clear();
        friends.addAll(newFriends);
    }

    public static boolean isFriend(String name) {
        // Ignore case cho chắc cú
        return friends.stream().anyMatch(f -> f.equalsIgnoreCase(name));
    }
}