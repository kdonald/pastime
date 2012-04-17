package com.pastime.util;

public final class SecurityContext {

    private static final ThreadLocal<UserPrincipal> currentPlayer = new ThreadLocal<UserPrincipal>();

    public static UserPrincipal getPrincipal() {
        return currentPlayer.get();
    }

    public static void setCurrentPlayer(UserPrincipal player) {
        currentPlayer.set(player);
    }

    public static boolean authorized() {
        return currentPlayer.get() != null;
    }

    public static void remove() {
        currentPlayer.remove();
    }

}
