package es.ujaen.ssccdd.curso2023_24.Procesos;
import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;
import es.ujaen.ssccdd.curso2023_24.Listener.TextMsgListenerGestion;
import es.ujaen.ssccdd.curso2023_24.Utils.Estancia;
import es.ujaen.ssccdd.curso2023_24.Utils.GsonUtil;
import es.ujaen.ssccdd.curso2023_24.Utils.Mensaje;
import es.ujaen.ssccdd.curso2023_24.Utils.Viaje;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.*;
import java.util.concurrent.*;

public class GestionViaje implements Runnable {
    private final Integer Num_Clientes;
    private final List<Viaje> Lista_Viajes;
    private final List<Estancia> Lista_Estancias;
    private final List<Mensaje> Lista_Agencias_Viaje;
    private final List<Mensaje> Lista_Cliente_Particular;
    private final List<Mensaje> Lista_Peticiones_Reserva;
    private final List<Mensaje> Lista_Peticiones_Pago;
    private final List<Mensaje> Lista_Peticiones_Cancelacion;
    private int Contador_Agencia;

    private Connection connection;
    private Session session;

    private final List<Destination> Preguntar_Disponibilidad;
    private final Destination[][] Respuesta_Disponibilidad;
    private final List<Destination> Realizacion_Pago;
    private final Destination[][] Confirmacion_Pago;
    private final List<Destination> Realizacion_Cancelacion;
    private final Destination[][] Respuesta_Cancelacion;
    private final List <Destination> Realizacion_Reserva;
    private final Destination[][] Confirmacion_Reserva;

    /**
     * Constructor parametrizado de la clase GestionViaje.
     * @param Num_Clientes Número de clientes.
     */
    public GestionViaje( int Num_Clientes ) {
        this.Num_Clientes = Num_Clientes;
        this.Lista_Viajes = new ArrayList<>();
        this.Lista_Estancias = new ArrayList<>();
        this.Lista_Agencias_Viaje = new ArrayList<>();
        this.Lista_Cliente_Particular = new ArrayList<>();
        this.Lista_Peticiones_Reserva = new ArrayList<>();
        this.Lista_Peticiones_Pago = new ArrayList<>();
        this.Lista_Peticiones_Cancelacion = new ArrayList<>();
        this.Contador_Agencia = 0;

        Preguntar_Disponibilidad = new ArrayList<>(NUM_TIPOS_CLIENTES);
        Realizacion_Pago = new ArrayList<>(NUM_TIPOS_CLIENTES);
        Realizacion_Cancelacion = new ArrayList<>(NUM_TIPOS_CLIENTES);
        Realizacion_Reserva = new ArrayList<>(NUM_TIPOS_CLIENTES);

        Respuesta_Disponibilidad = new Destination[NUM_TIPOS_CLIENTES][Num_Clientes];
        Confirmacion_Pago = new Destination[NUM_TIPOS_CLIENTES][Num_Clientes];
        Confirmacion_Reserva = new Destination[NUM_TIPOS_CLIENTES][Num_Clientes];
        Respuesta_Cancelacion = new Destination[NUM_TIPOS_CLIENTES][Num_Clientes];

        for (int i = 0; i < Numero_Aleatorio.nextInt(MIN_VIAJES, MAX_VIAJES); ++i) {
            Lista_Viajes.add( i, new Viaje(i) );
        }
        for (int i = 0; i < Numero_Aleatorio.nextInt(MIN_ESTANCIAS, MAX_ESTANCIAS); ++i) {
            Lista_Estancias.add( i, new Estancia(i) );
        }
    }


    @Override
    public void run() {
       System.out.println( "GestionViajes comienza su EJECUCIÓN " );
       try {
           before();
           while( !Thread.interrupted() ){
               ComprobarDisponibilidad();
               ComprobarSolicitudReserva();
               ComprobarPagoReserva();
               ComprobarSolicitudCancelacion();
           }
       } catch (Exception e) {
           System.out.println( "GestionViajes ha sido INTERRUMPIDO " );
       } finally {
           after();
           System.out.println( "GestionViajes finaliza su EJECUCIÓN " );
       }
    }

    /**
     * Método para preparar las conexiones y sesiones antes de ejecutar las operaciones del servidor.
     * @throws Exception  Si ocurre algún error durante la configuración de las conexiones y sesiones.
     */
    public void before() throws Exception {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Preguntar_Disponibilidad.add(0, session.createQueue(DESTINO + "Preguntar_Disponibilidad.Cliente") );
        Preguntar_Disponibilidad.add(1, session.createQueue(DESTINO + "Preguntar_Disponibilidad.Agencia") );
        Realizacion_Reserva.add(0, session.createQueue(DESTINO + "Realizacion_Reserva.Cliente") );
        Realizacion_Reserva.add(1, session.createQueue(DESTINO + "Realizacion_Reserva.Agencia") );
        Realizacion_Pago.add(0, session.createQueue(DESTINO + "Realizacion_Pago.Cliente") );
        Realizacion_Pago.add(1, session.createQueue(DESTINO + "Realizacion_Pago.Agencia") );
        Realizacion_Cancelacion.add(0, session.createQueue(DESTINO + "Realizacion_Cancelacion.Cliente") );
        Realizacion_Cancelacion.add(1, session.createQueue(DESTINO + "Realizacion_Cancelacion.Agencia") );

        for (int i = 0; i < Num_Clientes; ++i) {
            Respuesta_Disponibilidad[0][i] = session.createQueue(DESTINO + "Respuesta_Disponibilidad.Cliente" + i );
            Respuesta_Disponibilidad[1][i] = session.createQueue(DESTINO + "Respuesta_Disponibilidad.Agencia" + i );
            Confirmacion_Pago[0][i] = session.createQueue(DESTINO + "Confirmacion_Pago.Cliente" + i );
            Confirmacion_Pago[1][i] = session.createQueue(DESTINO + "Confirmacion_Pago.Agencia" + i );
            Confirmacion_Reserva[0][i] = session.createQueue(DESTINO + "Confirmacion_Reserva.Cliente" + i );
            Confirmacion_Reserva[1][i] = session.createQueue(DESTINO + "Confirmacion_Reserva.Agencia" + i );
            Respuesta_Cancelacion[0][i] = session.createQueue(DESTINO + "Respuesta_Cancelacion.Cliente" + i );
            Respuesta_Cancelacion[1][i] = session.createQueue(DESTINO + "Respuesta_Cancelacion.Agencia" + i );
        }

        MessageConsumer Consumer_Cliente_Disponibilidad = session.createConsumer(Preguntar_Disponibilidad.get(0));
        Consumer_Cliente_Disponibilidad.setMessageListener( new TextMsgListenerGestion("Disponibilidad Cliente", Lista_Cliente_Particular) );

        MessageConsumer Consumer_Agencia_Disponibilidad = session.createConsumer(Preguntar_Disponibilidad.get(1));
        Consumer_Agencia_Disponibilidad.setMessageListener( new TextMsgListenerGestion("Disponibilidad Agencia", Lista_Agencias_Viaje) );

        MessageConsumer Consumer_Cliente_Reserva = session.createConsumer(Realizacion_Reserva.get(0));
        Consumer_Cliente_Reserva.setMessageListener( new TextMsgListenerGestion("Reserva", Lista_Peticiones_Reserva) );

        MessageConsumer Consumer_Agencia_Reserva = session.createConsumer(Realizacion_Reserva.get(1));
        Consumer_Agencia_Reserva.setMessageListener( new TextMsgListenerGestion("Reserva", Lista_Peticiones_Reserva) );

        MessageConsumer Consumer_Cliente_Pago = session.createConsumer(Realizacion_Pago.get(0));
        Consumer_Cliente_Pago.setMessageListener ( new TextMsgListenerGestion("Pago", Lista_Peticiones_Pago) );

        MessageConsumer Consumer_Agencia_Pago = session.createConsumer(Realizacion_Pago.get(1));
        Consumer_Agencia_Pago.setMessageListener( new TextMsgListenerGestion("Pago", Lista_Peticiones_Pago) );

        MessageConsumer Consumer_Cliente_Cancelacion = session.createConsumer(Realizacion_Cancelacion.get(0));
        Consumer_Cliente_Cancelacion.setMessageListener( new TextMsgListenerGestion("Cancelacion", Lista_Peticiones_Cancelacion) );

        MessageConsumer Consumer_Agencia_Cancelacion = session.createConsumer(Realizacion_Cancelacion.get(1));
        Consumer_Agencia_Cancelacion.setMessageListener( new TextMsgListenerGestion("Cancelacion", Lista_Peticiones_Cancelacion) );

        connection.start();
    }

    /**
     * Método que comprueba la disponibilidad de viajes y estancias.
     * @throws JMSException Si ocurre algún error relacionado con JMS.
     * @throws InterruptedException Si la operación es interrumpida.
     */
    private void ComprobarDisponibilidad() throws JMSException, InterruptedException{
        TimeUnit.SECONDS.sleep( TIEMPO_ESPERA_MENSAJE );
        Mensaje Pregunta_Cliente;

        if( !Lista_Agencias_Viaje.isEmpty() ){
            if( !Lista_Cliente_Particular.isEmpty()) {
                if( Contador_Agencia < 2 ){
                    ++Contador_Agencia;
                    Pregunta_Cliente = Lista_Agencias_Viaje.remove(0);
                }else{
                    Pregunta_Cliente = Lista_Cliente_Particular.remove(0);
                    Contador_Agencia = 0;
                }
            }else{
                Pregunta_Cliente = Lista_Agencias_Viaje.remove(0);
            }
            ObtenerViajesDisponibles( Pregunta_Cliente );
            ObtenerEstanciasDisponibles( Pregunta_Cliente );

            if( Pregunta_Cliente.getTipo_Cliente() == TipoCliente.PARTICULAR ) {
                System.out.println( "SER--> Envia un mensaje al cliente con Nombre: " + Pregunta_Cliente.getNombre_Cliente() + " e Id: '" + Pregunta_Cliente.getId_Cliente() + "' con la disponibilidades." );
                EnviarMensaje( Pregunta_Cliente, Respuesta_Disponibilidad[0][Pregunta_Cliente.getId_Cliente()] );
            }else if( Pregunta_Cliente.getTipo_Cliente() == TipoCliente.AGENCIA ){
                System.out.println( "SER--> Envia un mensaje a la agencia con Nombre: " + Pregunta_Cliente.getNombre_Cliente() + " e Id: '" + Pregunta_Cliente.getId_Cliente() + "' con la disponibilidades." );
                EnviarMensaje( Pregunta_Cliente, Respuesta_Disponibilidad[1][Pregunta_Cliente.getId_Cliente()] );
            }

        }else if( !Lista_Cliente_Particular.isEmpty() ){
            Pregunta_Cliente = Lista_Cliente_Particular.remove(0);
            ObtenerViajesDisponibles( Pregunta_Cliente );
            ObtenerEstanciasDisponibles( Pregunta_Cliente );
            System.out.println( "SER--> Envia un mensaje al cliente con Nombre: " + Pregunta_Cliente.getNombre_Cliente() + " e Id: '" + Pregunta_Cliente.getId_Cliente() + "' con la disponibilidades." );
            EnviarMensaje( Pregunta_Cliente, Respuesta_Disponibilidad[0][Pregunta_Cliente.getId_Cliente()] );
        }
    }

    /**
     * Método que comprueba la solicitud de reserva de viajes y estancias.
     * @throws JMSException Si ocurre algún error relacionado con JMS.
     */
    private void ComprobarSolicitudReserva() throws JMSException{
        if( !Lista_Peticiones_Reserva.isEmpty() ){
            Mensaje Peticion_Reserva = Lista_Peticiones_Reserva.remove(0);

            if( Peticion_Reserva.getNum_Viaje() != -1 && Peticion_Reserva.getNum_Estancia() == -1 ){
                boolean Viaje_Disponible = ComprobarViajeReserva( Peticion_Reserva );
                if( Viaje_Disponible ){
                    Peticion_Reserva.setReserva_Correcta( true );
                    Lista_Viajes.get(Peticion_Reserva.getNum_Viaje()).DecrementarPlazasViaje();
                }else{
                    Peticion_Reserva.setReserva_Correcta( false );
                }
            }
            if(Peticion_Reserva.getNum_Viaje() == -1 && Peticion_Reserva.getNum_Estancia() != -1 ){
                boolean Estancia_Disponible = ComprobarEstanciaReserva( Peticion_Reserva );
                if( Estancia_Disponible ){
                    Peticion_Reserva.setReserva_Correcta( true );
                    Lista_Estancias.get(Peticion_Reserva.getNum_Estancia()).DecrementarPlazasEstancia();
                }else{
                    Peticion_Reserva.setReserva_Correcta( false );
                }
            }
            if( Peticion_Reserva.getNum_Viaje() != -1 && Peticion_Reserva.getNum_Estancia() != -1 ){
                boolean Viaje_Disponible = ComprobarViajeReserva( Peticion_Reserva ) ;
                boolean Estancia_Disponible = ComprobarEstanciaReserva( Peticion_Reserva );
                if( Estancia_Disponible && Viaje_Disponible ){
                    Peticion_Reserva.setReserva_Correcta( true );
                    Lista_Viajes.get(Peticion_Reserva.getNum_Viaje()).DecrementarPlazasViaje();
                    Lista_Estancias.get(Peticion_Reserva.getNum_Estancia()).DecrementarPlazasEstancia();
                }else{
                    Peticion_Reserva.setReserva_Correcta( false );
                }
            }

            if( Peticion_Reserva.getTipo_Cliente() == TipoCliente.PARTICULAR ) {
                System.out.println( "SER--> Envia un mensaje al cliente con Nombre: " + Peticion_Reserva.getNombre_Cliente() + " e Id: '" + Peticion_Reserva.getId_Cliente() + "' con la confirmacion de la reserva." );
                EnviarMensaje( Peticion_Reserva, Confirmacion_Reserva[0][Peticion_Reserva.getId_Cliente()] );
            }else if( Peticion_Reserva.getTipo_Cliente() == TipoCliente.AGENCIA ){
                System.out.println( "SER--> Envia un mensaje a la agencia con Nombre: " + Peticion_Reserva.getNombre_Cliente() + " e Id: '" + Peticion_Reserva.getId_Cliente() + "' con la confirmacion de la reserva." );
                EnviarMensaje( Peticion_Reserva, Confirmacion_Reserva[1][Peticion_Reserva.getId_Cliente()] );
            }
        }
    }

    /**
     * Método que comprueba el pago de las reservas de viajes y estancias.
     * @throws JMSException Si ocurre algún error relacionado con JMS.
     */
    private void ComprobarPagoReserva() throws JMSException{
        if( !Lista_Peticiones_Pago.isEmpty() ) {
            Mensaje Pago_Reserva = Lista_Peticiones_Pago.remove(0);
            Pago_Reserva.setPago_Correcto(true);

            if( Pago_Reserva.getTipo_Cliente() == TipoCliente.PARTICULAR ) {
                System.out.println( "SER--> Envia un mensaje al cliente con Nombre: " + Pago_Reserva.getNombre_Cliente() + " e Id: '" + Pago_Reserva.getId_Cliente() + "' con la confirmacion del pago." );
                EnviarMensaje( Pago_Reserva, Confirmacion_Pago[0][Pago_Reserva.getId_Cliente()] );
            }else if(Pago_Reserva.getTipo_Cliente() == TipoCliente.AGENCIA ){
                System.out.println( "SER--> Envia un mensaje a la agencia con Nombre: " + Pago_Reserva.getNombre_Cliente() + " e Id: '" + Pago_Reserva.getId_Cliente() + "' con la confirmacion del pago." );
                EnviarMensaje( Pago_Reserva, Confirmacion_Pago[1][Pago_Reserva.getId_Cliente()] );
            }
        }
    }

    /**
     * Método que comprueba la solicitud de cancelación de viajes y estancias.
     * @throws JMSException Si ocurre algún error relacionado con JMS.
     */
    private void ComprobarSolicitudCancelacion() throws JMSException{
        if(!Lista_Peticiones_Cancelacion.isEmpty()) {
            Mensaje Peticion_Cancelacion = Lista_Peticiones_Cancelacion.remove(0);

            if( Peticion_Cancelacion.getTipo_Cliente() == TipoCliente.PARTICULAR ) {
                System.out.println( "SER--> Envia un mensaje al cliente con Nombre: " + Peticion_Cancelacion.getNombre_Cliente() + " e Id: '" + Peticion_Cancelacion.getId_Cliente() + "' con la confirmacion de la cancelacion." );
                EnviarMensaje( Peticion_Cancelacion, Respuesta_Cancelacion[0][Peticion_Cancelacion.getId_Cliente()] );
            }else if( Peticion_Cancelacion.getTipo_Cliente() == TipoCliente.AGENCIA ){
                System.out.println( "SER--> Envia un mensaje a la agencia con Nombre: " + Peticion_Cancelacion.getNombre_Cliente() + " e Id: '" + Peticion_Cancelacion.getId_Cliente() + "' con la confirmacion de la cancelacion." );
                EnviarMensaje( Peticion_Cancelacion, Respuesta_Cancelacion[1][Peticion_Cancelacion.getId_Cliente()] );
            }
        }
    }

    /**
     * Método que obtiene la lista de viajes disponibles.
     * @param Datos_Disponibilidad Objeto Mensaje que contiene los datos de disponibilidad.
     */
    private void ObtenerViajesDisponibles( Mensaje Datos_Disponibilidad ) {
        for ( Viaje viaje : Lista_Viajes ) {
            if ( viaje.getPlazasDisponibles() > 0 ) {
                Datos_Disponibilidad.getLista_Viajes_Disponibles().add( viaje );
            }
        }
    }

    /**
     * Método que obtiene la lista de estancias disponibles.
     * @param Datos_Disponibilidad Objeto Mensaje que contiene los datos de disponibilidad.
     */
    private void ObtenerEstanciasDisponibles( Mensaje Datos_Disponibilidad ){
        for( Estancia estancia : Lista_Estancias ){
            if( estancia.getPlazas_Disponibles() > 0){
                Datos_Disponibilidad.getLista_Estancias_Disponibles().add( estancia );
            }
        }
    }

    /**
     * Método que comprueba la disponibilidad de una reserva de viaje.
     * @param Peticion_Reserva Objeto Mensaje que contiene los datos de la reserva.
     * @return true si el viaje está disponible, false en caso contrario.
     */
    private boolean ComprobarViajeReserva( Mensaje Peticion_Reserva ){
        for( Viaje viaje : Lista_Viajes ){
            if( viaje.getID() == Peticion_Reserva.getNum_Viaje() ){
                if( viaje.getPlazasDisponibles() > 0 ){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Método que comprueba la disponibilidad de una reserva de estancia.
     * @param Peticion_Reserva Objeto Mensaje que contiene los datos de la reserva.
     * @return true si la estancia está disponible, false en caso contrario.
     */
    private boolean ComprobarEstanciaReserva( Mensaje Peticion_Reserva ){
        for( Estancia estancia : Lista_Estancias ){
            if( estancia.getId() == Peticion_Reserva.getNum_Estancia() ){
                if( estancia.getPlazas_Disponibles() > 0 ){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Método para codificar y enviar un mensaje al cliente correspondiente.
     * @param MensajeCliente Mensaje que va a ser codificado y posteriormente enviado al cliente.
     * @param Buzon Buzon por dónde se va a enviar el mensaje.
     * @throws JMSException Si ocurre algún error durante el proceso de envío del mensaje.
     */
    private void EnviarMensaje( Mensaje MensajeCliente, Destination Buzon ) throws JMSException{
        MensajeCliente.setTipo_Cliente( TipoCliente.SERVIDOR );

        GsonUtil<Mensaje> gsonUtil = new GsonUtil<>();
        MessageProducer producer = session.createProducer( Buzon );
        producer.send( session.createTextMessage(gsonUtil.encode(MensajeCliente, Mensaje.class)) );
        producer.close();
    }

    /** Método que cierra la conexión con el servidor, liberando todos los recursos asociados. */
    public void after() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException ex) {
            System.out.println( "Error al cerrar la conexión" );
        }
    }
}