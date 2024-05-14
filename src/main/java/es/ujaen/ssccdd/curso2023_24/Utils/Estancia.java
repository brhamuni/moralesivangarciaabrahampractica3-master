package es.ujaen.ssccdd.curso2023_24.Utils;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;

public class Estancia {
    private final Integer Id;
    private final String Nombre;
    private Integer Plazas_Disponibles;
    private final double Precio;

    public Estancia(Integer Id) {
        this.Id = Id;
        this.Nombre = ""+NombreEstancia.getNombreEstancia() ;
        this.Plazas_Disponibles = Numero_Aleatorio.nextInt(MIN_PLAZAS,MAX_PLAZAS);
        this.Precio = Numero_Aleatorio.nextDouble(MIN_PRECIO_ESTANCIA,MAX_PRECIO_ESTANCIA);
    }

    public int getId() {
        return Id;
    }

    public String getNombre() {
        return Nombre;
    }

    public double getPrecio() {
        return Precio;
    }

    public int getPlazas_Disponibles() {
        return Plazas_Disponibles;
    }

    public void decrementarPlazasEstancia() { Plazas_Disponibles = Plazas_Disponibles -1 ;}

    @Override
    public String toString() {
        return "Estancia{" +
                "Id=" + Id +
                ", Nombre='" + Nombre + '\'' +
                ", Plazas_Disponibles=" + Plazas_Disponibles +
                ", Precio=" + Precio +
                '}';
    }
}
