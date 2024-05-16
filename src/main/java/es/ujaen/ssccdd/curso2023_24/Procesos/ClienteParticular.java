package es.ujaen.ssccdd.curso2023_24.Procesos;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;
import es.ujaen.ssccdd.curso2023_24.Utils.Estancia;
import es.ujaen.ssccdd.curso2023_24.Utils.GsonUtil;
import es.ujaen.ssccdd.curso2023_24.Utils.Mensaje;
import es.ujaen.ssccdd.curso2023_24.Utils.Viaje;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class ClienteParticular implements Runnable {
    private final int Id;
    private final String Nombre;
    private final List<Semaphore> Sem_Clientes_Particulares;

    private Connection connection;
    private Session session;

    private Destination Realizacion_Pago;
    private Destination Confirmacion_Pago;
    private Destination Realizacion_Cancelacion;
    private Destination Respuesta_Cancelacion;
    private Destination Realizacion_Reserva;
    private Destination Confirmacion_Reserva;
    private Destination Preguntar_Disponibilidad;
    private Destination Respuesta_Disponibilidad;

    /**
     * Constructor parametrizado de la clase ClienteParticular.
     * @param Id Número de identificación unico del cliente.
     */
    public ClienteParticular( int Id, List<Semaphore> Sem_Clientes_Particulares ) {
        this.Id = Id;
        this.Nombre = "" + NombreUsuarios.getNombre();
        this.Sem_Clientes_Particulares = Sem_Clientes_Particulares;

        Realizacion_Pago = null;
        Realizacion_Cancelacion = null;
        Realizacion_Reserva = null;
        Preguntar_Disponibilidad = null;
        Confirmacion_Pago = null;
        Respuesta_Cancelacion = null;
        Confirmacion_Reserva = null;
        Respuesta_Disponibilidad = null;
    }


    @Override
    public void run() {
        System.out.println( "Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' comienza su EJECUCIÓN ");
        try {
            before();
            execution();
        }catch(Exception e){
            System.out.println( "Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' ha sido INTERRUMPIDO " );
        }finally {
            after();
            System.out.println( "Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' finaliza su EJECUCIÓN ");
        }
    }

    /**
     * Método para preparar las conexiones y sesiones antes de ejecutar las operaciones del cliente.
     * @throws Exception  Si ocurre algún error durante la configuración de las conexiones y sesiones.
     */
    public void before() throws Exception{
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory( BROKER_URL );
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE );

        Preguntar_Disponibilidad = session.createQueue(QUEUE + "Preguntar_Disponibilidad.Cliente" );
        Realizacion_Reserva = session.createQueue(QUEUE + "Realizacion_Reserva.Cliente" );
        Realizacion_Pago = session.createQueue(QUEUE + "Realizacion_Pago.Cliente" );
        Realizacion_Cancelacion = session.createQueue(QUEUE + "Realizacion_Cancelacion.Cliente" );
        Respuesta_Disponibilidad = session.createQueue(QUEUE + "Respuesta_Disponibilidad.Cliente"+Id );
        Confirmacion_Reserva = session.createQueue(QUEUE + "Confirmacion_Reserva.Cliente"+Id );
        Confirmacion_Pago =  session.createQueue(QUEUE + "Confirmacion_Pago.Cliente"+Id );
        Respuesta_Cancelacion = session.createQueue(QUEUE + "Respuesta_Cancelacion.Cliente"+Id );

        connection.start();
    }

    public void execution() throws Exception {

        ComprobarDisponibilidad();
        Mensaje Respuesta_Servidor = RecibirMensaje(Respuesta_Disponibilidad);

        RealizarReserva(Respuesta_Servidor);
        Respuesta_Servidor = RecibirMensaje(Confirmacion_Reserva);

        if( !Respuesta_Servidor.isReserva_Correcta() ){
            System.out.println( ANSI_RED + "No habia plazas suficientes y por tanto la peticion de reserva no se ha podido realizar correctamente." + ANSI_RESET );
        }else {
            RealizarPagoReserva(Respuesta_Servidor);
            Respuesta_Servidor = RecibirMensaje(Confirmacion_Pago);

            if (Respuesta_Servidor.isPago_Correcto() && Numero_Aleatorio.nextInt(100) <= PROBABILIDAD_CANCELACION ) {
                CancelarReserva(Respuesta_Servidor);
                RecibirMensaje(Respuesta_Cancelacion);
                System.out.println( ANSI_RED + "CP--> Cliente con Nombre: '" + Nombre + "' e Id: '" + Id + "' finalmente ha cancelado la reserva y no se va de vacaciones. " + ANSI_RESET );
            }else{
                System.out.println( ANSI_GREEN + "CP--> Cliente con Nombre: '" + Nombre + "' e Id: '" + Id + "' se va de vacaciones. " + ANSI_RESET );
            }
        }
    }

    /**
     * Método que envia un mensaje al servidor preguntando por la disponibilidad de viajes y estancias.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void ComprobarDisponibilidad() throws JMSException{
        Mensaje Datos_Cliente = new Mensaje(TipoMensaje.DISPONIBILIDAD, Nombre, Id);
        EnviarMensaje(Datos_Cliente,Preguntar_Disponibilidad);
        System.out.println( ANSI_CYAN + "CP--> Cliente con Nombre: '" + Nombre + "' e Id: '" + Id + "' envia un mensaje solicitando las disponibilidades. " + ANSI_RESET );
    }

    /**
     * Método que envia un mensaje al servidor con una reserva de una estancio y\o viaje.
     * @param Respuesta Mensaje que contiene la información sobre las opciones disponibles.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void RealizarReserva( Mensaje Respuesta ) throws JMSException{

        int Num_Viaje = -1, Num_Estancia = -1;
        boolean Elegir_Estancia = Numero_Aleatorio.nextInt(100) <= PROBABILIDAD_ESTANCIA;
        boolean Elegir_Viaje = Numero_Aleatorio.nextInt(100)<=PROBABILIDAD_VIAJE;
        boolean Cancelacion = Numero_Aleatorio.nextInt(100)<=PROBABILIDAD_CANCELACION;

        List<Viaje> Lista_Viajes_Disponibles = Respuesta.getLista_Viajes_Disponibles();
        List<Estancia> Lista_Estancias_Disponibles = Respuesta.getLista_Estancias_Disponibles();

        if( Elegir_Viaje && Elegir_Estancia ){
            Viaje Viaje_Elegido = Lista_Viajes_Disponibles.get( Numero_Aleatorio.nextInt(Lista_Viajes_Disponibles.size()));
            Num_Viaje = Viaje_Elegido.getID();
            Estancia Estancia_Elegida = Lista_Estancias_Disponibles.get( Numero_Aleatorio.nextInt(Lista_Estancias_Disponibles.size()));
            Num_Estancia = Estancia_Elegida.getId();
        }
        if(Elegir_Viaje && !Elegir_Estancia){
            Viaje Viaje_Elegido = Lista_Viajes_Disponibles.get( Numero_Aleatorio.nextInt(Lista_Viajes_Disponibles.size()));
            Num_Viaje = Viaje_Elegido.getID();
        }
        if(!Elegir_Viaje && Elegir_Estancia){
            Estancia Estancia_Elegida = Lista_Estancias_Disponibles.get( Numero_Aleatorio.nextInt(Lista_Estancias_Disponibles.size()));
            Num_Estancia = Estancia_Elegida.getId();
        }

        Respuesta.setTipo_Mensaje(TipoMensaje.RESERVA);
        Respuesta.setCancelacion_Viaje(Cancelacion);
        Respuesta.setNum_Viaje(Num_Viaje);
        Respuesta.setNum_Estancia(Num_Estancia);

        EnviarMensaje(Respuesta, Realizacion_Reserva);
        System.out.println( ANSI_CYAN + "CP--> Cliente con Nombre: '" + Nombre + "' e Id: '" + Id + "' envia una peticion de reserva." + ANSI_RESET );
    }

    /**
     * Método que realiza el pago y envia un mensaje al servidor de verificacion.
     * @param Respuesta Mensaje que contiene la información de la reserva.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void RealizarPagoReserva(Mensaje Respuesta) throws JMSException{
        double Pago_Total_Cliente = Respuesta.getNum_Viaje() != -1 ? Respuesta.getLista_Viajes_Disponibles().get(Respuesta.getNum_Viaje()).getPrecio() : 0;
        Pago_Total_Cliente = Respuesta.getNum_Estancia() != -1 ? Pago_Total_Cliente + Respuesta.getLista_Estancias_Disponibles().get(Respuesta.getNum_Estancia()).getPrecio() : Pago_Total_Cliente + 0;
        Pago_Total_Cliente = Respuesta.isCancelacion_Viaje() ? Pago_Total_Cliente * PENALIZACION_POR_CANCELACION : Pago_Total_Cliente;

        Respuesta.setTipo_Mensaje(TipoMensaje.PAGAR);
        Respuesta.setPago(Pago_Total_Cliente);
        EnviarMensaje(Respuesta, Realizacion_Pago);
        System.out.println( ANSI_CYAN + "CP--> Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' envia un mensaje indicando que ha realizado el pago de la reserva. " + ANSI_RESET );

    }


    /**
     * Método para enviar un mensaje al servidor pidiendo la cancelacion de la reserva previamente hecha.
     * @param MensajeCancelacion Mensaje que contiene la información de la reserva.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void CancelarReserva(Mensaje MensajeCancelacion) throws JMSException{

        MensajeCancelacion.setTipo_Mensaje(TipoMensaje.CANCELACION);
        EnviarMensaje(MensajeCancelacion,Realizacion_Cancelacion);
        System.out.println( ANSI_CYAN + "CP--> Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' envia un mensaje solicitando la cancelacion de la reserva que habia realizado. " + ANSI_RESET );

    }

    /**
     * Método para codificar y enviar un mensaje al servidor de GestionViajes.
     * @param MensajeCliente Mensaje que va a ser codificado y posteriormente enviado al servidor.
     * @param Buzon Buzon por dónde se va a enviar el mensaje.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    private void EnviarMensaje( Mensaje MensajeCliente, Destination Buzon ) throws JMSException{
        MensajeCliente.setTipo_Cliente(TipoCliente.PARTICULAR);

        GsonUtil<Mensaje> GsonUtil = new GsonUtil<>();
        MessageProducer Producer_Particular = session.createProducer( Buzon );
        TextMessage Mensaje_Codificado = session.createTextMessage(GsonUtil.encode(MensajeCliente,Mensaje.class));
        Producer_Particular.send( Mensaje_Codificado );
        Producer_Particular.close();
    }

    /**
     * Metodo que recibe un mensaje del servidor y lo decodifica.
     * @param Buzon Buzon por donde se va a recibir el mensaje.
     * @return El mensaje que ha sido recibido del servidor decodificado.
     * @throws JMSException Si ocurre algún error durante el proceso de recibir el mensaje.
     */
    private Mensaje RecibirMensaje( Destination Buzon ) throws JMSException,InterruptedException{
        Sem_Clientes_Particulares.get(Id).acquire();

        GsonUtil<Mensaje> gsonUtil = new GsonUtil<>();
        MessageConsumer Consumer_Particular = session.createConsumer( Buzon );
        TextMessage Mensaje_Codificado = (TextMessage) Consumer_Particular.receive();
        Mensaje Respuesta_Servidor = null;
        
        try {
            Respuesta_Servidor = gsonUtil.decode( Mensaje_Codificado.getText(), Mensaje.class );
        } catch (JMSException ex) {
            Logger.getLogger( ClienteParticular.class.getName()).log(Level.SEVERE, null, ex );
        }
        Consumer_Particular.close();
        System.out.println( ANSI_PURPLE + "CLIENTE PARTICULAR----> Se ha recibido un mensaje: " + Respuesta_Servidor.toString() + ". " + ANSI_RESET );
        return Respuesta_Servidor;
    }

    /** Método que cierra la conexión con el servidor, liberando todos los recursos asociados. */
    public void after() {
        try {
            if (connection != null) {
                connection.close();
            }
        }catch (JMSException ex){
            System.out.println("Error al desconectar la conexion");
        }
    }
}