package es.ujaen.ssccdd.curso2023_24.Procesos;
import static es.ujaen.ssccdd.curso2023_24.Listener.TextMsgListenerClientes.*;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;

import es.ujaen.ssccdd.curso2023_24.Listener.TextMsgListenerClientes;
import es.ujaen.ssccdd.curso2023_24.Listener.TextMsgListenerGestion;
import es.ujaen.ssccdd.curso2023_24.Utils.Estancia;
import es.ujaen.ssccdd.curso2023_24.Utils.GsonUtil;
import es.ujaen.ssccdd.curso2023_24.Utils.Mensaje;
import es.ujaen.ssccdd.curso2023_24.Utils.Viaje;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.*;
import java.util.concurrent.*;

public class ClienteParticular implements Runnable {
    private final int Id;
    private final String Nombre;
    private final List<Mensaje> Mensaje;
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
        this.Mensaje = new ArrayList<>(1);
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
        System.out.println( "Cliente con nombre '" + Nombre + "' e ID '" + Id + "' comienza su EJECUCIÓN ");
        try {
            before();
            execution();
        }catch(Exception e){
            System.out.println( "Cliente con nombre '" + Nombre + "' e ID '" + Id + "' ha sido INTERRUMPIDO " );
        }finally {
            after();
            System.out.println( "Cliente con nombre '" + Nombre + "' e ID '" + Id + "' finaliza su EJECUCIÓN ");
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
        Respuesta_Disponibilidad = session.createQueue(QUEUE + "Respuesta_Disponibilidad.Cliente" + Id );
        Confirmacion_Reserva = session.createQueue(QUEUE + "Confirmacion_Reserva.Cliente" + Id );
        Confirmacion_Pago =  session.createQueue(QUEUE + "Confirmacion_Pago.Cliente" + Id );
        Respuesta_Cancelacion = session.createQueue(QUEUE + "Respuesta_Cancelacion.Cliente" + Id );

        MessageConsumer Consumer_Cliente_Disponibilidad = session.createConsumer(Respuesta_Disponibilidad);
        Consumer_Cliente_Disponibilidad.setMessageListener(new TextMsgListenerClientes("Disponibilidad", Mensaje, Sem_Clientes_Particulares));

        MessageConsumer Consumer_Cliente_Reserva = session.createConsumer(Confirmacion_Reserva);
        Consumer_Cliente_Reserva.setMessageListener(new TextMsgListenerClientes("Reserva", Mensaje,Sem_Clientes_Particulares));

        MessageConsumer Consumer_Cliente_Pago = session.createConsumer(Confirmacion_Pago);
        Consumer_Cliente_Pago.setMessageListener(new TextMsgListenerClientes("Pago", Mensaje, Sem_Clientes_Particulares));

        MessageConsumer Consumer_Cliente_Cancelacion = session.createConsumer(Respuesta_Cancelacion);
        Consumer_Cliente_Cancelacion.setMessageListener(new TextMsgListenerClientes("Cancelacion", Mensaje, Sem_Clientes_Particulares));

        connection.start();
    }

    public void execution() throws Exception {
        ComprobarDisponibilidad();
        Mensaje Respuesta_Servidor = RecibirMensaje( Mensaje );

        RealizarReserva( Respuesta_Servidor );
        Respuesta_Servidor = RecibirMensaje( Mensaje );

        if( !Respuesta_Servidor.isReserva_Correcta() ){
            System.out.println( TEXTO_ROJO + "No habia plazas suficientes y por tanto la peticion de reserva no se ha podido realizar correctamente." + RESET_COLOR );
        }else {
            RealizarPagoReserva( Respuesta_Servidor );
            Respuesta_Servidor = RecibirMensaje( Mensaje );

            if (Respuesta_Servidor.isPago_Correcto() && Numero_Aleatorio.nextInt(100) <= PROBABILIDAD_CANCELACION ) {
                CancelarReserva( Respuesta_Servidor );
                RecibirMensaje( Mensaje );
                System.out.println( TEXTO_ROJO + "CP--> Cliente con Nombre: '" + Nombre + "' e Id: '" + Id + "' finalmente ha cancelado la reserva y no se va de vacaciones. " + RESET_COLOR );
            }else{
                System.out.println( TEXTO_VERDE + "CP--> Cliente con Nombre: '" + Nombre + "' e Id: '" + Id + "' se va de vacaciones. " + RESET_COLOR );
            }
        }
    }

    /**
     * Método que envia un mensaje al servidor preguntando por la disponibilidad de viajes y estancias.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void ComprobarDisponibilidad() throws JMSException{
        Mensaje Datos_Cliente = new Mensaje( TipoMensaje.DISPONIBILIDAD, Nombre, Id );
        System.out.println( TEXTO_AZUL + "CP--> Cliente con Nombre: '" + Nombre + "' e Id: '" + Id + "' envia un mensaje solicitando las disponibilidades. " + RESET_COLOR );
        EnviarMensaje( Datos_Cliente, Preguntar_Disponibilidad );
    }

    /**
     * Método que envia un mensaje al servidor con una reserva de una estancio y\o viaje.
     * @param Respuesta Mensaje que contiene la información sobre las opciones disponibles.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void RealizarReserva( Mensaje Respuesta ) throws JMSException{
        int Num_Viaje = -1, Num_Estancia = -1;
        boolean Elegir_Estancia = Numero_Aleatorio.nextInt(100) <= PROBABILIDAD_ESTANCIA;
        boolean Cancelacion = Numero_Aleatorio.nextInt(100)<=PROBABILIDAD_CANCELACION;

        List<Viaje> Lista_Viajes = Respuesta.getLista_Viajes_Disponibles();
        List<Estancia> Lista_Estancias = Respuesta.getLista_Estancias_Disponibles();

        if( Elegir_Estancia ){
            Viaje Viaje_Elegido = Lista_Viajes.get( Numero_Aleatorio.nextInt(Lista_Viajes.size()));
            Num_Viaje = Viaje_Elegido.getID();
            Estancia Estancia_Elegida = Lista_Estancias.get( Numero_Aleatorio.nextInt(Lista_Estancias.size()));
            Num_Estancia = Estancia_Elegida.getId();
        }else{
            Viaje Viaje_Elegido = Lista_Viajes.get( Numero_Aleatorio.nextInt(Lista_Viajes.size()));
            Num_Viaje = Viaje_Elegido.getID();
        }

        Respuesta.setTipo_Mensaje( TipoMensaje.RESERVA );
        Respuesta.setCancelacion_Viaje( Cancelacion );
        Respuesta.setNum_Viaje( Num_Viaje );
        Respuesta.setNum_Estancia( Num_Estancia );

        System.out.println( TEXTO_AZUL + "CP--> Cliente con Nombre: '" + Nombre + "' e Id: '" + Id + "' envia una peticion de reserva." + RESET_COLOR );
        EnviarMensaje( Respuesta, Realizacion_Reserva );
    }

    /**
     * Método que realiza el pago y envia un mensaje al servidor de verificacion.
     * @param Respuesta Mensaje que contiene la información de la reserva.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void RealizarPagoReserva( Mensaje Respuesta ) throws JMSException{
        double Pago_Total_Cliente = Respuesta.getNum_Viaje() != -1 ? Respuesta.getLista_Viajes_Disponibles().get(Respuesta.getNum_Viaje()).getPrecio() : 0;
        Pago_Total_Cliente = Respuesta.getNum_Estancia() != -1 ? Pago_Total_Cliente + Respuesta.getLista_Estancias_Disponibles().get(Respuesta.getNum_Estancia()).getPrecio() : Pago_Total_Cliente + 0;
        Pago_Total_Cliente = Respuesta.isCancelacion_Viaje() ? Pago_Total_Cliente * PENALIZACION_POR_CANCELACION : Pago_Total_Cliente;

        Respuesta.setPago( Pago_Total_Cliente );
        Respuesta.setTipo_Mensaje( TipoMensaje.PAGAR );
        System.out.println( TEXTO_AZUL + "CP--> Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' envia un mensaje indicando que ha realizado el pago de la reserva. " + RESET_COLOR );
        EnviarMensaje( Respuesta, Realizacion_Pago );
    }

    /**
     * Método para enviar un mensaje al servidor pidiendo la cancelacion de la reserva previamente hecha.
     * @param MensajeCancelacion Mensaje que contiene la información de la reserva.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    public void CancelarReserva( Mensaje MensajeCancelacion ) throws JMSException{
        MensajeCancelacion.setTipo_Mensaje( TipoMensaje.CANCELACION );
        System.out.println( TEXTO_AZUL + "CP--> Cliente con nombre '" + Nombre + "' e ID '(" + Id + ")' envia un mensaje solicitando la cancelacion de la reserva que habia realizado. " + RESET_COLOR );
        EnviarMensaje( MensajeCancelacion, Realizacion_Cancelacion );
    }

    /**
     * Método para codificar y enviar un mensaje al servidor de GestionViajes.
     * @param MensajeCliente Mensaje que va a ser codificado y posteriormente enviado al servidor.
     * @param Buzon Buzon por dónde se va a enviar el mensaje.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    private void EnviarMensaje( Mensaje MensajeCliente, Destination Buzon ) throws JMSException{
        MensajeCliente.setTipo_Cliente( TipoCliente.PARTICULAR );

        GsonUtil<Mensaje> GsonUtil = new GsonUtil<>();
        MessageProducer Producer_Particular = session.createProducer( Buzon );
        Producer_Particular.send( session.createTextMessage(GsonUtil.encode(MensajeCliente,Mensaje.class)) );
        Producer_Particular.close();
    }

    /**
     * Metodo que recibe un mensaje del servidor y lo decodifica.
     * @param Mensaje Lista de mensajes donde se encontrara el mensaje que recibimos del servidor.
     * @return El mensaje que ha sido recibido del servidor decodificado.
     * @throws JMSException Si ocurre algún error durante el proceso de recibir el mensaje.
     */
    private Mensaje RecibirMensaje( List<Mensaje> Mensaje ) throws JMSException, InterruptedException{
        Sem_Clientes_Particulares.get(Id).acquire();
        Mensaje Respuesta_Servidor = Mensaje.remove(0);
        return Respuesta_Servidor;
    }

    /** Método que cierra la conexión con el servidor, liberando todos los recursos asociados. */
    public void after() {
        try {
            if (connection != null) {
                connection.close();
            }
        }catch (JMSException ex){
            System.out.println( "Error al desconectar la conexion" );
        }
    }
}