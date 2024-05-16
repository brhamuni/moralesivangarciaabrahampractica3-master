package es.ujaen.ssccdd.curso2023_24.Utils;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;

public class Viaje {

    private final Integer ID;
    private final String Origen_Viaje;
    private final String Destino_Viaje;
    private Integer Plazas_Disponibles;
    private final Double Precio_Viaje;

    /**
     * Constructor parametrizado de la clase Viaje.
     * @param ID Número de identificación único del viaje.
     */
    public Viaje( Integer ID ) {
        this.ID = ID;
        this.Origen_Viaje = "" + LugaresViaje.getNombreViajes();
        this.Destino_Viaje = "" + LugaresViaje.getNombreViajes();
        this.Plazas_Disponibles = Numero_Aleatorio.nextInt( MIN_PLAZAS, MAX_PLAZAS );
        this.Precio_Viaje = Numero_Aleatorio.nextDouble(MIN_PRECIO_VIAJE,MAX_PRECIO_VIAJE);
    }


    public int getID() { return ID; }
    public double getPrecio(){ return  Precio_Viaje; }
    public int getPlazasDisponibles(){ return Plazas_Disponibles; }

    /**Método para decrementar en una unidad las plazas que hay disponibles en el viaje.*/
    public void DecrementarPlazasViaje(){ Plazas_Disponibles = Plazas_Disponibles - 1; }

    @Override
    public String toString() {
        return "Los datos del viaje son {" + "ID= '" + ID + "', Origen_Viaje= '" + Origen_Viaje + "', Destino_Viaje= '" + Destino_Viaje + "', Plazas_Disponibles= '" + Plazas_Disponibles + "', Precio_Viaje= '" + Precio_Viaje + "'}";
    }
}