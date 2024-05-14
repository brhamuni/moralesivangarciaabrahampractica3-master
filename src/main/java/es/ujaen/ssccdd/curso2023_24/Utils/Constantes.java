package es.ujaen.ssccdd.curso2023_24.Utils;
import java.util.Random;

public interface Constantes {
    Random Numero_Aleatorio = new Random();

    enum NombreAgencias {
        Viajes_Carrefour, Corte_Ingles, Halcon_Viajes, Travel_Perk;
        public static NombreAgencias getNombre(){
            return Estados_Disponibles_Agencias[Numero_Aleatorio.nextInt(Estados_Disponibles_Agencias.length)];
        }
    }

    enum LugaresViaje{
        Barcelona, Madrid, Berlin, Maldivas, Paris, Tokio, Manchester, NewYork, Dubai, Roma, Londres, Venecia;
        public static LugaresViaje getNombreViajes(){
            return Estados_Nombre_Lugares[Numero_Aleatorio.nextInt(Estados_Nombre_Lugares.length)];
        }
    }

    enum NombreUsuarios{
        IVAN, ABRAHAM, RODRIGO, MARIA, LUCIA, LEWANDOSKI, FERMIN, CARMEN;
        public static NombreUsuarios getNombre(){
            return Estados_Disponibles_Usuarios[Numero_Aleatorio.nextInt(Estados_Disponibles_Usuarios.length)];
        }
    }

    enum NombreEstancia{
        Hotel_Relax_Suites, Soft_Arc_Hotel_Spa, Sunset_Oyster_Hotel, Silver_Cosmos_Resort;
        public static NombreEstancia getNombreEstancia(){
            return Estados_Disponibles_Estancia[Numero_Aleatorio.nextInt(Estados_Disponibles_Estancia.length)];
        }
    }

    enum TipoMensaje{
        CLIENTE , RESERVA , DISPONIBLE , CANCELACION , PAGAR
    }

    enum TipoCliente{
        PARTICULAR, AGENCIA
    }

    NombreAgencias[] Estados_Disponibles_Agencias = NombreAgencias.values();
    LugaresViaje[]  Estados_Nombre_Lugares = LugaresViaje.values();
    NombreUsuarios[] Estados_Disponibles_Usuarios = NombreUsuarios.values();
    NombreEstancia[] Estados_Disponibles_Estancia = NombreEstancia.values();

    int MAX_CLIENTES = 4;
    int MIN_CLIENTES = 3;
    int MAX_PLAZAS = 5;
    int MIN_PLAZAS = 2;
    int MAX_VIAJES = 5;
    int MIN_VIAJES = 2;
    int MAX_ESTANCIAS = 5;
    int MIN_ESTANCIAS = 2;
    int NUM_TIPOS_CLIENTES = 2;
    int NUM_GESTION_VIAJES = 1;
    double MIN_PRECIO_ESTANCIA = 100.0;
    double MAX_PRECIO_ESTANCIA = 900.0;
    double MIN_PRECIO_VIAJE = 500.0;
    double MAX_PRECIO_VIAJE = 3000.0;
    int PROBABILIDAD_CANCELACION = 50;
    int PROBABILIDAD_VIAJE = 85;
    int PROBABILIDAD_ESTANCIA = 85;
    double PENALIZACION_POR_CANCELACION = 1.2;

    int TIEMPO_EJECUCION = 60;
    int TIEMPO_ESPERA_MENSAJE = 1;
    String QUEUE = "ssccdd.curso2024.MoralesIvanGarciaAbraham.";

    //public static final String BROKER_URL = "tcp://suleiman.ujaen.es:8018";
    String BROKER_URL = "tcp://localhost:61616";
}