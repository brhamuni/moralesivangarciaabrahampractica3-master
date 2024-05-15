package es.ujaen.ssccdd.curso2023_24.Utils;

import es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;
import java.util.*;

public class Mensaje {

    private TipoCliente Tipo_Cliente;
    private TipoMensaje Tipo_Mensaje;
    private final String Nombre_Cliente;
    private final int Id_Cliente;
    private Integer Num_Viaje;
    private Integer Num_Estancia;
    private boolean Cancelacion_Viaje;
    private boolean Reserva_Correcta;
    private boolean Pago_Correcto;
    private double Pago;
    private final List<Viaje> Lista_Viajes_Disponibles;
    private final List<Estancia> Lista_Estancias_Disponibles;
    private Date fecha;

    /**
     * @brief Constructor por defecto de la clase Mensaje
     */
    public Mensaje(){
        this.Tipo_Cliente = null;
        this.Tipo_Mensaje = null;
        this.Nombre_Cliente = null;
        this.Id_Cliente = -1;
        this.Num_Viaje = -1;
        this.Num_Estancia = -1;
        this.Cancelacion_Viaje = false;
        this.Lista_Viajes_Disponibles = new ArrayList<>();
        this.Lista_Estancias_Disponibles = new ArrayList<>();
        this.fecha = null;
    }

    /**
     * @brief Constructor parametrizado de la clase Mensaje
     * @param tipo_Cliente
     * @param tipo_Mensaje
     * @param nombre_Cliente
     * @param Id_Cliente
     */
    public Mensaje(TipoCliente tipo_Cliente, TipoMensaje tipo_Mensaje, String nombre_Cliente, int Id_Cliente) {
        this.Tipo_Cliente = tipo_Cliente;
        this.Tipo_Mensaje = tipo_Mensaje;
        this.Nombre_Cliente = nombre_Cliente;
        this.Id_Cliente = Id_Cliente;
        this.Num_Viaje = -1;
        this.Num_Estancia = -1;
        this.Cancelacion_Viaje = false;
        this.Lista_Viajes_Disponibles = new ArrayList<>();
        this.Lista_Estancias_Disponibles = new ArrayList<>();
        this.fecha = new Date();
    }

    /**
     * @brief Metodo toString de la clase Mensaje
     */
    @Override
    public String toString() {

       if( Tipo_Mensaje == TipoMensaje.RESERVA ){
            return DatosReserva();
       }else if( Tipo_Mensaje == TipoMensaje.DISPONIBLE ){
            return DatosDisponibilidad();
       }else if( Tipo_Mensaje == TipoMensaje.CANCELACION ){
            return Cancelacion();
       }else if ( Tipo_Mensaje == TipoMensaje.PAGAR ){
            return Pagar();
       }
        return "";
    }


    private String DatosReserva() {
            return "Mensaje con los datos de la reserva {" +
                    "Nombre=" + Nombre_Cliente + ", ID" + Id_Cliente +
                    ", Num_Viaje=" + Num_Viaje +
                    ", Num_Estancia=" + Num_Estancia + ", Cancelacion_Vaje=" + Cancelacion_Viaje + '}';

    }

    private String DatosDisponibilidad() {
        return "Mensaje con los viajes y estancias disponibles {"+
                "Lista con los viajes=" + Lista_Viajes_Disponibles +
                ", Lista con las estancias=" + Lista_Estancias_Disponibles + '}';
    }

    private String Cancelacion() {
        return "Mensaje con los datos de la cancelacion {"+
                "Nombre=" + Nombre_Cliente + ", ID" + Id_Cliente +
                ", Num_Viaje=" + Num_Viaje +
                ", Num_Estancia=" + Num_Estancia + ", Cancelacion_Vaje=" + Cancelacion_Viaje + '}';
    }

    private String Pagar() {
        return "Mensaje con los datos de el pago de la reserva {"+
                "Nombre=" + Nombre_Cliente + ", ID" + Id_Cliente +
                ", Num_Viaje=" + Num_Viaje +
                ", Num_Estancia=" + Num_Estancia + ", Cancelacion_Vaje=" + Cancelacion_Viaje + '}';
    }

    //endregion

    //region Getters

    public List<Estancia> getLista_Estancias_Disponibles() {
        return Lista_Estancias_Disponibles;
    }

    public List<Viaje> getLista_Viajes_Disponibles() {
        return Lista_Viajes_Disponibles;
    }

    public TipoMensaje getTipo_Mensaje() {
        return Tipo_Mensaje;
    }

    public boolean isReserva_Correcta() {
        return Reserva_Correcta;
    }

    public boolean isPago_Correcto() {
        return Pago_Correcto;
    }

    public String getNombre_Cliente() {
        return Nombre_Cliente;
    }

    public Integer getNum_Viaje() {
        return Num_Viaje;
    }

    public Integer getNum_Estancia() {
        return Num_Estancia;
    }

    public Integer getId_Cliente() {
        return Id_Cliente;
    }

    public boolean isCancelacion_Viaje() {
        return Cancelacion_Viaje;
    }

    public double getPago() {
        return Pago;
    }

    public TipoCliente getTipo_Cliente() {
        return Tipo_Cliente;
    }

    public Date getFecha() {
        return fecha;
    }

    //endregion

    //region Setters

    public void setTipo_Mensaje(TipoMensaje tipo_Mensaje) { this.Tipo_Mensaje = tipo_Mensaje;}

    public void setNum_Viaje(Integer num_Viaje) { this.Num_Viaje = num_Viaje; }

    public void setNum_Estancia(Integer num_Estancia) { this.Num_Estancia = num_Estancia;}

    public void setCancelacion_Viaje(boolean cancelacion_Viaje) { this.Cancelacion_Viaje = cancelacion_Viaje; }

    public void setReserva_Correcta(boolean reserva_Correcta) { this.Reserva_Correcta = reserva_Correcta; }

    public void setPago_Correcto(boolean pago_Correcto) { this.Pago_Correcto = pago_Correcto; }

    public void setPago(double pago) { this.Pago = pago; }

    //endregion
}