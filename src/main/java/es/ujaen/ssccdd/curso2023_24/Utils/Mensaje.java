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
    private double Cantidad_Pagar;
    private final List<Viaje> Lista_Viajes_Disponibles;
    private final List<Estancia> Lista_Estancias_Disponibles;

    /**
     * Constructor parametrizado de la clase Mensaje.
     * @param Tipo_Mensaje Tipo de mensaje que es.
     * @param Nombre Nombre de la persona que envio el mensaje.
     * @param Id Número de identificación unico de la persona que envio el mensaje.
     */
    public Mensaje( TipoMensaje Tipo_Mensaje, String Nombre, int Id) {
        this.Tipo_Cliente = null;
        this.Tipo_Mensaje = Tipo_Mensaje;
        this.Nombre_Cliente = Nombre;
        this.Id_Cliente = Id;
        this.Num_Viaje = -1;
        this.Num_Estancia = -1;
        this.Cancelacion_Viaje = false;
        this.Reserva_Correcta = false;
        this.Pago_Correcto = false;
        this.Cantidad_Pagar = 0;
        this.Lista_Viajes_Disponibles = new ArrayList<>();
        this.Lista_Estancias_Disponibles = new ArrayList<>();
    }


    public List<Estancia> getLista_Estancias_Disponibles() { return Lista_Estancias_Disponibles; }
    public List<Viaje> getLista_Viajes_Disponibles() { return Lista_Viajes_Disponibles; }
    public boolean isReserva_Correcta() { return Reserva_Correcta; }
    public boolean isPago_Correcto() { return Pago_Correcto; }
    public String getNombre_Cliente() { return Nombre_Cliente; }
    public Integer getNum_Viaje() { return Num_Viaje; }
    public Integer getNum_Estancia() { return Num_Estancia; }
    public Integer getId_Cliente() { return Id_Cliente; }
    public boolean isCancelacion_Viaje() { return Cancelacion_Viaje; }
    public TipoCliente getTipo_Cliente() { return Tipo_Cliente; }

    public void setTipo_Cliente( TipoCliente Tipo_Cliente ) { this.Tipo_Cliente = Tipo_Cliente; }
    public void setTipo_Mensaje( TipoMensaje Tipo_Mensaje ) { this.Tipo_Mensaje = Tipo_Mensaje;}
    public void setNum_Viaje( int Num_Viaje ) { this.Num_Viaje = Num_Viaje; }
    public void setNum_Estancia( int Num_Estancia ) { this.Num_Estancia = Num_Estancia;}
    public void setCancelacion_Viaje( boolean Cancelacion_Viaje ) { this.Cancelacion_Viaje = Cancelacion_Viaje; }
    public void setReserva_Correcta( boolean Reserva_Correcta ) { this.Reserva_Correcta = Reserva_Correcta; }
    public void setPago_Correcto( boolean Pago_Correcto ) { this.Pago_Correcto = Pago_Correcto; }
    public void setPago( double Pago_Total ) { this.Cantidad_Pagar = Pago_Total; }

    @Override
    public String toString() {
        if( Tipo_Mensaje == TipoMensaje.RESERVA ){
            return DatosReserva();
        }else if( Tipo_Mensaje == TipoMensaje.DISPONIBILIDAD){
            return DatosDisponibilidad();
        }else if( Tipo_Mensaje == TipoMensaje.CANCELACION ){
            return Cancelacion();
        }
        return Pagar();
    }

    private String DatosReserva() {
        if( Tipo_Cliente == TipoCliente.SERVIDOR ){
            return "Estimado " + Nombre_Cliente + " con Id '" + Id_Cliente + "' le comunicamos que la reserva se ha realizado correctamente. Realize el pago de la reserva en un maximo de 5 dias";
        }else{
            return "Datos de la peticion de reserva del cliente [" + "Nombre: " + Nombre_Cliente + ", Id: '" + Id_Cliente + "', Viaje: '" + Num_Viaje + "', Estancia: '" + Num_Estancia + "', Cancelacion_Vaje: " + Cancelacion_Viaje + ']';
        }
    }

    private String DatosDisponibilidad() {
        if( Tipo_Cliente == TipoCliente.SERVIDOR ) {
            return "Estimado " + Nombre_Cliente + " '" + Id_Cliente +"' los viajes y estancias que hay disponibles son [" + "Lista viajes: " + Lista_Viajes_Disponibles + ", Lista estancias: " + Lista_Estancias_Disponibles + ']';
        }else{
            return "Datos del cliente que solicita los viajes y estancias disponibles [" + "Nombre: " + Nombre_Cliente + ", Id: '" + Id_Cliente + "']";
        }
    }

    private String Cancelacion() {
        if( Tipo_Cliente == TipoCliente.SERVIDOR ){
            return "La cancelacion de la reserva con Nombre " + Nombre_Cliente + " e Id '" + Id_Cliente + "' se ha realizado correctamente. Recibira el dinero de la reserva en un plazo de 5 dias";
        }else{
            return "Datos del cliente que solicita la cancelacion de la reserva [" + "Nombre: " + Nombre_Cliente + ", Id: '" + Id_Cliente + "', Num_Viaje: " + Num_Viaje + ", Num_Estancia: " + Num_Estancia + ", Cancelacion_Vaje: " + Cancelacion_Viaje + ", Cantidad Pagada: " + Cantidad_Pagar + ']';
        }
    }

    private String Pagar() {
        if( Tipo_Cliente == TipoCliente.SERVIDOR ){
            return "!!ENHORABUENA!! El pago de la reserva del cliente con Nombre " + Nombre_Cliente + " e Id '" + Id_Cliente + "' se ha realizado correctamente";
        }else{
            return "Datos del cliente que ha realizado el pago de la reserva [ Nombre: " + Nombre_Cliente + ", Id: '" + Id_Cliente + "', Num_Viaje: " + Num_Viaje + ", Num_Estancia: " + Num_Estancia + ", Cancelacion_Vaje: " + Cancelacion_Viaje + "Cantidad a Pagar: " + Cantidad_Pagar + ']';
        }
    }
}