package es.ujaen.ssccdd.curso2023_24.Procesos;

import static es.ujaen.ssccdd.curso2023_24.Utils.Constantes.*;
import es.ujaen.ssccdd.curso2023_24.Listener.TextMsgListenerGestion;
import es.ujaen.ssccdd.curso2023_24.Utils.Estancia;
import es.ujaen.ssccdd.curso2023_24.Utils.GsonUtil;
import es.ujaen.ssccdd.curso2023_24.Utils.Mensaje;
import es.ujaen.ssccdd.curso2023_24.Utils.Viaje;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @class GestionViaje
 * @brief Clase que implementa la gestión de viajes.
 */
public class GestionViaje implements Runnable {

    private final Integer Num_Clientes;
    private final List<Viaje> Lista_Viajes;
    private final List<Estancia> Lista_Estancias;
    private final List<Mensaje> Lista_Agencias_Viaje;
    private final List<Mensaje> Lista_Cliente_Particular;
    private final List<Mensaje> Lista_Reservas;
    private final List<Mensaje> Lista_Pago;
    private final List<Mensaje> Lista_Cancelacion;
    private final Semaphore Sem_Clientes_Particulares[];
    private final Semaphore Sem_Agencias_Viaje[];

    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;

    private final Destination[] Preguntar_Disponibilidad;
    private final Destination[][] Respuesta_Disponibilidad;

    private final Destination[] Realizacion_Pago;
    private final Destination[][] Confirmacion_Pago;

    private final Destination[] Realizacion_Cancelacion;
    private final Destination[][] Respuesta_Cancelacion;

    private final Destination[] Realizacion_Reserva;
    private final Destination[][] Confirmacion_Reserva;

    /**
     * @brief Constructor parametrizado de la clase GestionViaje.
     * @param Num_Clientes Número de clientes.
     * @param Sem_Clientes_Particulares Array de semáforos para clientes particulares.
     * @param Sem_Agencias_Viaje Array de semáforos para agencias de viaje.
     */
    public GestionViaje(int Num_Clientes, Semaphore Sem_Clientes_Particulares[], Semaphore Sem_Agencias_Viaje[]){

        this.Num_Clientes = Num_Clientes;
        this.Lista_Viajes = new ArrayList<>();
        this.Lista_Estancias = new ArrayList<>();
        this.Lista_Agencias_Viaje = new ArrayList<>();
        this.Lista_Cliente_Particular = new ArrayList<>();
        this.Lista_Reservas = new ArrayList<>();
        this.Lista_Pago = new ArrayList<>();
        this.Lista_Cancelacion = new ArrayList<>();
        this.Sem_Clientes_Particulares = Sem_Clientes_Particulares;
        this.Sem_Agencias_Viaje = Sem_Agencias_Viaje;

        Preguntar_Disponibilidad = new Destination[NUM_TIPOS_CLIENTES];
        Realizacion_Pago = new Destination[NUM_TIPOS_CLIENTES];
        Realizacion_Cancelacion = new Destination[NUM_TIPOS_CLIENTES];
        Realizacion_Reserva = new Destination[NUM_TIPOS_CLIENTES];

        Respuesta_Disponibilidad =  new Destination[NUM_TIPOS_CLIENTES][Num_Clientes];
        Confirmacion_Pago =  new Destination[NUM_TIPOS_CLIENTES][Num_Clientes];
        Confirmacion_Reserva =  new Destination[NUM_TIPOS_CLIENTES][Num_Clientes];
        Respuesta_Cancelacion =  new Destination[NUM_TIPOS_CLIENTES][Num_Clientes];

        int Num_Viajes = Numero_Aleatorio.nextInt(MIN_VIAJES,MAX_VIAJES);
        int Num_Estancias = Numero_Aleatorio.nextInt(MIN_ESTANCIAS,MAX_ESTANCIAS);
        for( int i=0; i<Num_Viajes;++i){
            Lista_Viajes.add(i, new Viaje(i));
        }
        for( int i=0; i<Num_Estancias;++i){
            Lista_Estancias.add(i, new Estancia(i));
        }

    }


    @Override
    public void run() {
       System.out.println("GestionViajes Comienza ejecucion");
       try {
           before();
           execution();
       } catch (Exception e) {
           System.out.println("GestionViajes interrumpido en su ejecución: " + e.getMessage());
       } finally {
           after();
           System.out.println("GestionViajes Finaliza su ejecución");
       }
    }

    /**
     * @brief  Método para preparar las conexiones y sesiones antes de ejecutar las operaciones del servidor.
     * @throws Exception  Si ocurre algún error durante la configuración de las conexiones y sesiones.
     */
    public void before() throws Exception{

        connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Preguntar_Disponibilidad[0] = session.createQueue(QUEUE+"Preguntar_Disponibilidad.Cliente");
        Preguntar_Disponibilidad[1] = session.createQueue(QUEUE+"Preguntar_Disponibilidad.Agencia");

        Realizacion_Reserva[0] = session.createQueue(QUEUE+"Realizacion_Reserva.Cliente");
        Realizacion_Reserva[1] = session.createQueue(QUEUE+"Realizacion_Reserva.Agencia");

        Realizacion_Pago[0] = session.createQueue(QUEUE+"Realizacion_Pago.Cliente");
        Realizacion_Pago[1] = session.createQueue(QUEUE+"Realizacion_Pago.Agencia");

        Realizacion_Cancelacion[0] = session.createQueue(QUEUE+"Realizacion_Cancelacion.Cliente");
        Realizacion_Cancelacion[1] = session.createQueue(QUEUE+"Realizacion_Cancelacion.Agencia");

        for(int i=0;i<Num_Clientes;++i){
            Respuesta_Disponibilidad[0][i] = session.createQueue(QUEUE+"Respuesta_Disponibilidad.Cliente"+i);
            Respuesta_Disponibilidad[1][i] = session.createQueue(QUEUE+"Respuesta_Disponibilidad.Agencia"+i);
            Confirmacion_Pago[0][i] = session.createQueue(QUEUE+"Confirmacion_Pago.Cliente"+i);
            Confirmacion_Pago[1][i] = session.createQueue(QUEUE+"Confirmacion_Pago.Agencia"+i);
            Confirmacion_Reserva[0][i] = session.createQueue(QUEUE+"Confirmacion_Reserva.Cliente"+i);
            Confirmacion_Reserva[1][i] = session.createQueue(QUEUE+"Confirmacion_Reserva.Agencia"+i);
            Respuesta_Cancelacion[0][i] = session.createQueue(QUEUE+"Respuesta_Cancelacion.Cliente"+i);
            Respuesta_Cancelacion[1][i] = session.createQueue(QUEUE+"Respuesta_Cancelacion.Agencia"+i);
        }


        MessageConsumer Consumer_Cliente_Disponibilidad = session.createConsumer(Preguntar_Disponibilidad[0]);
        TextMsgListenerGestion Listener_Disponibilidad_Cliente = new TextMsgListenerGestion("Disponibilidad Cliente",Lista_Cliente_Particular);
        Consumer_Cliente_Disponibilidad.setMessageListener(Listener_Disponibilidad_Cliente);

        MessageConsumer Consumer_Agencia_Disponibilidad = session.createConsumer(Preguntar_Disponibilidad[1]);
        TextMsgListenerGestion Listener_Disponibilidad_Agencia = new TextMsgListenerGestion("Disponibilidad Agencia",Lista_Agencias_Viaje);
        Consumer_Agencia_Disponibilidad.setMessageListener(Listener_Disponibilidad_Agencia);


        MessageConsumer Consumer_Cliente_Reserva = session.createConsumer(Realizacion_Reserva[0]);
        TextMsgListenerGestion Listener_Cliente_Reserva = new TextMsgListenerGestion("Reserva",Lista_Reservas);
        Consumer_Cliente_Reserva.setMessageListener(Listener_Cliente_Reserva);

        MessageConsumer Consumer_Agencia_Reserva = session.createConsumer(Realizacion_Reserva[1]);
        TextMsgListenerGestion Listener_Agencia_Reserva = new TextMsgListenerGestion("Reserva",Lista_Reservas);
        Consumer_Agencia_Reserva.setMessageListener(Listener_Agencia_Reserva);

        MessageConsumer Consumer_Cliente_Pago = session.createConsumer(Realizacion_Pago[0]);
        TextMsgListenerGestion Listener_Cliente_Pago = new TextMsgListenerGestion("Pago",Lista_Pago);
        Consumer_Cliente_Pago.setMessageListener(Listener_Cliente_Pago);

        MessageConsumer Consumer_Agencia_Pago = session.createConsumer(Realizacion_Pago[1]);
        TextMsgListenerGestion Listener_Agencia_Pago = new TextMsgListenerGestion("Pago",Lista_Pago);
        Consumer_Agencia_Pago.setMessageListener(Listener_Agencia_Pago);

        MessageConsumer Consumer_Cliente_Cancelacion = session.createConsumer(Realizacion_Cancelacion[0]);
        TextMsgListenerGestion Listener_Cliente_Cancelacion = new TextMsgListenerGestion("Cancelacion",Lista_Cancelacion);
        Consumer_Cliente_Cancelacion.setMessageListener(Listener_Cliente_Cancelacion);

        MessageConsumer Consumer_Agencia_Cancelacion = session.createConsumer(Realizacion_Cancelacion[1]);
        TextMsgListenerGestion Listener_Agencia_Cancelacion = new TextMsgListenerGestion("Cancelacion",Lista_Cancelacion);
        Consumer_Agencia_Cancelacion.setMessageListener(Listener_Agencia_Cancelacion);

        connection.start();
    }

    /**
     * @brief Método principal donde se gestiona la disponibilidad, reservas, pagos y cancelaciones de los viajes.
     * @throws Exception Si ocurre algún error durante la ejecución.
     */
    public void execution() throws Exception {
        while(true){
            ComprobarDisponibilidad();
            ComprobarSolicitudReserva();
            ComprobarPagoReserva();
            ComprobarSolicitudCancelacion();
        }
    }

    /**
     * @brief Método que comprueba la disponibilidad de viajes y estancias.
     * @throws JMSException Si ocurre algún error relacionado con JMS.
     * @throws InterruptedException Si la operación es interrumpida.
     */
    private void ComprobarDisponibilidad() throws JMSException, InterruptedException{

        Mensaje Pregunta_Cliente;

        if( !Lista_Agencias_Viaje.isEmpty() ){
            if( !Lista_Cliente_Particular.isEmpty()) {
                long Tiempo_Esperado = ObtenerTiempoEsperando();

                if( Tiempo_Esperado > 3 ){
                    Pregunta_Cliente = Lista_Cliente_Particular.remove(0);
                }else{
                    Pregunta_Cliente = Lista_Agencias_Viaje.remove(0);
                }
            }else{
                Pregunta_Cliente = Lista_Agencias_Viaje.remove(0);
            }

            ObtenerViajesDisponibles( Pregunta_Cliente );
            ObtenerEstanciasDisponibles( Pregunta_Cliente );
            if(Pregunta_Cliente.getTipo_Cliente() == TipoCliente.PARTICULAR) {
                EnviarMensaje(Pregunta_Cliente, Respuesta_Disponibilidad[0][Pregunta_Cliente.getId_Cliente()], Sem_Clientes_Particulares[Pregunta_Cliente.getId_Cliente()] );
                System.out.println("Le enviamos el mensaje con la disponibilidad que hay de viajes y estancias al cliente con nombre '" + Pregunta_Cliente.getNombre_Cliente() + "' e ID '" + Pregunta_Cliente.getId_Cliente() +"' .");
            }else if(Pregunta_Cliente.getTipo_Cliente() == TipoCliente.AGENCIA ){
                EnviarMensaje(Pregunta_Cliente, Respuesta_Disponibilidad[1][Pregunta_Cliente.getId_Cliente()], Sem_Agencias_Viaje[Pregunta_Cliente.getId_Cliente()] );
                System.out.println("Le enviamos el mensaje con la disponibilidad que hay de viajes y estancias a la agencia con nombre '" + Pregunta_Cliente.getNombre_Cliente() + "' e ID '" + Pregunta_Cliente.getId_Cliente() +"' .");
            }

        }else if( !Lista_Cliente_Particular.isEmpty()){
            Pregunta_Cliente = Lista_Cliente_Particular.remove(0);
            ObtenerViajesDisponibles( Pregunta_Cliente );
            ObtenerEstanciasDisponibles( Pregunta_Cliente );
            EnviarMensaje( Pregunta_Cliente, Respuesta_Disponibilidad[0][Pregunta_Cliente.getId_Cliente()], Sem_Clientes_Particulares[Pregunta_Cliente.getId_Cliente()]);
            System.out.println("Le enviamos el mensaje con la disponibilidad que hay de viajes y estancias al cliente con nombre '" + Pregunta_Cliente.getNombre_Cliente() + "' e ID '" + Pregunta_Cliente.getId_Cliente() +"' .");
        }

    }

    /**
     * @brief Método que comprueba la solicitud de reserva de viajes y estancias.
     * @throws JMSException Si ocurre algún error relacionado con JMS.
     * @throws InterruptedException Si la operación es interrumpida.
     */
    private void ComprobarSolicitudReserva() throws JMSException, InterruptedException{
        if( !Lista_Reservas.isEmpty() ){
            Mensaje Peticion_Reserva = Lista_Reservas.remove(0);
            boolean Viaje_Disponible;
            boolean Estancia_Disponible;

            if( Peticion_Reserva.getNum_Viaje() != -1 && Peticion_Reserva.getNum_Estancia() == -1 ){
                Viaje_Disponible = ComprobarViajeReserva( Peticion_Reserva );
                if( Viaje_Disponible ){
                    Peticion_Reserva.setReserva_Correcta( true );
                    Lista_Viajes.get(Peticion_Reserva.getNum_Viaje()).decrementarPlazasViaje();
                }else{
                    Peticion_Reserva.setReserva_Correcta( false );
                }
            }
            if(Peticion_Reserva.getNum_Viaje() == -1 && Peticion_Reserva.getNum_Estancia() != -1 ){
                Estancia_Disponible = ComprobarEstanciaReserva( Peticion_Reserva );
                if( Estancia_Disponible ){
                    Peticion_Reserva.setReserva_Correcta( true );
                    Lista_Estancias.get(Peticion_Reserva.getNum_Estancia()).decrementarPlazasEstancia();
                }else{
                    Peticion_Reserva.setReserva_Correcta( false );
                }
            }
            if( Peticion_Reserva.getNum_Viaje() != -1 && Peticion_Reserva.getNum_Estancia() != -1 ){
                Viaje_Disponible = ComprobarViajeReserva( Peticion_Reserva ) ;
                Estancia_Disponible = ComprobarEstanciaReserva( Peticion_Reserva );
                if( Estancia_Disponible && Viaje_Disponible ){
                    Peticion_Reserva.setReserva_Correcta( true );
                    Lista_Viajes.get(Peticion_Reserva.getNum_Viaje()).decrementarPlazasViaje();
                    Lista_Estancias.get(Peticion_Reserva.getNum_Estancia()).decrementarPlazasEstancia();
                }else{
                    Peticion_Reserva.setReserva_Correcta( false );
                }
            }
            if(Peticion_Reserva.getTipo_Cliente() == TipoCliente.PARTICULAR) {
                EnviarMensaje(Peticion_Reserva, Confirmacion_Reserva[0][Peticion_Reserva.getId_Cliente()], Sem_Clientes_Particulares[Peticion_Reserva.getId_Cliente()] );
                System.out.println("Le enviamos el mensaje con la confirmacion de la reserva a el cliente con nombre '" + Peticion_Reserva.getNombre_Cliente() + "' e ID '" + Peticion_Reserva.getId_Cliente() +"' .");

            }else if(Peticion_Reserva.getTipo_Cliente() == TipoCliente.AGENCIA ){
                EnviarMensaje(Peticion_Reserva, Confirmacion_Reserva[1][Peticion_Reserva.getId_Cliente()], Sem_Agencias_Viaje[Peticion_Reserva.getId_Cliente()]);
                System.out.println("Le enviamos el mensaje con la confirmacion de la reserva a la agencia con nombre '" + Peticion_Reserva.getNombre_Cliente() + "' e ID '" + Peticion_Reserva.getId_Cliente() +"' .");

            }
        }
    }

    /**
     * @brief Método que comprueba el pago de las reservas de viajes y estancias.
     * @throws JMSException Si ocurre algún error relacionado con JMS.
     * @throws InterruptedException Si la operación es interrumpida.
     */
    private void ComprobarPagoReserva() throws JMSException, InterruptedException{
        if(!Lista_Pago.isEmpty()) {
            Mensaje Pago_Reserva = Lista_Pago.remove(0);
            Pago_Reserva.setPago_Correcto(true);
            if(Pago_Reserva.getTipo_Cliente() == TipoCliente.PARTICULAR) {
                EnviarMensaje( Pago_Reserva, Confirmacion_Pago[0][Pago_Reserva.getId_Cliente()],Sem_Clientes_Particulares[Pago_Reserva.getId_Cliente()] );
                System.out.println( Pago_Reserva.getTipo_Cliente() +" "+ Pago_Reserva.getNombre_Cliente() + " ha realizado el pago de la reserva del viaje correctamente" );
            }else if(Pago_Reserva.getTipo_Cliente() == TipoCliente.AGENCIA ){
                EnviarMensaje( Pago_Reserva, Confirmacion_Pago[1][Pago_Reserva.getId_Cliente()],Sem_Agencias_Viaje[Pago_Reserva.getId_Cliente()] );
                System.out.println( Pago_Reserva.getTipo_Cliente() +" "+ Pago_Reserva.getNombre_Cliente() + " ha realizado el pago de la reserva del viaje correctamente" );
            }
        }
    }

    /**
     * @brief Método que comprueba la solicitud de cancelación de viajes y estancias.
     * @throws JMSException Si ocurre algún error relacionado con JMS.
     * @throws InterruptedException Si la operación es interrumpida.
     */
    private void ComprobarSolicitudCancelacion() throws JMSException, InterruptedException{
        if(!Lista_Cancelacion.isEmpty()) {
            Mensaje Peticion_Cancelacion = Lista_Cancelacion.remove(0);
            if(Peticion_Cancelacion.getTipo_Cliente() == TipoCliente.PARTICULAR) {
                EnviarMensaje( Peticion_Cancelacion, Respuesta_Cancelacion[0][Peticion_Cancelacion.getId_Cliente()],Sem_Clientes_Particulares[Peticion_Cancelacion.getId_Cliente()] );
                System.out.println( Peticion_Cancelacion.getTipo_Cliente() +" "+ Peticion_Cancelacion.getNombre_Cliente() + " ha cancelado el viaje correctamente" );
            }else if(Peticion_Cancelacion.getTipo_Cliente() == TipoCliente.AGENCIA ){
                EnviarMensaje( Peticion_Cancelacion, Respuesta_Cancelacion[1][Peticion_Cancelacion.getId_Cliente()], Sem_Agencias_Viaje[Peticion_Cancelacion.getId_Cliente()]);
                System.out.println( Peticion_Cancelacion.getTipo_Cliente() +" "+ Peticion_Cancelacion.getNombre_Cliente() + " ha cancelado el viaje correctamente" );
            }
        }
    }

    /**
     * @brief Método que obtiene la lista de viajes disponibles.
     * @param Datos_Disponibilidad Objeto Mensaje que contiene los datos de disponibilidad.
     */
    private void ObtenerViajesDisponibles(Mensaje Datos_Disponibilidad) {
        for (int i = 0; i < Lista_Viajes.size(); ++i) {
            if (Lista_Viajes.get(i).getPlazasDisponibles() > 0) {
                Datos_Disponibilidad.getLista_Viajes_Disponibles().add(Lista_Viajes.get(i));
            }
        }
    }

    /**
     * @brief Método que obtiene la lista de estancias disponibles.
     * @param Datos_Disponibilidad Objeto Mensaje que contiene los datos de disponibilidad.
     */
    private void ObtenerEstanciasDisponibles(Mensaje Datos_Disponibilidad){
        for( int i=0; i<Lista_Estancias.size(); ++i ){
            if( Lista_Estancias.get(i).getPlazas_Disponibles() > 0){
                Datos_Disponibilidad.getLista_Estancias_Disponibles().add( Lista_Estancias.get(i) );
            }
        }
    }

    /**
     * @brief Método que comprueba la disponibilidad de una reserva de viaje.
     * @param Peticion_Reserva Objeto Mensaje que contiene los datos de la reserva.
     * @return true si el viaje está disponible, false en caso contrario.
     */
    private boolean ComprobarViajeReserva( Mensaje Peticion_Reserva ){
        for( int i=0; i<Lista_Viajes.size(); ++i ){
            if( Lista_Viajes.get(i).getID() == Peticion_Reserva.getNum_Viaje() ){
                if( Lista_Viajes.get(i).getPlazasDisponibles() > 0 ){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @brief Método que comprueba la disponibilidad de una reserva de estancia.
     * @param Peticion_Reserva Objeto Mensaje que contiene los datos de la reserva.
     * @return true si la estancia está disponible, false en caso contrario.
     */
    private boolean ComprobarEstanciaReserva(Mensaje Peticion_Reserva){
        for( int i=0; i<Lista_Estancias.size(); ++i ){
            if( Lista_Estancias.get(i).getId() == Peticion_Reserva.getNum_Estancia() ){
                if( Lista_Estancias.get(i).getPlazas_Disponibles() > 0 ){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @brief Método que obtiene el tiempo que un cliente particular lleva esperando.
     * @return El tiempo en segundos que el cliente lleva esperando.
     */
    private long ObtenerTiempoEsperando(){
        Date actual = new Date();
        long Tiempo_Esperando = actual.getTime() - Lista_Cliente_Particular.get(0).getFecha().getTime();
        return Tiempo_Esperando = TimeUnit.SECONDS.convert(Tiempo_Esperando, TimeUnit.MILLISECONDS);
    }

    /**
     * @brief Método que envía un mensaje a un buzón específico y libera el semáforo correspondiente.
     * @param MensajeCliente Objeto Mensaje que contiene los datos del cliente.
     * @param Buzon Destino del mensaje.
     * @param Semaforo_Desbloquear Semáforo a liberar después de enviar el mensaje.
     * @throws JMSException Si ocurre algún error relacionado con JMS.
     * @throws InterruptedException Si la operación es interrumpida.
     */
    private void EnviarMensaje( Mensaje MensajeCliente, Destination Buzon, Semaphore Semaforo_Desbloquear) throws JMSException,InterruptedException{

        GsonUtil<Mensaje> gsonUtil = new GsonUtil();
        MessageProducer producer = session.createProducer(Buzon);
        TextMessage mensaje = session.createTextMessage(gsonUtil.encode(MensajeCliente,Mensaje.class));

        producer.send(mensaje);
        Semaforo_Desbloquear.release();
        producer.close();

    }

    /**
     * @brief Método que cierra la conexión y la sesión JMS.
     */
    public void after() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException ex) {}
    }

}