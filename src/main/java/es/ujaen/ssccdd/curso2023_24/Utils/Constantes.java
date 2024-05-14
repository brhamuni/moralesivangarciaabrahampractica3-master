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
        Ivan, Abraham, Toni, Maria, Lucia, Manuel, Nico, Carmen;
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
        CLIENTE , RESERVA , DISPONIBLE , CANCELACION , PAGAR;
        public static TipoMensaje getTipoMensaje(){
            return Estados_Disponibles_Mensajes[Numero_Aleatorio.nextInt(Estados_Disponibles_Mensajes.length)];
        }
    }

    enum TipoCliente{
        PARTICULAR, AGENCIA;
    }

    NombreAgencias[] Estados_Disponibles_Agencias = NombreAgencias.values();
    LugaresViaje[]  Estados_Nombre_Lugares = LugaresViaje.values();
    NombreUsuarios[] Estados_Disponibles_Usuarios = NombreUsuarios.values();
    NombreEstancia[] Estados_Disponibles_Estancia = NombreEstancia.values();
    TipoMensaje[] Estados_Disponibles_Mensajes = TipoMensaje.values();


    public static final int MAX_PLAZAS = 5;
    public static final int MIN_PLAZAS = 2;
    public static final int MAX_VIAJES = 5;
    public static final int MIN_VIAJES = 2;
    public static final int MAX_ESTANCIAS = 7;
    public static final int MIN_ESTANCIAS = 2;
    public static final int NUM_TIPOS_CLIENTES = 2;
    public static final int NUM_GESTION_VIAJES = 1;
    public static final double MIN_PRECIO_ESTANCIA = 100.0;
    public static final double MAX_PRECIO_ESTANCIA = 900.0;
    public static final double MIN_PRECIO_VIAJE = 500.0;
    public static final double MAX_PRECIO_VIAJE = 3000.0;
    public static final int PROBABILIDAD_CANCELACION = 50;
    public static final int PROBABILIDAD_VIAJE = 85;
    public static final int PROBABILIDAD_ESTANCIA = 85;
    public static double PENALIZACION_POR_CANCELACION = 1.2;

    //Para el hilo principal
    public static final int MAX_CLIENTES = 4;
    public static final int MIN_CLIENTES = 3;
    public static final int TIEMPO_EJECUCION = 50;
    public static final int TIEMPO_ESPERA_MENSAJE = 1;
    public static final int TIEMPO_MAXIMO_ESPERADO = 3;
    public static final String QUEUE = "uja.ssccdd.connection.agh00040ims00051.";

    //public static final String BROKER_URL = "tcp://suleiman.ujaen.es:8018";
    public static final String BROKER_URL = "tcp://localhost:61616";
}