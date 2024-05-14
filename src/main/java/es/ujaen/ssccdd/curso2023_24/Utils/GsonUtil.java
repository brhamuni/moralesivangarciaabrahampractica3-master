package es.ujaen.ssccdd.curso2023_24.Utils;
import com.google.gson.*;

public class GsonUtil<T> {
    private final Gson gson;

    /**
     * @brief Constructor por defecto de la clase GsonUtil.
     */
    public GsonUtil() {
        this.gson = new GsonBuilder().create();
    }
    
    /**
     * Método que nos permite trasnformar un objeto de tipo T a un String en su formato JSON.
     * @param contenido objeto que se transformará en el contenido del mensaje
     * @param typeParameterClass tipo de la clase T
     * @return String en formato JSON
     */
    public String encode(T contenido, Class<T> typeParameterClass) {
        if( contenido != null ) {
            return gson.toJson(contenido, typeParameterClass);
        }else {
            throw new NullPointerException();
        }
    }
    
    /**
     * Método que nos permite decodificar el contenido de un mensaje de tipo String en formato JSON a un objeto de tipo T
     * @param contenido String que corresponde con el contenido del mensaje
     * @param typeParameterClass Tipo de la clase T que se espera
     * @return Un objeto del tipo T
     */
    public T decode(String contenido, Class<T> typeParameterClass) {
        if( contenido != null ) {
            return gson.fromJson(contenido, typeParameterClass);
        }else {
            throw new NullPointerException();
        }
    }   
}