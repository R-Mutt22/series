package com.r_mutt.tucine.service;

public interface IConvierteDatos {
    <T> T obtenerDatos(String json, Class<T> clase);
}
