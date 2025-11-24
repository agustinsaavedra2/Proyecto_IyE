package com.backendie.multitenancy;

public class TenantContext {

    private static final ThreadLocal<Long> currentCategoria = new ThreadLocal<>();

    public static void setCurrentCategoria(Long categoria){
        currentCategoria.set(categoria);
    }

    public static Long getCurrentCategoria(){
        return currentCategoria.get();
    }

    public static void clear(){
        currentCategoria.remove();
    }
}
