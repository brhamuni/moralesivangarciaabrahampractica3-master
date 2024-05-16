package es.ujaen.ssccdd.curso2023_24.Utils;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;

public class Estancia {

    private final Integer Id;
    private final String Nombre;
    private Integer Plazas_Disponibles;
    private final double Precio;

    /**
     * Constructor parametrizado de la clase Estancia.
     * @param Id Número de identificación único de la estancia.
     */
    public Estancia( Integer Id ) {
        this.Id = Id;
        this.Nombre = ""+NombreEstancia.getNombreEstancia() ;
        this.Plazas_Disponibles = Numero_Aleatorio.nextInt(MIN_PLAZAS,MAX_PLAZAS);
        this.Precio = Numero_Aleatorio.nextDouble(MIN_PRECIO_ESTANCIA,MAX_PRECIO_ESTANCIA);
    }


    public int getId() { return Id; }
    public double getPrecio() { return Precio; }
    public int getPlazas_Disponibles() { return Plazas_Disponibles; }

    /** Método para decrementar en una unidad las plazas que hay disponibles para la estancia. */
    public void DecrementarPlazasEstancia() { Plazas_Disponibles = Plazas_Disponibles - 1; }

    @Override
    public String toString() {
        return "Los datos de la estancia son {" + "Id= '" + Id + "', Nombre= '" + Nombre + "', Plazas_Disponibles= '" + Plazas_Disponibles + "', Precio= '" + Precio + "'}";
    }
}