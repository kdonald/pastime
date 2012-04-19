package com.pastime.util;

public final class SecurityContext {

    private static final ThreadLocal<Principal> principal = new ThreadLocal<Principal>();

    public static Principal getPrincipal() {
        return principal.get();
    }

    public static void setPrincipal(Principal player) {
        principal.set(player);
    }

    public static boolean authorized() {
        return principal.get() != null;
    }

    public static void remove() {
        principal.remove();
    }

}
