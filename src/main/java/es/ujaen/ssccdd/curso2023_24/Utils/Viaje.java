package es.ujaen.ssccdd.curso2023_24.Utils;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;

public class Viaje {

    private final Integer ID;
    private final String Tipo_Viaje;
    private final String Origen_Viaje;
    private final String Destino_Viaje;
    private Integer Plazas_Disponibles;
    private final Double Precio_Viaje;

    public Viaje( Integer ID ) {
        this.ID = ID;
        this.Tipo_Viaje = "" + TipoTurismo.getTipoTurismo();
        this.Origen_Viaje = "" + LugaresViaje.getNombreViajes();
        this.Destino_Viaje = "" + LugaresViaje.getNombreViajes();
        this.Plazas_Disponibles = Numero_Aleatorio.nextInt( MIN_PLAZAS, MAX_PLAZAS );
        this.Precio_Viaje = Numero_Aleatorio.nextDouble(MIN_PRECIO_VIAJE,MAX_PRECIO_VIAJE);
    }

    public Integer getID() { return ID; }
    public Double getPrecio(){ return  Precio_Viaje; }
    public Integer getPlazasDisponibles(){ return Plazas_Disponibles; }
    public void decrementarPlazasViaje(){ Plazas_Disponibles = Plazas_Disponibles - 1; }

    @Override
    public String toString() {
        return "Los datos del Viaje son {" + "ID='" + ID + "', Tipo_Viaje='" + Tipo_Viaje + "', Origen_Viaje='" + Origen_Viaje + "', Destino_Viaje='" + Destino_Viaje + "', Plazas_Disponibles='" + Plazas_Disponibles + "', Precio_Viaje='" + Precio_Viaje + "'}";
    }
}