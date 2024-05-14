package es.ujaen.ssccdd.curso2023_24.Utils;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;

public class Estancia {
    private final Integer Id;
    private final String Nombre;
    private Integer Plazas_Disponibles;
    private final double Precio;

    /**
     * @brief Constructor parametrizado de la clase Estancia.
     * @param Id Número de identificación único de la estancia.
     */
    public Estancia(Integer Id) {
        this.Id = Id;
        this.Nombre = ""+NombreEstancia.getNombreEstancia() ;
        this.Plazas_Disponibles = Numero_Aleatorio.nextInt(MIN_PLAZAS,MAX_PLAZAS);
        this.Precio = Numero_Aleatorio.nextDouble(MIN_PRECIO_ESTANCIA,MAX_PRECIO_ESTANCIA);
    }


    /**
     * @brief Método para obtener el número de identificación de la estancia.
     * @return El número de identificación de la estancia.
     */
    public int getId() {return Id;}

    /**
     * @brief Métdodo para obtener el precio de la estancia.
     * @return El precio que cuesta la estancia.
     */
    public double getPrecio() { return Precio; }

    /**
     * @breief Método para obtener el número de plazas que quedan disponibles en la estancia.
     * @return El número de plazas que hay disponibles en la estancia.
     */
    public int getPlazas_Disponibles() { return Plazas_Disponibles; }

    /**
     * @breief Método para decrementar en una unidad las plazas que hay disponibles para la estancia.
     */
    public void decrementarPlazasEstancia() { Plazas_Disponibles = Plazas_Disponibles - 1; }

    @Override
    public String toString() {
        return "Los datos de la estancia son {" + "Id= '" + Id + "', Nombre= '" + Nombre + "', Plazas_Disponibles= '" + Plazas_Disponibles + "', Precio= '" + Precio + "'}";
    }
}
