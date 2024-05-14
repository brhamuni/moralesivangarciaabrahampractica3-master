package es.ujaen.ssccdd.curso2023_24.Utils;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;

public class Viaje {

    private final Integer ID;
    private final String Origen_Viaje;
    private final String Destino_Viaje;
    private Integer Plazas_Disponibles;
    private final Double Precio_Viaje;

    /**
     * @brief Constructor parametrizado de la clase Viaje.
     * @param ID Número de identificación único del viaje.
     */
    public Viaje( Integer ID ) {
        this.ID = ID;
        this.Origen_Viaje = "" + LugaresViaje.getNombreViajes();
        this.Destino_Viaje = "" + LugaresViaje.getNombreViajes();
        this.Plazas_Disponibles = Numero_Aleatorio.nextInt( MIN_PLAZAS, MAX_PLAZAS );
        this.Precio_Viaje = Numero_Aleatorio.nextDouble(MIN_PRECIO_VIAJE,MAX_PRECIO_VIAJE);
    }


    /**
     * @brief Método para obtener el número de identificación del viaje.
     * @return El número de identificación del viaje.
     */
    public int getID() { return ID; }

    /**
     * @brief Métdodo para obtener el precio del viaje.
     * @return El precio que cuesta el viaje.
     */
    public double getPrecio(){ return  Precio_Viaje; }

    /**
     * @breief Método para obtener el número de plazas que quedan disponibles en el viaje.
     * @return El número de plazas que hay disponibles en el viaje.
     */
    public int getPlazasDisponibles(){ return Plazas_Disponibles; }

    /**
     * @breief Método para decrementar en una unidad las plazas que hay disponibles en el viaje.
     */
    public void decrementarPlazasViaje(){ Plazas_Disponibles = Plazas_Disponibles - 1; }

    @Override
    public String toString() {
        return "Los datos del viaje son {" + "ID= '" + ID + "', Origen_Viaje= '" + Origen_Viaje + "', Destino_Viaje= '" + Destino_Viaje + "', Plazas_Disponibles= '" + Plazas_Disponibles + "', Precio_Viaje= '" + Precio_Viaje + "'}";
    }
}