package hu.nomindz.devkit.config;

public interface ConfigListener<T> {
    void onReload(T newConfig);
}
